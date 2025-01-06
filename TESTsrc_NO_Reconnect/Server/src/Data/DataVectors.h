#ifndef DATAVECTORS_H
#define DATAVECTORS_H

#include <vector>
#include "Player.h"
#include "Game.h"

class DataVectors {
public:
    static void initDataVectors();
    static std::vector<Player> players;
    static std::vector<Game> games;
};

#endif //DATAVECTORS_H
