#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"

namespace GameM {
    std::string throwDice(int fd) {
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

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_THROW_DICE);
        std::string respInfo;
        respInfo.append(name).append(";"); // Player Name
        respInfo.append(" ").append(";"); // Score - null here
        respInfo.append(throwedDices).append(";"); // Dices
        respInfo.append(switchPlayer).append(";"); // Switch - null here

        return MessageFormat::prepareResponse(respInfo, tag);

    }
    std::string selectDice(int fd, std::string& information) {
        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;
        std::string changedDice, score;

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

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_SELECT_DICE);
        std::string respInfo;
        respInfo.append(name).append(";"); // Player Name
        respInfo.append(score).append(";"); // Score - null here
        respInfo.append(changedDice).append(";"); // Dices
        respInfo.append(" ").append(";"); // Switch - null here

        return MessageFormat::prepareResponse(respInfo, tag);
    }
    std::string nextTurn(int fd, std::string& information) {
        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;

        // Which player is throwing the dices
        std::string score, throwedDices, switchPlayer;
        if (name == finder.first.playerNames[0]) {
            finder.first.nextRound(0);
            std::pair<std::string, std::string> results = finder.first.rollDices(0);
            throwedDices = results.first;
            switchPlayer = results.second;
            score.append(std::to_string(finder.first.scores[0][0])).append(",")
            .append(std::to_string(finder.first.scores[0][1])).append(",")
            .append(std::to_string(finder.first.scores[0][2]));
        } else {
            finder.first.nextRound(1);
            std::pair<std::string, std::string> results = finder.first.rollDices(1);
            throwedDices = results.first;
            switchPlayer = results.second;
            score.append(std::to_string(finder.first.scores[1][0])).append(",")
            .append(std::to_string(finder.first.scores[1][1])).append(",")
            .append(std::to_string(finder.first.scores[1][2]));
        }

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_NEXT_TURN);
        std::string respInfo;
        respInfo.append(name).append(";"); // Player Name
        respInfo.append(score).append(";"); // Score - null here
        respInfo.append(throwedDices).append(";"); // Dices
        respInfo.append(switchPlayer).append(";"); // Switch

        return MessageFormat::prepareResponse(respInfo, tag);
    }
    std::string endTurn(int fd, std::string& information) {
        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // TODO: Score handle

        // Strings for the response
        const std::string name = finder.second.name;

        // Find the second player in the game
        std::string secondPlayer;
        if (name == finder.first.playerNames[0]) {
            secondPlayer = finder.first.playerNames[1];
        } else {
            secondPlayer = finder.first.playerNames[0];
        }

        // Switch the player on move
        finder.first.onMove = secondPlayer;

        // Update the game in the vector
        updateGame(finder.first);

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_END_TURN);
        std::string respInfo;
        respInfo.append(name).append(";"); // Player Name
        respInfo.append(" ").append(";"); // Score - null here
        respInfo.append(" ").append(";"); // Dices
        respInfo.append(secondPlayer).append(";"); // Switch

        return MessageFormat::prepareResponse(respInfo, tag);
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
        if (player == nullptr || game == nullptr) {
            throw std::runtime_error("Player or Game not found");
        }

        return std::make_pair(*game, *player);
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
