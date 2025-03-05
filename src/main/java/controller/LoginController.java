package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import service.NetworkService;
import domain.User;
import service.Service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static utils.Hasher.hashPassword;

public class LoginController {


    @FXML
    private TextField textFieldNume;

    @FXML
    private TextField textFieldPrenume;

    @FXML
    private PasswordField textFieldParola;


    private Service service;

    @FXML
    private ComboBox<User> userComboBox;

    @FXML
    private Button btnSelect;


    public LoginController() {
    }

    public void setService(Service service) {
        this.service = service;
        loadUsers();
    }

    public Service getService(){
        return service;

    }
    public void loadUsers() {
        for(User user : service.getUtilizatori()){
            userComboBox.getItems().add(user);
        }
    }

    @FXML
    private void handleLogin() {
        User selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            System.out.println("Selected user: " + selectedUser.getFirstName() + " "+selectedUser.getLastName()) ;
            System.out.println(selectedUser.getFriends());
            for(User user : selectedUser.getFriends()){
                System.out.println(user.getFirstName() + " " + user.getLastName());
            }
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/utilizator-view.fxml"));
                Scene UtilizatorScene = new Scene(fxmlLoader.load());



                UtilizatorController utilizatorController = fxmlLoader.getController();
                utilizatorController.setService(service);
                utilizatorController.setUserInfo(selectedUser);


                Stage stage = (Stage) btnSelect.getScene().getWindow();
                stage.setScene(UtilizatorScene);
                stage.setTitle("Utilizator");
                stage.show();



            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("No user selected!");
        }
    }
/*
    public boolean verifyCredentials(User user, String password) {
        //return user != null && user.getPassword().equals(password);
        return user != null && user.getPassword().equals(password);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
*/
    @FXML
    private void handleLoginButton() {
      /*  if(textFieldNume.getText().isEmpty() || textFieldPrenume.getText().isEmpty() || textFieldParola.getText().isEmpty()){
            System.out.println("Empty fields!");
            return;
        }

        String nume = textFieldNume.getText();
        String username = "";
        String prenume = textFieldPrenume.getText();
        String parola = textFieldParola.getText();

        parola = hashPassword(parola);

        User user_login = service.LogIn(nume, prenume, username, parola);

        if(user_login != null){
            for(User user : user_login.getFriends()){
                System.out.println(user.getFirstName() + " " + user.getLastName());
            }
            try{
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/utilizator-view.fxml"));
                Scene UtilizatorScene = new Scene(fxmlLoader.load());



                System.out.println(user_login);

                UtilizatorController utilizatorController = fxmlLoader.getController();
                utilizatorController.setService(service);
                utilizatorController.setUserInfo(user_login);





                Stage stage = (Stage) btnSelect.getScene().getWindow();
                stage.setScene(UtilizatorScene);
                stage.setTitle("Utilizator");
                stage.show();

                if(StreamSupport.stream(service.getReceiveReq(user_login.getId()).spliterator(), false).findAny().isPresent()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Notification");
                    alert.setHeaderText("Notification");
                    alert.setContentText("You have new friend requests!");

                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alertStage.setWidth(300);
                    alertStage.setHeight(200);

                    alert.showAndWait();
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else{
            System.out.println("Parola/Nume-Prenume incorect!");
        }

*/
    }

}
