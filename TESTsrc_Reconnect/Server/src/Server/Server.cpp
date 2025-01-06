#include "Server.h"
#include "../Utils/Consts.h"
#include "../Messages/MessageFormat.h"
#include "../Data/DataVectors.h"
#include "../Messages/TAGS.h"
#include "../Events/Events.h"
#include <iostream>
#include <cstring>
#include <fcntl.h>
#include <csignal>
#include <sys/ioctl.h>
#include <arpa/inet.h>
#include <mutex>
#include <thread>
#include <chrono>


bool nofd4 = false;

std::mutex playerMutex;

Server::Server() : running(false), serverSocket(-1) {}

Server::~Server() {
    stop();
}

bool Server::init(const std::string& ip, int port) {
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
    serverAddr.sin_addr.s_addr = inet_addr(ip.c_str());
    serverAddr.sin_port = htons(port);

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


    // Start the player activity check thread
    std::thread activityThread([this]() { checkPlayerActivity(); });
    activityThread.detach();

    // Main loop
    while (running) {
        readfds = client_socks;

        // int activity = select(FD_SETSIZE, &readfds, nullptr, nullptr, &timeout);
        int activity = select(FD_SETSIZE, &readfds, nullptr, nullptr, nullptr);
        if (activity < 0 && errno != EINTR) {
            std::cerr << "Select error" << std::endl;
            continue;
        }

        std::vector<std::pair<int, std::string>> update = {};
        for (int fd = 3; fd < FD_SETSIZE; fd++) {
            if (FD_ISSET(fd, &readfds)) {
                if (fd == serverSocket) {
                    sockaddr_in peer_addr{};
                    socklen_t len_addr = sizeof(peer_addr);
                    socket_t client_socket = accept(serverSocket, reinterpret_cast<sockaddr *>(&peer_addr), &len_addr);
                    if (client_socket >= 0) {
                        FD_SET(client_socket, &client_socks);
                        std::cout << "New client connected and added to socket set" << std::endl;
                        if (client_socket != 3){
                            Player player = Player(client_socket, -2);
                            DataVectors::players.push_back(player);
                            std::string tag;
                            tag.append(BASE_LOGIN).append(LOGIN);
                            std::string response = MessageFormat::prepareResponse(SUCCESS, tag);
                            send(client_socket , response.c_str() , response.size() , 0);
                        }
                    } else {
                        std::cerr << "Failed to accept new connection" << std::endl;
                    }
                } else {
                    int a2read;
                    ioctl(fd, FIONREAD, &a2read);
                    if (a2read > 0) {
                        // Read the message
                        std::string message = MessageProcessing::readMessage(fd);
                        std::string response;
                        if (message.empty()) {
                            std::cerr << "Failed to read message" << std::endl;
                            goto terminate;
                        }

                        // Update for player last message
                        for (Player& player : DataVectors::players) {
                            if (player.fd == fd) {
                                if (nofd4) {
                                    if (fd != 4) {
                                        player.lastMess = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
                                    }
                                } else {
                                    player.lastMess = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
                                }
                                // Check if player was disconnected
                                if (player.status == -1) {
                                    if (fd == 4 && nofd4) {
                                        break;
                                    }
                                    // Check if player was in game
                                    bool inGame = false;
                                    for (Game game : DataVectors::games) {
                                        for (Player p : game.gamePlayers) {
                                            if (p.fd == fd) {
                                                player.status = 1;
                                                inGame = true;
                                                // Announce the other player that the player has reconnected
                                                std::pair<int, std::string> result = Events::announcePlayerReconnect(player);
                                                if (result.first != -1) {
                                                    send(result.first, result.second.c_str(), result.second.size(), 0);
                                                }
                                            }
                                        }
                                    }
                                    if (!inGame) {
                                        player.status = 0;
                                    }
                                }
                            }
                        }

                        // Process the message and generate a response
                        response = MessageProcessing::processMessage(fd, message);
                        if (response.empty() || response == "\n") {
                            terminate:
                            response = MessageFormat::createViolationMess();
                        }
                        bool killed = false;
                        if ((response.find(BASE_LOGIN) != std::string::npos && response.find(LOGIN) != std::string::npos && response.find(ERROR) != std::string::npos)
                            || (response.find(BASE_LOGIN) != std::string::npos && response.find(NAMESET) != std::string::npos && response.find(ERROR) != std::string::npos)
                            || response.find(TERMINATION) != std::string::npos) {
                            send(fd, response.c_str(), response.size(), 0);
                            sleep(1);
                            // Remove the player from the game if he is in the game
                            for (Game& game : DataVectors::games) {
                                game.gamePlayers.erase(std::remove_if(game.gamePlayers.begin(), game.gamePlayers.end(), [fd](const Player& player) {
                                    return player.fd == fd;
                                }), game.gamePlayers.end());
                            }
                            // Remove the player from the data vectors
                            DataVectors::players.erase(std::remove_if(DataVectors::players.begin(), DataVectors::players.end(), [fd](const Player& player) {
                                return player.fd == fd;
                            }), DataVectors::players.end());
                            close(fd);
                            FD_CLR(fd, &client_socks);
                            killed = true;
                        }

                        // Update for all in players in modified game
                        if (!killed && message.find(BASE_GAME) != std::string::npos) {
                            // Get the game and player
                            std::pair<Game, Player> finder = GameM::whereAndWho(fd);
                            for (Player& player : finder.first.gamePlayers) {
                                if (player.fd != fd) {
                                    std::cout << "Update ready for player: " << player.name << std::endl;
                                    update.emplace_back(player.fd, response);
                                }
                            }

                        }

                        // Message contains the command logout close the socket
                        if (!killed && message.find(LOGOUT) != std::string::npos) {
                            send(fd, response.c_str(), response.size(), 0);
                            // if the player was in game remove him from the game and announce the other player that he left
                            for (Player p : DataVectors::players) {
                                if (fd == p.fd && p.status == 1) {
                                    for (auto & game : DataVectors::games) {
                                        if ((game.playerNames[0] == p.name || game.playerNames[1] == p.name) && game.gamePlayers.size() == 2) {
                                            std::pair<int, std::string> result = Events::announcePlayerLeft(p);
                                            if (result.first != -1) {
                                                send(result.first, result.second.c_str(), result.second.size(), 0);
                                            }
                                        }
                                        // Erase from DataVectors::games gamePlayers vector the player that left
                                        std::erase_if(game.gamePlayers, [p](const Player& player) {
                                            return player.name == p.name;
                                        });
                                    }
                                }
                            }


                            // Remove the player from the data vectors
                            std::erase_if(DataVectors::players, [fd](const Player& player) {
                                return player.fd == fd;
                            });
                            close(fd);
                            FD_CLR(fd, &client_socks);

                            std::cout << "Client disconnected and removed from socket set" << std::endl;
                        }

                        if (fd == 4 && nofd4) {
                            response = "";
                        } else {
                            send(fd, response.c_str(), response.size(), 0);
                        }
                    }
                }
            }
        }

        // Special Events
        /* UPDATE PLAYERS */
        for (std::pair<int, std::string> pair : update) {
            for (Player& player : DataVectors::players) {
                if (player.fd == pair.first) {
                    std::cout << "Update player: " << player.name << std::endl;
                    std::cout << "Response: " << pair.second << std::endl;
                    send(pair.first, pair.second.c_str(), pair.second.size(), 0);
                    std::erase(update, pair);
                }
            }
        }

        /* GAME DELETE */
        // Check if game have 0 players
        // Erase the game from the data vectors
        for (Game& game : DataVectors::games) {
            if (game.gamePlayers.empty()) {
                game.gameEnd = true;
                // Game Does not have gameID
                std::erase_if(DataVectors::games, [&game](const Game& g) {
                    return game.gameEnd;
                });
            }
        }
        // Check if some games have ended
        // Erase the game from the data vectors
        if (!DataVectors::games.empty()) {
            std::erase_if(DataVectors::games, [](const Game& game) {
                return game.gameEnd;
            });
        }

        /* RECONNECT GAME (NEW CLIENT) */
        // Player connects to server, and he was in the game when he disconnected reconnect him to the game
        // This event can occur only when the player is in the players vector
        if (!DataVectors::games.empty()) {
            for (auto & player : DataVectors::players) {
                if (player.status == 0) {
                    std::string response = Events::reconnectGame(player);
                    if (!response.empty()) {
                        player.status = 1;
                        send(player.fd, response.c_str(), response.size(), 0);

                        // announce the other player that the player has reconnected
                        std::pair<int, std::string> result = Events::announcePlayerReconnect(player);
                        if (result.first != -1) {
                            send(result.first, result.second.c_str(), result.second.size(), 0);
                        }
                    }
                }
            }
        }

        /* CREATE GAME */
        // Create new game and change response to the game message
        // This event can occur only when there is more than 2 players in players vector
        // Check if there are more than 2 players in queue
        int playersInQueue = 0;
        for (Player& player : DataVectors::players) {
            if (player.status == 0) {
                playersInQueue++;
            }
        }

        if (DataVectors::players.size() >= 2 && playersInQueue >= 2) {
            bool allInGame = true;
            for (Player& player : DataVectors::players) {
                if (player.status != 1) {
                    allInGame = false;
                    break;
                }
            }

            if (allInGame) {
                continue;
            }

            std::string response = Events::createGame();
            Game *ptrToGame = &DataVectors::games[DataVectors::games.size() - 1];
            std::cout << "Players: " << ptrToGame->gamePlayers[0].name
            << " and " << ptrToGame->gamePlayers[1].name << std::endl;
            std::cout << response << std::endl;

            // send the response to all players that are in now created game
            for (Player& player : ptrToGame->gamePlayers) {
                send(player.fd, response.c_str(), response.size(), 0);
            }
        }
    }
}

