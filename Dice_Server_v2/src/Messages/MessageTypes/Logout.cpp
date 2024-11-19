#include "../MessageFormat.h"
#include "../TAGS.h"
#include "../../Utils/Consts.h"
#include "../../Data/DataVectors.h"
#include "../../Data/Player.h"


namespace Logout {
    std::string logout(int fd, std::string& information) {
        // Process the logout information
        std::string name = information.substr(0, information.find(';'));

        // Trim name of \n and \r
        if (name.find('\n') != std::string::npos) {
            name = name.substr(0, name.find('\n'));
        }
        if (name.find('\r') != std::string::npos) {
            name = name.substr(0, name.find('\r'));
        }

        // Remove player from the vector
        for (int i = 0; i < DataVectors::players.size(); i++) {
            if (DataVectors::players[i].fd == fd) {
                // Remove the pointer from the vector
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
