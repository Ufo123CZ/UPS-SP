#include "ServerCommands.h"
#include "../Server/Server.h"

extern std::atomic<bool> runningCLI;

// Initialize the command map
std::unordered_map<std::string, std::function<void(Server &server, std::string&)>> ServerCommands::commandMap;

void ServerCommands::initCommandMap() {
    commandMap = {
        {"exit", [](Server &server, std::string&) {
            server.stop();
            runningCLI = false;
        }}
    };
}