#ifndef PLAYER_H
#define PLAYER_H

#include <stdbool.h>

typedef struct Player {
    char *name;
    struct Player *next;
} Player;

void trim_newline(char *str);
void add_player(Player **head, const char *name);
void remove_player(Player **head, const char *name);
void free_players(Player **head);
bool player_exists(const Player *head, const char *name);
void print_players(const Player *head);


#endif // PLAYER_H