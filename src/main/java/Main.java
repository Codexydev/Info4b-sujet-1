import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void walkFile(String cheminRepertoire, DocumentStore documentStore, InvertedIndex invertedIndex) {
        Path start = Paths.get(cheminRepertoire);

        try {
            Files.walk(start).forEach(path -> {
                indexFile(path.toString(), documentStore, invertedIndex);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexFile(String cheminFichier, DocumentStore documentStore, InvertedIndex invertedIndex) {
        ExtractText extractText = new ExtractText(cheminFichier);
        String texte = extractText.extraireTexte(); // renvoie le texte du fichier

    }

    public static void main(String[] args) {
        System.out.println("Chemin du repertoire à indexer : ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        DocumentStore documentStore = new DocumentStore();
        InvertedIndex invertedIndex = new InvertedIndex();

        walkFile(path, documentStore, invertedIndex);
    }
}