#ifndef STATE_H
#define STATE_H

#include "dice.h"
#include "player.h"

typedef struct State {
    int id;
    Dice dices[6];
    Player *player;
    int total_score;
    int sub_total;
    int round_score;
} State;
#endif //STATE_H
