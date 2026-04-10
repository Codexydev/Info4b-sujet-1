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
                    .parallel()
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
                // Ajout de ThreadGroup (TP1) pour regroupper les threads
                ThreadGroup clientsGroup = new ThreadGroup("Clients-connectes");
                new Thread(clientsGroup, () -> {
                    try {
                        DataInputStream in = new DataInputStream(socket.getInputStream());
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

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

                        out.writeUTF(help);
                        out.writeUTF("END_OF_MESSAGE");

                        boolean clientConnected = true;

                        while (clientConnected) {
                            String str = in.readUTF();

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
                                        out.writeUTF(ANSI_BLEU);
                                        for (String file : stockagesDocuments.getStockagesDocuments().keySet()) {
                                            out.writeUTF(file);
                                        }
                                        out.writeUTF(ANSI_RESET);
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-h":
                                        out.writeUTF("\n" + help);
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-t":
                                        out.writeUTF("Message reçu : " + ANSI_BLEU + str.substring(3) + ANSI_RESET);
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-s":
                                        if (arg.length < 2) {
                                            out.writeUTF("Erreur: Veuillez spécifier un mot à chercher.");
                                            out.writeUTF("END_OF_MESSAGE");
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

                                        out.writeUTF(recherche.effectuerRecherche());
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-ar":
                                        if (str.length() <= 4) { // 4 car "-as " fait 4 caractères
                                            out.writeUTF("Erreur: Specifiez un/des mot(s) à chercher");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }
                                        String requete = str.substring(4).trim(); //phrase après les 4 prem caractères
                                        String[] motsAvances = requete.split(" "); //découpage avec les espaces

                                        Recherche maRecherche = new Recherche(indexInverse, stockagesDocuments, idToPath, motsAvances, new String[0]);
                                        out.writeUTF(maRecherche.RechercheAvance());

                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-m":
                                        if (str.startsWith("-m -rn")) {
                                            String[] argumentsRn = str.split(" ", 4);
                                            if (argumentsRn.length >= 4) {
                                                String ancienChemin = argumentsRn[2];
                                                String nouveauChemin = argumentsRn[3];
                                                UpdateFile updateFileRn = new UpdateFile(ancienChemin);
                                                String resultat = updateFileRn.renomerFichier(nouveauChemin);

                                                if (resultat.equals("Fichier renommé")) {
                                                    stockagesDocuments.supprimerDocument(ancienChemin);
                                                    journal.ecrireSuppression(ancienChemin, System.currentTimeMillis());
                                                    idToPath.addPath(nouveauChemin);
                                                    indexerFichier(idToPath.getIdCourant(), nouveauChemin, stockagesDocuments, indexInverse, journal, true, stopWord);
                                                }
                                                out.writeUTF(resultat);
                                            } else {
                                                out.writeUTF("Erreur: arguments manquants. Utilisation: -m -rn <ancien> <nouveau>");
                                            }
                                        } else if (str.startsWith("-m -rm")) {
                                            String cheminRm = str.substring(7).trim();
                                            UpdateFile updateFileRm = new UpdateFile(cheminRm);
                                            String resultat = updateFileRm.supprimerFichier();

                                            if (resultat.equals("Fichier supprimé")) {
                                                stockagesDocuments.supprimerDocument(cheminRm);
                                                journal.ecrireSuppression(cheminRm, System.currentTimeMillis());
                                                out.writeUTF("Fichier supprimé avec succès.");
                                            } else {
                                                out.writeUTF("Erreur lors de la suppression.");
                                            }
                                        } else {
                                            String cheminMeta = str.substring(3).trim();
                                            MetaDataDocument meta = stockagesDocuments.getMetaData(cheminMeta);

                                            if (meta != null) {
                                                out.writeUTF(meta.toString());
                                            } else {
                                                out.writeUTF("Fichier introuvable dans l'index.");
                                            }
                                        }
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-exif":
                                        if (str.length() <= 6) {
                                            out.writeUTF("Erreur: Spécifiez un chemin d'image.");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }
                                        String cheminImage = str.substring(6).trim();
                                        ExtracteurTexte extracteurExiv = new ExtracteurTexte(cheminImage);
                                        String metaExiv = extracteurExiv.extraireTexte();

                                        if (metaExiv == null || metaExiv.isEmpty()) {
                                            out.writeUTF("Impossible d'extraire les métadonnées.");
                                        } else {
                                            out.writeUTF(metaExiv);
                                        }
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-r":
                                        path = str.substring(3).trim();
                                        ExtracteurTexte extracteurTexte = new ExtracteurTexte(path);
                                        String texte = extracteurTexte.extraireTexte();
                                        out.writeUTF("\n" + texte);
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-dl":
                                        if (str.length() <= 4) {
                                            out.writeUTF("Erreur : Spécifiez un nom de fichier. (ex: -dl monfichier.txt)");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }
                                        String fichier = str.substring(4).trim();
                                        File monFichier = new File(fichier);
                                        if (!monFichier.exists()) {
                                            out.writeUTF("fichier inexistant");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        } else {
                                            long taille_fichier = monFichier.length();

                                            out.writeUTF("File_incomming... " + taille_fichier + " " + monFichier.getName());

                                            FileInputStream lecteur = new FileInputStream(monFichier);
                                            byte[] buffer = new byte[4096];
                                            int quantite_lu;
                                            while ((quantite_lu = lecteur.read(buffer)) != -1) {
                                                out.write(buffer, 0, quantite_lu);
                                            }
                                            lecteur.close();
                                            out.flush();
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
                                        out.writeUTF("Réindexation terminée !");
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-d":
                                        if (arg.length < 3) {
                                            out.writeUTF("Erreur : arguments manquants. Utilisation : -d <chemin_fichier_1> <chemin_fichier_2>");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }

                                        String chemin1 = arg[1];
                                        String chemin2 = arg[2];
                                        File fichier1 = new File(chemin1);
                                        File fichier2 = new File(chemin2);

                                        if (!fichier1.exists() || !fichier1.isFile()) {
                                            out.writeUTF("Erreur : Le premier fichier est introuvable ou est un dossier (" + chemin1 + ").");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }
                                        if (!fichier2.exists() || !fichier2.isFile()) {
                                            out.writeUTF("Erreur : Le deuxième fichier est introuvable ou est un dossier (" + chemin2 + ").");
                                            out.writeUTF("END_OF_MESSAGE");
                                            break;
                                        }

                                        Doublon doublon = new Doublon(chemin1, chemin2);
                                        boolean estDublon = doublon.EstDoublon();

                                        if (estDublon) {
                                            out.writeUTF("Ces fichiers sont similaires.");
                                        } else {
                                            out.writeUTF("Ces fichiers sont différents.");
                                        }

                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    case "-sw":
                                        if (arg.length < 3 && !arg[1].equals("-l")) {
                                            out.writeUTF("Erreur : Arguments manquants. Exemple : -sw -add le,la");
                                            out.writeUTF("END_OF_MESSAGE");
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
                                                    out.writeUTF("Mot ajouté aux stopwords et supprimé de l'index !");
                                                } catch (IOException e) {
                                                    out.writeUTF("Erreur d'écriture dans le fichier stopword");
                                                }
                                                break;

                                            case "-rm" :
                                                try {
                                                    String[] motsASupprimer = arg[2].split(",");
                                                    stopWord.removeMot(motsASupprimer);
                                                    out.writeUTF("Mot retiré des Stop Words !");
                                                    out.writeUTF("Lancement de la reindexation!");
                                                    Journal.resetJournal(stockagesDocuments, indexInverse, idToPath);
                                                    try {journal.supprimerJournal();
                                                    }catch (IOException e){
                                                        System.out.println("Erreur lors de la reindexation du journal.");
                                                    }
                                                    parcoursFichiers(cheminRepertoire, stockagesDocuments, indexInverse, idToPath, journal, stopWord);
                                                    out.writeUTF("Réindexation terminée !");
                                                } catch (IOException e) {
                                                    out.writeUTF("Erreur de réécriture dans le fichier stopword.");
                                                }
                                                break;

                                            case "-l":
                                                out.writeUTF(ANSI_BLEU + "Liste des mots vides actuels :" + ANSI_RESET);
                                                out.writeUTF(String.valueOf(stopWord));
                                                break;
                                        }
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;

                                    default:
                                        out.writeUTF("Commande inconnuee. Tapez -h pour afficher l'aide.");
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;
                                    case "-clean":
                                        try {
                                            journal.compacter(stockagesDocuments, indexInverse);
                                            out.writeUTF("Journal compacté avec succès.");
                                        } catch (IOException e) {
                                            out.writeUTF("Erreur lors de la compaction : " + e.getMessage());
                                        }
                                        out.writeUTF("END_OF_MESSAGE");
                                        break;
                                }
                            } else {
                                out.writeUTF("donnez le(s) parametre(s)");
                                out.writeUTF("END_OF_MESSAGE");
                            }
                        }
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }, "Client-" + clientsGroup.activeCount()).start();
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
        threadSurveillance.setPriority(Thread.MIN_PRIORITY);
        threadSurveillance.setDaemon(true);
        threadSurveillance.start();

        server(indexInverse, stockagesDocuments, idVersChemin, journal, stopWord, path);
        journal.fermer();
    }
}