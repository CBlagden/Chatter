package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private String name;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Controller controller;

    public ClientHandler(Controller controller, Socket socket) {
        this.controller = controller;
        this.socket = socket;
        buildObjectStreams(socket);
    }

    private void buildObjectStreams(Socket socket) {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.name = (String) objectInputStream.readObject();
            System.out.println("Connected to " + name + " successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not connect to new client.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                String input = (String) objectInputStream.readObject();
                if (input.contains("/quit"))
                    break;
                if (input.contains(":")) {
                    String recipient = input.substring(0, input.indexOf(":"));
                    String message = input.substring(input.indexOf(":") + 1);
                    ClientHandler client = controller.getClient(recipient);
                    if (client != null) {
                        System.out.println("Send " + recipient + " : " + message);
                        objectOutputStream.writeObject("good");
                    } else {
                        objectOutputStream.writeObject("bad");
                    }
                } else {
                    objectOutputStream.writeObject("no recipient");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Lost connection, closing " + name);
                break;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        closeSocket(socket);
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
            System.out.println("Closed socket for " + name);
            controller.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

}
