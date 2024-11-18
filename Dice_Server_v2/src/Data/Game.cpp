#include "Game.h"
#include "Player.h"
#include "Dice.h"
#include <iostream>

std::vector<Player> gamePlayers;

Game::Game(Player player1, Player player2) {
    // Dice ids
    std::vector<std::string> dicesIds = {"D1", "D2", "D3", "D4", "D5", "D6"};

    // Set players
    gamePlayers.push_back(player1);
    gamePlayers.push_back(player2);

    // Set player names
    playerNames.push_back(player1.name);
    playerNames.push_back(player2.name);

    // Set onMove
    onMove = player1.name;

    // Set dices
    for (int i = 0; i < 5; i++) {
        dicesP1.emplace_back(dicesIds[i]);
        dicesP2.emplace_back(dicesIds[i]);
    }

    // Set scores
    scoresP1 = {0, 0, 0};
    scoresP2 = {0, 0, 0};

    std::cout << "Game created." << std::endl;
}