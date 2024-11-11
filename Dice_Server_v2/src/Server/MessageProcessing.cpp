#include "Server.h"
#include "../Messages/MessageFormat.h"
#include "../Messages/TAGS.h"
#include "../Utils/Consts.h"
#include <iostream>
#include <cstring>

namespace MessageProcessing {
    std::string readMessage(const socket_t fd) {
        // Variables
        char buffer[INIT_BUFFER_SIZE] = {};
        int messageLength = 0;
        std::string message;
        bool found = false;
        // Receive the first 8 bytes
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

        // remove /n from the end
        if (message[message.length() - 1] == '\n') {
            message = message.substr(0, message.length() - 1);
        }
        // remove /r from the end
        if (message[message.length() - 1] == '\r') {
            message = message.substr(0, message.length() - 1);
        }

        if (message.empty()) return "";

        return message;
    }

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
            std::cerr << "Invalid message" << std::endl;
        }

        // Check if response is empty
        if (response.empty()) {
            return "";
        }

        return response;
    }
}