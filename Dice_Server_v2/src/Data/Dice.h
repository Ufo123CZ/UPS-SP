#ifndef DICE_H
#define DICE_H

#include <string>

class Dice {
public:
    Dice(const std::string &id);
    // ~Dice();

    int rollDice();
    int getDiceValue();

    std::string id;
    int value;
    bool selected;
    bool hold;
};

#endif //DICE_H
