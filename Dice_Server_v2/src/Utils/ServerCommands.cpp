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
            int i = 0;
            for (const Player &player : DataVectors::players) {
                std::cout << "Player " << ++i << ": " << std::endl
                << "Name: " << player.name << std::endl
                << "Score: " << player.score << std::endl
                << "Status: " << player.status << std::endl
                << "Socket: " << player.fd << std::endl;
            }
        }},
        {"exit", [](Server &server, std::string&) {
            server.stop();
            runningCLI = false;
        }}
    };
}