#include "../MessageFormat.h"
#include "../../Data/Player.h"
#include "../../Data/DataVectors.h"
#include "../../Messages/TAGS.h"

/**
 * Namespace Login
 * Collection of functions that handle the login of a player
 */
namespace Login {
    /**
     * @brief Function login
     * Handles the login of a player
     * @param fd - File descriptor of the player
     * @param information - Information of the player
     * @return Response to the player
     */
    std::string login(const int fd, std::string& information) {
        // Check if message could come
        for (Player &player : DataVectors::players) {
            if (player.fd == fd) {
                if (player.status != -2) {
                    return MessageFormat::createViolationMess();
                }
            }
        }

        // Process the login information
        std::string name = information.substr(0, information.find(';'));

        // Trim name of \n and \r
        if (name.find('\n') != std::string::npos) {
            name = name.substr(0, name.find('\n'));
        }
        if (name.find('\r') != std::string::npos) {
            name = name.substr(0, name.find('\r'));
        }

        // Check if the player is already logged in
        for (auto &player : DataVectors::players) {
            if (player.name == name) {
                std::string tag;
                tag.append(BASE_LOGIN).append(NAMESET);

                // Remove player from vector with fd
                DataVectors::players.erase(std::remove_if(DataVectors::players.begin(), DataVectors::players.end(), [fd](Player &player) {
                    return player.fd == fd;
                }), DataVectors::players.end());
                return MessageFormat::prepareResponse(ERROR, tag);
            }
        }

        for (auto &player : DataVectors::players) {
            if (player.status == -2) {
                player.name = name;
                player.status = 0;
                std::string tag;
                // update player in vector
                for (auto & i : DataVectors::players) {
                    if (i.fd == fd) {
                        i = player;
                    }
                }
            }
        }

        std::cout << "Player " << name << " has logged in." << std::endl;

        // Prepare response and return
        std::string tag;
        tag.append(BASE_LOGIN).append(NAMESET);

        std::cout << "Sending response: " << MessageFormat::prepareResponse(SUCCESS, tag) << std::endl;
        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}
