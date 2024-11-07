#include "DataVectors.h"

std::vector<Player> DataVectors::players;
Game* DataVectors::game;

void DataVectors::initDataVectors() {
    players = std::vector<Player>();
    game = nullptr;
}