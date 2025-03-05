    package controller;

    import domain.Friendship;
    import domain.Message;
    import domain.User;
    import javafx.beans.property.SimpleStringProperty;
    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.collections.ObservableMap;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Pos;
    import javafx.scene.Scene;
    import javafx.scene.control.*;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.HBox;
    import javafx.stage.Modality;
    import javafx.stage.Stage;
    import service.Service;
    import utils.Constants;
    import utils.Pageable.Page;
    import utils.Pageable.Pageable;

    import java.io.IOException;
    import java.time.LocalDateTime;
    import java.util.*;
    import java.util.stream.StreamSupport;

    public class UtilizatorController {


        public Button prevButton;
        public Button nextButton;
        public Label pageNumber;

        @FXML
        private AnchorPane mainAnchorPane;

        @FXML
        private Button SendMessageButton;

        @FXML
        private Button firstPageButton;
        @FXML
        private Button lastPageButton;

        @FXML
        private TextField MessageText;
        @FXML
        private ListView listMessages;

        ObservableList<Message> messagesModel = FXCollections.observableArrayList();

        //variabile care imi zice una din cele 3 stari: prieteni, friendrequest, sugestii
        private Integer state = 0;

        private Service service;


        private ObservableList<User> observableFriends = FXCollections.observableArrayList();








        @FXML
        private TableView<User> TablePrieteni;

        @FXML
        private Label welcomeLabel;

        @FXML
        private TableColumn<User, String> ColoanaNume;

        @FXML
        private TableColumn<User, String> ColoanaPrenume;

        @FXML
        private  TableColumn<User, String> ColoanaFriendsFrom;




        private User user;
        private int currentPage = 0;
        private int pageSize = 2;




        public void setService(Service service) {
            this.service = service;
        }

        public UtilizatorController() {
        }

        public void initialize() {
            ColoanaNume.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getFirstName())
            );
            ColoanaPrenume.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getLastName())
            );
            ColoanaFriendsFrom.setCellValueFactory(cellData -> {
                User friend = cellData.getValue();
                String dateString = service.get_friends_from(friend, user)
                        .map(date -> date.format(Constants.DATE_TIME_FORMATTER))
                        .orElse("Unknown");
                return new SimpleStringProperty(dateString);
            });


            TablePrieteni.setItems(observableFriends);
            TablePrieteni.setPlaceholder(new Label("Nu există date disponibile"));

            TablePrieteni.getSelectionModel().selectedItemProperty().addListener((obs,oldSelection,newSelection) -> {
                if(newSelection != null){
                    handle_select_friend(newSelection);
                }
            });

            configureContextMenu();

        }

        public void load_list_message(Long id1, Long id2) {
            listMessages.getItems().clear();
            messagesModel.clear();
            messagesModel.addAll(service.getMessages(id1, id2));

            listMessages.setItems(messagesModel);

            listMessages.setCellFactory(param -> new ListCell<Message>() {
                @Override
                protected void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || msg == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        HBox hbox = new HBox();
                        Label messageLabel = new Label();
                        messageLabel.setText(msg.getText() + " at: " + msg.getDate().format(Constants.DATE_TIME_FORMATTER));

                        if (Objects.equals(msg.getSender().getId(), user.getId())) {
                            hbox.setAlignment(Pos.CENTER_RIGHT);
                            messageLabel.getStyleClass().add("sent-message");
                        } else {
                            hbox.setAlignment(Pos.CENTER_LEFT);
                            messageLabel.getStyleClass().add("received-message");
                        }

                        hbox.getChildren().add(messageLabel);
                        setGraphic(hbox);
                    }
                }
            });
        }

        private void handle_select_friend(User selected_user) {
            load_list_message(user.getId(), selected_user.getId());
        }

        private void configureContextMenu() {
            ContextMenu contextMenu = new ContextMenu();

            MenuItem viewProfile = new MenuItem("Vezi Profil");
            viewProfile.setOnAction(e -> {
                User selectedUser = TablePrieteni.getSelectionModel().getSelectedItem();
                if (selectedUser != null) {
                    openUserProfile(selectedUser);
                }
            });

            contextMenu.getItems().add(viewProfile);

            TablePrieteni.setRowFactory(tv -> {
                TableRow<User> row = new TableRow<>();
                row.setOnContextMenuRequested(event -> {
                    if (!row.isEmpty()) {
                        TablePrieteni.getSelectionModel().select(row.getItem());
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


        public void setUserInfo(User user) {
            this.user = user;
            if(user.getFriends() == null){
                user.setFriends(new ArrayList<User>());
            }
            System.out.println(user.getId());
            welcomeLabel.setText("@" + user.getUsername());


            for(User friend : service.getFriends(user.getId())){
                user.addFriend(friend);
            }





            loadData();
            initModelFriendsForPage();
        }


        private void initModelFriendsForPage() {
            Page<Friendship> page = service.getUsersFriendsDTOOnPage(new Pageable(currentPage, pageSize), user.getId());

            if (page == null || page.getElementsOnPage() == null) {
                System.out.println("Pagina este null!");
                return;
            }


            Iterable<Friendship> aux = page.getElementsOnPage();

            List<User> friend = StreamSupport.stream(aux.spliterator(), false).map(friendship -> {
                User auxUser = service.findUser(friendship.getSecondUserId());
                return auxUser;
            }).collect(java.util.stream.Collectors.toList());
            observableFriends.setAll(friend);
            prevButton.setDisable(currentPage == 0);
            int noOfPages = (int)(Math.ceil((double) service.getFriends(user.getId()).size() / pageSize));
            if(noOfPages == 0)
                currentPage = -1;
            nextButton.setDisable(currentPage + 1 == noOfPages);
            pageNumber.setText((currentPage + 1) + " / " + noOfPages);
        }




        private void initModelFriendReqForPage(){

            Page<Friendship> page = service.getUsersFriendsRequestsDTOOnPage(new Pageable(currentPage, pageSize), user.getId());

            if (page == null || page.getElementsOnPage() == null) {
                System.out.println("Pagina este null!");
                return;
            }


            Iterable<Friendship> aux = page.getElementsOnPage();

            List<User> friend = StreamSupport.stream(aux.spliterator(), false).map(friendship -> {
                User auxUser = service.findUser(friendship.getFirstUserId());
                return auxUser;
            }).collect(java.util.stream.Collectors.toList());
            observableFriends.setAll(friend);
            prevButton.setDisable(currentPage == 0);
            int noOfPages = (int)(Math.ceil((double) StreamSupport.stream(
                    service.getReceiveReq(user.getId()).spliterator(), false).count() / pageSize));
            if(noOfPages == 0){
                currentPage = -1;
            }
            nextButton.setDisable(currentPage + 1 == noOfPages);
            pageNumber.setText((currentPage + 1) + " / " + noOfPages);
        }

        private void initModelFindPeople(){
            Iterable<User> allUsers = service.getUtilizatori();

            List<User> non_friend_users = new ArrayList<>();
            for(User u : allUsers){
                Friendship f = service.findFriendship(u.getId(), user.getId());
                if(f == null && u.getId() != user.getId()){
                    non_friend_users.add(u);
                }
            }
            observableFriends.setAll(non_friend_users);
        }


        @FXML
        private void onPreviousPage(ActionEvent actionEvent) {
            currentPage--;
            if(state == 0)
                initModelFriendsForPage();
            else
                initModelFriendReqForPage();
            if(firstPageButton.isDisabled())
                firstPageButton.setDisable(false);
            if(lastPageButton.isDisabled())
                lastPageButton.setDisable(false);
        }

        @FXML
        private void onNextPage(ActionEvent actionEvent) {
            currentPage++;
            if(state == 0)
                initModelFriendsForPage();
            else
                initModelFriendReqForPage();
            if(firstPageButton.isDisabled())
                firstPageButton.setDisable(false);
            if(lastPageButton.isDisabled())
                lastPageButton.setDisable(false);
        }


        @FXML
        private void onFirstPage(ActionEvent actionEvent) {
            currentPage=0;
            if(state == 0)
                initModelFriendsForPage();
            else
                initModelFriendReqForPage();
            firstPageButton.setDisable(true);
            lastPageButton.setDisable(false);
        }

        @FXML
        private void onLastPage(ActionEvent actionEvent) {
            int noOfPages;
            if(state == 0)
                noOfPages = (int)(Math.ceil((double) service.getFriends(user.getId()).size() / pageSize));
            else{

                noOfPages = (int)(Math.ceil((double) StreamSupport.stream(service.getReceiveReq(user.getId()).spliterator(), false).count() / pageSize));
            }
            currentPage= noOfPages-1;
            if(state == 0)
                initModelFriendsForPage();
            else
                initModelFriendReqForPage();
            lastPageButton.setDisable(true);
            firstPageButton.setDisable(false);


        }

        private void loadData(){
            observableFriends.clear();
            TablePrieteni.setItems(observableFriends);
        }




        @FXML
        private void handleMyProfile(ActionEvent actionEvent) {
            try{
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/views/profile-view.fxml"));
                AnchorPane root = loader.load();
                Stage stage = new Stage();
                stage.initModality(Modality.WINDOW_MODAL);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
                ProfileViewController controller = loader.getController();
                controller.setService(service,user, user);
            }
            catch(IOException e){
                throw new RuntimeException(e);
            }
        }

        @FXML
        private void handleSendMessage(ActionEvent actionEvent) {
            User selectedFriend = TablePrieteni.getSelectionModel().getSelectedItem();
            if (selectedFriend != null) {
                Long idFrom = user.getId();
                Long idTo = selectedFriend.getId();

                String msg = MessageText.getText();

                service.addMessage(idFrom, idTo, msg);
                load_list_message(idFrom, idTo);

                MessageText.clear();
                TablePrieteni.getSelectionModel().select(selectedFriend);
            } else {
                Dialog dialog = new Dialog();
                dialog.setContentText("You must select a friend to send a message!");
                ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().add(okButton);
                dialog.showAndWait();
            }
        }



        private void create_add_friend_button(){
            Button addFriendButton = new Button("Add");

            addFriendButton.setLayoutX(TablePrieteni.getLayoutX() + TablePrieteni.getWidth() + 20);
            addFriendButton.setLayoutY(TablePrieteni.getLayoutY());

            addFriendButton.setOnAction(e -> handleAddFriend());

            mainAnchorPane.getChildren().addAll(addFriendButton);
            addFriendButton.getStyleClass().add("accept-button");
        }


        private void destroy_add_friend_button(){
            mainAnchorPane.getChildren().removeIf(node -> node instanceof Button &&
                    ("Add".equals(((Button) node).getText())));
        }

        private void handleAddFriend() {
            User selectedUser = TablePrieteni.getSelectionModel().getSelectedItem();
            if(selectedUser != null){
                service.addPrietenie(user.getId(), selectedUser.getId(), 1L, LocalDateTime.now());
                initModelFindPeople();
            }
            else{
                System.out.println("No user selected!");
            }
        }


        private void create_accept_reject_buttons(){
            Button acceptButton = new Button("Accept");
            Button rejectButton = new Button("Reject");

            acceptButton.setLayoutX(TablePrieteni.getLayoutX() + TablePrieteni.getWidth() + 20);
            acceptButton.setLayoutY(TablePrieteni.getLayoutY());

            rejectButton.setLayoutX(TablePrieteni.getLayoutX() + TablePrieteni.getWidth() + 20);
            rejectButton.setLayoutY(TablePrieteni.getLayoutY() + 40);

            acceptButton.setOnAction(e -> handleAcceptRequest());
            rejectButton.setOnAction(e -> handleRejectRequest());

            mainAnchorPane.getChildren().addAll(acceptButton, rejectButton);
            acceptButton.getStyleClass().add("accept-button");
            rejectButton.getStyleClass().add("reject-button");


        }

        private void destroy_accept_reject_buttons(){
            mainAnchorPane.getChildren().removeIf(node -> node instanceof Button &&
                    ("Accept".equals(((Button) node).getText()) || "Reject".equals(((Button) node).getText())));
        }

        private void handleRejectRequest() {
            User selectedUser = TablePrieteni.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                Friendship f = service.findFriendship(user.getId(), selectedUser.getId());
                service.removePrietenie(f.getId());
                initModelFriendReqForPage();

            } else {
                System.out.println("Trebuie să selectezi un utilizator!");
            }
        }

        private void handleAcceptRequest() {
            User selectedUser = TablePrieteni.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                service.changeFriendStatus(selectedUser.getId(), user.getId(),3L, LocalDateTime.now());
                initModelFriendReqForPage();

            } else {
                System.out.println("Trebuie să selectezi un utilizator!");
            }
        }


        private void disable_prev_next_buttons(){
            prevButton.setVisible(false);
            nextButton.setVisible(false);
            firstPageButton.setVisible(false);
            lastPageButton.setVisible(false);
            pageNumber.setVisible(false);
        }

        private void enable_prev_next_buttons(){
            prevButton.setVisible(true);
            nextButton.setVisible(true);
            firstPageButton.setVisible(true);
            lastPageButton.setVisible(true);
            pageNumber.setVisible(true);
        }

        @FXML
        private void handleFriendReqShow(ActionEvent actionEvent) {
            if(state != 1) {
                listMessages.setVisible(false);
                MessageText.setVisible(false);
                SendMessageButton.setVisible(false);
                ColoanaFriendsFrom.setText("Cerere primita la data de");
                if(state != 2) {
                    TablePrieteni.setLayoutX(TablePrieteni.getLayoutX() - 150);
                    prevButton.setLayoutX(prevButton.getLayoutX() - 150);
                    nextButton.setLayoutX(nextButton.getLayoutX() - 150);
                    firstPageButton.setLayoutX(firstPageButton.getLayoutX() - 150);
                    lastPageButton.setLayoutX(lastPageButton.getLayoutX() - 150);
                    pageNumber.setLayoutX(pageNumber.getLayoutX() - 150);
                }

                state = 1;
                currentPage = 0;
                initModelFriendReqForPage();
                create_accept_reject_buttons();
                enable_prev_next_buttons();
                destroy_add_friend_button();
            }

        }

        @FXML
        private void handleFriendShow(ActionEvent actionEvent) {

            if(state != 0) {
                listMessages.setVisible(true);
                MessageText.setVisible(true);
                SendMessageButton.setVisible(true);
                ColoanaFriendsFrom.setText("Prieteni de la data de");
                TablePrieteni.setLayoutX(TablePrieteni.getLayoutX() + 150);
                prevButton.setLayoutX(prevButton.getLayoutX() + 150);
                nextButton.setLayoutX(nextButton.getLayoutX() + 150);
                firstPageButton.setLayoutX(firstPageButton.getLayoutX() + 150);
                lastPageButton.setLayoutX(lastPageButton.getLayoutX() + 150);
                pageNumber.setLayoutX(pageNumber.getLayoutX() + 150);
                state = 0;
                currentPage = 0;
                initModelFriendsForPage();
                destroy_accept_reject_buttons();
                enable_prev_next_buttons();
                destroy_add_friend_button();

            }

        }

        @FXML
        private void handleFindPeopleShow(ActionEvent actionEvent) {


            if(state != 2){
                listMessages.setVisible(false);
                MessageText.setVisible(false);
                SendMessageButton.setVisible(false);
                if(state != 1) {
                    TablePrieteni.setLayoutX(TablePrieteni.getLayoutX() - 150);
                   // ColoanaFriendsFrom.setVisible(false);
                    prevButton.setLayoutX(prevButton.getLayoutX() - 150);
                    nextButton.setLayoutX(nextButton.getLayoutX() - 150);
                    firstPageButton.setLayoutX(firstPageButton.getLayoutX() - 150);
                    lastPageButton.setLayoutX(lastPageButton.getLayoutX() - 150);
                    pageNumber.setLayoutX(pageNumber.getLayoutX() - 150);
                }
                initModelFindPeople();
                destroy_accept_reject_buttons();
                disable_prev_next_buttons();
                create_add_friend_button();


                state = 2;
            }

        }
    }
