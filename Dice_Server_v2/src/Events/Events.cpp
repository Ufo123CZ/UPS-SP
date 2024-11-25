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

        // Player names
        for (int i = 0; i < game.playerNames.size(); i++) {
            information.append(game.playerNames[i]).append("|");
            // Scores
            for (int j = 0; j < game.scores[i].size(); j++) {
                information.append(std::to_string(game.scores[i][j]));
                if (j != game.scores[i].size() - 1) {
                    information.append(",");
                } else {
                    information.append("|");
                }
            }
            // Dices
            for (int j = 0; j < game.dices[i].size(); j++) {
                information.append(game.dices[i][j].id).append(",")
                .append(std::to_string(game.dices[i][j].value)).append(",")
                .append(std::to_string(game.dices[i][j].selected)).append(",")
                .append(std::to_string(game.dices[i][j].hold));
                if (j != game.dices[i].size() - 1) {
                    information.append("/");
                } else {
                    information.append("|");
                }
            }
            information.append(";");
        }

        // On move
        information.append(game.onMove).append(";");

        std::string response = MessageFormat::prepareResponse(information, tag);
        return response;
    }
}