void Server::stop() {
    running = false;
    if (serverSocket != -1) {
        close(serverSocket);
    }
    for (int clientSocket : clientSockets) {
        close(clientSocket);
    }
    clientSockets.clear();
}
void Server::checkPlayerActivity() {
    while (running) {
        std::this_thread::sleep_for(std::chrono::seconds(1)); // Check every 1 seconds

        std::lock_guard<std::mutex> lock(playerMutex);
        for (auto it = DataVectors::players.begin(); it != DataVectors::players.end(); ) {
            std::time_t now = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
            if (now - it->lastMess > 25) { // If no message for 25 seconds
                std::cout << "Player " << it->name << " is inactive for 25 seconds and will be removed." << std::endl;
                close(it->fd);
                for (Game& game : DataVectors::games) {
                    for (Player p : game.gamePlayers) {
                        if (p.fd == it->fd) {
                            std::pair<int, std::string> result = Events::announcePlayerLeft(*it);
                            if (result.first != -1) {
                                send(result.first, result.second.c_str(), result.second.size(), 0);
                            }
                            // Remove the player from the game and from Player vector
                            game.gamePlayers.erase(std::remove_if(game.gamePlayers.begin(), game.gamePlayers.end(), [it](const Player& player) {
                                return player.fd == it->fd;
                            }), game.gamePlayers.end());
                            it = DataVectors::players.erase(it);
                        }
                    }
                }
                // Remove the player from the data vectors
                close(it->fd);
                FD_CLR(it->fd, &client_socks);
                DataVectors::players.erase(std::remove_if(DataVectors::players.begin(), DataVectors::players.end(), [it](const Player& player) {
                    return player.fd == it->fd;
                }), DataVectors::players.end());


                // if DataVectors::players.size() == 0 break
                if (DataVectors::players.empty()) {
                    break;
                }
            } else if (now - it->lastMess > 3 && it->status != -1) { // If no message for 5 seconds
                it->status = -1; // Set state to -1 (disconnected)
                std::cout << "Player " << it->name << " is inactive for 3 seconds and set to disconnected." << std::endl;
                // Send message to the player that he is disconnected
                // check if the player is in the game
                // if yes send message to the other player that he left
                for (Game& game : DataVectors::games) {
                    for (Player p : game.gamePlayers) {
                        if (p.fd == it->fd) {
                            std::pair<int, std::string> result = Events::announcePlayerTempLeft(*it);
                            if (result.first != -1) {
                                std::cout << result.second << std::endl;
                                send(result.first, result.second.c_str(), result.second.size(), 0);
                            }
                        }
                    }
                }
                ++it;
            } else {
                ++it;
            }
        }
    }
}