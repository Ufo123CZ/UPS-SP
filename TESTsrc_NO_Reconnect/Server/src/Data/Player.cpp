#include "Player.h"
#include <iostream>

Player::Player(const int fd, const int status, int ping) {
    this->fd = fd;
    this->status = status; // 0 - queue, 1 - in game
    this->ping = ping;
}
