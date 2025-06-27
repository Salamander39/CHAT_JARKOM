import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ConnectedUser {
    private Socket socket;
    private User user;
    private Room currentRoom;
    private Server server;
    private PrintWriter out;
    
    public ConnectedUser(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.user = null;
        this.currentRoom = null;
        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating output stream: " + e.getMessage());
        }
    }
    
    public void listenForInputs() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String input;
            while ((input = in.readLine()) != null) {
                handleInput(input);
            }
        } catch (IOException e) {
            System.err.println("User disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }
    
    private void handleInput(String input) {
        try {
            String[] args = input.split(" ");
            String command = args[0];
            
            switch (command) {
                case "/login":
                    handleLogin(args);
                    break;
                case "/register":
                    handleRegister(args);
                    break;
                case "/msg":
                    handleMessage(args);
                    break;
                case "/createroom":
                    handleCreateRoom(args);
                    break;
                case "/joinroom":
                    handleJoinRoom(args);
                    break;
                case "/listroom":
                    handleListRooms();
                    break;
                case "/leaveroom":
                    handleLeaveRoom();
                    break;
                case "/ban":
                    handleBanUser(args);
                    break;
                case "/kick":
                    handleKickUser(args);
                    break;
                default:
                    handleDefaultMessage(input);
            }
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleLogin(String[] args) throws Exception {
        if (args.length != 3) {
            throw new Exception("The command /login needs {username} and {password}");
        }
        
        if (user != null) {
            sendMessage("You are already logged in.");
            return;
        }
        
        try {
            ConnectedUser existingUser = server.findConnectedUser(args[1]);
            if (existingUser != null) {
                sendMessage("Someone is already logged in with that account");
                return;
            }
            
            user = server.loginUser(args[1], args[2]);
            server.addConnectedUser(this);
            sendMessage("You have logged in.");
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleRegister(String[] args) throws Exception {
        if (args.length != 3) {
            throw new Exception("The command /register needs {username} and {password}");
        }
        
        if (user != null) {
            sendMessage("You are already logged in.");
            return;
        }
        
        try {
            user = server.addUser(args[1], args[2]);
            server.addConnectedUser(this);
            sendMessage("You have logged in.");
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleMessage(String[] args) throws Exception {
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom == null) {
            throw new Exception("Enter room first.");
        }
        
        if (args.length < 2) {
            throw new Exception("Message cannot be empty");
        }
        
        String message = String.join(" ", args).substring(args[0].length()).trim();
        currentRoom.sendMessageToAllFromUser(message, user);
    }
    
    private void handleCreateRoom(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("The command /createroom needs exactly 1 argument following it");
        }
        
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom != null) {
            throw new Exception("You are in a room.");
        }
        
        try {
            currentRoom = server.addRoom(args[1], user);
            currentRoom.addUser(this);
            sendMessage("You have created room " + currentRoom.getName());
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleJoinRoom(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("The command /joinroom needs exactly 1 argument following it");
        }
        
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom != null) {
            throw new Exception("You are in a room.");
        }
        
        try {
            Room room = server.findRoomByName(args[1]);
            
            if (room.isUserBanned(user)) {
                throw new Exception("You are banned from this room.");
            }
            
            currentRoom = room;
            currentRoom.addUser(this);
            currentRoom.sendMessageToAll(user.getUsername() + " has joined the room");
            sendMessage("You have joined room " + currentRoom.getName());
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleListRooms() throws Exception {
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        try {
            List<String> rooms = server.getRoomList();
            sendMessage("Rooms: " + String.join(", ", rooms));
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleLeaveRoom() throws Exception {
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom == null) {
            throw new Exception("You are not in a room.");
        }
        
        try {
            currentRoom.kickUser(user);
            currentRoom.sendMessageToAll("User " + user.getUsername() + " has left the room.");
            sendMessage("You have left room " + currentRoom.getName());
            currentRoom = null;
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleBanUser(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("The command /ban needs exactly 1 argument following it");
        }
        
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom == null) {
            throw new Exception("You are not in a room.");
        }
        
        if (!currentRoom.getOwner().equals(user)) {
            throw new Exception("You are not the owner of the room.");
        }
        
        if (args[1].equals(user.getUsername())) {
            throw new Exception("You cannot ban yourself.");
        }
        
        try {
            User userToBan = server.findUser(args[1]);
            ConnectedUser connectedUser = server.findConnectedUser(args[1]);
            
            connectedUser.sendMessage("You have been banned from room " + currentRoom.getName());
            currentRoom.banUser(userToBan);
            currentRoom.sendMessageToAll("User " + userToBan.getUsername() + " has been banned from this room.");
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleKickUser(String[] args) throws Exception {
        if (args.length != 2) {
            throw new Exception("The command /kick needs exactly 1 argument following it");
        }
        
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom == null) {
            throw new Exception("You are not in a room.");
        }
        
        if (!currentRoom.getOwner().equals(user)) {
            throw new Exception("You are not the owner of the room.");
        }
        
        try {
            User userToKick = server.findUser(args[1]);
            ConnectedUser connectedUser = server.findConnectedUser(args[1]);
            
            connectedUser.sendMessage("You have been kicked from room " + currentRoom.getName());
            currentRoom.kickUser(userToKick);
            currentRoom.sendMessageToAll("User " + userToKick.getUsername() + " has been kicked from this room.");
        } catch (Exception e) {
            sendMessage("Error: " + e.getMessage());
        }
    }
    
    private void handleDefaultMessage(String input) throws Exception {
        if (user == null) {
            throw new Exception("You need to be logged in.");
        }
        
        if (currentRoom == null) {
            throw new Exception("Enter room first.");
        }
        
        currentRoom.sendMessageToAllFromUser(input, user);
    }
    
    public void sendMessage(String message) {
        out.println(message);
    }
    
    private void cleanup() {
        if (currentRoom != null && user != null) {
            try {
                currentRoom.sendMessageToAll("User " + user.getUsername() + " has left the room.");
                currentRoom.kickUser(user);
            } catch (Exception e) {
                System.err.println("Error during cleanup: " + e.getMessage());
            }
        }
        
        server.getConnectedUsers().remove(this);
        
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
    
    public User getUser() {
        return user;
    }
    
    public Room getCurrentRoom() {
        return currentRoom;
    }
    
    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }
}