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

#ifdef _WIN32
typedef SOCKET socket_t;
#else
typedef int socket_t;
#endif

class Server {
public:
    Server();
    ~Server();

    bool init();
    void start();
    void pingClients();
    void stop();

private:
    void handleClient(socket_t clientSocket);
    std::atomic<bool> running;
    socket_t serverSocket;
    std::vector<socket_t> clientSockets;

#ifdef _WIN32
    WSADATA wsaData;
#endif
};

namespace MessageProcessing{
    std::string readMessage(socket_t fd, int a2read);
    std::string processMessage(int fd, const std::string& message);
}

#endif //SERVER_H