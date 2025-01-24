#ifndef DICE_H
#define DICE_H

#include <string>

/**
 * @brief A class to represent a dice
 */
class Dice {
public:
    Dice(const std::string &id);

    void rollDice();
    void selectDice();
    void holdDice();

    std::string id;
    int value;
    bool selected;
    bool hold;
};

#endif //DICE_H
