package sample;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    private static final int PORT = 4322;

    private ServerSocket serverSocket;
    private final ExecutorService pool;

    private Controller controller;
    private boolean running = true;

    public Server(int poolSize, Controller controller) throws IOException {
        this.controller = controller;
        pool = Executors.newFixedThreadPool(poolSize);
        serverSocket = new ServerSocket(PORT);
        System.out.println("Started server on port " + PORT);
    }

    @Override
    public void run() {
        while (running) try {
            ClientHandler client = new ClientHandler(controller, serverSocket.accept());
            controller.addClient(client);
            pool.execute(client);
        } catch (IOException e) {
            break;
        }
        pool.shutdown();
    }

    public void terminate() throws IOException {
        serverSocket.close();
        running = false;
    }

}
