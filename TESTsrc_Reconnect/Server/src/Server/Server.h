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
    void stop();
    void checkPlayerActivity();

private:
    std::atomic<bool> running;
    socket_t serverSocket;
    std::vector<socket_t> clientSockets;
    fd_set client_socks = {};
};



namespace MessageProcessing {
    std::string readMessage(int fd);
    std::string processMessage(int fd, const std::string& message);
}

#endif //SERVER_H