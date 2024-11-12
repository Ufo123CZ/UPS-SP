#include "MessageFormat.h"
#include "../Messages/TAGS.h"

#include "../Utils/Consts.h"

// Initialize the message format map
std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> MessageFormat::messFormatMap;

void MessageFormat::initMessageFormatMap() {
    // TAGS
    std::string tagLogin;
    tagLogin.append(BASE_LOGIN).append(LOGIN);
    std::string tagLogout;
    tagLogout.append(BASE_LOGIN).append(LOGOUT);
    std::string tagPing;
    tagPing.append(PONG);

    messFormatMap = {
        {tagLogin, [](int fd, std::string& information) -> std::string {
            return Login::login(fd, information);
        }},
        {tagLogout, [](int fd, std::string& information) -> std::string {
            return Logout::logout(fd, information);
        }},
        {tagPing, [](int, std::string&) -> std::string {
            return createPingMessage();
        }}
    };
}

std::string MessageFormat::prepareResponse(const std::string& information, const std::string& tag) {
    // Response is based on the information
    std::string responseHeader, response;
    // Make Header
    int messLen = information.length() + tag.length() + 3;
    std::string messLenStr = std::to_string(messLen);
    for (int i = 0; messLenStr.length() < 4; i++) {
        messLenStr.insert(0, "0");
    }
    responseHeader.append(BASE_OUT).append(";").append(messLenStr).append(";");
    response.append(responseHeader).append(tag).append(";").append(information).append("\n");

    return response;
}

std::string MessageFormat::createFailMessage() {
    // Response is based on the information
    std::string responseHeader, response;
    std::string tag = FAIL;
    // Make Header
    int messLen = tag.length() + 2;
    std::string messLenStr = std::to_string(messLen);
    for (int i = 0; messLenStr.length() < 4; i++) {
        messLenStr.insert(0, "0");
    }
    responseHeader.append(BASE_OUT).append(";").append(messLenStr).append(";");
    response.append(responseHeader).append(tag).append(";").append("\n");

    return response;
}

std::string MessageFormat::createPingMessage() {
    // Response is based on the information
    std::string response;
    response.append("ds;0007;").append(PING).append(";\n");

    return response;

    // std::string responseHeader, response;
    // int messLen = 0;
    // if (information.empty()) {
    //     messLen = tag.length() + 2;
    // } else {
    //     messLen = information.length() + tag.length() + 3;
    // }
    //
    // // Make Header
    // std::string messLenStr = std::to_string(messLen);
    // for (int i = 0; messLenStr.length() < 4; i++) {
    //     messLenStr.insert(0, "0");
    // }
    // responseHeader.append(BASE_OUT).append(";").append(messLenStr).append(";");
    // response.append(responseHeader).append(tag).append(";");
    // if (information.empty()) {
    //     response.append("\n");
    // } else {
    //     response.append(information).append("\n");
    // }

}
