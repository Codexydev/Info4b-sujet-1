package Serveur;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        boolean status = file.renameTo(file2);
        if (status) {
            return "Fichier renommé";
        }
        return "erreur";
    }

    public String supprimerFichier() {
        Path cheminPath = Paths.get(file.getAbsolutePath());
        System.out.println("\n[DEBUG-DELETE] 1. Ordre de suppression reçu pour : " + cheminPath);

        if (!Files.exists(cheminPath)) {
            System.out.println("[DEBUG-DELETE] 2. ❌ L'OS affirme que le fichier n'existe DÉJÀ PLUS.");
            return "Erreur : Le fichier n'existe pas.";
        }

        try {
            System.out.println("[DEBUG-DELETE] 3. Tentative de destruction via NIO.2...");
            Files.delete(cheminPath);
            System.out.println("[DEBUG-DELETE] 4. ✅ Ordre de suppression accepté par le noyau Linux sans erreur.");

            // Pause d'1/10ème de seconde pour laisser le réseau de la fac se synchroniser
            Thread.sleep(100);

            if (Files.exists(cheminPath)) {
                System.out.println("[DEBUG-DELETE] 5. 🚨 ANOMALIE NFS : Java a supprimé sans erreur, mais Files.exists() le voit toujours (Problème de Cache Réseau de la fac) !");
                return "Fichier supprimé de l'Index, mais l'OS réseau de la fac force son maintien physique.";
            }

            System.out.println("[DEBUG-DELETE] 6. Suppression totale confirmée.");
            return "Fichier supprimé";

        } catch (java.nio.file.AccessDeniedException e) {
            System.out.println("[DEBUG-DELETE] ❌ REFUSÉ : Droits insuffisants ou fichier verrouillé par l'OS de la fac.");
            return "Erreur Linux : L'OS refuse la suppression (Fichier verrouillé ou en lecture seule).";
        } catch (Exception e) {
            System.out.println("[DEBUG-DELETE] ❌ ERREUR INCONNUE : " + e.getMessage());
            return "Erreur : " + e.getMessage();
        }
    }

    public String getIn() {
        return this.in;
    }
}
