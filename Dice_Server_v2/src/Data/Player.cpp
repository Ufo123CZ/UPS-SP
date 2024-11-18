#include "Player.h"
#include <iostream>

Player::Player(const int fd, const std::string &name, const int status, int ping) {
    this->fd = fd;
    this->name = name;
    this->status = status;
    this->ping = ping;
}
