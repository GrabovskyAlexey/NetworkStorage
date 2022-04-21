package ru.gb.storage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.gb.storage.client.controller.LoginController;
import ru.gb.storage.client.controller.MainController;
import ru.gb.storage.client.services.NetworkServiceImpl;
import ru.gb.storage.client.services.interfaces.NetworkService;

import java.io.IOException;

public class Client extends Application {

    private NetworkService networkService;
    private LoginController loginController;
    private MainController mainController;
    private boolean authenticate;

    public NetworkService getNetworkService() {
        return networkService;
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public boolean isAuthenticate() {
        return authenticate;
    }

    public void setAuthenticate(boolean authenticate) {
        this.authenticate = authenticate;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage stage) throws Exception {
        this.networkService = new NetworkServiceImpl(this);
        networkService.start();
        showLoginWindow(stage);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Client.class.getResource("/mainForm.fxml"));
        AnchorPane mainForm = loader.load();
        stage.setTitle("Main Window");
        mainController = loader.getController();
        stage.setScene(new Scene(mainForm));
        if (authenticate) {
            stage.show();
        }
        stop();
    }

    public void showLoginWindow(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Client.class.getResource("/loginForm.fxml"));
            AnchorPane loginPane = loader.load();
            Stage loginForm = new Stage();
            loginForm.setTitle("Login");
            loginForm.initModality(Modality.WINDOW_MODAL);
            loginForm.initOwner(stage);
            loginController = loader.getController();
            loginController.setClient(this);
            loginForm.setScene(new Scene(loginPane));
            loginForm.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        networkService.stop();
    }
}
