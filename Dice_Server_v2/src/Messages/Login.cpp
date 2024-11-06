#include "MessageFormat.h"
#include "../Data/Player.h"
#include "../Data/DataVectors.h"
#include "../Messages/TAGS.h"
#include "../Utils/Consts.h"

namespace Login {
    std::string login(const int fd, std::string& information) {
        // Process the login information
        std::string name = information.substr(0, information.find(';'));

        // Trim name of \n
        if (name.find('\n') != std::string::npos) {
            name = name.substr(0, name.find('\n'));
        }

        Player player(fd, name, 0, 0);
        DataVectors::players.push_back(player);

        std::cout << "Player " << name << " has logged in." << std::endl;

        // prepare response and return
        const std::string tag1 = BASE_OUT;
        const std::string tag2 = LOGIN;
        std::string tag = tag.append(tag1).append(tag2);
        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}
