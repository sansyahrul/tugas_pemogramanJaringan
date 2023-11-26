package Tugas_2.NonBlocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {
    private static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("localhost", PORT));
        socketChannel.configureBlocking(false);

        Scanner scanner = new Scanner(System.in);

        // Handle incoming messages in a separate thread
        Thread messageReceiverThread = new Thread(() -> {
            try {
                while (true) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    int bytesRead = socketChannel.read(buffer);

                    if (bytesRead == -1) {
                        System.out.println("Server closed the connection");
                        break;
                    } else if (bytesRead > 0) {
                        buffer.flip();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);
                        String receivedMessage = new String(data);
                        System.out.println("Received message from server: " + receivedMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        messageReceiverThread.start();

        // Send messages from the client
        while (true) {
            System.out.print("Enter a message (or 'exit' to quit): ");
            String message = scanner.nextLine();

            if ("exit".equalsIgnoreCase(message)) {
                break;
            }

            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            socketChannel.write(buffer);
        }

        socketChannel.close();
    }
}
