#ifndef DATAVECTORS_H
#define DATAVECTORS_H

#include "Player.h"
#include "Game.h"
#include <vector>

/**
 * @brief A class to store the data vectors
 */
class DataVectors {
public:
    static void initDataVectors();
    static std::vector<Player> players;
    static std::vector<Game> games;
};

#endif //DATAVECTORS_H
