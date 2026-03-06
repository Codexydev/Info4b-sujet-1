package Serveur;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class Journal {
    private String chemin;
    private BufferedWriter writer;

    public Journal(String chemin) throws IOException {
        this.chemin = chemin;
        writer =  new BufferedWriter(new FileWriter(chemin, true));
    }

    // Liste de mots + fréquence
    private String formaterMots(Map<String, Integer> mots){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Integer> entry : mots.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue() + ",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}

