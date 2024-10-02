#ifndef DICE_H
#define DICE_H

#include <stdbool.h>

typedef struct Dice {
    int value;
    bool hold;
    bool selected;
} Dice;

#endif //DICE_H
