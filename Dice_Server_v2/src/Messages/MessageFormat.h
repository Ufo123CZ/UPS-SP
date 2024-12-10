#ifndef MESSAGEFORMAT_H
#define MESSAGEFORMAT_H

#include <unordered_map>
#include <functional>
#include <string>
#include <iostream>
#include "../Data/Game.h"
#include "../Data/Player.h"


class MessageFormat {
public:
    static void initMessageFormatMap();
    static std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> messFormatMap;
    static std::string prepareResponse(const std::string& information, const std::string& tag);
    static std::string createFailMessage();
    static std::string createPingMessage();
};

namespace Login {
    std::string login(int fd, std::string& information);
}
namespace Logout {
    std::string logout(int fd);
}
namespace GameM {
    std::string throwDice(int fd);
    std::string selectDice(int fd, std::string& information);
    std::string nextTurn(int fd);
    std::string endTurn(int fd);

    std::string gameMessGen(const std::string& name, const std::string& score, const std::string& dice, const std::string& switchE, const std::string& unique);
    void updateGame(Game& game);
    std::pair<Game, Player> whereAndWho(int fd);
}
namespace RejoinQ {
    std::string rejoinQueue(int fd);
}

#endif //MESSAGEFORMAT_H
