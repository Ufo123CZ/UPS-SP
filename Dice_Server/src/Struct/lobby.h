#ifndef LOBBY_H
#define LOBBY_H

#include "player.h"
#include "state.h"

typedef struct Lobby {
    char *name;
    int maxPlayers;
    int currentPlayers;
    char *creator;
    State *state[2];
    struct Lobby *next;
} Lobby;

void add_lobby(Lobby **head, const char *name, Player *creator);
void remove_lobby(Lobby **head, const char *name);
void free_lobbies(Lobby **head);
void print_lobbies(const Lobby *head);

#endif // LOBBY_H