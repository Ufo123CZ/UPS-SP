#ifndef GAME_H
#define GAME_H

#include "Player.h"
#include "Dice.h"
#include <vector>

class Game {
public:
    Game(Player player1, Player player2);

    std::pair<std::string, std::string> rollDices(int who);
    std::string selectDices(int who, std::string& information);
    void holdDices(int who);
    int calculateThrow(int who);
    void nextRound(int who);
    std::string endRound(int who);

    // ~Game();

    std::vector<Player> gamePlayers;
    std::vector<std::string> playerNames;

    std::string onMove;

    std::vector<std::vector<Dice>> dices;

    // Scores - length 3. 0 - total, 1 - round, 2 - throw
    std::vector<std::vector<int>> scores;

private:
    int calculateSelected(const int diceVals[]);
};


#endif //GAME_H
