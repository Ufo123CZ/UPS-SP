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
                << "Status: " << player.status << std::endl
                << "Socket: " << player.fd << std::endl
                << "Ping: " << player.ping << "/5" << std::endl;
            }
        }},
        {"game", [](Server &server, std::string&) {
            // Print the game
            if (DataVectors::games.empty()) {
                std::cout << "No games are created." << std::endl;
                return;
            }
            int i = 0;
            for (const Game &game : DataVectors::games) {
                std::cout << "Game " << ++i << ": " << std::endl
                << "Player 1: " << game.playerNames[0] << std::endl
                << "Player 2: " << game.playerNames[1] << std::endl
                << "On move: " << game.onMove << std::endl
                << "Scores: " << std::endl
                << "Player 1: " << game.scores[0][0] << " (total), " << game.scores[0][1] << " (round), " << game.scores[0][2] << " (throw)" << std::endl
                << "Player 2: " << game.scores[1][0] << " (total), " << game.scores[1][1] << " (round), " << game.scores[1][2] << " (throw)" << std::endl
                << "Dices: " << std::endl
                << "Player 1: " << std::endl;
                for (const Dice &dice : game.dices[0]) {
                    std::cout << "ID: " << dice.id << ", Value: " << dice.value << ", Selected: " << dice.selected << ", Hold: " << dice.hold << std::endl;
                }
                std::cout << "Player 2: " << std::endl;
                for (const Dice &dice : game.dices[1]) {
                    std::cout << "ID: " << dice.id << ", Value: " << dice.value << ", Selected: " << dice.selected << ", Hold: " << dice.hold << std::endl;
                }
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