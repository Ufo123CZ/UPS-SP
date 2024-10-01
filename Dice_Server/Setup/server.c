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

void *server_thread(void *arg) {
    while (1) {
        set_up_server();
    }
    return NULL;
}

void set_up_server() {
    int server_fd, new_socket;
    struct sockaddr_in address;
    int opt = 1;
    int addrlen = sizeof(address);
    char buffer[BUFFER_SIZE] = {0};
    char *hello = "Hello from server";

    // Creating socket file descriptor
    if ((server_fd = socket(AF_INET, SOCK_STREAM, 0)) == 0) {
        perror("socket failed");
        exit(EXIT_FAILURE);
    }

    // Forcefully attaching socket to the port 8080
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) {
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

    // Accepting an incoming connection
    if ((new_socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t*)&addrlen)) < 0) {
        perror("accept");
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    // Reading data from the client
    read(new_socket, buffer, BUFFER_SIZE);
    proccesInput(buffer);

    // Sending a response to the client
    send(new_socket, hello, strlen(hello), 0);
    proccesOutput(hello);

    // Closing the socket
    close(new_socket);
    close(server_fd);
}

void start_server() {
    pthread_t thread_id;
    bool running = true;

    // Create a thread to run the server
    pthread_create(&thread_id, NULL, server_thread, NULL);

    // Read input from the console
    char input[BUFFER_SIZE];
    while (running) {
//        printf("Enter 'exit' to stop the server: \n");
        fgets(input, BUFFER_SIZE, stdin);
        input[strcspn(input, "\n")] = 0;

        if (strcmp(input, "exit") == 0) {
            running = false;
        }
    }

    // Wait for the server thread to finish
    pthread_cancel(thread_id);
    pthread_join(thread_id, NULL);

}