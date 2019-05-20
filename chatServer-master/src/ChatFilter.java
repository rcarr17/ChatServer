import java.io.*;
import java.util.ArrayList;

public class ChatFilter {
    ArrayList<String> words;

    public ChatFilter(String badWordsFileName) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(new File(badWordsFileName)));
        } catch (FileNotFoundException e) {
            System.out.println("IO File not found");
        }
        String line = null;
        words = new ArrayList<String>();
        try {
            line = br.readLine();
            while(line != null) {
                words.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            System.out.print("IO error");
        }

        for(int i = 0; i < words.size(); i++) {
            words.set(i, words.get(i).toLowerCase());
        }

    }

    public synchronized String filter(String msg) {
        String[] msgSplit = msg.split("\\s+");

        for(int i = 0; i < msgSplit.length; i++) {
            String str = msgSplit[i];
            str = str.replaceAll("[^a-zA-Z]", "");
            if(words.contains(str.toLowerCase()) && msgSplit[i].length() > str.length()) {
                msgSplit[i] = censor(str) + msgSplit[i].substring(str.length());
            } else if(words.contains(str.toLowerCase())) {
                msgSplit[i] = censor(str);
            }
        }

        String newMsg = "";
        for(int j = 0; j < msgSplit.length; j++) {
            if(j == msgSplit.length-1) {
                newMsg = newMsg + msgSplit[j];
            } else {
                newMsg = newMsg + msgSplit[j] + " ";
            }
        }

        return newMsg;
    }


    private String censor(String word) {
        String cen = "";
        for(int i = 0; i < word.length(); i++) {
            cen = cen + '*';
        }
        return cen;
    }

    public void print(){
        for (int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i));
        }
    }
}
