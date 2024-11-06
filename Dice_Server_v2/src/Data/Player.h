#ifndef PLAYER_H
#define PLAYER_H
#include <string>


class Player {
public:
    Player(int fd, const std::string &name, int status, int score);
    int fd;
    std::string name;
    int status; // -1 = disconnected, 0 = queue, 1 = playing
    int score;

    ~Player();
};



#endif //PLAYER_H
