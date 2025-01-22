#include "Player.h"

/**
 * @brief Construct a new Player:: Player object
 *
 * @param fd The file descriptor of the player
 * @param status The status of the player
 */
Player::Player(const int fd, const int status) {
    this->fd = fd;
    this->status = status;
}
