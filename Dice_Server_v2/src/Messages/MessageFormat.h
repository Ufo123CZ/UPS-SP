#ifndef MESSAGEFORMAT_H
#define MESSAGEFORMAT_H

#include <unordered_map>
#include <functional>
#include <string>
#include <iostream>


class MessageFormat {
public:
    static void initMessageFormat();
    static std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> messFormatMap;
    static std::string prepareResponse(const std::string& information, const std::string& tag);
    static std::string createFailMessage();
};

namespace Login {
    std::string login(int fd, std::string& information);
}
namespace Logout {
    std::string logout(int fd, std::string& information);
}

#endif //MESSAGEFORMAT_H
