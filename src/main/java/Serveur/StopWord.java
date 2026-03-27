package Serveur;

import java.io.*;
import java.util.HashSet;

public class StopWord {
    private HashSet<String> words = new HashSet<String>();

    public StopWord() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/java/Serveur/stopword.txt"));
            String line = reader.readLine();

            while (line != null) {
                this.words.add(line);
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashSet<String> getWords() {
        return this.words;
    }

    public void addMot(String[] words) throws IOException {
        FileWriter writer = new FileWriter("/Users/antoine/Documents/etude/l2/s4/info4b/projet/src/main/java/Serveur/stopword.txt", true);
        for (String word : words) {
            writer.write("\n"+word);
            this.words.add(word);
        }
        writer.close();
    }

    @Override
    public String toString() {
        return this.words.toString();
    }
}
