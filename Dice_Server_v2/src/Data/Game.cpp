#include "Game.h"

#include "Player.h"
#include "Dice.h"
#include <iostream>
#include <vector>
#include <algorithm>
#include <cstring>

std::vector<Player> gamePlayers;

Game::Game(Player player1, Player player2) {
    // Dice ids
    std::vector<std::string> dicesIds = {"D1", "D2", "D3", "D4", "D5", "D6"};

    // Set players
    gamePlayers.push_back(player1);
    gamePlayers.push_back(player2);

    // Set player names
    playerNames.push_back(player1.name);
    playerNames.push_back(player2.name);

    // Set onMove
    onMove = player1.name;

    // Set dices
    std::vector<Dice> dicesP1, dicesP2;
    dices.push_back(dicesP1);
    dices.push_back(dicesP2);

    for (auto & dicesId : dicesIds) {
        dices[0].emplace_back(dicesId);
        dices[1].emplace_back(dicesId);
    }

    // Set scores
    std::vector<int> scoresP1, scoresP2;

    scores.push_back(scoresP1);
    scores.push_back(scoresP2);

    for (int i = 0; i < 3; i++) {
        scores[0].push_back(0);
        scores[1].push_back(0);
    }

    std::cout << "Game created." << std::endl;
}

std::vector<std::string> Game::rollDices(int who) {
    // Strings for the response
    std::string throwedDices, switchPlayer;

    // Check if all dices are held
    bool allHeld = true;
    for (Dice& dice : dices[who]) {
        if (!dice.hold) {
            allHeld = false;
            break;
        }
    }
    if (allHeld) {
        // Set the dices to not held
        for (Dice& dice : dices[who]) {
            dice.hold = false;
        }
    }

    for (Dice& dice : dices[who]) {
        if (!dice.hold) {
            dice.rollDice();
            // std::cout << "Dice: " << dice.id << " Value: " << dice.value << std::endl;
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
    for (Dice& dice : dices[who]) {
        if (!dice.hold) {
            if (dice.value == 1 || dice.value == 5) {
                oneOrFive = true;
            }
            if (std::ranges::count_if(dices[who], [dice](Dice& d) {
                return d.value == dice.value && !d.hold;
            }) >= 3) {
                threeOfAKind = true;
            }
        }
    }
    if (oneOrFive || threeOfAKind) {
        switchPlayer = " ";
    } else {
        switchPlayer = playerNames[who == 0 ? 1 : 0];
        this -> onMove = switchPlayer;
    }

    return {throwedDices, switchPlayer};
}

void Game::selectDice(int who, std::string& information) {
    // Select or un-Select the dice in the set
    for (Dice& dice : dices[who]) {
        if (dice.id == information) {
            dice.selected = !dice.selected;
        }
    }
}

int Game::calculateThrow(int who) {
    int dicesVals[6] = {0, 0, 0, 0, 0, 0};

    for (int i = 0; i < 6; i++) {
        if (dices[who][i].selected && !dices[who][i].hold) {
            dicesVals[i] = dices[who][i].value;
        } else {
            dicesVals[i] = -1;
        }
    }

    int throwScore = calculateSelected(dicesVals);
    scores[who][2] = throwScore;

    return throwScore;
}
int Game::calculateRound(int who) {
    int throwD = calculateThrow(who);
    scores[who][1] += throwD;
    scores[who][2] = 0;
    return 0;
}

std::string Game::endRound(int who) {
    int throwD = calculateThrow(who);
    scores[who][1] += throwD;
    scores[who][0] += scores[who][1];
    scores[who][1] = 0;
    scores[who][2] = 0;

    // Check if the player has 4000 points
    // TODO: Check if the player has 4000 points

    return "";
}

int Game::calculateSelected(int diceVals[]) {
    // Variables
    bool straight1To5 = false;
    bool straight2To6 = false;
    int score = 0;

    // Each possition of diceVals is 1-6 dice value or -1 if the dice is not selected
    // Straight combinations
    // 1, 2, 3, 4, 5, 6 - Straight 1 to 6 - 1500
    // 2, 3, 4, 5, 6  - Straight 2 to 6 - 750
    // 1, 2, 3, 4, 5 - Straight 1 to 5 - 500
    int selected = 0;
    bool vals[6] = {false, false, false, false, false, false};
    for (int i = 0; i < 6; i++) {
        if (diceVals[i] == -1) {
            if (selected > 1) break;
            selected++;
            continue;
        }
        for (int j = 1; j <= 6; j++) {
            if (diceVals[i] == j) {
                vals[j - 1] = true;
            }
        }
    }
    // Check for straight combinations
    if (vals[0] && vals[1] && vals[2] && vals[3] && vals[4] && vals[5]) {
        return 1500;
    }

    if (vals[1] && vals[2] && vals[3] && vals[4] && vals[5]) {
        score += 750;
        straight2To6 = true;
    } else if (vals[0] && vals[1] && vals[2] && vals[3] && vals[4]) {
        score += 500;
        straight1To5 = true;
    }

    // Check three to six of a kind
    // 1 - 8000, 2 - 1600, 3 - 2400, 4 - 3200, 5 - 4000, 6 - 4800
    int numbers[6] = {1, 2, 3, 4, 5, 6};
    int scoreAdd[6] = {1000, 200, 300, 400, 500, 600};
    bool kindOf1 = false;
    bool kindOf5 = false;
    int kind = 0;
    if (!straight1To5 && !straight2To6) {
        memset(vals, false, 6);
        for (int i = 0; i < 6; i++) {
            int count = 0;
            for (int j = 0; j < 6; j++) {
                if (diceVals[j] == numbers[i]) {
                    count++;
                }
            }
            // Six
            if (count == 6) {
                score += scoreAdd[i] * 2 * 2 * 2;
                return score;
            }
            // Five
            if (count == 5) {
                score += scoreAdd[i] * 2 * 2;
                kind = numbers[i];
                // Used dices
                for (int j = 0; j < 6; j++) {
                    if (diceVals[j] == numbers[i]) {
                        vals[j] = true;
                    }
                }
                if (i == 0) {
                    kindOf1 = true;
                } else if (i == 4) {
                    kindOf5 = true;
                }
                break;
            }
            // Four
            if (count == 4) {
                score += scoreAdd[i] * 2;
                kind = numbers[i];
                // Used dices
                for (int j = 0; j < 6; j++) {
                    if (diceVals[j] == numbers[i]) {
                        vals[j] = true;
                    }
                }
                if (i == 0) {
                    kindOf1 = true;
                } else if (i == 4) {
                    kindOf5 = true;
                }
                break;
            }
            // Three
            if (count == 3) {
                score += scoreAdd[i];
                // Used dices
                for (int j = 0; j < 6; j++) {
                    if (diceVals[j] == numbers[i]) {
                        vals[j] = true;
                    }
                }
                if (kind == 0) {
                    kind = numbers[i];
                } else {
                    return score;
                }
                if (i == 0) {
                    kindOf1 = true;
                } else if (i == 4) {
                    kindOf5 = true;
                }
            }
        }
    }

    // Check all unused dices
    // if there is 1 or 5, add the score
    // else score = 0
    for (int i = 0; i < 6; i++) {
        if (diceVals[i] == -1 && vals[i]) {
            continue;
        }
        if (!kindOf1 && diceVals[i] == 1) {
            score += 100;
        } else if (!kindOf5 && diceVals[i] == 5) {
            score += 50;
        } else {
            score = 0;
            break;
        }
    }

    return 0;
}

