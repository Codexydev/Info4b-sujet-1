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
            case "txt", "csv", "md", "json", "xml" -> {
                try {
                    return new String(Files.readAllBytes(Paths.get(this.cheminFichier)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "pdf" -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("pdftotext", this.cheminFichier, "-");
                    processBuilder.redirectErrorStream(true);
                    Process process = processBuilder.start();
                    String texteExtrait = new String(process.getInputStream().readAllBytes());
                    process.waitFor();
                    return texteExtrait;
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            case "docx" -> {
                try {
                    java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(this.cheminFichier); //ouvre les fichiers docx comme des zip
                    java.util.zip.ZipEntry documentXML = zipFile.getEntry("word/document.xml"); //et on recup le fichier xml

                    if (documentXML != null) {
                        java.io.InputStream chaine = zipFile.getInputStream(documentXML); //on met tout sous forme de chaine de caractères
                        String contenuXML = new String(chaine.readAllBytes()); //on lit
                        zipFile.close();
                        String textePur = contenuXML.replaceAll("<[^>]+>", " "); //permet d'enlever toutes les balises dont on a pas besoin

                        return textePur;
                    }
                    zipFile.close();
                } catch (Exception e) {
                    System.out.println("Impossible de lire le fichier DOCX : " + this.cheminFichier);
                }
                return "";
            }
            case "html", "htm" -> {
                try {
                    String contenuHTML = new String(Files.readAllBytes(Paths.get(this.cheminFichier)));
                    String sansScript = contenuHTML.replaceAll("(?s)<script.*?</script>", " "); //on enelve les balises
                    String sansStyle = sansScript.replaceAll("(?s)<style.*?</style>", " "); //idem (le ?s permet au regex de fonctionner sur plusieurs lignes)
                    String textePur = sansStyle.replaceAll("<[^>]+>", " "); //on garde le texte pur

                    return textePur;
                } catch (IOException e) {
                    System.err.println("Impossible de lire le fichier HTML : " + this.cheminFichier);
                }
                return "";
            }

            case "jpg", "jpeg", "png" -> {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder("exiv2", "-pa", this.cheminFichier);
                    processBuilder.redirectErrorStream(true);
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

    public static void modifierMetadataPhysique(String chemin, String champ, String valeur) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "exiv2",
                    "-M", "set Iptc.Application2.Caption String " + valeur,
                    chemin
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            System.err.println("Erreur d'écriture EXIF : " + e.getMessage());
        }
    }
}
