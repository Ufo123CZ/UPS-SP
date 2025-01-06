#include "DataVectors.h"

std::vector<Player> DataVectors::players;
std::vector<Game> DataVectors::games;

void DataVectors::initDataVectors() {
    players = std::vector<Player>();
    games = std::vector<Game>();
}