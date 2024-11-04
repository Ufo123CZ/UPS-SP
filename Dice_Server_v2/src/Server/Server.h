#ifndef SERVER_H
#define SERVER_H

#include <vector>
#include <atomic>

#ifdef _WIN32
#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")
#else
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#endif

class Server {
public:
    Server();
    ~Server();

    bool init();
    void start();
    void stop();

private:
    void handleClient(int clientSocket);
    std::atomic<bool> running;
    int serverSocket;
    std::vector<int> clientSockets;

#ifdef _WIN32
    WSADATA wsaData;
#endif
};

#endif //SERVER_H