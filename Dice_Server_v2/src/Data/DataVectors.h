#ifndef DATAVECTORS_H
#define DATAVECTORS_H

#include <vector>
#include "Player.h"

class DataVectors {
public:
    static void initDataVectors();
    static std::vector<Player> players;
};

#endif //DATAVECTORS_H
