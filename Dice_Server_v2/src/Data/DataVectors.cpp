#include "DataVectors.h"

// Initialize the static variables
std::vector<Player> DataVectors::players;
std::vector<Game> DataVectors::games;

// Initialize the data vectors
void DataVectors::initDataVectors() {
    players = std::vector<Player>();
    games = std::vector<Game>();
}