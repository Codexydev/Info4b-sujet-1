package Serveur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExtracteurTexte {
    private final String cheminFichier;
    private final String extension;

    public ExtracteurTexte(String cheminFichier) {
        this.cheminFichier = cheminFichier;
        this.extension = cheminFichier.substring(cheminFichier.lastIndexOf(".") + 1).toLowerCase();
    }

    public String extraireTexte() {
        // Utilisation de processBuilder pour exécuter la commande pdf2text, ... pour renvoyer le texte
        switch (this.extension) {
            case "txt" -> {
                try {
                    return new String(Files.readAllBytes(Paths.get(this.cheminFichier)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "pdf" -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("pdftotext", this.cheminFichier, "-");
                    Process process = processBuilder.start();
                    String texteExtrait = new String(process.getInputStream().readAllBytes());
                    process.waitFor();
                    return texteExtrait;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "jpg", "jpeg", "png" -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("exiv2", this.cheminFichier);
                    Process process = processBuilder.start();
                    String texteExtrait = new String(process.getInputStream().readAllBytes());
                    process.waitFor();
                    return texteExtrait;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }
}
