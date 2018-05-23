package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class Controller implements Initializable {

    @FXML
    public TextField clientNameField;

    public TextField addressField;
    public TextField portField;

    public TextField messageField;
    public Button sendButton;
    public TextField recipientField;

    private ExecutorService executorService;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Properties properties;

    private String address;
    private int port;
    private String clientName;

    private File propertiesFile = new File(System.getProperty("user.dir"),
            "src/resources/config.properties");

    public Controller() throws IOException {
        executorService = Executors.newFixedThreadPool(100);
        properties = new Properties();
        InputStream inputStream = new FileInputStream(propertiesFile);
        properties.load(inputStream);
        address = properties.getProperty("address");
        port = Integer.valueOf(properties.getProperty("port"));
        clientName = properties.getProperty("name");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        addressField.setText(address);
        portField.setText(String.valueOf(port));
        clientNameField.setText(clientName);

        try {
            socket = new Socket(addressField.getText(),
                    Integer.valueOf(portField.getText()));
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream.writeObject(clientNameField.getText());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Connection Failed!");
            alert.setContentText("Socket could not be set on\nport: " + port + " "
                    + "\naddress: " + address + "\nClosing program on exit");
            alert.showAndWait();
            System.exit(-1);
        }

    }

    public void sendText(String recipient, String text) {
        try {
            String message;
            if (recipient.equals(""))
                message = text;
            else
                message = recipient + ":" + text;
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Could not send message "
                    + messageField.getText()
                    + " \nlost connection"
                    + "\nclosing program");
            alert.showAndWait();
            System.exit(-1);
        }
        Future<String> status = executorService.submit(() -> (String)objectInputStream.readObject());
        try {
            System.out.println("status: " + status.get(3, TimeUnit.SECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void cleanAndWrite() throws IOException {
        sendText("", "/quit");
        socket.close();
        OutputStream outputStream = new FileOutputStream(propertiesFile);
        properties.setProperty("port", portField.getText());
        properties.setProperty("address", addressField.getText());
        properties.setProperty("name", clientNameField.getText());
        properties.store(outputStream, "");
    }

    public void onSendButtonClick() {
        sendText(recipientField.getText(),
                messageField.getText());
    }

}
