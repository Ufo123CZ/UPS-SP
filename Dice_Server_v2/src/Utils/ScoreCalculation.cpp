#include "ScoreCalculation.h"

#include <cmath>
#include <vector>

namespace ScoreCalculator {
    int isCompleteStraight(const int diceVals[]) {
        bool found[6] = {false, false, false, false, false, false};

        // Check if the dices are in the range
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= 1 && diceVals[i] <= 6) {
                found[diceVals[i] - 1] = true;
            }
        }

        // Check if the dices are correct
        for (bool i : found) {
            if (!i) {
                return 0;
            }
        }

        return STRAIGHT;
    }

    int isIncompleteStraight(const int lB, const int  rB, const int diceVals[]) {
        bool found[5] = {false, false, false, false, false};

        // Changer the straight is 1 to 5 or 2 to 6
        int offset, score;
        if (lB == 1) {
            offset = 1;
            score = STRAIGHT1TO5;
        } else {
            offset = 2;
            score = STRAIGHT2TO6;
        }

        // Check if the dices are in the range
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= lB && diceVals[i] <= rB) {
                found[diceVals[i] - offset] = true;
            }
        }

        // Check if the dices are correct
        for (bool i : found) {
            if (!i) {
                return 0;
            }
        }

        return score;
    }

    std::pair<int, int> isFourToSixOfAKind(const int kindOf, const int diceVals[]) {
        int count[6] = {0, 0, 0, 0, 0, 0};

        // Count the dices Values
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= 1 && diceVals[i] <= 6) {
                count[diceVals[i] - 1]++;
            }
        }

        // Check if there are four to six of a
        for (int i = 0; i < 6; i++) {
            if (count[i] == kindOf) {
                int power = 0;
                switch (kindOf) {
                    case 6: power = 3; break;
                    case 5: power = 2; break;
                    case 4: power = 1; break;
                    default: break;
                }
                switch (i + 1) {
                    case 1: return std::make_pair(THREEOFAKIND1 * pow(2, power), 1);
                    case 2: return std::make_pair(THREEOFAKIND2 * pow(2, power), 2);
                    case 3: return std::make_pair(THREEOFAKIND3 * pow(2, power), 3);
                    case 4: return std::make_pair(THREEOFAKIND4 * pow(2, power), 4);
                    case 5: return std::make_pair(THREEOFAKIND5 * pow(2, power), 5);
                    case 6: return std::make_pair(THREEOFAKIND6 * pow(2, power), 6);
                    default: return std::make_pair(0, 0);
                }
            }
        }
        return std::make_pair(0, 0);
    }

    std::pair<std::pair<int, int>, std::pair<int, int>> isThreeOfAKind(const int diceVals[]) {
        int count[6] = {0, 0, 0, 0, 0, 0};

        // Count the dices Values
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= 1 && diceVals[i] <= 6) {
                count[diceVals[i] - 1]++;
            }
        }

        // Initialize the pairs
        std::pair firstThreeOfAKind = {0, 0};
        std::pair secondThreeOfAKind = {0, 0};

        // Check if there are two three of a kind
        for (int i = 0; i < 6; i++) {
            if (count[i] >= 3) {
                if (firstThreeOfAKind.first == 0) {
                    switch (i + 1) {
                        case 1: firstThreeOfAKind = std::make_pair(THREEOFAKIND1, 1); break;
                        case 2: firstThreeOfAKind = std::make_pair(THREEOFAKIND2, 2); break;
                        case 3: firstThreeOfAKind = std::make_pair(THREEOFAKIND3, 3); break;
                        case 4: firstThreeOfAKind = std::make_pair(THREEOFAKIND4, 4); break;
                        case 5: firstThreeOfAKind = std::make_pair(THREEOFAKIND5, 5); break;
                        case 6: firstThreeOfAKind = std::make_pair(THREEOFAKIND6, 6); break;
                        default: firstThreeOfAKind = std::make_pair(0, 0);
                    }
                } else {
                    switch (i + 1) {
                        case 1: secondThreeOfAKind = std::make_pair(THREEOFAKIND1, 1); break;
                        case 2: secondThreeOfAKind = std::make_pair(THREEOFAKIND2, 2); break;
                        case 3: secondThreeOfAKind = std::make_pair(THREEOFAKIND3, 3); break;
                        case 4: secondThreeOfAKind = std::make_pair(THREEOFAKIND4, 4); break;
                        case 5: secondThreeOfAKind = std::make_pair(THREEOFAKIND5, 5); break;
                        case 6: secondThreeOfAKind = std::make_pair(THREEOFAKIND6, 6); break;
                        default: secondThreeOfAKind = std::make_pair(0, 0);
                    }
                    break;
                }
            }
        }

        return {firstThreeOfAKind, secondThreeOfAKind};
    }

    std::pair<int, bool> additionalDiceWithStraight(const int diceVals[]) {
        int count[6] = {0, 0, 0, 0, 0, 0};
        int score = 0;
        bool incorrect = false;
        // Count the dices Values
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= 1) {
                count[diceVals[i] - 1]++;
            }
        }

        // Check if the dices are correct
        for (int i = 0; i < 6; i++) {
            if (i != 0 && i != 4 && count[i] < 1) {
                score = 0;
                incorrect = true;
                break;
            }
            if (i == 0) {
                score += (count[i] - 1) * ONE;
            }
            if (i == 4) {
                score += (count[i] - 1) * FIVE;
            }
        }
        return {score, incorrect};
    }

    std::pair<int, bool> additionalDice(const int kindOf, const int diceVals[]) {
        int count[6] = {0, 0, 0, 0, 0, 0};
        int score = 0;
        bool incorrect = false;
        // Count the dices Values
        for (int i = 0; i < 6; i++) {
            if (diceVals[i] >= 1) {
                count[diceVals[i] - 1]++;
            }
        }

        // Check if the dices are correct
        for (int i = 0; i < 6; i++) {
            if (kindOf - 1 != i && count[i] > 0) {
                if (i == 0) {
                    score += count[i] * ONE;
                } else if (i == 4) {
                    score += count[i] * FIVE;
                } else {
                    score = 0;
                    incorrect = true;
                    break;
                }
            }
        }
        return {score, incorrect};
    }
}
