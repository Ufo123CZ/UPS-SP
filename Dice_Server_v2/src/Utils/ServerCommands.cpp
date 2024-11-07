#include "ServerCommands.h"
#include "../Server/Server.h"
#include "../Data/Player.h"
#include "../Data/DataVectors.h"
#include <iostream>

extern std::atomic<bool> runningCLI;

// Initialize the command map
std::unordered_map<std::string, std::function<void(Server &server, std::string&)>> ServerCommands::commandMap;

void ServerCommands::initCommandMap() {
    commandMap = {
        {"players", [](Server &server, std::string&) {
            // Print all players
            if (DataVectors::players.empty()) {
                std::cout << "No players are logged in." << std::endl;
                return;
            }
            int i = 0;
            for (const Player &player : DataVectors::players) {
                std::cout << "Player " << ++i << ": " << std::endl
                << "Name: " << player.name << std::endl
                << "Score: " << player.score << std::endl
                << "Status: " << player.status << std::endl
                << "Socket: " << player.fd << std::endl;
            }
        }},
        {"game", [](Server &server, std::string&) {
            // Print the game
            if (DataVectors::game != nullptr) {
                std::cout << "Game exists." << std::endl;
                std::cout << "Players: " << std::endl;
                for (const Player &player : DataVectors::game->gamePlayers) {
                    std::cout << "Name: " << player.name << std::endl;
                }
            } else {
                std::cout << "No game exists." << std::endl;
            }
        }},
        {"stop", [](Server &server, std::string&) {
            server.stop();
        }},
        {"exit", [](Server &server, std::string&) {
            server.stop();
            runningCLI = false;
        }}
    };
}