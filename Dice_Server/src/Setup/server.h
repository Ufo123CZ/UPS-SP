#ifndef SERVERL_H
#define SERVERL_H

#define PORT 8080
#define BUFFER_SIZE 1024

void start_server(int server_fd);
void set_up_server();
void handle_client_connection(int server_fd, struct sockaddr_in *address, int *addrlen);

#endif //SERVERL_H
