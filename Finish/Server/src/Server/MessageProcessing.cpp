#include "Server.h"
#include "../Messages/MessageFormat.h"
#include "../Messages/TAGS.h"
#include "../Utils/Consts.h"
#include <iostream>
#include <cstring>

/**
 * Namespace for message processing
 * Collects functions for reading and processing messages
 */
namespace MessageProcessing {
    /**
     * Function for reading message from socket
     * @param fd - file descriptor
     * @return - received message
     */
    std::string readMessage(const int fd) {
        // Variables
        char buffer[INIT_BUFFER_SIZE] = {};
        int messageLength = 0;
        std::string message;
        bool found = false;

        // Receive the first 8 bytes
        memset(buffer, 0, sizeof(buffer));
        recv(fd, &buffer, 8, 0);


        // Find the message length
        int required = 0;
        for (const char i : buffer) {
            // Check if there is 2 times ';'
            if (i == ';') {
                required++;
                if (required == 2) {
                    std::string temp(buffer);
                    // Get Message prefix
                    std::string prefix = temp.substr(0, temp.find(';'));
                    if (prefix != BASE_IN) {
                        std::cerr << "Invalid message prefix" << std::endl;
                        return "";
                    }

                    // Get message length
                    std::string messLenStr = temp.substr(temp.find(';') + 1, temp.find_last_of(';') - temp.find(';') - 1);
                    messageLength = std::stoi(messLenStr);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            std::cerr << "Failed to get message length" << std::endl;
            return "";
        }

        // Receive the rest of the message
        // Clear the buffer
        memset(buffer, 0, sizeof(buffer));
        buffer[messageLength] = {};
        recv(fd, &buffer, messageLength, 0);
        message.append(buffer);

        // Remove trailing newline and carriage return characters
        if (!message.empty() && (message.back() == '\n' || message.back() == '\r')) {
            message.pop_back();
        }
        if (!message.empty() && (message.back() == '\n' || message.back() == '\r')) {
            message.pop_back();
        }

        if (message.empty()) return "";

        return message;
    }

    /**
     * @brief Function for processing message
     * @param fd - file descriptor
     * @param message - received message
     * @return - response message
     */
    std::string processMessage(int fd, const std::string& message) {
        // split message into parts (command, information)
        std::string command = message.substr(0, message.find(';'));
        std::string information = message.substr(message.find(';') + 1);

        // based on command decide what to do
        std::string response;
        auto mfm = MessageFormat::messFormatMap.find(command);
        if (mfm != MessageFormat::messFormatMap.end()) {
            response = mfm->second(fd, information);
        } else {
            std::cerr << "Invalid message from fd: " << fd << std::endl
                    << "Terminate connection" << std::endl;
            // Create terminating message
            response = MessageFormat::createViolationMess();
        }

        // Check if response is empty
        if (response.empty()) {
            return "";
        }

        if (!response.find(PONG, 0) || !response.find(PING, 0)) std::cout << "Response: " << response;

        return response;
    }
}
