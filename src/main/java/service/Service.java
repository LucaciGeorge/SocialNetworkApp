package service;


import domain.Friendship;
import domain.Message;
import domain.User;
import domain.validators.ServiceException;
import domain.validators.ValidationException;
import repository.FriendshipPageRepository;
import repository.PagingRepository;
import repository.Repository;
import utils.Pageable.Pageable;
import utils.Pageable.Page;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static utils.Hasher.hashPassword;

public class Service {
    private Repository<Long, User> utilizatorRepository;
    private FriendshipPageRepository<Long, Friendship> prietenieRepository;
    private Long lastUserId = 0L;
    private Long lastFriendshipId = 0L;


    private final Repository<Long, Message> messageRepository;


    public Service(Repository<Long, User> utilizatorRepo, FriendshipPageRepository<Long, Friendship> prietenieRepo, Repository<Long, Message> messageRepository) {
        this.utilizatorRepository = utilizatorRepo;
        this.prietenieRepository = prietenieRepo;
        this.messageRepository = messageRepository;
        initializeLastIds();
    }

    public ArrayList<Message> getMessages(Long id1, Long id2) {
       // Optional<User> user1 = utilizatorRepository.findOne(id1);
       // Optional<User> user2 = utilizatorRepository.findOne(id2);

        Collection<Message> messages = (Collection<Message>) messageRepository.findAll();

        return messages.stream()
                .filter(msg -> ((msg.getSender().getId().equals(id1)) && msg.getReceivers().getId().equals(id2)) ||
                        (msg.getSender().getId().equals(id2) && msg.getReceivers().getId().equals(id1)))
                .sorted(Comparator.comparing(Message::getDate))
                .collect(Collectors.toCollection(ArrayList::new));


    }


    public Optional<LocalDateTime> get_friends_from(User user1, User user2) {
        Friendship friendship = findFriendship(user1.getId(), user2.getId());
        return Optional.ofNullable(friendship != null ? friendship.getFriendsFrom() : null);
    }



    public boolean addMessage(Long id_from, Long id_to, String message) {
        try {
            Optional<User> from = utilizatorRepository.findOne(id_from);
            Optional<User> to = utilizatorRepository.findOne(id_to);

            if (from.isPresent() && to.isPresent()) {
                if (message == null || message.trim().isEmpty()) {
                    throw new ServiceException("Mesajul nu poate fi gol");
                }

                Message msg = new Message(from.get(), to.get(), message);
                messageRepository.save(msg);

                List<Message> messagesBetweenUsers = getMessages(id_to, id_from);
                if (messagesBetweenUsers.size() > 1) {
                    Message oldReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 2);
                    Message newReplyMessage = messagesBetweenUsers.get(messagesBetweenUsers.size() - 1);
                    oldReplyMessage.setReply(newReplyMessage);
                    messageRepository.update(oldReplyMessage);
                }
                else if (messagesBetweenUsers.size() == 1) {
                    Message newReplyMessage = messagesBetweenUsers.get(0);
                    newReplyMessage.setReply(newReplyMessage);
                    messageRepository.update(newReplyMessage);
                }

                return true;
            } else {
                throw new ServiceException("Unul sau ambii utilizatori nu au fost găsiți");
            }
        } catch (ServiceException se) {
            System.out.println("Eroare utilizator: " + se.getMessage());
        } catch (Exception ex) {
            System.out.println("Eroare creare mesaj: " + ex.getMessage());
        }
        return false;
    }

    private void initializeLastIds() {
        for (User utilizator : utilizatorRepository.findAll()) {
            if (utilizator.getId() > lastUserId) {
                lastUserId = utilizator.getId();
            }
        }
        for (Friendship prietenie : prietenieRepository.findAll()) {
            if (prietenie.getId() > lastFriendshipId) {
                lastFriendshipId = prietenie.getId();
            }
        }
    }

    public User findUser(Long userId) {
        return utilizatorRepository.findOne(userId).orElse(null);
    }



    public Friendship findFriendship(Long userId, Long user2Id) {
        for (Friendship prietenie : prietenieRepository.findAll()) {
            if((prietenie.getFirstUserId() == user2Id && prietenie.getSecondUserId() == userId)||
              (prietenie.getFirstUserId() == userId && prietenie.getSecondUserId() == user2Id)){
                return prietenie;
            }
        }
        return null;
    }

    public Friendship removePrietenie(Long friendshipId) {
        Friendship friendship = prietenieRepository.findOne(friendshipId).orElseThrow(()-> new ServiceException("Prietenia nu exista!"));
        Optional<User> u1 = utilizatorRepository.findOne(friendship.getFirstUserId());
        Optional<User> u2 = utilizatorRepository.findOne(friendship.getSecondUserId());
        u1.ifPresent(user1 -> u2.ifPresent(user2 -> {
            user1.removeFriend(user2);
            user2.removeFriend(user1);
        }));
        prietenieRepository.delete(friendshipId);
        return friendship;
    }

    public void set_password(Long id, String password){
        for(User u : utilizatorRepository.findAll()){
            u.setPassword(hashPassword(password));
            utilizatorRepository.update(u);
        }
    }


    public User LogIn(String username,  String password) {
        for(User user : utilizatorRepository.findAll()) {
            if(user.getUsername().equals(username) && user.getPassword().equals(password)){
                return user;
            }

        }
        throw new ServiceException("Credidentiale invalide!");
    }


    private boolean user_already_exists(User user) {
        for(User u : utilizatorRepository.findAll()){
            if(u.equals(user)){
                return true;
            }
        }
        return false;
    }

    public User register_user(String firstName, String lastName, String username, String password, byte[] profile_pic) {
        User user = new User(firstName, lastName, username,  hashPassword(password), profile_pic, null);

        if(user_already_exists(user)){
            throw new ServiceException("User already exists!");
        }

        user.setId(++lastUserId);
        utilizatorRepository.save(user);
        return user;
    }


    public void changeFriendStatus(Long userid1, Long userid2, Long status, LocalDateTime friendsFrom) {
        Friendship f = new Friendship(userid1, userid2, status,friendsFrom);
       prietenieRepository.update(f);
    }

