package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {

/*        System.out.print("IP > ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        System.out.print("Port > ");
        String port = scanner.nextLine();*/

        try {
            System.out.println("Client started...\n");
            System.out.println(" Commandes disponibles :\n" +
                    "-h : " + ANSI_BLEU + "afficher l'aide\n" + ANSI_RESET +
                    "-t" + ANSI_VERT + " <message> : " + ANSI_BLEU + "Afficher le message reçu pour tester la communication\n" + ANSI_RESET +
                    "-q : " + ANSI_BLEU + "Quitter la connexion\n" + ANSI_RESET +
                    "-s" + ANSI_VERT + " <mot(s) (sépration (,) ) > : " + ANSI_BLEU + "Rechercher un mot dans l'index et afficher les documents associés\n" + ANSI_RESET +
                    "-s " + ANSI_VERT + "<mot(s)> -- <mot(s) qui ne sera pas présent dans les fichier trouvé> : " + ANSI_BLEU + "separateur de mot ,\n" + ANSI_RESET +
                    "-m " + ANSI_VERT + "<chemin du document> : " + ANSI_BLEU + "Afficher les métadonnées d'un document donné\n" + ANSI_RESET +
                    "-m -rn " + ANSI_VERT + "<chemin du document> <chemin du document> " + ANSI_BLEU + "Renommé un fichier\n" + ANSI_RESET +
                    "-m -update" + ANSI_BLEU + "Permet de modifier les métadonnées\n" + ANSI_RESET +
                    "-p" + ANSI_VERT + " <chemin du document> : " + ANSI_BLEU + "affiche le texte du document\n" + ANSI_RESET +
                    "-ar" + ANSI_VERT + " <mot1 ET/OU/SAUF mot2 ET/OU/SAUF mots3 etc...> : " + ANSI_BLEU + "rechercher les fichiers de plusieurs mots (ET), d'un mot OU l'autre (OU), d'un fichier contenant un mot mais pas un autre(SAUF)\n" + ANSI_RESET +
                    "-kw " + ANSI_VERT + "add/remove/list/search : " + ANSI_BLEU + "Gérer les mots-clés utilisateur\n" + ANSI_RESET +
                    "-exif " + ANSI_VERT + "<chemin> : " + ANSI_BLEU + "Afficher les métadonnées EXIF d'une image\n" + ANSI_RESET +
                    "-sw " + ANSI_VERT + "add/remove <mot> : " + ANSI_BLEU + "Ajouter ou supprimer un stop-word\n" + ANSI_RESET);
            Socket socket = new Socket("localhost", 12345);
            /*Socket socket = new Socket(ip, Integer.parseInt(port));*/

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String str;

            do {
                System.out.print("\n > ");
                str = userInput.readLine(); // lecture entrée utilisateur

                out.println(str); // envoie str au server

                String reponse; // reponse server
                while ((reponse = in.readLine()) != null) {
                    if (reponse.equals("END_OF_MESSAGE")) { // vérifie la fin de la réponse du server
                        break;
                    }
                    System.out.println(reponse);
                }
            }
            while (!str.equals("q"));
            System.out.println("\nFermeture du client...");
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());

        }
    }
}
