#include "Events.h"
#include "../Data/Player.h"
#include "../Data/Game.h"
#include "../Data/DataVectors.h"
#include "../Messages/MessageFormat.h"
#include "../Messages/TAGS.h"

namespace Events {
    std::string createGame() {
        // Take first two players from the players vector that are not in the game
        // Create a new game with these two players
        Player *p1 = nullptr;
        Player *p2 = nullptr;
        for (Player& player : DataVectors::players) {
            if (player.status == 1) {
                continue;
            }
            // Check if player is already registered in the game
            if (player.status == 0) {
                bool isRegistered = false;
                for (Game& game : DataVectors::games) {
                    for (Player& gamePlayer : game.gamePlayers) {
                        if (gamePlayer.name == player.name) {
                            isRegistered = true;
                        }
                    }
                }
                if (isRegistered) {
                    continue;
                }
            }

            if (p1 == nullptr) {
                p1 = &player;
                p1->status = 1;
                continue;
            }
            if (p2 == nullptr) {
                p2 = &player;
                p2->status = 1;
                break;
            }
        }
        if (p1 == nullptr || p2 == nullptr) {
            return "";
        }

        Game game = Game(*p1, *p2);
        DataVectors::games.push_back(game);

        std::string tag;
        tag.append(BASE_GAME).append(GAME_CREATE);

        // Make information
        std::string information;
        // Player1
        information.append(game.playerNames[0]).append("|");
        for (int i = 0; i < game.scoresP1.size(); i++) {
            information.append(std::to_string(game.scoresP1[i]));
            if (i != game.scoresP1.size() - 1) {
                information.append(",");
            } else {
                information.append("|");
            }
        }
        for (int i = 0; i < game.dicesP1.size(); i++) {
            information.append(game.dicesP1[i].id).append(",")
            .append(std::to_string(game.dicesP1[i].value)).append(",")
            .append(std::to_string(game.dicesP1[i].selected)).append(",")
            .append(std::to_string(game.dicesP1[i].hold));
            if (i != game.dicesP1.size() - 1) {
                information.append("/");
            } else {
                information.append("|");
            }
        }
        information.append(";");
        // Player2
        information.append(game.playerNames[1]).append("|");
        for (int i = 0; i < game.scoresP2.size(); i++) {
            information.append(std::to_string(game.scoresP2[i]));
            if (i != game.scoresP2.size() - 1) {
                information.append(",");
            } else {
                information.append("|");
            }
        }
        for (int i = 0; i < game.dicesP2.size(); i++) {
            information.append(game.dicesP2[i].id).append(",")
            .append(std::to_string(game.dicesP2[i].value)).append(",")
            .append(std::to_string(game.dicesP2[i].selected)).append(",")
            .append(std::to_string(game.dicesP2[i].hold));
            if (i != game.dicesP2.size() - 1) {
                information.append("/");
            } else {
                information.append("|");
            }
        }
        information.append(";");
        // On move
        information.append(game.onMove).append(";");

        std::string response = MessageFormat::prepareResponse(information, tag);
        return response;
    }
}
