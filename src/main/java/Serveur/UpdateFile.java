package Serveur;

import java.io.*;
import java.net.Socket;

public class UpdateFile {
    StockagesDocuments stockagesDocuments;
    File file;
    File file2;
    String out;
    String in = "";


    public UpdateFile(String chemin) {
        this.file = new File(chemin);
    }

    public String renomerFichier(String chemin2) {
        this.file2 = new File(chemin2);

        if (!file.exists()) {
            return "Erreur : Le fichier source n'existe pas.";
        }

        boolean statut = file.renameTo(file2);
        if (statut) {
            return "Fichier Renommé";
        }
        return "erreur";
    }

    public String getIn() {
        return this.in;
    }
}
