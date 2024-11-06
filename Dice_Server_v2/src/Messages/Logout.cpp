#include "MessageFormat.h"
#include "TAGS.h"
#include "../Utils/Consts.h"
#include "../Data/DataVectors.h"
#include "../Data/Player.h"


namespace Logout {
    std::string logout(int fd, std::string& information) {

        // Process the logout information
        std::string name = information.substr(0, information.find(';'));

        // Remove player from the vector
        for (int i = 0; i < DataVectors::players.size(); i++) {
            if (DataVectors::players[i].fd == fd) {
                DataVectors::players[i].~Player();
                DataVectors::players.erase(DataVectors::players.begin() + i);
                break;
            }
        }

        // Print the logout message
        std::cout << "Player " << name << " has logged out." << std::endl;

        // prepare response and return
        const std::string tag1 = BASE_OUT;
        const std::string tag2 = LOGIN;
        std::string tag = tag.append(tag1).append(tag2);

        return MessageFormat::prepareResponse(LOGOUT, tag);
    }
}