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
                this.words.add(line.trim().toLowerCase()); //permet de mettre les stopword en minuscule(sinon il seront ignoré)
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
        FileWriter writer = new FileWriter("src/main/java/Serveur/stopword.txt", true);
        for (String word : words) {
            String motPropre = word.trim().toLowerCase(); //passe tout en minuscule et enleve les espaces inutiles
            writer.write(motPropre + "\n");
            this.words.add(motPropre);
        }
        writer.close();
    }

    public void removeMot(String[] words) throws IOException {
        for (String word : words) {
            this.words.remove(word.trim().toLowerCase());
        }

        FileWriter writer = new FileWriter("src/main/java/Serveur/stopword.txt", false);
        for (String motRestant : this.words) {
            writer.write(motRestant + "\n");
        }
        writer.close();
    }

    @Override
    public String toString() {
        return this.words.toString();
    }
}