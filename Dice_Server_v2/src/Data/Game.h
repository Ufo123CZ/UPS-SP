#ifndef GAME_H
#define GAME_H

#include "Player.h"
#include <vector>

class Game {
public:
    Game();
    ~Game();

    std::vector<Player> gamePlayers;
};


#endif //GAME_H
