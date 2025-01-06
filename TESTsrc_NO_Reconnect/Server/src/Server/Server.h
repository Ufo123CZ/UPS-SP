#ifndef SERVER_H
#define SERVER_H

#include <vector>
#include <atomic>

#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>


typedef int socket_t;

class Server {
public:
    Server();
    ~Server();

    bool init(const std::string& ip, int port);
    void start();
    void pingClients();
    void stop();

private:
    void handleClient(socket_t clientSocket);
    std::atomic<bool> running;
    socket_t serverSocket;
    std::vector<socket_t> clientSockets;
};

namespace MessageProcessing {
    std::string readMessage(int fd);
    std::string processMessage(int fd, const std::string& message);
}

#endif //SERVER_H