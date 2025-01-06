#ifndef PLAYER_H
#define PLAYER_H
#include <string>


class Player {
public:
    Player(int fd, int status, int ping);
    int fd;
    std::string name;
    int status; // -2 = nameless, -1 = disconnected, 0 = queue, 1 = playing
    int ping;

    // ~Player();
};



#endif //PLAYER_H
