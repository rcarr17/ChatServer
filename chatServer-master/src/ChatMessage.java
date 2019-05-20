
import java.io.Serializable;

final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    private int status;
    private String msg;
    private String recipient;
    private boolean direct = false;


    public ChatMessage (int status, String msg) {
        // status zero is normal message, 1 is logout
        this.status = status;
        this.msg = msg;
        direct = false;
    }

    public ChatMessage (int status, String msg, String recipient) {
        // status zero is normal message, 1 is logout
        this.status = status;
        this.msg = msg;
        this.recipient = recipient;
        direct = true;

    }

    public int getStatus(){
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public String getRecipient() {
        return recipient;
    }

    public boolean isDirect(){
        return direct;
    }
}
