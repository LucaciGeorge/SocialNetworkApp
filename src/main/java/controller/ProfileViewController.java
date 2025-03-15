package controller;

import domain.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.Service;
import utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ProfileViewController {


    @FXML
    private AnchorPane mainAnchorPane;


    @FXML
    private Label lblNumePrenume;


    @FXML
    private Label lblUsername;

    @FXML
    private Label lblBio;

    @FXML
    private Label lblPrieteniSugestii;


    @FXML
    private Button btnSugestii;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnPrieteni;


    @FXML
    private TableView<User> tableMain;

    @FXML
    private TableColumn<User, String> ColoanaNume;

    @FXML
    public TableColumn<User, String> ColoanaPrenume;

    @FXML
    private TableColumn<User, String> ColoanaData;

    @FXML
    private ImageView profileImageView;


    ObservableList<User> observableUsers = FXCollections.observableArrayList();



    Service service;


    User user;
    User loggedUser;
    private boolean state = false;

    public void setService(Service service, User user, User loggedUser) {
        this.service = service;
        this.user = user;
        this.loggedUser = loggedUser;
        load();

    }



    private void configureContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewProfile = new MenuItem("Vezi Profil");
        viewProfile.setOnAction(e -> {
            User selectedUser = tableMain.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                openUserProfile(selectedUser);
            }
        });

        contextMenu.getItems().add(viewProfile);

        tableMain.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    tableMain.getSelectionModel().select(row.getItem());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            });
            return row;
        });
    }

    private void openUserProfile(User selectedUser)
    {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/profile-view.fxml"));
            AnchorPane root = loader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            ProfileViewController controller = loader.getController();
            controller.setService(service, selectedUser, user);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }


    private void change_pic_bio_buttons(){
        Button uploadButton = new Button("Update Profile Picture");
        Button updatebio = new Button("Update Bio");

        TextField textBio = new TextField();

        uploadButton.setLayoutX(btnEdit.getLayoutX());
        uploadButton.setLayoutY(btnEdit.getLayoutY());


        updatebio.setLayoutX(btnPrieteni.getLayoutX());
        updatebio.setLayoutY(btnPrieteni.getLayoutY());
        textBio.setLayoutX(btnPrieteni.getLayoutX());
        textBio.setLayoutY(btnPrieteni.getLayoutY() + updatebio.getHeight() + 50);

        updatebio.setPrefWidth(textBio.getPrefWidth());
        updatebio.setMinWidth(textBio.getMinWidth());
        updatebio.setMaxWidth(textBio.getMaxWidth());


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select an Image");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));


        updatebio.setOnAction(e -> {
            String text = textBio.getText();
            if(text == ""){
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Text must contain something");
                alert.showAndWait();
            }

            service.update_user_profile(loggedUser.getId(), loggedUser.getPicture(), text);
            loggedUser.setBio(textBio.getText());
            lblBio.setText(loggedUser.getBio());

            set_buttons(true);
            uploadButton.setVisible(false);
            updatebio.setVisible(false);
            textBio.setVisible(false);

        });

        uploadButton.setOnAction(e -> {

            try {
                File file = fileChooser.showOpenDialog(uploadButton.getScene().getWindow());


                if (file != null) {
                    Image image = new Image(file.toURI().toString());
                    byte[] defaultImageBytes = Files.readAllBytes(file.toPath());
                    profileImageView.setImage(image); // Display the selected image
                    service.update_user_profile(loggedUser.getId(), defaultImageBytes, loggedUser.getBio());
                    loggedUser.setPicture(defaultImageBytes);
                    set_buttons(true);
                    uploadButton.setVisible(false);
                    updatebio.setVisible(false);
                    textBio.setVisible(false);
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        });
        mainAnchorPane.getChildren().addAll(uploadButton, updatebio, textBio);


    }

    private void set_buttons(boolean state){
        btnEdit.setVisible(state);
        btnSugestii.setVisible(state);
        btnPrieteni.setVisible(state);

    }

    private void load_friends(){
        observableUsers.clear();
        observableUsers.addAll(service.getFriends(loggedUser.getId()));
    }

    private void load_friends_suggestions(){
        observableUsers.clear();
        observableUsers.addAll(service.getFriendsSuggestion(loggedUser.getId()));
    }

    private void load_commong_friends(){
        observableUsers.clear();
        observableUsers.addAll(service.getCommonFriends(user.getId(), loggedUser.getId()));
    }

    private void load(){
        if(user.getPicture() != null) {
            Image image = new Image(new ByteArrayInputStream(user.getPicture()));
            profileImageView.setImage(image);
            profileImageView.getStyleClass().add("round-image");

        }


        if(user == loggedUser) {// am dat click pe propriul profil
            state = true;
            load_friends();
        }else{
            load_commong_friends();
            lblPrieteniSugestii.setText("Prieteni comuni");
        }
        set_buttons(state);



        btnSugestii.setOnAction(e -> {
            load_friends_suggestions();
            lblPrieteniSugestii.setText("Suggestii");

        });
        btnPrieteni.setOnAction(e -> {
            load_commong_friends();
            lblPrieteniSugestii.setText("Prieteni");
        });

        btnEdit.setOnAction(e -> {
           set_buttons(false);
           change_pic_bio_buttons();
        });


        lblUsername.setText("@" + user.getUsername());
        lblBio.setText(user.getBio());
        lblNumePrenume.setText(user.getFirstName() + " " + user.getLastName());

        ColoanaNume.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLastName()));
        ColoanaPrenume.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFirstName()));
        ColoanaData.setCellValueFactory(cellData -> {
            User friend = cellData.getValue();
            String dateString = service.get_friends_from(friend, user)
                    .map(date -> date.format(Constants.DATE_TIME_FORMATTER))
                    .orElse("Unknown");
            return new SimpleStringProperty(dateString);
        });
        tableMain.setItems(observableUsers);
        configureContextMenu();

    }


}
