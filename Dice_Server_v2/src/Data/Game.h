#ifndef GAME_H
#define GAME_H

#include "Player.h"
#include "Dice.h"
#include <vector>

class Game {
public:
    Game(Player player1, Player player2);

    std::vector<std::string> rollDices(int who);
    void selectDice(int who, std::string& information);
    int calculateThrow(int who);
    int calculateRound(int who);
    std::string endRound(int who);

    // ~Game();

    std::vector<Player> gamePlayers;
    std::vector<std::string> playerNames;

    std::string onMove;

    std::vector<std::vector<Dice>> dices;

    // Scores - length 3. 0 - total, 1 - round, 2 - throw
    std::vector<std::vector<int>> scores;

private:
    int calculateSelected(int diceVals[]);
};


#endif //GAME_H
