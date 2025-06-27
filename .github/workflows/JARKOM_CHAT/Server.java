import java.util.ArrayList;
import java.util.List;

public class Server {
    private List<User> users = new ArrayList<>();
    private List<Room> rooms = new ArrayList<>();
    private List<ConnectedUser> connectedUsers = new ArrayList<>();
    
    public User addUser(String username, String password) throws Exception {
        if (checkUsernameExists(username)) {
            throw new Exception("Username already exists");
        }
        User newUser = new User(username, password);
        users.add(newUser);
        return newUser;
    }
    
    public User loginUser(String username, String password) throws Exception {
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return user;
            }
        }
        throw new Exception("Invalid username or password");
    }
    
    public boolean checkUsernameExists(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
    
    public Room addRoom(String name, User owner) throws Exception {
        if (checkRoomNameExists(name)) {
            throw new Exception("Room already exists");
        }
        Room newRoom = new Room(name, owner);
        rooms.add(newRoom);
        return newRoom;
    }
    
    public boolean checkRoomNameExists(String name) {
        for (Room room : rooms) {
            if (room.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getRoomList() throws Exception {
        if (rooms.isEmpty()) {
            throw new Exception("There are no rooms at the moment");
        }
        
        List<String> roomNames = new ArrayList<>();
        for (Room room : rooms) {
            roomNames.add(room.getName());
        }
        return roomNames;
    }
    
    public Room findRoomByName(String name) throws Exception {
        for (Room room : rooms) {
            if (room.getName().equals(name)) {
                return room;
            }
        }
        throw new Exception("Room not found");
    }
    
    public User findUser(String username) throws Exception {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        throw new Exception("User not found");
    }
    
    public ConnectedUser findConnectedUser(String username) throws Exception {
        for (ConnectedUser user : connectedUsers) {
            if (user.getUser() != null && user.getUser().getUsername().equals(username)) {
                return user;
            }
        }
        throw new Exception("User not found");
    }
    
    public void addConnectedUser(ConnectedUser user) {
        connectedUsers.add(user);
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public List<ConnectedUser> getConnectedUsers() {
        return connectedUsers;
    }
}