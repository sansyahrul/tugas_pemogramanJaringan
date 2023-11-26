package Tugas_2.Blocking;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class BlockingClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 90;

    public static void main(String[] args) {
        BlockingClient client = new BlockingClient();
        client.start();
    }

    public void start() {
        try {
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected to server: " + socket);

            Thread messageReceiverThread = new Thread(() -> {
                try {
                    InputStream inputStream = socket.getInputStream();
                    while (true) {
                        byte[] buffer = new byte[1024];
                        int bytesRead = inputStream.read(buffer);

                        if (bytesRead == -1) {
                            System.out.println("Server closed the connection");
                            break;
                        }

                        String message = new String(buffer, 0, bytesRead);
                        System.out.println("Received message from server: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            messageReceiverThread.start();

            try {
                OutputStream outputStream = socket.getOutputStream();
                Scanner scanner = new Scanner(System.in);

                while (true) {
                    System.out.print("Enter a message (or 'exit' to quit): ");
                    String message = scanner.nextLine();

                    if ("exit".equalsIgnoreCase(message)) {
                        break;
                    }

                    outputStream.write(message.getBytes());
                    outputStream.flush();
                }

                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
