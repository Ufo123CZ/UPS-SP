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
    std::string holdDices(int who);
    std::string nextRound(int who);
    std::string endRound(int who);
    std::string updateDiceEnd(int who);
    void scoreRestart(int who);
    int calculateThrow(int who);

    // ~Game();

    std::vector<Player> gamePlayers;
    std::vector<std::string> playerNames;

    std::string onMove;
    bool lastSwitch = false;


    std::vector<bool> throwB;
    std::vector<std::vector<Dice>> dices;

    // Scores - length 3. 0 - total, 1 - round, 2 - throw
    std::vector<std::vector<int>> scores;

    // Game state
    bool gamePaused = false;
    bool gameEnd = false;

private:
    static int calculateSelected(const int diceVals[]);
};


#endif //GAME_H
