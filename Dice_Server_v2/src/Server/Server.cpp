#include "Server.h"
#include "../Utils/Consts.h"
#include <iostream>
#include <cstring>
#include <fcntl.h>

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
    fd_set readfds;

    while (running) {
        FD_ZERO(&readfds);
        FD_SET(serverSocket, &readfds);
        int max_sd = serverSocket;

        for (int clientSocket : clientSockets) {
            FD_SET(clientSocket, &readfds);
            if (clientSocket > max_sd) {
                max_sd = clientSocket;
            }
        }

        int activity = select(max_sd + 1, &readfds, nullptr, nullptr, nullptr);
        if (activity < 0 && errno != EINTR) {
            std::cerr << "Select error" << std::endl;
            continue;
        }

        // fcntl(serverSocket, F_SETFL, O_NONBLOCK);

        if (FD_ISSET(serverSocket, &readfds)) {
            int newSocket = accept(serverSocket, nullptr, nullptr);
            if (newSocket >= 0) {
                std::cout << "New connection accepted" << std::endl;
                clientSockets.push_back(newSocket);
            } else {
                std::cerr << "Failed to accept new connection" << std::endl;
            }
        }

        for (auto it = clientSockets.begin(); it != clientSockets.end();) {
            int clientSocket = *it;
            if (FD_ISSET(clientSocket, &readfds)) {
                char buffer[1024] = {};
                int valread = recv(clientSocket, buffer, 1024, 0);
                if (valread == 0) {
                    std::cout << "Client disconnected" << std::endl;
                    close(clientSocket);
                    it = clientSockets.erase(it);
                } else if (valread < 0) {
                    std::cerr << "Failed to receive data" << std::endl;
                    ++it;
                } else {
                    std::cout << "Received: " << buffer << std::endl;
                    const char *response = "Message received. Hello from server!";
                    send(clientSocket, response, strlen(response), 0);
                    ++it;
                }
            } else {
                ++it;
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