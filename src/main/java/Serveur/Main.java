package Serveur;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static boolean DEBUG = false;

    public static void walkFile(String cheminRepertoire, DocumentStore documentStore, InvertedIndex invertedIndex, IdToPath idToPath, Journal journal) {
        Path start = Paths.get(cheminRepertoire);

        try {
            Files.walk(start).filter(Files::isRegularFile).forEach(path -> {
                idToPath.addPath(path.toString());
                if (DEBUG) System.out.println("\nIndexation du fichier : " + path);
                indexFile(idToPath.getCurrentId(), path.toString(), documentStore, invertedIndex, journal);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexFile(int id, String cheminFichier, DocumentStore documentStore, InvertedIndex invertedIndex, Journal journal) {
        File file = new File(cheminFichier);
        ExtractText extractText = new ExtractText(cheminFichier);
        String texte = extractText.extraireTexte(); // renvoie le texte du fichier

        if (DEBUG) System.out.println(texte);

        if (texte == null || texte.isEmpty()) {
            if (DEBUG) System.out.println("-- text vide");
            return;
        }

        String[] bypassMotInit = {"le", "la", "les", "un", "une", "des", "de", "du", "et", "en", "à", "pour", "dans", "sur", "avec", "sans"};
        List<String> bypassMot = Arrays.asList(bypassMotInit);

        String texteMinuscule = texte.toLowerCase();
        String[] motsExtraits = texteMinuscule.split("[^\\p{L}\\p{N}]+");

        if (DEBUG) System.out.println("id : " + id);
        if (DEBUG) System.out.printf("poids : %d octets\n", file.length());
        if (DEBUG) System.out.println("date de modification : " + file.lastModified());
        if (DEBUG) System.out.println("Texte extrait : " + texte);

        int nbMots = 0;

        for (String mot : motsExtraits) {
            if (mot.isEmpty()) {
                continue;
            }

            if (bypassMot.contains(mot)) {
                if (DEBUG) System.out.println("Mot ignoré : " + mot);
            } else {
                invertedIndex.indexerMot(mot, id);
                nbMots += 1;
                if (DEBUG) System.out.println("Indexation du mot : " + mot);
            }
        }

        if (DEBUG) System.out.println("nombre de mots : " + nbMots);
        documentStore.ajouterDocument(id, cheminFichier, file.length(), file.lastModified(), nbMots);
        // enregistre dans journal chaque fichier indexer (= sauvegarde)

        ConcurrentHashMap<String, Integer> mots = invertedIndex.getMotsDocument(id);
        journal.ecrireAjout(cheminFichier, file.lastModified(), file.length(), mots);
    }

    public static void server(InvertedIndex invertedIndex, DocumentStore documentStore, IdToPath idToPath) {
        try {
            System.out.println("Server is running...");
            ServerSocket server = new ServerSocket(12345);
            Socket socket = server.accept();
            System.out.println("Client connected");

            boolean running = true;
            while (running) {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String str = in.readLine();
                String path;

                String command = str.split(" ")[0];

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                if (str.length() > 2 || str.equals("-h")) {

                    switch (command) {
                        case "-h":
                            out.println("""
                                     Commandes disponibles :
                                     -h : Afficher l'aide
                                     -t <message> : Afficher le message reçu pour tester la communication
                                     -q : Quitter la connexion
                                     -s <mot(s) (sépration (,) ) > : Rechercher un mot dans l'index et afficher les documents associés
                                     -s <mot(s)> -- <mot(s) qui ne sera pas présent dans les fichier trouvé> : separateur de mot ","
                                     -m <chemin du document> : Afficher les métadonnées d'un document donné
                                     -p <chemin du document> : affiche le texte du document
                                    END_OF_MESSAGE""");
                            break;

                        case "-t":
                            out.println("Message reçu : " + str.substring(3));
                            out.println("END_OF_MESSAGE");
                            break;

                        case "-s":
                            String[] arguments = str.split(" ");
                            if (arguments.length < 2) {
                                out.println("Erreur: Veuillez spécifier un mot à chercher.");
                                out.println("END_OF_MESSAGE");
                                break;
                            }

                            String[] mots = arguments[1].split(",");

                            Recherche recherche;
                            if (arguments.length >= 4 && arguments[2].equals("--")) {
                                String[] motsNonRecherches = arguments[3].split(",");
                                recherche = new Recherche(invertedIndex, documentStore, idToPath, mots, motsNonRecherches);

                            } else {
                                recherche = new Recherche(invertedIndex, documentStore, idToPath, mots);
                                /*out.println(recherche.effectuerRecherche());*/
                            }
                            out.println(recherche.effectuerRecherche());
                            out.println("END_OF_MESSAGE");
                            break;

                        case "-m":
                            String arg = str.split(" ")[1];

                            if (arg.equals("update")) {
                                UpdateFile update = new UpdateFile();
                            }


                            out.println(documentStore.getDocumentMetaData(arg));
                            out.println("END_OF_MESSAGE");
                            break;

                        case "-p":
                            path = str.split(" ")[1];
                            ExtractText extractText = new ExtractText(path);
                            String texte = extractText.extraireTexte();
                            out.println("\n" + texte);
                            out.println("END_OF_MESSAGE");
                            break;

                        default:
                            out.println("Commande inconnuee. Tapez -h pour afficher l'aide.");
                            out.println("END_OF_MESSAGE");
                            break;
                    }
                } else {
                    out.println("donnez le(s) parametre(s)");
                    out.println("END_OF_MESSAGE");
                }

                if (str.equals("q")) {
                    running = false;
                    System.out.println("Client disconnected");
                    break;
                }


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        /*System.out.print("Chemin du repertoire à indexer : ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();*/

        String path = "testIndexed/";

        DocumentStore documentStore = new DocumentStore();
        InvertedIndex invertedIndex = new InvertedIndex();
        IdToPath idToPath = new IdToPath();
        Journal journal = null;
        String cheminJournal = "journal.csv";
        try {
            journal = new Journal(cheminJournal);
        } catch (IOException e) {
            System.out.println("Impossible d'ouvrir journal : " + e.getMessage());
            return;
        }

        // restauration + réconciliation + walkFile
        Journal.restaurerDepuisJournal(cheminJournal, documentStore, invertedIndex, idToPath);
        Journal.reconcilier(documentStore, invertedIndex, journal);
        if (documentStore.getNombreDocuments() == 0) {
            System.out.println("1er lancement : indexation ");
            walkFile(path, documentStore, invertedIndex, idToPath, journal);
        }else {
            System.out.println("Restauration depuis journal.csv : " + documentStore.getNombreDocuments() + " documents rechargés, pas de réindexation");
        }

        if (DEBUG) System.out.println("\nServeur.DocumentStore : " + documentStore.getDocumentStore());
        if (DEBUG) System.out.println("Index global : " + invertedIndex.getIndexGlobal());

        System.out.println("\nIndexation terminée. Nombre de documents indexés : " + documentStore.getNombreDocuments());

        server(invertedIndex, documentStore, idToPath);
        journal.fermer(); //ferme proprement le journal
    }

}