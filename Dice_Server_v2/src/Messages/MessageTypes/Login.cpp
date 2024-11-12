#include "../MessageFormat.h"
#include "../../Data/Player.h"
#include "../../Data/DataVectors.h"
#include "../../Messages/TAGS.h"
#include "../../Utils/Consts.h"

namespace Login {
    std::string login(const int fd, std::string& information) {
        // Process the login information
        std::string name = information.substr(0, information.find(';'));

        // Trim name of \n and \r
        if (name.find('\n') != std::string::npos) {
            name = name.substr(0, name.find('\n'));
        }
        if (name.find('\r') != std::string::npos) {
            name = name.substr(0, name.find('\r'));
        }

        auto *player = new Player(fd, name, 0, 0, 5);
        DataVectors::players.push_back(*player);


        std::cout << "Player " << name << " has logged in." << std::endl;

        // prepare response and return
        std::string tag;
        tag.append(BASE_LOGIN).append(LOGIN);

        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}
