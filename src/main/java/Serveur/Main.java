package Serveur;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void walkFile(String cheminRepertoire, DocumentStore documentStore, InvertedIndex invertedIndex) {
        Path start = Paths.get(cheminRepertoire);
        IdToPath idToPath = new IdToPath();

        try {
            Files.walk(start)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        idToPath.addPath(path.toString());
                        System.out.println("\nIndexation du fichier : " + path.toString());
                        indexFile(idToPath.getCurrentId(),path.toString(), documentStore, invertedIndex);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexFile(int id, String cheminFichier, DocumentStore documentStore, InvertedIndex invertedIndex) {
        File file = new File(cheminFichier);
        ExtractText extractText = new ExtractText(cheminFichier);
        String texte = extractText.extraireTexte(); // renvoie le texte du fichier

        System.out.println(texte);

        if (texte == null || texte.isEmpty()) {
            System.out.println("heyrrry");
            return;
        }

        String[] bypassMotInit = {"le", "la", "les", "un", "une", "des", "de", "du", "et", "en", "à", "pour", "dans", "sur", "avec", "sans"};
        List<String> bypassMot = Arrays.asList(bypassMotInit);

        String texteMinuscule = texte.toLowerCase();
        String[] motsExtraits = texteMinuscule.split("[^\\p{L}\\p{N}]+");

        System.out.printf("poids : %d octets\n", file.length());
        System.out.println("date de modification : " + file.lastModified());
        System.out.println("Texte extrait : " + texte);

        int nbMots = 0;

        for (String mot : motsExtraits) {
            if (mot.isEmpty()) {
                continue;
            }

            if (bypassMot.contains(mot)) {
                System.out.println("Mot ignoré : " + mot);
            } else {
                invertedIndex.indexerMot(mot, id);
                nbMots+=1;
                System.out.println("Indexation du mot : " + mot);
            }
        }

        System.out.println("nombre de mots : " + nbMots);
        documentStore.ajouterDocument(id, cheminFichier, file.length(), file.lastModified(),nbMots);
    }

    public static void main(String[] args) {
        System.out.print("Chemin du repertoire à indexer : ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();

        DocumentStore documentStore = new DocumentStore();
        InvertedIndex invertedIndex = new InvertedIndex();

        walkFile(path, documentStore, invertedIndex);

        System.out.println("Serveur.DocumentStore : " + documentStore.getDocumentStore());
        System.out.println("Index global : " + invertedIndex.getIndexGlobal());
        System.out.println("\nIndexation terminée. Nombre de documents indexés : " + documentStore.getNombreDocuments());
    }
}