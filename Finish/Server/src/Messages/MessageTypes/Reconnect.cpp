#include "../MessageFormat.h"
#include "../../Data/Player.h"
#include "../../Data/DataVectors.h"
#include "../../Events/Events.h"
#include "../../Messages/TAGS.h"
#include "../../Server/Server.h"

/**
 * Namespace Reconnect
 * Collection of functions that handle the reconnection of a player
 */
namespace Reconnect {
    /**
     * @brief Function reconnect
     * Handles the reconnection of a player
     * @param fd - File descriptor of the player
     * @return Response to the player | Empty string if no response
     */
    std::string reconnect(int fd) {
        // Find the player
        bool found = false;
        Player *player = nullptr;
        for (Player& p : DataVectors::players) {
            if (p.fd == fd) {
                player = &p;
                found = true;
                break;
            }
        }
        if (!found) {
            return MessageFormat::createViolationMess();
        }

        // Check if player is in right state
        if (player->status != -1) {
            return MessageFormat::createViolationMess();
        }

        // Check if player is in the queue or game
        Game *game = nullptr;
        found = false;
        for (Game &g : DataVectors::games) {
            for (Player &p : g.gamePlayers) {
                if (p.fd == player->fd) {
                    game = &g;
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            player->status = 0;
            // Update player in the vector
            for (Player& p : DataVectors::players) {
                if (p.fd == player->fd) {
                    p = *player;
                    break;
                }
            }
            std::string tag;
            tag.append(BASE_CONNECTION).append(CONNECTION_RECONNECT);
            std::string information = "noGame";
            std::string response = MessageFormat::prepareResponse(information, tag);
        } else {
            player->status = 1;
            // Update player in the vector and in Game
            for (Player& p : DataVectors::players) {
                if (p.fd == player->fd) {
                    p = *player;
                    break;
                }
            }
            for (Game &g : DataVectors::games) {
                for (Player &p : g.gamePlayers) {
                    if (p.fd == player->fd) {
                        p = *player;
                        break;
                    }
                }
            }

            // Send Mess to this player with gamedata
            // Unpause the game
            game->gamePaused = false;
            // Update game
            GameM::updateGame(*game);


            std::string tag;
            tag.append(BASE_CONNECTION).append(CONNECTION_RECONNECT);

            // Make information
            std::string information;

            // Player names
            for (int i = 0; i < game->playerNames.size(); i++) {
                information.append(game->playerNames[i]).append("|");
                // Scores
                for (int j = 0; j < game->scores[i].size(); j++) {
                    information.append(std::to_string(game->scores[i][j]));
                    if (j != game->scores[i].size() - 1) {
                        information.append(",");
                    } else {
                        information.append("|");
                    }
                }
                // Dices
                for (int j = 0; j < game->dices[i].size(); j++) {
                    information.append(game->dices[i][j].id).append(",")
                    .append(std::to_string(game->dices[i][j].value)).append(",")
                    .append(std::to_string(game->dices[i][j].selected)).append(",")
                    .append(std::to_string(game->dices[i][j].hold));
                    if (j != game->dices[i].size() - 1) {
                        information.append("/");
                    } else {
                        information.append("|");
                    }
                }
                information.append(";");
            }

            // On move
            information.append(game->onMove).append(";");

            // States
            information.append(std::to_string(game->throwB[0])).append(",").append(std::to_string(game->throwB[1]));

            // Game paused
            information.append(";").append(std::to_string(game->gamePaused));

            std::string response = MessageFormat::prepareResponse(information, tag);

            // Send response to the other player
            for (Player &p : game->gamePlayers) {
                if (p.fd != player->fd) {
                    std::pair<int, std::string> result = Events::announcePlayerReconnect(*player);
                    if (result.first != -1) {
                        send(result.first, result.second.c_str(), result.second.size(), 0);
                    }
                }
            }
            return response;
        }
        return "";
    }
}
