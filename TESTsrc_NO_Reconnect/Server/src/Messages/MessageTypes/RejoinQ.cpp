#include "../MessageFormat.h"
#include "../../Data/DataVectors.h"
#include "../../Messages/TAGS.h"

namespace RejoinQ {
    std::string rejoinQueue(int fd) {
        // Check if message could come
        for (Player &player : DataVectors::players) {
            if (player.fd == fd) {
                if (player.status != 1) {
                    return MessageFormat::createViolationMess();
                }
            }
        }
        // Check if player is in game
        for (Game &game : DataVectors::games) {
            for (Player &player : game.gamePlayers) {
                if (player.fd == fd) {
                    // Remove player from game
                    game.gamePlayers.erase(std::remove_if(game.gamePlayers.begin(), game.gamePlayers.end(), [fd](Player &player) {
                        return player.fd == fd;
                    }), game.gamePlayers.end());
                }
            }
        }

        // Get the player that wants to rejoin queue
        for (int i = 0; i < DataVectors::players.size(); i++) {
            if (DataVectors::players[i].fd == fd) {
                DataVectors::players[i].status = 0;
                break;
            }
        }

        // Prepare response and return
        std::string tag;
        tag.append(BASE_QUEUE).append(QUEUE_REJOIN);

        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}