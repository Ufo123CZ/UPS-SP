#ifndef MESSAGEFORMAT_H
#define MESSAGEFORMAT_H

#include "../Data/Game.h"
#include "../Data/Player.h"
#include <unordered_map>
#include <functional>
#include <string>
#include <iostream>

/**
 * Class MessageFormat
 */
class MessageFormat {
public:
    static void initMessageFormatMap();
    static std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> messFormatMap;
    static std::string prepareResponse(const std::string& information, const std::string& tag);
    static std::string createViolationMess();
    static std::string createPingMessage();
    static std::string aliveCheck();
};

/**
 * Namespace Login
 * Collection of functions that handle the login of a player
 */
namespace Login {
    std::string login(int fd, std::string& information);
}

/**
 * Namespace Logout
 * Collection of functions that handle the logout of a player
 */
namespace Logout {
    std::string logout(int fd);
}

/**
 * Namespace for the game messages
 * Collection of functions that are used to handle the game messages
 */
namespace GameM {
    std::string throwDice(int fd);
    std::string selectDice(int fd, std::string& information);
    std::string nextTurn(int fd);
    std::string endTurn(int fd);

    std::string gameMessGen(const std::string& name, const std::string& score, const std::string& dice, const std::string& throwB, const std::string& switchE, const std::string& unique);
    void updateGame(Game& game);
    std::pair<Game, Player> whereAndWho(int fd);
    bool checkValidInput(bool throwCheck, std::string diceID, bool turnS, int fd);
}

/**
 * Namespace RejoinQ
 * Collection of functions that handle the rejoining of the queue
 */
namespace RejoinQ {
    std::string rejoinQueue(int fd);
}

/**
 * Namespace Reconnect
 * Collection of functions that handle the reconnection of a player
 */
namespace Reconnect {
    std::string reconnect(int fd);
}

#endif //MESSAGEFORMAT_H
