#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"

namespace Queue {
    std::string status(int fd) {
        // Check if game exists
        std::cout << "Checking if game exists" << std::endl;
        if (DataVectors::game != nullptr) {
            // Set player 1 status to "playing"
            for (int i = 0; i < DataVectors::players.size() || i < 2; i++) {
                if (DataVectors::players[i].fd == fd) {
                    DataVectors::players[i].status = 1;
                    DataVectors::game->gamePlayers.push_back(DataVectors::players[i]);
                }
            }
            return QUEUE_START_INFO;
        }

        // Check if DataVector Player is less than 2
        std::cout << "Checking if players are less than 2" << std::endl;
        /* TODO: Implement the following:
         * If there are less than 2 players, return "wait"
         * If there are 2 players, return "start"
         * Create a game
         */
        if (DataVectors::players.size() < 2) {
            return QUEUE_WAIT_INFO;
        }

        // If there are 2 players, return "start"
        // Create a game
        std::cout << "Creating a game" << std::endl;
        auto* game = new Game();
        DataVectors::game = game;
        return QUEUE_WAIT_INFO;
    }
}