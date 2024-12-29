#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"

namespace GameM {
    std::string throwDice(int fd) {
        // Check if the message could come
        if (!checkValidInput(true, "", false, fd)) {
            return MessageFormat::createViolationMess();
        }

        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;

        // Which player is throwing the dices
        std::pair<std::string, std::string> results;
        if (name == finder.first.playerNames[0]) {
            results = finder.first.rollDices(0);
        } else {
            results = finder.first.rollDices(1);
        }

        std::string throwedDices = results.first;
        std::string switchPlayer = results.second;

        std::string throwB = std::to_string(finder.first.throwB[0]) + "," + std::to_string(finder.first.throwB[1]);

        // Last switch
        if (switchPlayer != " ") {
            finder.first.lastSwitch = true;
        }

        // Update the game in the vector
        updateGame(finder.first);


        // Prepare response and return
        std::string tag, respInfo;
        tag.append(BASE_GAME).append(GAME_THROW_DICE);
        respInfo.append(gameMessGen(name, " ", throwedDices, throwB, switchPlayer, " "));

        return MessageFormat::prepareResponse(respInfo, tag);

    }

    std::string selectDice(int fd, std::string& information) {
        // Check if the message could come
        if (!checkValidInput(false, information, false,fd)) {
            return MessageFormat::createViolationMess();
        }

        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;
        std::string changedDice, score, throwB;

        // Which player is throwing the dices
        if (name == finder.first.playerNames[0]) {
            std::string response = finder.first.selectDices(0, information);
            changedDice.append(response);

            finder.first.calculateThrow(0);
            score.append(std::to_string(finder.first.scores[0][0])).append(",")
            .append(std::to_string(finder.first.scores[0][1])).append(",")
            .append(std::to_string(finder.first.scores[0][2]));

        } else {
            std::string response = finder.first.selectDices(1, information);
            changedDice.append(response);

            finder.first.calculateThrow(1);
            score.append(std::to_string(finder.first.scores[1][0])).append(",")
            .append(std::to_string(finder.first.scores[1][1])).append(",")
            .append(std::to_string(finder.first.scores[1][2]));
        }
        throwB = std::to_string(finder.first.throwB[0]) + "," + std::to_string(finder.first.throwB[1]);

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag, respInfo;
        tag.append(BASE_GAME).append(GAME_SELECT_DICE);
        respInfo.append(gameMessGen(name, score, changedDice, throwB," ", " "));

        return MessageFormat::prepareResponse(respInfo, tag);
    }

    std::string nextTurn(int fd) {
        // Check if the message could come
        if (!checkValidInput(false, "", true, fd)) {
            return MessageFormat::createViolationMess();
        }

        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;

        // Which player is throwing the dices
        std::string holdedDices, score, throwedDices, throwB, switchPlayer;
        if (name == finder.first.playerNames[0]) {
            holdedDices = finder.first.nextRound(0);
            std::pair<std::string, std::string> results = finder.first.rollDices(0);
            throwedDices = results.first;
            switchPlayer = results.second;
            score.append(std::to_string(finder.first.scores[0][0])).append(",")
            .append(std::to_string(finder.first.scores[0][1])).append(",")
            .append(std::to_string(finder.first.scores[0][2]));
        } else {
            holdedDices = finder.first.nextRound(1);
            std::pair<std::string, std::string> results = finder.first.rollDices(1);
            throwedDices = results.first;
            switchPlayer = results.second;
            score.append(std::to_string(finder.first.scores[1][0])).append(",")
            .append(std::to_string(finder.first.scores[1][1])).append(",")
            .append(std::to_string(finder.first.scores[1][2]));
        }
        throwB = std::to_string(finder.first.throwB[0]) + "," + std::to_string(finder.first.throwB[1]);

        // Last switch
        if (switchPlayer != " ") {
            finder.first.lastSwitch = true;
        }

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag, respInfo;
        tag.append(BASE_GAME).append(GAME_NEXT_TURN);
        respInfo.append(gameMessGen(name, score, holdedDices + throwedDices, throwB, switchPlayer, " "));

        return MessageFormat::prepareResponse(respInfo, tag);
    }

