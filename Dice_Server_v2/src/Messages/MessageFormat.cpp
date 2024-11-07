#include "MessageFormat.h"
#include "../Messages/TAGS.h"
#include "../Utils/Consts.h"

// Initialize the message format map
std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> MessageFormat::messFormatMap;

void MessageFormat::initMessageFormat() {
    // TAGS
    std::string tagLogin;
    tagLogin.append(BASE_IN).append(LOGIN);
    std::string tagLogout;
    tagLogout.append(BASE_IN).append(LOGOUT);
    std::string tagQueueStatus;
    tagQueueStatus.append(BASE_IN).append(QUEUE).append(QUEUE_STATUS);

    messFormatMap = {
        {tagLogin, [](int fd, std::string& information) -> std::string {
            return Login::login(fd, information);
        }},
        {tagLogout, [](int fd, std::string& information) -> std::string {
            return Logout::logout(fd, information);
        }},
        {tagQueueStatus, [](int fd, std::string&) -> std::string {
            return Queue::status(fd);
        }}
    };
}

std::string MessageFormat::prepareResponse(const std::string& information, const std::string& tag) {
    // Response is based on the information
    int iLen = information.length();
    int tLen = tag.length();
    int itLen = iLen + tLen;
    // How long is iLen string
    int iCLen = std::to_string(itLen).length();

    int totalLen = iCLen + itLen + 3;

    if (totalLen > 3) {
        std::string response;
        // Build the response
        response.append(std::to_string(totalLen));
        response.append(";");
        response.append(tag);
        response.append(";");
        response.append(information);

        response.append("\n");

        return response;
    }
    return "\n";
}

std::string MessageFormat::createFailMessage() {
    std::string response;
    std::cerr << "Failed to process message" << std::endl
            << "Sending message: " << FAILURE << std::endl;
    // Prepare the failure response
    std::string fail = FAILURE;
    std::string tag = BASE_OUT;
    int fLen = fail.size();
    int tLen = tag.size();
    int ftLen = fLen + tLen;
    int mCLen = std::to_string(ftLen).size();

    int fLenTotal = mCLen + ftLen + 3;

    response.append(std::to_string(fLenTotal));
    response.append(";");
    response.append(BASE_OUT).append(FAIL);
    response.append(";");
    response.append(FAILURE);
    response.append("\n");

    return response;
}
