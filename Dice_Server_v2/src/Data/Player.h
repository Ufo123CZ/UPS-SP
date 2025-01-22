#ifndef PLAYER_H
#define PLAYER_H

#include <string>

/**
 * @brief A class to represent a player
 */
class Player {
public:
    Player(int fd, int status);
    int fd;
    std::string name;
    int status; // -2 = nameless, -1 = disconnected, 0 = queue, 1 = playing
    int lastMess;

    // ~Player();
};



#endif //PLAYER_H
