package domain;


import java.time.LocalDateTime;

public class Friendship extends Entity<Long> {
    private Long firstUserId;
    private Long secondUserId;
    private Long status;
    LocalDateTime friendsFrom;




    public Friendship(Long firstUserId, Long secondUserId, Long status, LocalDateTime friendsFrom) {
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
        this.status = status;
        this.friendsFrom = friendsFrom;
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    public Long getStatus(){
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getFirstUserId() {
        return firstUserId;
    }

    public void setFirstUserId(Long firstUserId) {
        this.firstUserId = firstUserId;
    }

    public Long getSecondUserId() {
        return secondUserId;
    }

    public void setSecondUserId(Long secondUserId) {
        this.secondUserId = secondUserId;
    }

    @Override
    public String toString() {
        return "Prietenie{" +
                "firstUserId=" + firstUserId +
                ", secondUserId=" + secondUserId +
                '}';
    }
}
