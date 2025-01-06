#include "Server/Server.h"
#include "Utils/ServerCommands.h"
#include "Utils/Consts.h"
#include <iostream>
#include <thread>
#include <sstream>

std::atomic<bool> runningCLI(true);

void commandLineInterface(Server &server) {
    ServerCommands::initCommandMap();
    std::string command;
    // std::cout << "Enter a command:" << std::endl;
    while (runningCLI) {
        std::string input;
        std::string command, arg1;
        getline(std::cin, input);

        // Parse the input
        std::istringstream iss(input);
        iss >> command >> arg1;

        // Empty command
        if (command.empty()) {
            continue;
        }

        // Check if the command is valid
        auto cmd = ServerCommands::commandMap.find(command);
        if (cmd != ServerCommands::commandMap.end()) {
            cmd->second(server, arg1);
        } else {
            std::cout << "Invalid command" << std::endl
                      << "Type 'help' for a list of commands" << std::endl;
        }
        // Clear the variables
        command.clear();
        arg1.clear();
    }
}

int main() {
    Server server;
    srand(time(0));

    std::string ip;
    int port;

    std::cout << "Enter IP address: ";
    std::cin >> ip;
    std::cout << "Enter port: ";
    std::cin >> port;

    bool result = server.init(ip, port);
    if (result == FAILURE_INIT) {
        std::cerr << "Server initialization failed" << std::endl;
        return 1;
    }
    std::cout << "Server initialized" << std::endl
                << "Listening on port " << port << std::endl;

    std::thread cliThread(commandLineInterface, std::ref(server));

    server.start();

    // Stop the server
    server.stop();

    // Join the CLI thread
    runningCLI = false;
    cliThread.join();

    return 0;
}