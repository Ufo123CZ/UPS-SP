#include "Player.h"
#include <iostream>

Player::Player(const int fd, const std::string &name, const int status, const int score) {
    this->fd = fd;
    this->name = name;
    this->status = status;
    this->score = score;
}

// Destructor
// Player::~Player() {
//     std::cout << "Player " << this->name << " has been deleted." << std::endl;
// }