    std::string endTurn(int fd) {
        // Check if the message could come
        if (!checkValidInput(false, "", true, fd)) {
            return MessageFormat::createViolationMess();
        }

        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;

        // Find the second player in the game
        std::string updateDiceS, score, throwB, switchPlayer, unique;
        if (name == finder.first.playerNames[0]) {
            unique = finder.first.endRound(0);
            updateDiceS = finder.first.updateDiceEnd(0);
            finder.first.scoreRestart(0);
            score.append(std::to_string(finder.first.scores[0][0])).append(",")
            .append(std::to_string(finder.first.scores[0][1])).append(",")
            .append(std::to_string(finder.first.scores[0][2]));
            switchPlayer = finder.first.playerNames[1];
        } else {
            unique = finder.first.endRound(1);
            updateDiceS = finder.first.updateDiceEnd(1);
            finder.first.scoreRestart(1);
            score.append(std::to_string(finder.first.scores[1][0])).append(",")
            .append(std::to_string(finder.first.scores[1][1])).append(",")
            .append(std::to_string(finder.first.scores[1][2]));
            switchPlayer = finder.first.playerNames[0];
        }
        throwB = std::to_string(finder.first.throwB[0]) + "," + std::to_string(finder.first.throwB[1]);

        // Check if the game is over
        if (unique.empty()) {
            unique = " ";
        }

        // Switch the player on move
        finder.first.onMove = switchPlayer;

        // Last switch
        if (switchPlayer != " ") {
            finder.first.lastSwitch = true;
        }

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag, respInfo;
        tag.append(BASE_GAME).append(GAME_END_TURN);
        respInfo.append(gameMessGen(name, score, updateDiceS, throwB,switchPlayer, unique));

        return MessageFormat::prepareResponse(respInfo, tag);
    }

    std::string gameMessGen(const std::string& name, const std::string& score, const std::string& dice, const std::string& throwB,const std::string& switchE, const std::string& unique) {
        std::string info;
        info.append(name).append(";"); // Player Name
        info.append(score).append(";"); // Score
        info.append(dice).append(";"); // Dices
        info.append(throwB).append(";"); // Throw Bool
        info.append(switchE).append(";"); // Switch
        info.append(unique).append(";"); // Unique state (Disconnect, Game state)

        return info;
    }

    std::pair<Game, Player> whereAndWho(int fd) {
        Player *player = nullptr;
        Game *game = nullptr;
        for (Player& p : DataVectors::players) {
            if (p.fd == fd) {
                player = &p;
                break;
            }
        }
        for (Game& g : DataVectors::games) {
            for (Player& p : g.gamePlayers) {
                if (p.fd == fd) {
                    game = &g;
                    break;
                }
            }
        }
        // if (player == nullptr || game == nullptr) {
        //     throw std::runtime_error("Player or Game not found");
        // }

        return std::make_pair(*game, *player);
    }

    bool checkValidInput(bool throwCheck, std::string diceID, bool turnS, int fd) {
        for (Player &player : DataVectors::players) {
            if (player.fd == fd) {
                if (player.status != 1) {
                    return false;
                }
                // Check if the player is in a game that exists
                bool foundGame = false;
                Game *gameH = nullptr;
                for (Game &game : DataVectors::games) {
                    for (Player &p : game.gamePlayers) {
                        if (p.fd == fd) {
                            foundGame = true;
                            gameH = &game;
                            break;
                        }
                    }
                }
                if (!foundGame) {
                    return false;
                }
                // Check if the player is on move
                if (gameH->onMove != player.name) {
                    return false;
                }
                // Check if next turn is possible
                if (turnS) {
                    if (gameH->playerNames[0] == player.name && gameH->scores[0][2] == 0) {
                        return false;
                    }
                    if (gameH->playerNames[1] == player.name && gameH->scores[1][2] == 0) {
                        return false;
                    }
                }
                // Check if the dice is held
                if (!diceID.empty()) {
                    if (gameH->playerNames[0] == player.name && gameH->throwB[0]) {
                        bool found = false;
                        for (Dice &dice : gameH->dices[0]) {
                            if (dice.id == diceID) {
                                found = true;
                                if (dice.hold) {
                                    return false;
                                }
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    } else if (gameH->playerNames[1] == player.name && gameH->throwB[1]) {
                        bool found = false;
                        for (Dice &dice : gameH->dices[1]) {
                            if (dice.id == diceID) {
                                found = true;
                                if (dice.hold) {
                                    return false;
                                }
                            }
                        }
                        if (!found) {
                            return false;
                        }
                    } else {
                        // Player did not throw the dices
                        return false;
                    }
                }
                // Check if the player has thrown the dices
                if (throwCheck) {
                    if (gameH->playerNames[0] == player.name && gameH->throwB[0]) {
                        return false;
                    }
                    if (gameH->playerNames[1] == player.name && gameH->throwB[1]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    void updateGame(Game& game) {
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == game.playerNames[0]) {
                g = game;
                break;
            }
        }
    }
}
