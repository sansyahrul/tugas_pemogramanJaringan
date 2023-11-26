package Tugas_2.Blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BlockingServer {
    private static final int PORT = 90;
    private List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        BlockingServer server = new BlockingServer();
        server.start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.inputStream = clientSocket.getInputStream();
                this.outputStream = clientSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    byte[] buffer = new byte[1024];
                    int bytesRead = inputStream.read(buffer);

                    if (bytesRead == -1) {
                        break; // Client disconnected
                    }

                    String message = new String(buffer, 0, bytesRead);
                    System.out.println("Received message from " + clientSocket + ": " + message);

                    // Broadcast the message to all clients
                    broadcast(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // Remove the client handler when the client disconnects
                clients.remove(this);
                System.out.println("Client disconnected: " + clientSocket);
                closeConnection();
            }
        }

        public void sendMessage(String message) {
            try {
                outputStream.write(message.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                if (client != this) {
                    client.sendMessage(message);
                }
            }
        }

        private void closeConnection() {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
