package domain;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User extends Entity<Long> {
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private List<User> friends;
    private byte[] picture;
    private String bio;

    public User(String firstName, String lastName, String username, String password, byte[] picture, String bio) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.picture = picture;
        this.bio = bio;
        this.friends = new ArrayList<>();
    }



    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio(){
        return bio;
    }

    public String getPassword(){
        return password;
    }

    public byte[] getPicture(){
        return picture;
    }

    public void setPicture(byte[] picture){
        this.picture = picture;
    }

    public void setBio(String bio){
        this.bio = bio;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getFirstName() {

        return firstName;
    }

    public void setFirstName(String firstName) {

        this.firstName = firstName;
    }

    public String getLastName() {

        return lastName;
    }

    public void setLastName(String lastName) {

        this.lastName = lastName;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public List<User> getFriends() {

        return friends;
    }

    public void addFriend(User friend) {
        this.friends.add(friend);
    }

    public void removeFriend(User friend) {
        this.friends.remove(friend);
    }

    @Override
    public String toString() {
        return "Utilizator{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, password);
    }
}
