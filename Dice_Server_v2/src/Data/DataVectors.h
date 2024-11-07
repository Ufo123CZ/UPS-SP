#ifndef DATAVECTORS_H
#define DATAVECTORS_H

#include <vector>
#include "Player.h"
#include "Game.h"

class DataVectors {
public:
    static void initDataVectors();
    static std::vector<Player> players;
    static Game* game;
};

#endif //DATAVECTORS_H
