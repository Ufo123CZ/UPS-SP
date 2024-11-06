#include "Server.h"
#include "../Messages/MessageFormat.h"
#include <iostream>
#include <cstring>

namespace MessageProcessing {
    std::string readMessage(const socket_t fd, int a2read) {
        // Variables
        char buffer[12] = {};
        int messageLength = 0;
        std::string messFirst;
        std::string message;
        bool found = false;

        // Receive the first 12 bytes
        recv(fd, &buffer, 12, 0);

        // Find the message length
        for (const char i : buffer) {
            if (i == ';') {
                std::string sLen = buffer;
                messageLength = std::stoi(sLen.substr(0, sLen.find(';')));
                messFirst = sLen.substr(sLen.find(';') + 1);
                found = true;
                break;
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
        // remove from messFirst \022, \023, \024
        char spec[] = {'\022', '\023', '\024'};
        for (char i : spec) {
            if (messFirst.find(i) != std::string::npos) {
                messFirst = messFirst.substr(0, messFirst.find(i));
                a2read--;
                messageLength--;
            }
        }
        message.append(messFirst).append(buffer);

        // Process the message and Return
        if (messageLength == a2read - 1) {
            return message;
        }
        return "";
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