


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.io.File;

final class ChatServer {
    private static int uniqueId = 0;
    private final List<ClientThread> clients = new ArrayList<>();
    private final int port;
    private File filter;



    private ChatServer(int port, File filter ) {
        this.port = port;
        this.filter =  filter;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            //System.out.println("server started");
            ServerSocket serverSocket = new ServerSocket(port);
            
            System.out.printf("Banned Words File: %s\n", filter.getPath());
            ChatFilter cf = new ChatFilter(filter.getPath());
            System.out.println("Banned words: ");
            cf.print();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            String time = format.format(new Date());
            System.out.println(time + " Server waiting for the connection on port " + this.port);
            while (true) {

                Socket socket = serverSocket.accept();
                
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
                SimpleDateFormat format1 = new SimpleDateFormat("HH:mm:ss");
                String time1 = format.format(new Date());
                System.out.println(time1 + " Server waiting for the connection on port " + this.port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        ChatServer server = null;
        if (args.length == 0)
            server = new ChatServer(1500, new File("badwords.txt"));
        else if (args.length == 1)
            server = new ChatServer(Integer.parseInt(args[0]), new File("badwords.txt"));
        else if (args.length == 2)
            server = new ChatServer(Integer.parseInt(args[0]), new File(args[1]));

        server.start();
    }

    private synchronized void broadcast(String message) {

    	
    	ChatFilter cf = new ChatFilter(filter.getPath());
    	message = cf.filter(message);
        System.out.println(message);
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).writeMessage(message);
        }

    }

    // private void listUsers() {
    //     for(ClientThread client : clients) {
    //         System.out.println(client.username);
    //     }
    // }

    private synchronized void directMessage(String message, String username) {
        System.out.println(message);
        ChatFilter cf = new ChatFilter(filter.getPath());
        message = cf.filter(message);
        boolean found = false;
        ClientThread r = null;
        for (int i = 0;i<clients.size() ;i++ ) {
            if (clients.get(i).username.equals(username)) {
                r = clients.get(i);
                found = true;
                break;
            }
        }
        if (found == false) {
            System.out.println("The user specified does not exist");
            return;
        }
        r.writeMessage(message);
    }

    private synchronized void remove(int id) {
        ClientThread r = null;
        for (int i = 0; i<clients.size() ;i++ ) {
            if (clients.get(i).id == id) {
                r = clients.get(i);
                break;
            }
        }
        r.close();
        clients.remove(r);
    }

    private synchronized void list(int id) {
        String s = "";
        ClientThread r = null;
        for (int i = 0;i<clients.size() ;i++ ) {
            if (clients.get(i).id == id) {
                r = clients.get(i);
                continue;
            }
            s = s+clients.get(i).username+"\n";
        }
        r.writeMessage(s);
    }

    /*
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                System.out.println(username + " just connected");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private synchronized boolean writeMessage(String message) {
            try {
                sOutput.writeObject(message+"\n");
            } catch (IOException e) {
                //e.printStackTrace();
                return false;
            }
            return true;
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            while(true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    //e.printStackTrace();
                    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    String time = format.format(new Date());
                    System.out.println(time+" "+this.username+" disconnected using by closing the connection.");
                    return;
                }
                // Send message back to the client
                try {
                    if (cm.getStatus()==0) {
                    	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
                    	String time = format.format(new Date());
                        //System.out.println(cm.isDirect());
                    	if(!cm.isDirect()){
                            //System.out.println("broadcasting");
                            broadcast(time + " "+ this.username+": "+cm.getMsg());
                        }else if(cm.isDirect() && !this.username.equals(cm.getRecipient())) {
                            //System.out.println("Direct messaging");
                    	    directMessage(time + " "+ this.username + " -> "+cm.getRecipient()+": "+cm.getMsg(), cm.getRecipient());
                    	} else if(cm.isDirect() && this.username.equals(cm.getRecipient()))
                    	    System.out.println("Cannot Direct Message yourself\n");
                    }
                    if (cm.getStatus() == 9) {
                        list(id);
                    }
                    if (cm.getStatus()==1) {
                        remove(id);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

            }
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
            String time = format.format(new Date());
            System.out.println(time+" "+this.username+" disconnected with a LOGOUT message.");
        }
    }
}
