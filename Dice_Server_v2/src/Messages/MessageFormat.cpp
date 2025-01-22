#include "MessageFormat.h"
#include "../Messages/TAGS.h"

// Initialize the message format map
std::unordered_map<std::string, std::function<std::string(int fd, std::string&)>> MessageFormat::messFormatMap;

/**
 * @brief Initialize the message format map
 * This will determine the function to call based on the tag
 */
void MessageFormat::initMessageFormatMap() {
    // TAGS
    // LOGIN LOGOUT
    std::string tagNameset;
    tagNameset.append(BASE_LOGIN).append(NAMESET);
    std::string tagLogout;
    tagLogout.append(BASE_LOGIN).append(LOGOUT);
    // PING PONG
    std::string tagPing;
    tagPing.append(PONG);
    // RECONNECT TO QUEUE
    std::string tagQueueRejoin;
    tagQueueRejoin.append(BASE_QUEUE).append(QUEUE_REJOIN);
    // GAME
    std::string tagGameThrow;
    tagGameThrow.append(BASE_GAME).append(GAME_THROW_DICE);
    std::string tagGameSelect;
    tagGameSelect.append(BASE_GAME).append(GAME_SELECT_DICE);
    std::string tagGameConfirm;
    tagGameConfirm.append(BASE_GAME).append(GAME_NEXT_TURN);
    std::string tagGameEndTurn;
    tagGameEndTurn.append(BASE_GAME).append(GAME_END_TURN);
    // RECONNECT
    std::string tagReconnect;
    tagReconnect.append(BASE_CONNECTION).append(CONNECTION_RECONNECT);


    messFormatMap = {
        {tagNameset, [](int fd, std::string& information) -> std::string {
            return Login::login(fd, information);
        }},
        {tagLogout, [](int fd, std::string&) -> std::string {
            return Logout::logout(fd);
        }},
        {tagPing, [](int, std::string&) -> std::string {
            return createPingMessage();
        }},
        {tagQueueRejoin, [](int fd, std::string&) -> std::string {
            return RejoinQ::rejoinQueue(fd);
        }},
        {tagGameThrow, [](int fd, std::string&) -> std::string {
            return GameM::throwDice(fd);
        }},
        {tagGameSelect, [](int fd, std::string& information) -> std::string {
            return GameM::selectDice(fd, information);
        }},
        {tagGameConfirm, [](int fd, std::string&) -> std::string {
            return GameM::nextTurn(fd);
        }},
        {tagGameEndTurn, [](int fd, std::string&) -> std::string {
            return GameM::endTurn(fd);
        }},
        {tagReconnect, [](int fd, std::string&) -> std::string {
            return Reconnect::reconnect(fd);
        }}
    };
}

/**
 * @brief Function to build the response with the information and tag
 * @param information - Information to send
 * @param tag - Tag of the message
 * @return response
 */
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

/**
 * @brief Function to build the violation response that will force disconnect client
 * @return response
 */
std::string MessageFormat::createViolationMess() {
    // Response is based on the information
    std::string responseHeader, response;
    std::string tag = TERMINATION;
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

/**
 * @brief Function to build the ping message
 * @return response
 */
std::string MessageFormat::createPingMessage() {
    // Response is based on the information
    std::string response;
    response.append("ds;0007;").append(PING).append(";\n");

    return response;
}

/**
 * @brief Function to build the alive check message
 * @return response
 */
std::string MessageFormat::aliveCheck() {
    // Response is based on the information
    std::string response;
    response.append("ds;0008;").append(CONNECTION_ALIVE).append(";\n");

    return response;
}

