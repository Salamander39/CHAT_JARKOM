import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 8080); //TCP CONNECTIOn
            System.out.println("Connected to server!");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            
            // Start a thread to listen for messages from server
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server");
                }
            }).start();
            
            printHelp();
            
            // Read user input and send to server
            String input;
            while ((input = consoleReader.readLine()) != null) {
                if (input.equals("/logout")) {
                    System.exit(0);
                } else if (input.equals("/help")) {
                    printHelp();
                    continue;
                }
                
                if (!checkValidity(input)) {
                    System.err.println("Invalid command format");
                    continue;
                }
                
                out.println(input);
            }
        } catch (IOException e) {
            System.err.println("Cannot connect to server: " + e.getMessage());
        }
    }
    
    private static boolean checkValidity(String input) {
        String[] args = input.split(" ");
        String cmd = args[0];
        
        switch (cmd) {
            case "/login":
            case "/register":
                return args.length == 3;
            case "/createroom":
            case "/joinroom":
            case "/ban":
            case "/kick":
                return args.length == 2;
            default:
                return true;
        }
    }
    
    private static void printHelp() {
    System.out.println("type");
    System.out.println(" - /register {username} {password} or");
    System.out.println(" - /login {username} {password}");
    System.out.println("to start using chat.");
    System.out.println();
    System.out.println("after logging in, use commands");
    System.out.println("/createroom {roomName} : Create a chat room.");
    System.out.println("/joinroom {roomName}   : Join existing room.");
    System.out.println("/leaveroom             : Leave current room.");
    System.out.println("/listroom              : List all available rooms");
    System.out.println("/groupinfo             : Show current room details");
    System.out.println("/logout                : Close client side application.");
    System.out.println();
    System.out.println("[Messaging]");
    System.out.println("/msg {message}         : Send message to room");
    System.out.println("(or type directly)     : Send message without /msg");
    System.out.println();
    System.out.println("[Room Owner Commands]");
    System.out.println("/ban {username}        : Ban user from room");
    System.out.println("/kick {username}       : Kick user from room");
    System.out.println();
    System.out.println("/help                  : Show this help message");
    }
}