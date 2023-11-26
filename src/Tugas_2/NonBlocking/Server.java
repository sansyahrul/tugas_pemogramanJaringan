package Tugas_2.NonBlocking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private static final int PORT = 8888;
    private static final String EXIT_COMMAND = "exit";

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Server started on port " + PORT);

        while (true) {
            selector.select();

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    acceptConnection(serverSocketChannel, selector);
                } else if (key.isReadable()) {
                    readData(key, selector);
                }

                keyIterator.remove();
            }
        }
    }

    private static void acceptConnection(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("Connection accepted from: " + clientChannel.getRemoteAddress());
    }

    private static void readData(SelectionKey key, Selector selector) throws IOException {
        SocketChannel senderChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = senderChannel.read(buffer);

        if (bytesRead == -1) {
            disconnectClient(senderChannel, selector);
        } else if (bytesRead > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data);
            System.out.println("Received message from " + senderChannel.getRemoteAddress() + ": " + message);

            if (EXIT_COMMAND.equalsIgnoreCase(message.trim())) {
                disconnectClient(senderChannel, selector);
            } else {
                broadcastMessage(senderChannel, message, selector);
            }
        }
    }

    private static void disconnectClient(SocketChannel clientChannel, Selector selector) throws IOException {
        clientChannel.close();
        System.out.println("Connection closed by client: " + clientChannel.getRemoteAddress());
    }

    private static void broadcastMessage(SocketChannel senderChannel, String message, Selector selector) throws IOException {
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            if (key.isValid() && key.channel() instanceof SocketChannel && key.channel() != senderChannel) {
                SocketChannel receiverChannel = (SocketChannel) key.channel();
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                receiverChannel.write(buffer);
            }
        }
    }
}
