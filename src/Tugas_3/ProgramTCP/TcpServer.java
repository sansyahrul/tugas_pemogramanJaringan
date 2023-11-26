package Tugas_3.ProgramTCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {

    public static void main(String[] args) {
        final int PORT = 12345;

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Menerima file dari klien
                receiveFile(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void receiveFile(Socket clientSocket) {
        try {
            // Inisialisasi input stream untuk membaca data dari klien
            InputStream inputStream = clientSocket.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

            // Menerima informasi file (nama dan ukuran)
            String fileName = objectInputStream.readUTF();
            long fileSize = objectInputStream.readLong();

            System.out.println("Receiving file: " + fileName + " (" + fileSize + " bytes)");

            // Inisialisasi output stream untuk menulis data ke file
            FileOutputStream fileOutputStream = new FileOutputStream("server_" + fileName);
            BufferedOutputStream bufferedOutputStream = new
                    BufferedOutputStream(fileOutputStream);

            // Menerima dan menulis data file
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }

            // Menutup output stream dan socket
            bufferedOutputStream.close();
            clientSocket.close();

            System.out.println("File received successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

