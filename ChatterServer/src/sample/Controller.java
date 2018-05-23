package sample;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Controller implements Initializable {

    @FXML
    public Label clientsLabel;

    private List<ClientHandler> clients;
    private Map<String, ClientHandler> clientHandlerMap;
    private Server server;
    private ExecutorService executor;

    public Controller() throws IOException {
        clientHandlerMap = new HashMap<>();
        clients = new ArrayList<>();
        server = new Server(10, this);
        executor = Executors.newSingleThreadExecutor();
        executor.submit(server);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientsLabel.setWrapText(true);
    }

    public void addClient(ClientHandler clientHandler) {
        clientHandlerMap.put(clientHandler.getName(), clientHandler);
        Iterator<Map.Entry<String, ClientHandler>> it = clientHandlerMap.entrySet().iterator();
        StringBuilder builder = new StringBuilder();
        while (it.hasNext()) {
            builder.append(it.next().getKey()).append("\n");
        }
        Platform.runLater(() -> clientsLabel.setText(builder.toString()));
    }

    public synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlerMap.remove(clientHandler.getName());
        Platform.runLater(() -> clientsLabel.setText(clientsLabel.getText().replaceAll(clientHandler.getName(), "")));
    }

    public void shutdown() throws IOException {
        server.terminate();
        executor.shutdown();
    }

    public ClientHandler getClient(String name) {
        return clientHandlerMap.get(name);
    }

}
