#include "Server.h"
#include "../Utils/Consts.h"
#include <iostream>
#include <cstring>
#include <fcntl.h>
#include <sys/ioctl.h>

Server::Server() : running(false), serverSocket(-1) {
#ifdef _WIN32
    WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif
}

Server::~Server() {
    stop();
#ifdef _WIN32
    WSACleanup();
#endif
}

bool Server::init() {
    serverSocket = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocket == -1) {
        std::cerr << "Failed to create socket" << std::endl;
        return FAILURE_INIT;
    }

    int opt = 1;
    if (setsockopt(serverSocket, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt)) < 0) {
        perror("setsockopt");
        close(serverSocket);
        return FAILURE_INIT;
    }

    sockaddr_in serverAddr{};
    serverAddr.sin_family = AF_INET;
    serverAddr.sin_addr.s_addr = INADDR_ANY;
    serverAddr.sin_port = htons(PORT);

    if (bind(serverSocket, reinterpret_cast<sockaddr *>(&serverAddr), sizeof(serverAddr)) < 0) {
        std::cerr << "Bind failed" << std::endl;
        return FAILURE_INIT;
    }

    if (listen(serverSocket, 3) < 0) {
        std::cerr << "Listen failed" << std::endl;
        return FAILURE_INIT;
    }

    return SUCCESS_INIT;
}

void Server::start() {
    running = true;
    fd_set readfds, client_socks;
    struct timeval timeout{};
    timeout.tv_sec = 5;
    timeout.tv_usec = 0;

    FD_ZERO(&client_socks);
    FD_SET(serverSocket, &client_socks);

    while (running) {
        readfds = client_socks;

        int activity = select(FD_SETSIZE, &readfds, nullptr, nullptr, &timeout);
        if (activity < 0 && errno != EINTR) {
            std::cerr << "Select error" << std::endl;
            continue;
        }

        for (int fd = 3; fd < FD_SETSIZE; fd++) {
            if (FD_ISSET(fd, &readfds)) {
                if (fd == serverSocket) {
                    sockaddr_in peer_addr{};
                    socklen_t len_addr = sizeof(peer_addr);
                    socket_t client_socket = accept(serverSocket, reinterpret_cast<sockaddr *>(&peer_addr), &len_addr);
                    if (client_socket >= 0) {
                        FD_SET(client_socket, &client_socks);
                        std::cout << "New client connected and added to socket set" << std::endl;
                    } else {
                        std::cerr << "Failed to accept new connection" << std::endl;
                    }
                } else {
                    int a2read;
                    ioctl(fd, FIONREAD, &a2read);
                    if (a2read > 0) {

                        std::string message = MessageProcessing::readMessage(fd, a2read);
                        if (message.empty()) {
                            std::cerr << "Failed to read message" << std::endl;
                            continue;
                        }
                        std::string response = MessageProcessing::processMessage(message);
                        if (response.empty()) {
                            std::cerr << "Failed to process message" << std::endl;
                            continue;
                        }
                        send(fd, response.c_str(), response.size(), 0);
                        /*
                        //
                        // // Variables
                        // char buffer[12] = {};
                        // int messageLength = 0;
                        // std::string messFirst;
                        // std::string message;
                        // bool found = false;
                        //
                        // // Receive the first 12 bytes
                        // recv(fd, &buffer, 12, 0);
                        //
                        // // Find the message length
                        // for (const char i : buffer) {
                        //     if (i == ';') {
                        //         std::string sLen = buffer;
                        //         messageLength = std::stoi(sLen.substr(0, sLen.find(';')));
                        //         messFirst = sLen.substr(sLen.find(';') + 1);
                        //         found = true;
                        //         break;
                        //     }
                        // }
                        // if (!found) {
                        //     std::cerr << "Failed to get message length" << std::endl;
                        //     continue;
                        // }
                        //
                        // // Receive the rest of the message
                        // // Clear the buffer
                        // memset(buffer, 0, sizeof(buffer));
                        // buffer[messageLength] = {};
                        // recv(fd, &buffer, messageLength, 0);
                        // // remove from messFirst \022, \023, \024
                        // char spec[] = {'\022', '\023', '\024'};
                        // for (char i : spec) {
                        //     if (messFirst.find(i) != std::string::npos) {
                        //         messFirst = messFirst.substr(0, messFirst.find(i));
                        //         a2read--;
                        //         messageLength--;
                        //     }
                        // }
                        // message.append(messFirst).append(buffer);
                        //
                        // // Proccess the message and Return the response
                        // std::string response;
                        // if (messageLength == a2read - 1 && message.size() == messageLength - 1) {
                        //     response = MessegProccesing::proccessMessage(message);
                        // }
                        // // Clear the variables
                        // message.clear();
                        // messFirst.clear();
                        // messageLength = 0;

                        //
                        // char buffer[12] = {};
                        // recv(fd, &buffer, 5, 0);
                        // std::cout << "Received: " << buffer << std::endl;
                        //
                        // //find char ';' in the buffer
                        // char *p = strchr(buffer, ';');
                        //     lenght
                        // }
                        // message += buffer;
                        // if (buffer == '\n' || buffer == '\0') {
                        //     MessegProccesing::proccessMessage(message);
                        //     message.clear();
                        // }
                        // char *response = "Message received. Hello from server!\n";
                        // send(fd, response, strlen(response), 0);
                        */
                    } else {
                        close(fd);
                        FD_CLR(fd, &client_socks);
                        std::cout << "Client disconnected and removed from socket set" << std::endl;
                    }
                }
            }
        }
    }
}

void Server::stop() {
    running = false;
    if (serverSocket != -1) {
#ifdef _WIN32
        closesocket(serverSocket);
#else
        close(serverSocket);
#endif
    }
    for (int clientSocket : clientSockets) {
#ifdef _WIN32
        closesocket(clientSocket);
#else
        close(clientSocket);
#endif
    }
    clientSockets.clear();
}