package sample;

import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

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
            String source = objectInputStream.readObject().toString();
            JSONObject mess = new JSONObject(source);
            name = mess.getString("from");
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
                JSONObject input = new JSONObject(objectInputStream.readObject().toString());
                String message = input.getString("mess");
                if (message.equals("/quit")) {
                    break;
                }
                String recipient = input.getString("to");
                ClientHandler client = controller.getClient(recipient);
                if (client != null) {
                    client.sendMessage(message);
                    System.out.println("Send " + recipient + " : " + message);
                    objectOutputStream.writeObject("good");
                } else {
                    objectOutputStream.writeObject("bad");
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

    private void sendMessage(String mess) throws IOException {
        objectOutputStream.writeObject(mess);
    }

    public String getName() {
        return name;
    }

}
