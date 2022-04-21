package ru.gb.storage.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ru.gb.storage.client.Client;
import ru.gb.storage.commons.messages.AuthRequestMessage;
import ru.gb.storage.commons.messages.RegisterRequestMessage;

public class LoginController {
    @FXML
    private Button btnOk;

    @FXML
    private CheckBox cbRegister;

    @FXML
    private TextField textLogin;

    @FXML
    private PasswordField textPassword;

    @FXML
    private Label labelStatus;

    private Client client;

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    private void initialize() {

    }

    public void changeBtnText(ActionEvent actionEvent) {
        if (cbRegister.isSelected()) {
            btnOk.setText("Register");
        } else {
            btnOk.setText("Ok");
        }
    }

    public void sendRequest(ActionEvent actionEvent) {
        String login = textLogin.getText();
        String password = textPassword.getText();
        if (cbRegister.isSelected()) {
            RegisterRequestMessage rrm = new RegisterRequestMessage();
            rrm.setLogin(login);
            rrm.setPassword(password);
            client.getNetworkService().send(rrm);
        } else {
            AuthRequestMessage arm = new AuthRequestMessage();
            arm.setLogin(login);
            arm.setPassword(password);
            client.getNetworkService().send(arm);
        }
    }

    public void authenticateSuccess() {
        Stage stage = (Stage) btnOk.getScene().getWindow();
        client.setAuthenticate(true);
        stage.close();
    }

    public void authenticateError(String errorMessage) {
        labelStatus.setText(errorMessage);
        labelStatus.setTextFill(Color.RED);
    }

    public void registerSuccess() {
        Stage stage = (Stage) btnOk.getScene().getWindow();
        client.setAuthenticate(true);
        stage.close();
    }

    public void registerError(String errorMessage) {
        labelStatus.setText(errorMessage);
        labelStatus.setTextFill(Color.RED);
    }
}
