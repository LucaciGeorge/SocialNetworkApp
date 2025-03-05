package service;


import domain.Friendship;
import domain.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkService {
    Service service;
    HashMap<Long, List<Long>> adjList;

    public NetworkService(Service service) {
        this.service = service;
    }

    void DFS(Long v, HashMap<Long, Boolean> visited) {
        visited.put(v, true);
        System.out.println(v + " " + this.service.findUser(v).getFirstName() + " " + this.service.findUser(v).getLastName());
        if (adjList.containsKey(v)) {
            for (Long x : adjList.get(v)) {
                if (!visited.containsKey(x))
                    DFS(x, visited);
            }
        }
    }

    public int connectedCommunities() {
        // creates an adjency list of user and its friends
        adjList = new HashMap<Long, List<Long>>();
        for (User user : service.getUtilizatori()) {
            List<Long> friends = new ArrayList<>();
            for (Friendship friendship : service.getPrietenii()) {
                if (friendship.getFirstUserId().equals(user.getId()))
                    friends.add(friendship.getSecondUserId());
                if (friendship.getSecondUserId().equals(user.getId()))
                    friends.add(friendship.getFirstUserId());
            }
            if (!friends.isEmpty())
                this.adjList.put(user.getId(), friends);
        }

        // list of ids of users
        List<Long> ids = new ArrayList<>();
        for (User user : service.getUtilizatori())
            ids.add(user.getId());

        int nrOfCommunities = 0;
        HashMap<Long, Boolean> visited = new HashMap<Long, Boolean>();
        for (Long v : ids) {
            if (!visited.containsKey(v)) {
                DFS(v, visited);
                nrOfCommunities++;
                System.out.println();
            }
        }
        return nrOfCommunities + 1;
    }

    public List<Long> mostSocialCommunity() {
        // creates an adjency list of user and its friends
        adjList = new HashMap<Long, List<Long>>();
        List<Long> max = new ArrayList<>();
        for (User user : service.getUtilizatori()) {
            List<Long> friends = new ArrayList<>();
            for (Friendship friendship : service.getPrietenii()) {
                if (friendship.getFirstUserId().equals(user.getId()))
                    friends.add(friendship.getSecondUserId());
                if (friendship.getSecondUserId().equals(user.getId()))
                    friends.add(friendship.getFirstUserId());
            }
            if (!friends.isEmpty()) {
                this.adjList.put(user.getId(), friends);
                if (max.size() < friends.size() + 1) {
                    max = friends;
                    max.add(user.getId());
                }
            }
        }
//        System.out.println(adjList.values());
//        System.out.println(max);
        return max;
    }

}
