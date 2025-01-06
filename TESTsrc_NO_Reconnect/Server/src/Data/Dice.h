#ifndef DICE_H
#define DICE_H

#include <string>


class Dice {
public:
    Dice(const std::string &id);
    // ~Dice();

    void rollDice();
    void selectDice();
    void holdDice();

    std::string id;
    int value;
    bool selected;
    bool hold;
};

#endif //DICE_H
