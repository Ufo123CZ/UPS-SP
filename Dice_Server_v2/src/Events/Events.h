#ifndef EVENTS_H
#define EVENTS_H

#include "../Data/Game.h"
#include <string>

/**
 * Namespace Events
 * Collections of different events on the server side
 */
namespace Events {
    std::string createGame();
    std::string reconnectGame(Player &player);
    std::pair<int, std::string> announcePlayerLeft(const Player &player);
    std::pair<int, std::string> announcePlayerTempLeft(const Player &player);
    std::pair<int, std::string> announcePlayerReconnect(const Player &player);
    std::string endGame();
}

#endif //EVENTS_H
