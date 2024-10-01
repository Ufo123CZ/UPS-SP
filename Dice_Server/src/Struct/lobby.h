#ifndef LOBBY_H
#define LOBBY_H

struct Lobby {
    int id;
    char *name;
    int maxPlayers;
    int currentPlayers;
    int gameID;
    int players[];
};

#endif //LOBBY_H
