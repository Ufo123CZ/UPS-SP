#include "../MessageFormat.h"
#include "../../Data/DataVectors.h"
#include "../../Messages/TAGS.h"

namespace RejoinQ {
    std::string rejoinQueue(int fd) {
        // Get the player that wants to rejoin queue
        for (int i = 0; i < DataVectors::players.size(); i++) {
            if (DataVectors::players[i].fd == fd) {
                DataVectors::players[i].status = 0;
                break;
            }
        }

        // Prepare response and return
        std::string tag;
        tag.append(BASE_QUEUE).append(QUEUE_REJOIN);

        return MessageFormat::prepareResponse(SUCCESS, tag);
    }
}