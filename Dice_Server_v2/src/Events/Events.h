#ifndef EVENTS_H
#define EVENTS_H

#include <string>
#include "../Data/Game.h"

namespace Events {
    std::string createGame();
    std::string reconnectGame(Player &player);
    std::pair<int, std::string> announcePlayerLeft(const Player &player);
    std::pair<int, std::string> announcePlayerTempLeft(const Player &player);
    std::pair<int, std::string> announcePlayerReconnect(const Player &player);
    std::string endGame();
}

#endif //EVENTS_H
