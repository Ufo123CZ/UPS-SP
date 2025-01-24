#include "Dice.h"
#include <cstdlib>
#include <ctime>

/**
 * @brief Construct a new Dice:: Dice object
 *
 * @param id The id of the dice
 */
Dice::Dice(const std::string &id) {
    this->id = id;
    this->value = 1;
    this->selected = false;
    this->hold = false;
}

/**
 * @brief Roll the dice
 */
void Dice::rollDice() {
    this->value = rand() % 6 + 1;
}

/**
 * @brief Select the dice
 */
void Dice::selectDice() {
    this->selected = !this->selected;
}

/**
 * @brief When dice is selected, hold the dice
 */
void Dice::holdDice() {
    this->hold = !this->hold;
    this->selected = !this->selected;
}