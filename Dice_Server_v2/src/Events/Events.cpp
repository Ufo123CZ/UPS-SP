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

        // States
        information.append(std::to_string(game.throwB[0])).append(",").append(std::to_string(game.throwB[1]));

        std::string response = MessageFormat::prepareResponse(information, tag);
        return response;
    }

    std::string reconnectGame(Player &player) {
        Game *game = nullptr;
        for (auto & i : DataVectors::games) {
            if (i.playerNames[0] == player.name || i.playerNames[1] == player.name) {
                i.gamePlayers.push_back(player);
                game = &i;
                break;
            }
        }
        if (game == nullptr) {
            return "";
        }
        // Unpause the game
        game->gamePaused = false;
        // Update game
        GameM::updateGame(*game);
        
        std::string tag;
        tag.append(BASE_GAME).append(GAME_RECONNECT);

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

        std::string response = MessageFormat::prepareResponse(information, tag);
        return response;
    }

    std::pair<int, std::string> announcePlayerLeft(const Player &player) {
        Game *game = nullptr;
        int makeAnouncementFor = 0, targetedPlayer = -1;
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == player.name) {
                makeAnouncementFor = 1;
                game = &g;
                break;
            }
            if (g.playerNames[1] == player.name) {
                makeAnouncementFor = 0;
                game = &g;
                break;
            }
        }
        if (game == nullptr) {
            return std::make_pair(-1, "");
        }
        // Pause the game
        game->gamePaused = true;
        // Update game
        GameM::updateGame(*game);

        // For who will be the announcement
        for (Player& p : game->gamePlayers) {
            if (p.name == game->playerNames[makeAnouncementFor]) {
                targetedPlayer = p.fd;
            }
        }

        std::string tag;
        tag.append(BASE_GAME).append(GAME_PLAYER_LEFT);

        std::string response = MessageFormat::prepareResponse(player.name, tag);
        return std::make_pair(targetedPlayer, response);
    }

    std::pair<int, std::string> announcePlayerTempLeft(const Player &player) {
        Game *game = nullptr;
        int makeAnouncementFor = 0, targetedPlayer = -1;
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == player.name) {
                makeAnouncementFor = 1;
                game = &g;
                break;
            }
            if (g.playerNames[1] == player.name) {
                makeAnouncementFor = 0;
                game = &g;
                break;
            }
        }
        if (game == nullptr) {
            return std::make_pair(-1, "");
        }
        // Pause the game
        game->gamePaused = true;
        // Update game
        GameM::updateGame(*game);

        // For who will be the announcement
        for (Player& p : game->gamePlayers) {
            if (p.name == game->playerNames[makeAnouncementFor]) {
                targetedPlayer = p.fd;
            }
        }

        std::string tag;
        tag.append(BASE_GAME).append(GAME_PLAYER_DISCONNECTED);

        std::string response = MessageFormat::prepareResponse(player.name, tag);
        return std::make_pair(targetedPlayer, response);
    }


    std::pair<int, std::string> announcePlayerReconnect(const Player &player) {
        Game *game = nullptr;
        int makeAnouncementFor = 0, targetedPlayer = -1;
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == player.name) {
                makeAnouncementFor = 1;
                game = &g;
                break;
            }
            if (g.playerNames[1] == player.name) {
                makeAnouncementFor = 0;
                game = &g;
                break;
            }
        }
        if (game == nullptr) {
            return std::make_pair(-1, "");
        }

        game->gamePaused = false;

        // For who will be the announcement
        for (Player& p : game->gamePlayers) {
            if (p.name == game->playerNames[makeAnouncementFor]) {
                targetedPlayer = p.fd;
            }
        }

        std::string tag;
        tag.append(BASE_GAME).append(GAME_PLAYER_JOINED);

        std::string response = MessageFormat::prepareResponse(player.name, tag);
        return std::make_pair(targetedPlayer, response);
    }

    std::string endGame() {
        std::string tag;
        tag.append(BASE_GAME).append(GAME_END);

        std::string response = MessageFormat::prepareResponse("Game Ended", tag);
        return response;

    }
}
