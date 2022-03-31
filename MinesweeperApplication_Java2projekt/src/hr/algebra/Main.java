/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra;

import chatContract.data.ChatMessage;
import chatContract.service.MessengerService;
import hr.algebra.controller.MainController;
import hr.algebra.model.MinefieldCell;
import hr.algebra.model.Player;
import hr.algebra.utils.AlertUtils;
import hr.algebra.utils.SerializationUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Tomo
 */
public class Main extends Application {

    private static final String CONFIG_FILE_PATH = "Network configuration.properties";
    private static int LOCAL_PORT; // = 1111;
    private static int REMOTE_PORT; // = 2222;
    private static String REMOTE_ADDRESS; // = "localhost";

    private MainController mainController;
    private ServerSocket serverSocket;
    private Thread serverThread;
    private static MessengerService chatServer;

    //START; STOP; MAIN
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("view/main.fxml"));
        Parent root = loader.load();
        mainController = loader.getController();

        Scene scene = new Scene(root);
        primaryStage.setTitle("Minesweeper multiplayer!");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadChat();
        new Thread(() -> startServer()).start();
    }

    @Override
    public void stop() throws Exception {
        if (!MainController.allMinefieldCells.isEmpty()) {
            SerializationUtils.write(MainController.allMinefieldCells, "Minefield.ser");
        }
        List<Player> players = Arrays.asList(MainController.player1, MainController.player2);
        SerializationUtils.write(players, "Results.ser");

        serverThread.stop();
        serverSocket.close();
        MainController.scoreboardThread.stop();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    //SERVER CONFIG
    private void loadConfig() throws FileNotFoundException, IOException {

        if (!Files.exists(Paths.get(CONFIG_FILE_PATH))) {
            Platform.runLater(()
                    -> AlertUtils.showErrorAlert("ERROR", "The server configuration file has not been found. Server did not start. Please exit and try again."));
        }

        Properties config = new Properties();
        config.load(new FileInputStream(new File(CONFIG_FILE_PATH)));
        LOCAL_PORT = Integer.parseInt(config.getProperty("LOCAL_PORT"));
        REMOTE_PORT = Integer.parseInt(config.getProperty("REMOTE_PORT"));
        REMOTE_ADDRESS = config.getProperty("ADDRESS");

    }

    //SERVER
    private void startServer() {

        try {
            loadConfig();
            serverSocket = new ServerSocket(LOCAL_PORT);
            System.err.println("Server listening on port: " + serverSocket.getLocalPort());
            serverThread = Thread.currentThread();
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.err.println("Client connected from port: " + clientSocket.getPort());
                readDataFromClient(clientSocket);
            }

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readDataFromClient(Socket clientSocket) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        List<Player> players = (List<Player>) ois.readObject();
        List<MinefieldCell> allMinefieldCells = (List<MinefieldCell>) ois.readObject();
        applyChangesLocally(players, allMinefieldCells);

    }

    private void applyChangesLocally(List<Player> players, List<MinefieldCell> allMinefieldCells) throws RemoteException {
        MainController.player1 = players.get(0);
        MainController.player2 = players.get(1);
        MainController.allMinefieldCells = allMinefieldCells;
        Platform.runLater(() -> mainController.loadPlayersData());
        Platform.runLater(() -> mainController.updateMinefield());
        mainController.refreshChat(chatServer.getChatHistory());
    }

    //KLIJENT
    public static void connectToServer() {
        try (Socket clientSocket = new Socket(REMOTE_ADDRESS, REMOTE_PORT)) {
            System.err.println("Client is connecting to: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            sendDataToServer(clientSocket);

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void sendDataToServer(Socket clientSocket) throws IOException, ClassNotFoundException {

        ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
        List<Player> players = Arrays.asList(MainController.player1, MainController.player2);

        oos.writeObject(players);
        oos.writeObject(MainController.allMinefieldCells);
    }

    //CHAT
    private void loadChat() {

        Registry registry;
        try {
            registry = LocateRegistry.getRegistry();

            System.out.println("Dohvatio sam RMI registry!");

            chatServer = (MessengerService) registry
                    .lookup("MessengerService");

            System.out.println("Dohvatio sam servis!");

        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(Main.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static List<ChatMessage> sendMessage(ChatMessage chatMessage) throws RemoteException {

        chatServer.sendMessage(chatMessage);
        System.err.println(chatServer.getChatHistory());
        connectToServer();
        return chatServer.getChatHistory();

    }

}
