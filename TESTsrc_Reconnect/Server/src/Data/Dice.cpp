#include "Dice.h"
#include <cstdlib>
#include <ctime>

Dice::Dice(const std::string &id) {
    this->id = id;
    this->value = 1;
    this->selected = false;
    this->hold = false;
}

void Dice::rollDice() {
    this->value = rand() % 6 + 1;
}

void Dice::selectDice() {
    this->selected = !this->selected;
}

void Dice::holdDice() {
    this->hold = !this->hold;
    this->selected = !this->selected;
}