import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Server not up yet");
            return true;
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {

            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            sOutput.writeObject(msg);
            //System.out.println("msg sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults

        // Create your client and start it
        ChatClient client = null;

        if (args.length == 0)
            client = new ChatClient("localHost", 1500, "Anonymous");
        if (args.length == 1)
            client = new ChatClient("localHost", 1500, args[0]);
        if (args.length == 2)
            client = new ChatClient("localHost", Integer.parseInt(args[1]), args[0]);
        if (args.length == 3)
            client = new ChatClient(args[2], Integer.parseInt(args[1]), args[0]);


        client.start();

        Scanner s = new Scanner(System.in);
        int status = 0;
        while(true) {
            String msg = s.nextLine();
            String[] msgDet = msg.split(" ");
            if (msg.equals("/logout")){
                status = 1;
                client.sendMessage(new ChatMessage(status, msg));
            }else if(msg.equals("/list")){
                client.sendMessage(new ChatMessage(9,msg));
            }else if (msgDet.length >2 && msg.substring(0,5).equals("/msg ")) {
                //System.out.println("dir messeage");
                String user = msg.substring(5,msg.indexOf(' ',5));
                //System.out.println(user);
                msg = msg.substring(6+user.length());
                client.sendMessage(new ChatMessage(0, msg , user));
            } else {
                client.sendMessage(new ChatMessage(status, msg));
            }
            if (status == 1) {
                break;
            }
        }

    }


    /*
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            while(true) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.print(msg);
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    System.out.println("Server has closed the connection");
                    return;
                }
            }
        }
    }
}
