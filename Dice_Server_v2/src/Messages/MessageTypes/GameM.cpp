#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"

namespace GameM {
    std::string throwDice(int fd) {
        // Get the game and player
        std::pair<Game, Player> finder = whereAndWho(fd);

        // Strings for the response
        const std::string name = finder.second.name;
        std::string throwedDices, switchPlayer;

        // Which player is throwing the dices
        std::vector<Dice> selectedDices;
        if (name == finder.first.playerNames[0]) {
            selectedDices = finder.first.dicesP1;
            switchPlayer = finder.first.playerNames[1];
        } else {
            selectedDices = finder.first.dicesP2;
            switchPlayer = finder.first.playerNames[0];
        }

        // Check if all dices are held
        bool allHeld = true;
        for (Dice& dice : selectedDices) {
            if (!dice.hold) {
                allHeld = false;
                break;
            }
        }
        if (allHeld) {
            // Set the dices to not held
            for (Dice& dice : selectedDices) {
                dice.hold = false;
            }
        }

        for (Dice& dice : selectedDices) {
            if (!dice.hold) {
                dice.rollDice();
                std::cout << "Dice: " << dice.id << " Value: " << dice.value << std::endl;
                throwedDices.append(dice.id).append(",")
                .append(std::to_string(dice.value)).append(",")
                .append(std::to_string(dice.selected)).append(",")
                .append(std::to_string(dice.hold)).append("|");
            }
        }

        // Check the rolled dices if there is possible combination
        // There must be at least one 1 or 5
        // if there isn´t any 1 or 5, there must be at least 3 of a kind
        // if there isn´t any 1 or 5 or 3 of a kind, the player ends the turn
        bool oneOrFive = false;
        bool threeOfAKind = false;
        for (Dice& dice : selectedDices) {
            if (!dice.hold) {
                if (dice.value == 1 || dice.value == 5) {
                    oneOrFive = true;
                }
                if (std::count_if(selectedDices.begin(), selectedDices.end(), [dice](Dice& d) {
                    return d.value == dice.value && !d.hold;
                }) >= 3) {
                    threeOfAKind = true;
                }
            }
        }
        if (oneOrFive || threeOfAKind) {
            switchPlayer = " ";
        }

        // Update the dice set in the game
        if (name == finder.first.playerNames[0]) {
            finder.first.dicesP1 = selectedDices;
        } else {
            finder.first.dicesP2 = selectedDices;
        }

        // Update the game in the vector
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == finder.first.playerNames[0]) {
                g = finder.first;
                break;
            }
        }

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_THROW_DICE);
        std::string respInfo = "";
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
        int score = 0;

        // Which player is throwing the dices
        std::vector<Dice> selectedDices;
        if (name == finder.first.playerNames[0]) {
            selectedDices = finder.first.dicesP1;
        } else {
            selectedDices = finder.first.dicesP2;
        }

        // Select or un-Select the dice in the set
        for (Dice& dice : selectedDices) {
            if (dice.id == information) {
                dice.selected = !dice.selected;
            }
        }

        // // Count the score from currently selected dices
        // for (Dice& dice : selectedDices) {
        //     if (dice.selected) {
        //         if (dice.value == 1) {
        //             score += 100;
        //         } else if (dice.value == 5) {
        //             score += 50;
        //         } else {
        //             score = 0;
        //         }
        //     }
        // }

        // Count the occurrences of each dice value
        std::unordered_map<int, int> diceCount;
        for (Dice& dice : selectedDices) {
            if (dice.selected) {
                diceCount[dice.value]++;
            }
        }

        // Check for straight combinations
        bool isStraight1To5 = diceCount[1] > 0 && diceCount[2] > 0 && diceCount[3] > 0 && diceCount[4] > 0 && diceCount[5] > 0;
        bool isStraight2To6 = diceCount[2] > 0 && diceCount[3] > 0 && diceCount[4] > 0 && diceCount[5] > 0 && diceCount[6] > 0;

        if (isStraight1To5 && isStraight2To6) {
            score += 1500;
        } else if (isStraight1To5) {
            score += 500;
        } else if (isStraight2To6) {
            score += 750;
        } else {
            for (auto& pair : diceCount) {
                int value = pair.first;
                int count = pair.second;
                if (value == 1) {
                    score += count * 100;
                } else if (value == 5) {
                    score += count * 50;
                } else if (count >= 3) {
                    score += value * 100;
                }
            }
        }


    }
    std::string confirmDice(int fd, std::string& information) {

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
        for (Game& g : DataVectors::games) {
            if (g.playerNames[0] == finder.first.playerNames[0]) {
                g = finder.first;
                break;
            }
        }

        // Prepare response and return
        std::string tag;
        tag.append(BASE_GAME).append(GAME_END_TURN);
        std::string respInfo = "";
        respInfo.append(name).append(";"); // Player Name
        respInfo.append(" ").append(";"); // Score - null here
        respInfo.append(" ").append(";"); // Dices
        respInfo.append(secondPlayer).append(";"); // Switch - null here

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
}
