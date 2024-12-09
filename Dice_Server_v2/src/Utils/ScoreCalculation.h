#ifndef SCORECALCULATION_H
#define SCORECALCULATION_H

#include <vector>

// End Score
#define ENDSCORE 4000

// Scores
#define STRAIGHT 1500
#define STRAIGHT1TO5 500
#define STRAIGHT2TO6 750
#define THREEOFAKIND1 1000
#define THREEOFAKIND2 200
#define THREEOFAKIND3 300
#define THREEOFAKIND4 400
#define THREEOFAKIND5 500
#define THREEOFAKIND6 600
#define ONE 100
#define FIVE 50

namespace ScoreCalculator {
    int isCompleteStraight(const int diceVals[]);
    int isIncompleteStraight(int lB, int  rB, const int diceVals[]);
    std::pair<int, int> isFourToSixOfAKind(int kindOf, const int diceVals[]);
    std::pair<std::pair<int, int>, std::pair<int, int>> isThreeOfAKind(const int diceVals[]);
    std::pair<int, bool> additionalDiceWithStraight(const int diceVals[], bool straight1to5, bool straight2to6, int kindOf);
    std::pair<int, bool> additionalDice(int kindOf, const int diceVals[]);
}
#endif //SCORECALCULATION_H
