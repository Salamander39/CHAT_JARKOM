import java.util.ArrayList;
import java.util.List;

public class Room {
    private String name;
    private User owner;
    private List<ConnectedUser> users;
    private List<User> bannedUsers;
    
    public Room(String name, User owner) {
        this.name = name;
        this.owner = owner;
        this.users = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
    }
    
    public void banUser(User user) throws Exception {
        if (isUserBanned(user)) {
            throw new Exception("User is already banned");
        }
        
        bannedUsers.add(user);
        kickUser(user);
    }
    
    public void kickUser(User user) throws Exception {
        ConnectedUser userToKick = null;
        for (ConnectedUser connectedUser : users) {
            if (connectedUser.getUser().equals(user)) {
                userToKick = connectedUser;
                break;
            }
        }
        
        if (userToKick == null) {
            throw new Exception("User is not in this room");
        }
        
        userToKick.setCurrentRoom(null);
        users.remove(userToKick);
    }
    
    public void sendMessageToAll(String message) {
        for (ConnectedUser user : users) {
            user.sendMessage(message);
        }
    }
    
    public void sendMessageToAllFromUser(String message, User sender) {
        for (ConnectedUser user : users) {
            if (!user.getUser().equals(sender)) {
                user.sendMessage(sender.getUsername() + ": " + message);
            }
        }
    }
    
    public boolean isUserBanned(User user) {
        for (User bannedUser : bannedUsers) {
            if (bannedUser.equals(user)) {
                return true;
            }
        }
        return false;
    }
    
    public void addUser(ConnectedUser user) {
        users.add(user);
        user.setCurrentRoom(this);
    }
    
    public String getName() {
        return name;
    }
    
    public User getOwner() {
        return owner;
    }
    
    public List<ConnectedUser> getUsers() {
        return users;
    }
}