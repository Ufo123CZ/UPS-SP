#include "Server.h"
#include "../Utils/Consts.h"
#include "../Messages/MessageFormat.h"
#include "../Data/DataVectors.h"
#include "../Messages/TAGS.h"
#include "../Events/Events.h"
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

    // Initialize the message format
    MessageFormat::initMessageFormatMap();

    // Initialize data vectors
    DataVectors::initDataVectors();

    // Main loop
    while (running) {
        readfds = client_socks;

        int activity = select(FD_SETSIZE, &readfds, nullptr, nullptr, &timeout);
        if (activity < 0 && errno != EINTR) {
            std::cerr << "Select error" << std::endl;
            continue;
        }

        // bool doNotPing = false;
        std::vector<int> currentPings = {};
        // sleep(1);
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
                        // Find name of the player
                        for (Player& player : DataVectors::players) {
                            if (player.fd == fd) {
                                std::cout << "Player " << player.name << ":" << std::endl;
                            }
                        }

                        // Read the message
                        std::string message = MessageProcessing::readMessage(fd);
                        if (message.empty()) {
                            std::cerr << "Failed to read message" << std::endl;
                            continue;
                        }

                        // Add the client to the current pings
                        currentPings.push_back(fd);

                        // Process the message and generate a response
                        std::string response = MessageProcessing::processMessage(fd, message);
                        if (response.empty() || response == "\n") {
                            response = MessageFormat::createFailMessage();
                        }

                        // Special Events
                        // Init Game
                        // Create new game and change response to the game message
                        // This event can occur only when there is more than 2 players in players vector
                        if (DataVectors::players.size() >= 2) {
                            response = Events::createGame();
                        }

                        // Message contains the command logout close the socket
                        if (message.find(LOGOUT) != std::string::npos) {
                            std::erase(currentPings, fd);
                            close(fd);
                            FD_CLR(fd, &client_socks);
                            std::cout << "Client disconnected and removed from socket set" << std::endl;
                        }

                        std::cout << "---------------------------" << std::endl;


                        // Send the response
                        send(fd, response.c_str(), response.size(), 0);
                    }
                }
            }
        }
        // TODO: uncomment this code for lost connection with the client
        // Check who send some message comparing the current pings and the data vectors
        // if (DataVectors::players.empty()) {
        //     currentPings.clear();
        //     continue;
        // }
        // for (int i = 0; i < DataVectors::players.size(); i++) {
        //     for (int currentPing : currentPings) {
        //         if (DataVectors::players[i].fd == currentPing) {
        //             DataVectors::players[i].ping = DataVectors::players[i].ping--;
        //             // If the player has a ping less than 0, remove it from the data vectors and log him out
        //             if (DataVectors::players[i].ping <= 0) {
        //                 std::cout << "Player " << DataVectors::players[i].name << " has been disconnected" << std::endl;
        //                 DataVectors::players.erase(DataVectors::players.begin() + i);
        //                 close(DataVectors::players[i].fd);
        //                 FD_CLR(DataVectors::players[i].fd, &client_socks);
        //             }
        //         }
        //     }
        // }
        currentPings.clear();
        // if (!DataVectors::players.empty()) {
        //     if (doNotPing) {
        //         continue;
        //     }
        //     sleep(1);
        //     for (Player& player : DataVectors::players) {
        //         std::cout << "PING: " << player.name << std::endl;
        //         std::string pingMessage = MessageFormat::createPingMessage();
        //         send(player.fd, pingMessage.c_str(), pingMessage.size(), 0);
        //     }
        // }
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