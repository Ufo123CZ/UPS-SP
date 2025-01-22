#ifndef SERVER_H
#define SERVER_H

#include <vector>
#include <atomic>

#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>

// socket_t is a type that represents a socket file descriptor
typedef int socket_t;

/**
 * @brief Server class that handles the server side of the game
 */
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
    std::vector<socket_t> fdsToRemove;
};

/**
 * @brief Namespace that handles the reading and processing of messages
 */
namespace MessageProcessing {
    std::string readMessage(int fd);
    std::string processMessage(int fd, const std::string& message);
}

#endif //SERVER_H