/*
    public User findUser(String nume, String prenume, String password){
        for(User user: utilizatorRepository.findAll()) {
            if(user.getFirstName() == nume && user.getLastName() == prenume) {
                return user;
            }
        }
        return null;
    }
*/
    public Page<Friendship> getUsersFriendsDTOOnPage(Pageable pageable, Long id) {
        return prietenieRepository.findAllOnPage(pageable, id);
    }

    public Page<Friendship> getUsersFriendsRequestsDTOOnPage(Pageable pageable, Long id){
        return prietenieRepository.findAllOnPageRequests(pageable, id);
    }

    public Iterable<User> getReceiveReq(Long userId){
        List<User> friend_requests = new ArrayList<>();
        for(Friendship prietenie: prietenieRepository.findAll()){
            if(prietenie.getStatus() == 1){
                if(prietenie.getSecondUserId().equals(userId)){
                    utilizatorRepository.findOne(prietenie.getFirstUserId()).ifPresent(friend_requests::add);
                }
            }
        }
        return friend_requests;
    }

    public Map<User, LocalDateTime> getFriendshipsWithLocalDateTime(Long userId) {
        Map<User, LocalDateTime> friends = new HashMap<>();
        for (Friendship prietenie : prietenieRepository.findAll()) {
            if (prietenie.getStatus() == 3) {
                User u = null;
                if (prietenie.getSecondUserId().equals(userId)) {
                    u = utilizatorRepository.findOne(prietenie.getFirstUserId()).orElse(null);
                } else if(prietenie.getFirstUserId().equals(userId)) {
                    u = utilizatorRepository.findOne(prietenie.getSecondUserId()).orElse(null);
                }
                if(u != null) {
                    friends.put(u, prietenie.getFriendsFrom());
                }
            }
        }
        return friends;
    }


    public List<User> getFriendsSuggestion(Long userId1){
        List<User> user_friends = this.getFriends(userId1);

        Iterable<User> iterableUsers = this.getUtilizatori();
        List<User> allUsers = StreamSupport.stream(iterableUsers.spliterator(), false)
                .collect(Collectors.toList());

        List<User> diferenta = allUsers.stream()
                .filter(element -> !user_friends.contains(element))
                .collect(Collectors.toList());

        List<User> toReturn = new ArrayList<>();

        Random random = new Random();

        for(User usr: diferenta){
            int select = random.nextInt(2);

            if(select == 1){
                toReturn.add(usr);
            }
        }
        return toReturn;
    }

    public List<User> getCommonFriends(Long userId1, Long userId2) {
        List<User> firends_user1 = new ArrayList<>();
        List<User> friends_user2 = new ArrayList<>();

        firends_user1 = this.getFriends(userId1);
        friends_user2 = this.getFriends(userId2);

        List<User> common_friends = firends_user1.stream().filter(friends_user2::contains).collect(Collectors.toList());
        return common_friends;

    }

    public List<Friendship> getFriendships(Long userId) {
        List<Friendship> friendships = new ArrayList<>();
        for (Friendship prietenie : prietenieRepository.findAll()) {
            if(prietenie.getFirstUserId().equals(userId) && prietenie.getStatus() == 3){
                friendships.add(prietenie);
            }
            else if(prietenie.getSecondUserId().equals(userId) && prietenie.getStatus() == 3){
                friendships.add(prietenie);

            }
        }
        return friendships;
    }

    public List<User> getFriends(Long userId) {
        List<User> friends = new ArrayList<>();
        for (Friendship prietenie : prietenieRepository.findAll()) {
            if(prietenie.getStatus() == 3) {
                if (prietenie.getFirstUserId().equals(userId)) {
                    utilizatorRepository.findOne(prietenie.getSecondUserId())
                            .ifPresent(friends::add);
                } else if (prietenie.getSecondUserId().equals(userId)) {
                    utilizatorRepository.findOne(prietenie.getFirstUserId())
                            .ifPresent(friends::add);
                }
            }
        }
        return friends;
    }

    public User removeUtilizator(Long userId) {
        Optional<User> userOpt = utilizatorRepository.findOne(userId);
        if(!userOpt.isPresent()){
            throw new ValidationException("No user");
        }

        User user = userOpt.get();
        try{
            List<Friendship> prietenii = new ArrayList<Friendship>();
            prietenieRepository.findAll().forEach(prietenii::add);
            prietenii.forEach(friendship -> {
                if(friendship.getFirstUserId().equals(userId) || friendship.getSecondUserId().equals(userId)){
                    removePrietenie(friendship.getId());
                }
            });
            utilizatorRepository.delete(userId);
        }catch( Exception e){
            e.printStackTrace();
            return null;
        }
        return user;
    }

    public Friendship addPrietenie(Long userId1, Long userId2, Long status, LocalDateTime friendsFrom) {
        User u1 = utilizatorRepository.findOne(userId1).orElseThrow(() -> new ValidationException("No user"));
        User u2 = utilizatorRepository.findOne(userId2).orElseThrow(() -> new ValidationException("No user"));
        Friendship friendship = new Friendship(userId1, userId2, status, friendsFrom);
        friendship.setId(++lastFriendshipId);
        prietenieRepository.findAll().forEach(prietenie -> {
            if ((prietenie.getFirstUserId().equals(userId1) && prietenie.getSecondUserId().equals(userId2)) ||
                    (prietenie.getFirstUserId().equals(userId2) && prietenie.getSecondUserId().equals(userId1))) {
                throw new ServiceException("Prietenia exista deja!");
            }
        });
        prietenieRepository.save(friendship);
        return friendship;
    }



    public User update_user_profile(Long userId,  byte[] profile_picture, String bio){
        User u = utilizatorRepository.findOne(userId).orElse(null);
        if(u != null){
            u.setPicture(profile_picture);
            u.setBio(bio);
            utilizatorRepository.update(u);
        }
        return u;
    }


    public Iterable<User> getUtilizatori() {
        return utilizatorRepository.findAll();
    }

    public Iterable<Friendship> getPrietenii() {
        return prietenieRepository.findAll();
    }
}
