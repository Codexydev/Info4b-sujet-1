package Serveur;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;


public class Main {
    public static boolean DEBUG = false;

    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void parcoursFichiers(String cheminRepertoire, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, IdVersChemin idVersChemin, Journal journal, StopWord stopWord) {
        Path start = Paths.get(cheminRepertoire);

        try {
            Files.walk(start)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        if (path.getFileName().toString().startsWith(".")) {
                            return; //permet d'eviter les fihciers .DS_store qui ne serve pas à l'indexation
                        }
                        String cheminFichier = path.toString();

                        if (stockagesDocuments.getMetaData(cheminFichier) == null) {
                                synchronized (idVersChemin) {
                                    idVersChemin.addPath(cheminFichier);
                                }
                                int nouvelId = idVersChemin.getIdCourant();

                                if (DEBUG) System.out.println("\nIndexation du NOUVEAU fichier : " + cheminFichier);
                                indexerFichier(nouvelId, cheminFichier, stockagesDocuments, indexInverse, journal, true, stopWord);
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void indexerFichier(int id, String cheminFichier, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, Journal journal, boolean estUnAjout, StopWord stopWord) {
        File file = new File(cheminFichier);
        ExtracteurTexte extracteurTexte = new ExtracteurTexte(cheminFichier);
        String texte = extracteurTexte.extraireTexte();

        if (DEBUG) System.out.println(texte);

        if (texte == null || texte.isEmpty()) {
            if (DEBUG) System.out.println("-- text vide");
            return;
        }

        String texteMinuscule = texte.toLowerCase();
        String[] motsExtraits = texteMinuscule.split("[^\\p{L}\\p{N}]+");

        if (DEBUG) System.out.println("id : " + id);
        if (DEBUG) System.out.printf("poids : %d octets\n", file.length());
        if (DEBUG) System.out.println("date de modification : " + file.lastModified());
        if (DEBUG) System.out.println("Texte extrait : " + texte);

        int nbMots = 0;

        for (String mot : motsExtraits) {
            if (mot.isEmpty()) continue;

            if (!stopWord.getWords().contains(mot)) {
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

    public static void server(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idToPath, Journal journal, StopWord stopWord, String cheminRepertoire) {
        try {
            System.out.println("Server is running...");
            ServerSocket server = new ServerSocket(12345);

            while (true) {
                Socket socket = server.accept();
                System.out.println("Client connected");
                // Ajout de threads pour gérer plusieurs clients au lieu d'un seul
                new Thread(() -> {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                        String help = "Commandes disponibles :\n" +
                                "-h : " + ANSI_BLEU + "afficher l'aide\n" + ANSI_RESET +
                                "-l : " + ANSI_BLEU + "liste tous les fichiers indexés\n" + ANSI_RESET +
                                "-t" + ANSI_VERT + " <message> : " + ANSI_BLEU + "Afficher le message reçu pour tester la communication\n" + ANSI_RESET +
                                "-q : " + ANSI_BLEU + "Quitter la connexion\n" + ANSI_RESET +
                                "-s" + ANSI_VERT + " <mot(s) (sépration (,) ) > : " + ANSI_BLEU + "Rechercher un mot dans l'index et afficher les documents associés\n" + ANSI_RESET +
                                "-s " + ANSI_VERT + "<mot(s)> -- <mot(s) qui ne sera pas présent dans les fichier trouvé> : " + ANSI_BLEU + "separateur de mot ,\n" + ANSI_RESET +
                                "-m " + ANSI_VERT + "<chemin du document> : " + ANSI_BLEU + "Afficher les métadonnées d'un document donné\n" + ANSI_RESET +
                                "-m -rn" + ANSI_VERT + " <chemin du document> " + ANSI_BLEU + "Renommé un fichier\n" + ANSI_RESET +
                                "-m -rm" + ANSI_VERT + " <chemin du document>" + ANSI_BLEU + " Supprimer fichier \n" + ANSI_RESET +
                                "-d" + ANSI_VERT + " <chemin du document> <chemin du document>" + ANSI_BLEU + " Permet de détecté sont deux fichiers sont les même \n" + ANSI_RESET +
                                "-r" + ANSI_VERT + " <chemin du document>" + ANSI_BLEU + " Affiche le contenu du fichier \n" + ANSI_RESET +
                                "-ar" + ANSI_VERT + " <mot1 ET/OU/SAUF mot2 ET/OU/SAUF mots3 etc...> : " + ANSI_BLEU + "rechercher les fichiers de plusieurs mots (ET), d'un mot OU l'autre (OU), d'un fichier contenant un mot mais pas un autre(SAUF)\n" + ANSI_RESET +
                                "-exif " + ANSI_VERT + "<chemin> : " + ANSI_BLEU + "Afficher les métadonnées EXIF d'une image\n" + ANSI_RESET +
                                "-dl" + ANSI_VERT + " <chemin du document> : " + ANSI_BLEU + "Permet au client de télécharger le fichier duquel on écrit le chemin\n" + ANSI_RESET+
                                "-clean : " + ANSI_BLEU + "Compacter le journal (garde uniquement l'état actuel)\n" + ANSI_RESET +
                                "-reindex " + ANSI_BLEU + "permet de supprimer le journal et refaire une indexation\n" + ANSI_RESET +
                                "-sw -l" + ANSI_VERT + " <mot> : " + ANSI_BLEU + "permet d'afficher la liste des stop words\n" + ANSI_RESET +
                                "-sw -rm" + ANSI_VERT + " <mot> : " + ANSI_BLEU + "permet d'enlever un mot de la liste des stop words\n" + ANSI_RESET +
                                "-sw -add" + ANSI_VERT + " <mot> : " + ANSI_BLEU + "permet d'ajouter un mot à la liste des stop words\n" + ANSI_RESET;

                        out.println(help);
                        out.println("END_OF_MESSAGE");

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
                            String[] arg = str.split(" ");

                            if (str.length() > 2 || str.equals("-h") || str.equals("-l")) {
                                switch (command) {
                                    case "-l":
                                        out.println(ANSI_BLEU);
                                        for (String file : stockagesDocuments.getStockagesDocuments().keySet()) {
                                            out.println(file);
                                        }
                                        out.println(ANSI_RESET);
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-h":
                                        out.println("\n" + help);
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-t":
                                        out.println("Message reçu : " + ANSI_BLEU + str.substring(3) + ANSI_RESET);
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-s":
                                        if (arg.length < 2) {
                                            out.println("Erreur: Veuillez spécifier un mot à chercher.");
                                            out.println("END_OF_MESSAGE");
                                            break;
                                        }

                                        String[] mots = arg[1].split(",");

                                        Recherche recherche;
                                        if (arg.length >= 4 && arg[2].equals("--")) {
                                            String[] motsNonRecherches = arg[3].split(",");
                                            recherche = new Recherche(indexInverse, stockagesDocuments, idToPath, mots, motsNonRecherches);

                                        } else {
                                            recherche = new Recherche(indexInverse, stockagesDocuments, idToPath, mots);
                                        }

                                        out.println(recherche.effectuerRecherche());
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-ar":
                                        if (str.length() <= 4) { // 4 car "-as " fait 4 caractères
                                            out.println("Erreur: Specifiez un/des mot(s) à chercher");
                                            out.println("END_OF_MESSAGE");
                                            break;
                                        }
                                        String requete = str.substring(4).trim(); //phrase après les 4 prem caractères
                                        String[] motsAvances = requete.split(" "); //découpage avec les espaces

                                        Recherche maRecherche = new Recherche(indexInverse, stockagesDocuments, idToPath, motsAvances, new String[0]);
                                        out.println(maRecherche.RechercheAvance());

                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-m":
                                        String chemin;
                                        if (arg.length > 3) chemin = arg[2];
                                        else chemin = arg[1];
                                        UpdateFile updateFile = new UpdateFile(chemin);
                                        switch (arg[1]) {
                                            case "-rn":
                                                if (arg.length >= 4) {
                                                    String nouveauChemin = arg[3];

                                                    String resultat = updateFile.renomerFichier(nouveauChemin);
                                                    if (resultat.equals("Fichier renommé")) {
                                                        stockagesDocuments.supprimerDocument(chemin);
                                                        journal.ecrireSuppression(chemin, System.currentTimeMillis());

                                                        idToPath.addPath(nouveauChemin);
                                                        indexerFichier(idToPath.getIdCourant(), nouveauChemin, stockagesDocuments, indexInverse, journal, true, stopWord);
                                                    }

                                                    out.println(resultat);
                                                } else {
                                                    out.println("Erreur: arg manquants. Utilisation: -m -rn <ancien> <nouveau>");
                                                }
                                                break;

                                            case "-rm":
                                                String resultat = updateFile.supprimerFichier();

                                                if (resultat.equals("Fichier supprimé")) {
                                                    stockagesDocuments.supprimerDocument(chemin);
                                                    journal.ecrireSuppression(chemin, System.currentTimeMillis());
                                                } else {
                                                    out.println("erreur");
                                                }
                                                break;

                                            default:
                                                out.println(stockagesDocuments.getMetaData(arg[1]));
                                                break;
                                        }
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-exif":
                                        if (arg.length < 2) {
                                            out.println("Erreur: Spécifiez un chemin d'image.");
                                            out.println("END_OF_MESSAGE");
                                            break;
                                        }
                                        ExtracteurTexte extracteurExiv = new ExtracteurTexte(arg[1]);
                                        String metaExiv = extracteurExiv.extraireTexte();
                                        if (metaExiv == null || metaExiv.isEmpty()) {
                                            out.println("Impossible d'extraire les métadonnées.");
                                        } else {
                                            out.println(metaExiv);
                                        }
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-r":
                                        path = str.substring(3).trim();
                                        ExtracteurTexte extracteurTexte = new ExtracteurTexte(path);
                                        String texte = extracteurTexte.extraireTexte();
                                        out.println("\n" + texte);
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-dl":
                                        String fichier = str.substring(4).trim();
                                        String unité = "octets";
                                        File monFichier = new File(fichier);
                                        if (!monFichier.exists()) {
                                            out.println("fichier inexistant");
                                            out.println("END_OF_MESSAGE");
                                            break;
                                        } else {
                                            String taille_fichier_affiche_fin = "";
                                            long taille_fichier = monFichier.length();
                                            out.println("File_incomming..." + " " + taille_fichier + " " + monFichier.getName());
                                            out.flush();
                                            byte[] buffer = new byte[4096];
                                            int quantité_actuelle_lu = 0;
                                            FileInputStream lecteur= new FileInputStream((monFichier));
                                            OutputStream tuyau_envoi = socket.getOutputStream();
                                            while((quantité_actuelle_lu = lecteur.read(buffer)) != -1){
                                                tuyau_envoi.write(buffer, 0, quantité_actuelle_lu);
                                            }
                                            lecteur.close();
                                            tuyau_envoi.flush();
                                        }
                                        break;

                                    case "q":
                                        clientConnected = false;
                                        System.out.println("Client disconnected");
                                        break;

                                    case "-reindex":
                                        Journal.resetJournal(stockagesDocuments, indexInverse, idToPath); //on suppr que la RAM ici
                                        try {journal.supprimerJournal();
                                        }catch (IOException e){
                                            System.out.println("Erreur lors de la reindexation du journal.");
                                        }
                                        parcoursFichiers(cheminRepertoire, stockagesDocuments, indexInverse, idToPath, journal, stopWord);
                                        out.println("Réindexation terminée !");
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-d":
                                        Doublon doublon = new Doublon(arg[1], arg[2]);
                                        boolean estDublon = doublon.EstDoublon();
                                        if (estDublon) out.println("C'est fichier sont similaire");
                                        else out.println("fichier différents");
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    case "-sw":
                                        if (arg.length < 3 && !arg[1].equals("-l")) {
                                            out.println("Erreur : Arguments manquants. Exemple : -sw -add le,la");
                                            out.println("END_OF_MESSAGE");
                                            break;
                                        }
                                        switch (arg[1]) {
                                            case "-add" :
                                                try {
                                                    String[] motsAAjouter = arg[2].split(",");
                                                    stopWord.addMot(motsAAjouter);

                                                    for (String m : motsAAjouter) {
                                                        indexInverse.supprimerMot(m);
                                                    }
                                                    out.println("Mot ajouté et supprimer de l'index !");
                                                } catch (IOException e) {
                                                    out.println("Erreur d'écriture dans le fichier stopword");
                                                }
                                                break;

                                            case "-rm" :
                                                try {
                                                    String[] motsASupprimer = arg[2].split(",");
                                                    stopWord.removeMot(motsASupprimer);
                                                    out.println("Mot retiré des Stop Words !");
                                                    out.println("Note : Ce changement s'appliquera aux futures indexations. Les anciens fichiers ne contiennent pas encore ce mot dans l'index.");
                                                } catch (IOException e) {
                                                    out.println("Erreur de réécriture dans le fichier stopword.");
                                                }
                                                break;

                                            case "-l":
                                                out.println(ANSI_BLEU + "Liste des mots vides actuels :" + ANSI_RESET);
                                                out.println(stopWord);
                                                break;
                                        }
                                        out.println("END_OF_MESSAGE");
                                        break;

                                    default:
                                        out.println("Commande inconnuee. Tapez -h pour afficher l'aide.");
                                        out.println("END_OF_MESSAGE");
                                        break;
                                    case "-clean":
                                        try {
                                            journal.compacter(stockagesDocuments, indexInverse);
                                            out.println("Journal compacté avec succès.");
                                        } catch (IOException e) {
                                            out.println("Erreur lors de la compaction : " + e.getMessage());
                                        }
                                        out.println("END_OF_MESSAGE");
                                        break;
                                }
                            } else {
                                out.println("donnez le(s) parametre(s)");
                                out.println("END_OF_MESSAGE");
                            }
                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) {

        /*System.out.print("Chemin du repertoire à indexer : ");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();*/

        String path = "src/testIndexed";
        StockagesDocuments stockagesDocuments = new StockagesDocuments();
        IndexInverse indexInverse = new IndexInverse();
        IdVersChemin idVersChemin = new IdVersChemin();
        Journal journal = null;
        String cheminJournal = "journal.csv";
        StopWord stopWord = new StopWord();
        Journal.restaurerDepuisJournal(cheminJournal, stockagesDocuments, indexInverse, idVersChemin, path);
        try {
            journal = new Journal(cheminJournal, path);
        } catch (IOException e) {
            System.out.println("Impossible d'ouvrir journal : " + e.getMessage());
            return;
        }

        // réconciliation + parcoursFichiers
        Journal.reconcilier(stockagesDocuments, indexInverse, journal, stopWord);

        parcoursFichiers(path, stockagesDocuments, indexInverse, idVersChemin, journal, stopWord);
        System.out.println("Restauration depuis journal.csv : " + stockagesDocuments.getNombreDocuments() + " documents rechargés, pas de réindexation");

        if (DEBUG) System.out.println("\nServeur.DocumentStore : " + stockagesDocuments.getStockagesDocuments());
        if (DEBUG) System.out.println("Index global : " + indexInverse.getIndexInverse());

        System.out.println("\nIndexation terminée. Nombre de documents indexés : " + stockagesDocuments.getNombreDocuments());

        SurveillanceTempsReel surveillance = new SurveillanceTempsReel(path, stockagesDocuments, indexInverse, idVersChemin, journal, stopWord);
        Thread threadSurveillance = new Thread(surveillance);
        threadSurveillance.start();

        server(indexInverse, stockagesDocuments, idVersChemin, journal, stopWord, path);
        journal.fermer();
    }
}