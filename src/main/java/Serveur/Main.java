package Serveur;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class Main {
    public static boolean DEBUG = false;

    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void parcoursFichiers(String cheminRepertoire, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, IdVersChemin idVersChemin, Journal journal) {
        Path start = Paths.get(cheminRepertoire);

        try {
            Files.walk(start)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String cheminFichier = path.toString();

                        if (stockagesDocuments.getMetaData(cheminFichier) == null) {
                            idVersChemin.addPath(cheminFichier);
                            int nouvelId = idVersChemin.getIdCourant();

                            if (DEBUG) System.out.println("\nIndexation du NOUVEAU fichier : " + cheminFichier);
                            indexerFichier(nouvelId, cheminFichier, stockagesDocuments, indexInverse, journal, true);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexerFichier(int id, String cheminFichier, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, Journal journal, boolean estUnAjout) {
        File file = new File(cheminFichier);
        ExtracteurTexte extracteurTexte = new ExtracteurTexte(cheminFichier);
        String texte = extracteurTexte.extraireTexte();

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
            if (mot.isEmpty()) continue;

            if (!bypassMot.contains(mot)) {
                indexInverse.indexerMot(mot, id);
                nbMots += 1;
            }
        }

        if (DEBUG) System.out.println("nombre de mots : " + nbMots);
        stockagesDocuments.ajouterDocument(id, cheminFichier, file.length(), file.lastModified(), nbMots);
        // enregistre dans journal chaque fichier indexer (= sauvegarde)

        ConcurrentHashMap<String, Integer> mots = indexInverse.getMotsDocument(id);
        if (estUnAjout) {
            journal.ecrireAjout(cheminFichier, file.lastModified(), file.length(), mots);
        } else {
            journal.ecrireMiseAJour(cheminFichier, file.lastModified(), file.length(), mots);
        }
    }

    public static void server(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idToPath) {
        try {
            System.out.println("Server is running...");
            ServerSocket server = new ServerSocket(12345);

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                boolean clientConnected = true;

                while (clientConnected) {
                    String str = in.readLine();

                    if (str == null || str.equals("-q") || str.equals("q")) {
                        clientConnected = false;
                        System.out.println("\nClient disconnected");
                        break;
                    }

                    String path;
                    String command = str.split(" ")[0];

                    if (str.length() > 2 || str.equals("-h")) {
                        switch (command) {
                            case "-h":
                                out.println(" Commandes disponibles :\n"+
                                         "-h : "+ANSI_BLEU+"afficher l'aide\n"+ANSI_RESET+
                                         "-t"+ANSI_VERT +" <message> : "+ANSI_BLEU+"Afficher le message reçu pour tester la communication\n"+ANSI_RESET+
                                         "-q : "+ANSI_BLEU+"Quitter la connexion\n"+ANSI_RESET+
                                         "-s"+ANSI_VERT +" <mot(s) (sépration (,) ) > : "+ANSI_BLEU+"Rechercher un mot dans l'index et afficher les documents associés\n"+ANSI_RESET+
                                         "-s "+ANSI_VERT +"<mot(s)> -- <mot(s) qui ne sera pas présent dans les fichier trouvé> : "+ANSI_BLEU+"separateur de mot ,\n"+ANSI_RESET+
                                         "-m "+ANSI_VERT +"<chemin du document> : "+ANSI_BLEU+"Afficher les métadonnées d'un document donné\n"+ANSI_RESET+
                                         "-p"+ANSI_VERT +" <chemin du document> : "+ANSI_BLEU+"affiche le texte du document\n"+ANSI_RESET);
                                out.println("END_OF_MESSAGE");
                                break;

                            case "-t":
                                out.println("Message reçu : " +ANSI_BLEU+ str.substring(3)+ANSI_RESET);
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
                                    recherche = new Recherche(indexInverse, stockagesDocuments, idToPath, mots, motsNonRecherches);

                                } else {
                                    recherche = new Recherche(indexInverse, stockagesDocuments, idToPath, mots);
                                }

                                out.println(recherche.effectuerRecherche());
                                out.println("END_OF_MESSAGE");
                                break;

                            case "-m":
                                String arg = str.split(" ")[1];

                                if (arg.equals("update")) {
                                    UpdateFile update = new UpdateFile();
                                }

                                out.println(stockagesDocuments.getMetaData(arg));
                                out.println("END_OF_MESSAGE");
                                break;

                            case "-p":
                                path = str.split(" ")[1];
                                ExtracteurTexte extracteurTexte = new ExtracteurTexte(path);
                                String texte = extracteurTexte.extraireTexte();
                                out.println("\n" + texte);
                                out.println("END_OF_MESSAGE");
                                break;

                            case "q":
                                clientConnected = false;
                                System.out.println("Client disconnected");
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
                }
                socket.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        /*System.out.print("Chemin du repertoire à indexer : ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();*/

        String path = "src/testIndexed/";

        StockagesDocuments stockagesDocuments = new StockagesDocuments();
        IndexInverse indexInverse = new IndexInverse();
        IdVersChemin idVersChemin = new IdVersChemin();
        Journal journal = null;
        String cheminJournal = "journal.csv";
        try {
            journal = new Journal(cheminJournal);
        } catch (IOException e) {
            System.out.println("Impossible d'ouvrir journal : " + e.getMessage());
            return;
        }

        // restauration + réconciliation + parcoursFichiers
        Journal.restaurerDepuisJournal(cheminJournal, stockagesDocuments, indexInverse, idVersChemin);
        Journal.reconcilier(stockagesDocuments, indexInverse, journal);

        parcoursFichiers(path, stockagesDocuments, indexInverse, idVersChemin, journal);
        System.out.println("Restauration depuis journal.csv : " + stockagesDocuments.getNombreDocuments() + " documents rechargés, pas de réindexation");

        if (DEBUG) System.out.println("\nServeur.DocumentStore : " + stockagesDocuments.getStockagesDocuments());
        if (DEBUG) System.out.println("Index global : " + indexInverse.getIndexInverse());

        System.out.println("\nIndexation terminée. Nombre de documents indexés : " + stockagesDocuments.getNombreDocuments());

        server(indexInverse, stockagesDocuments, idVersChemin);
        journal.fermer();
    }
}