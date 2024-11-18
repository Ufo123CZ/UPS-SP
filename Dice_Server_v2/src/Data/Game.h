#ifndef GAME_H
#define GAME_H

#include "Player.h"
#include "Dice.h"
#include <vector>

class Game {
public:
    Game(Player player1, Player player2);

    // ~Game();

    std::vector<Player> gamePlayers;
    std::vector<std::string> playerNames;

    std::string onMove;
    std::vector<Dice> dicesP1;
    std::vector<Dice> dicesP2;

    // Scores - length 3. 0 - total, 1 - round, 2 - throw
    std::vector<int> scoresP1;
    std::vector<int> scoresP2;
};


#endif //GAME_H
