#include "Game.h"

#include "Player.h"
#include "Dice.h"
#include "../Utils/ScoreCalculation.h"
#include <iostream>
#include <vector>
#include <algorithm>
#include <cstring>

std::vector<Player> gamePlayers;

Game::Game(Player player1, Player player2) {
    // Dice ids
    std::vector<std::string> dicesIds1 = {"DA1", "DA2", "DA3", "DA4", "DA5", "DA6"};
    std::vector<std::string> dicesIds2 = {"DB1", "DB2", "DB3", "DB4", "DB5", "DB6"};

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

    for (int i = 0; i < 6; i++) {
        dices[0].emplace_back(dicesIds1[i]);
        dices[1].emplace_back(dicesIds2[i]);
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

std::pair<std::string, std::string> Game::rollDices(int who) {
    // Strings for the response
    std::string throwedDices, switchPlayer;

    if (this->lastSwitch) {
        this->lastSwitch = false;
        for (Dice& dice : this->dices[who]) {
            dice.selected = false;
            dice.hold = false;
        }
    }

    // Check if all dices are held
    bool allHeld = true;
    for (Dice& dice : this->dices[who]) {
        if (!dice.hold) {
            allHeld = false;
            break;
        }
    }
    if (allHeld) {
        // Set the dices to not held
        for (Dice& dice : this->dices[who]) {
            dice.hold = false;
        }
    }

    // Test
    // this->dices[who][0].value = 1;
    // this->dices[who][1].value = 1;
    // this->dices[who][2].value = 1;
    // this->dices[who][3].value = 1;
    // this->dices[who][4].value = 1;
    // this->dices[who][5].value = 1;
    for (Dice& dice : this->dices[who]) {
        if (!dice.hold) {
            dice.rollDice();
            throwedDices.append(dice.id).append(",")
            .append(std::to_string(dice.value)).append(",")
            .append(std::to_string(dice.selected)).append(",")
            .append(std::to_string(dice.hold)).append("|");
        }
    }


    // Check the rolled dices if there is possible combination
    bool oneOrFive = false;
    bool threeOfAKind = false;
    for (Dice& dice : this->dices[who]) {
        if (!dice.hold) {
            if (dice.value == 1 || dice.value == 5) {
                oneOrFive = true;
            }
            if (std::ranges::count_if(this->dices[who], [dice](Dice& d) {
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
        this->onMove = switchPlayer;
        this->scores[who][1] = 0;
        this->scores[who][2] = 0;
    }

    return {throwedDices, switchPlayer};
}

std::string Game::selectDices(int who, std::string& information) {
    std::string response;
    // Select or un-Select the dice in the set
    for (Dice& dice : this->dices[who]) {
        if (dice.id == information) {
            dice.selectDice();
            response.append(dice.id).append(",")
            .append(std::to_string(dice.value)).append(",")
            .append(std::to_string(dice.selected)).append(",")
            .append(std::to_string(dice.hold));
            break;
        }
    }
    return response;
}

std::string Game::holdDices(int who) {
    // Hold the selected dices
    std::string holdedDices;
    for (Dice& dice : this->dices[who]) {
        if (dice.selected) {
            dice.holdDice();
            holdedDices.append(dice.id).append(",")
            .append(std::to_string(dice.value)).append(",")
            .append(std::to_string(dice.selected)).append(",")
            .append(std::to_string(dice.hold)).append("|");
        }
    }
    return holdedDices;
}

std::string Game::nextRound(int who) {
    int throwScore = calculateThrow(who);
    std::string change = holdDices(who);
    this->scores[who][1] += throwScore;
    this->scores[who][2] = 0;
    return change;
}

std::string Game::endRound(int who) {
    int throwD = calculateThrow(who);
    this->scores[who][2] = throwD;
    this->scores[who][1] += this->scores[who][2];
    this->scores[who][0] += this->scores[who][1];

    // Check if the player has 4000 points
    // TODO: Check if the player has 4000 points
    if (this->scores[who][0] >= 4000) {
        std::string res;
        res.append("winner:").append(this->playerNames[who]);
        return res;
    }

    return "";
}

std::string Game::updateDiceEnd(int who) {
    std::string change;
    for (Dice& dice : this->dices[who]) {
        if (dice.selected) {
            dice.holdDice();
            change.append(dice.id).append(",")
            .append(std::to_string(dice.value)).append(",")
            .append(std::to_string(dice.selected)).append(",")
            .append(std::to_string(dice.hold)).append("|");
        }
    }
    return change;
}

void Game::scoreRestart(int who) {
    this->scores[who][1] = 0;
    this->scores[who][2] = 0;
}

int Game::calculateThrow(int who) {
    int dicesVals[6] = {0, 0, 0, 0, 0, 0};

    for (int i = 0; i < 6; i++) {
        if (this->dices[who][i].selected && !this->dices[who][i].hold) {
            dicesVals[i] = this->dices[who][i].value;
        } else {
            dicesVals[i] = -1;
        }
    }

    int throwScore = calculateSelected(dicesVals);
    this->scores[who][2] = throwScore;

    return throwScore;
}

int Game::calculateSelected(const int diceVals[]) {
    // Each position of diceVals is 1-6 dice value or -1 if the dice is not selected
    // Check how many selected dices are there
    int selected = 0;
    for (int i = 0; i < 6; i++) {
        if (diceVals[i] != -1) {
            selected++;
        }
    }

    // Variables
    std::pair<int, int> result;

    // Score
    int score = 0;

    // Flags
    bool s1to5 = false, s2to6 = false;

    int funcScore;
    int whatKind = -1;
    // 6 selected dices
    switch (selected) {
        case 6:
            // Full straight
            funcScore = ScoreCalculator::isCompleteStraight(diceVals);
            if (funcScore == STRAIGHT) return funcScore;
            // Three to six of a kind
            result = ScoreCalculator::isFourToSixOfAKind(6, diceVals);
            if (result.first != 0) return result.first;
        case 5:
            funcScore = ScoreCalculator::isIncompleteStraight(1, 5, diceVals);
            if (funcScore == STRAIGHT1TO5) {
                s1to5 = true;
                score += funcScore;
            }
            funcScore = ScoreCalculator::isIncompleteStraight(2, 6, diceVals);
            if (funcScore == STRAIGHT2TO6) {
                s2to6 = true;
                score += funcScore;
            }
            result = ScoreCalculator::isFourToSixOfAKind(5, diceVals);
            if (result.first != 0 && !s1to5 && !s2to6) {
                score += result.first;
                whatKind = result.second;
            }
        case 4:
            if (whatKind == -1 && !s1to5 && !s2to6) {
                result = ScoreCalculator::isFourToSixOfAKind(4, diceVals);
                if (result.first != 0) {
                    score += result.first;
                    whatKind = result.second;
                }
            }
        case 3:
            if (whatKind == -1 && !s1to5 && !s2to6) {
                std::pair<std::pair<int, int>, std::pair<int, int> > result3 = ScoreCalculator::isThreeOfAKind(diceVals);
                if (result3.first.first != 0 && result3.second.first != 0) {
                    return result3.first.first + result3.second.first;
                }
                if (result3.first.first != 0) {
                    score += result3.first.first;
                    whatKind = result3.first.second;
                } else if (result3.second.first != 0) {
                    score += result3.second.first;
                    whatKind = result3.second.second;
                }
            }
        default:
            if (s1to5) {
                result = ScoreCalculator::additionalDiceWithStraight(diceVals, s1to5, s2to6, whatKind);
                if (result.second) return 0;
                score += result.first;
            } else if (s2to6) {
                result = ScoreCalculator::additionalDiceWithStraight(diceVals, s1to5, s2to6, whatKind);
                if (result.second) return 0;
                score += result.first;
            } else {
                result = ScoreCalculator::additionalDice(whatKind, diceVals);
                if (result.second) return 0;
                score += result.first;
            }
    }
    return score;
}

