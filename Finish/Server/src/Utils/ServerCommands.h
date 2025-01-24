#ifndef SERVERCOMMANDS_H
#define SERVERCOMMANDS_H

#include "../Server/Server.h"
#include <unordered_map>
#include <functional>
#include <string>

/**
 * Class for the server commands
 */
class ServerCommands {
public:
    static void initCommandMap();
    static std::unordered_map<std::string, std::function<void(Server &server, std::string&)>> commandMap;
    static void printGames();
    static void printPlayers();
};

#endif //SERVERCOMMANDS_H
