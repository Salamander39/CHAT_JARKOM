import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMain {
    private static Server server = new Server();
    
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8080); //TCP PORT
            System.out.println("### Server Started ###");
            
            // Start listening for client connections
            new Thread(() -> listenForConnections(serverSocket)).start();
            
            // Handle server commands
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            String command;
            while ((command = consoleReader.readLine()) != null) {
                if (!handleServerCommand(command)) {
                    break;
                }
            }
            
            serverSocket.close();
            System.out.println("### Server Stopped ###");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
    
    private static void listenForConnections(ServerSocket serverSocket) {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Someone Joined!");
                
                ConnectedUser connectedUser = new ConnectedUser(clientSocket, server);
                server.addConnectedUser(connectedUser);
                
                new Thread(connectedUser::listenForInputs).start();
            }
        } catch (IOException e) {
            System.err.println("Failed to accept connection: " + e.getMessage());
        }
    }
    
    private static boolean handleServerCommand(String command) {
        try {
            switch (command.trim()) {
                case "exit":
                    return false;
                case "listroom":
                    try {
                        List<String> rooms = server.getRoomList();
                        System.out.println("Rooms: \n" + String.join(", ", rooms));
                    } catch (Exception e) {
                        System.out.println("Error listing rooms: " + e.getMessage());
                    }
                    break;
                case "listcuruser":
                    System.out.println("Currently Active Users: \n");
                    for (ConnectedUser user : server.getConnectedUsers()) {
                        String roomName = user.getCurrentRoom() != null ? user.getCurrentRoom().getName() : "No room";
                        System.out.printf("%s (%s)\n", user.getUser().getUsername(), roomName);
                    }
                    break;
                case "listuser":
                    System.out.println("Users: \n");
                    for (User user : server.getUsers()) {
                        System.out.printf("%s\n", user.getUsername());
                    }
                    break;
                default:
                    System.out.printf("%s not found\n", command);
            }
            return true;
        } catch (Exception e) {
            System.out.println("Error handling command: " + e.getMessage());
            return true;
        }
    }
}