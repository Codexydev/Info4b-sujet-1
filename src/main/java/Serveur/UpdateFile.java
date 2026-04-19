package Serveur;

import java.io.*;

public class UpdateFile {
    File file;
    File file2;
    String in = "";


    public UpdateFile(String chemin) {
        this.file = new File(chemin);
    }

    public String renomerFichier(String chemin2) {
        this.file2 = new File(chemin2);

        if (!file.exists()) {
            return "Erreur : Le fichier source n'existe pas.";
        }

        if (file2.getParentFile() != null) {
            file2.getParentFile().mkdirs();
        }

        boolean status = file.renameTo(file2);
        if (status) {
            return "Fichier renommé";
        }
        return "erreur";
    }

    public String supprimerFichier() {
        if (!file.exists()) {
            return "Erreur : Le fichier source n'existe pas.";
        }
        boolean status = file.delete();
        if (status) {
            return "Fichier supprimé";
        }
        return "erreur";
    }
}
