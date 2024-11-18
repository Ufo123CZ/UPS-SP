#include "Dice.h"

Dice::Dice(const std::string &id) {
    this->id = id;
    this->value = 1;
    this->selected = false;
    this->hold = false;
}