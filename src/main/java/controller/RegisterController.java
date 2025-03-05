package controller;

import domain.User;
import domain.validators.ServiceException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static utils.Hasher.hashPassword;

public class RegisterController {

    @FXML
    private TextField textFieldUsername;



    @FXML
    private Button btnRegister;


    @FXML
    private PasswordField textFieldPassword;

    @FXML
    private TextField textFieldPrenume;

    @FXML
    private TextField textFieldNume;


    private Service service;

    public RegisterController() {
    }

    public void setService(Service service){
        this.service = service;
    }


    @FXML
    public void handleRegister() {
        if(textFieldNume.getText().isEmpty() || textFieldPrenume.getText().isEmpty()
                || textFieldPassword.getText().isEmpty() || textFieldUsername.getText().isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all the fields");
            alert.showAndWait();
        }

        String nume = textFieldNume.getText();
        String prenume = textFieldPrenume.getText();
        String username = textFieldUsername.getText();
        String password = textFieldPassword.getText();




        try{

            File defaultImageFile = new File("src/main/resources/profile-pics/default.png");
            byte[] defaultImageBytes = Files.readAllBytes(defaultImageFile.toPath());

            System.out.println(Paths.get(defaultImageFile.getAbsolutePath()));

            User added_user = service.register_user(nume, prenume, username, password, defaultImageBytes);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/utilizator-view.fxml"));
            Scene UtilizatorScene = new Scene(fxmlLoader.load());


            UtilizatorController utilizatorController = fxmlLoader.getController();
            utilizatorController.setService(service);
            utilizatorController.setUserInfo(added_user);


            Stage stage = (Stage) btnRegister.getScene().getWindow();
            stage.setScene(UtilizatorScene);
            stage.show();
        }
        catch(ServiceException e) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }


    }
}
