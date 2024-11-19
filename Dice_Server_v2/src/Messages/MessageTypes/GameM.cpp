#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"

namespace GameM {
    std::string throwDice(int fd, std::string& information) {
        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // // Make Game Copy
        // Game copyGame = finder.first;

        // What dice set to throw
        std::string throwedDices = "";
        if (finder.second.name == finder.first.playerNames[0]) {
            // Check if all dices are held
            bool allHeld = true;
            for (Dice& dice : finder.first.dicesP1) {
                if (!dice.hold) {
                    allHeld = false;
                    break;
                }
            }
            if (allHeld) {
                // Set the dices to not held
                for (Dice& dice : finder.first.dicesP1) {
                    dice.hold = false;
                }
            }

            throwedDices.append(finder.second.name).append(";");
            for (Dice& dice : finder.first.dicesP1) {
                if (!dice.hold) {
                    dice.rollDice();
                    throwedDices.append(dice.id).append(",")
                    .append(std::to_string(dice.value)).append(",")
                    .append(std::to_string(dice.selected)).append(",")
                    .append(std::to_string(dice.hold)).append(";");
                }
            }

            // Prepare response and return
            std::string tag;
            tag.append(BASE_GAME).append(GAME_THROW_DICE);

            return MessageFormat::prepareResponse(throwedDices, tag);
        }

    }
    std::string selectDice(int fd, std::string& information) {

    }
    std::string confirmDice(int fd, std::string& information) {

    }
    std::string endTurn(int fd, std::string& information) {

    }

    std::pair<Game, Player> whereAndWho(int fd) {
        Player* player = nullptr;
        Game* game = nullptr;
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
        return std::make_pair(*game, *player);
    }
}
