#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Data/DataVectors.h"
#include "../../Data/Player.h"


namespace Logout {
    std::string logout(int fd) {
        std::string name;
        // Remove player from the vector
        for (int i = 0; i < DataVectors::players.size(); i++) {
            if (DataVectors::players[i].fd == fd) {
                // Remove the pointer from the vector
                name = DataVectors::players[i].name;
                DataVectors::players.erase(DataVectors::players.begin() + i);
                break;
            }
        }

        // Print the logout message
        std::cout << "Player " << name << " has logged out." << std::endl;

        // Prepare response and return
        std::string tag;
        tag.append(BASE_LOGIN).append(LOGOUT);

        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}
