#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <pthread.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <sys/types.h>

#include "server.h"
#include "../Worker/procces.h"
#include "../Struct/player.h"
#include "../Struct/lobby.h"

extern Player *player_list;
extern Lobby *lobby_list;

/**
* @brief The server thread function that handles incoming connections
* @param arg The server file descriptor
*/
void *server_thread(void *arg) {
    int server_fd = *(int *)arg;
    struct sockaddr_in address;
    int addrlen = sizeof(address);

    while (1) {
        handle_client_connection(server_fd, &address, &addrlen);
    }
    return NULL;
}

/**
* @brief Sets up the server and starts it
*/
void set_up_server() {
    int server_fd;
    struct sockaddr_in address;
    int opt = 1;

    // Creating socket file descriptor
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    // Forcefully attaching socket to the port 8080
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt))) {
        perror("setsockopt");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(PORT);

    // Binding the socket to the network address and port
    if (bind(server_fd, (struct sockaddr *)&address, sizeof(address)) < 0) {
        perror("bind failed");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    // Listening for incoming connections
    if (listen(server_fd, 3) < 0) {
        perror("listen");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    start_server(server_fd);
}

/**
* @brief Handles an incoming connection
* @param server_fd The server file descriptor
* @param address The address of the client
* @param addrlen The length of the address
*/
void handle_client_connection(int server_fd, struct sockaddr_in *address, int *addrlen) {
    int new_socket;
    char buffer[BUFFER_SIZE] = {0};
    char output[BUFFER_SIZE] = {0};

    // Accepting an incoming connection
    if ((new_socket = accept(server_fd, (struct sockaddr *)address, (socklen_t *)addrlen)) < 0) {
        perror("accept");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    // Reading data from the client
    read(new_socket, buffer, BUFFER_SIZE);
    printf("%s\n", buffer);
    // remove \n from the end of the buffer
    if (buffer[strlen(buffer) - 1] == '\n' && buffer[strlen(buffer)] == '\0') {
        buffer[strlen(buffer) - 2] = '\0';
//      	printf("Removing \\n from the end of the buffer\n");
    }
    proccesInput(buffer);

    // Sending a response to the client
    proccesOutput(output);
    printf("Server -> Client;%s\n", output);
    send(new_socket, output, strlen(output), 0);

    // Closing the socket
    close(new_socket);
}

/**
* @brief Starts the server
* @param server_fd The server file descriptor
*/
void start_server(int server_fd) {
    pthread_t thread_id;
    bool running = true;

    // Create a thread to run the server
    pthread_create(&thread_id, NULL, server_thread, &server_fd);

    // Read input from the console
    char input[BUFFER_SIZE];
    while (running) {
        fgets(input, BUFFER_SIZE, stdin);
        input[strcspn(input, "\n")] = 0;

        if (strcmp(input, "exit") == 0) {
          	free_players(&player_list);
            free_lobbies(&lobby_list);
            running = false;
        } else if (strcmp(input, "printP") == 0) {
        	print_players(player_list);
    	} else if (strcmp(input, "printL") == 0) {
        	print_lobbies(lobby_list);
    	} else if (strcmp(input, "testing") == 0) {
//        	char *test = "word";
//            char *test2 = "word\n";
//            printf("Test: %d\n", strlen(test));
//            printf("Test2: %d\n", strlen(test2));
//            if (test[strlen(test) - 1] == '\n') {
//                printf("Test contains \\n\n");
//            }
//            if (test2[strlen(test2) - 1] == '\n') {
//                printf("Test2 contains \\n\n");
//            }
//            // \0
//            if (test[strlen(test)] == '\0') {
//                printf("Test contains \\0\n");
//            }
//            if (test2[strlen(test2)] == '\0') {
//                printf("Test2 contains \\0\n");
//            }
    	}
    }

    // Wait for the server thread to finish
    pthread_cancel(thread_id);
    pthread_join(thread_id, NULL);

    // Close the server socket
    close(server_fd);
}