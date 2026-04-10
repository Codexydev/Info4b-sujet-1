package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {

/*      System.out.print("IP > ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        System.out.print("Port > ");
        String port = scanner.nextLine();*/

        try {
            System.out.println("Client started...\n");

            Socket socket = new Socket("localhost", 12345);

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            // NOUVEAU : Utilisation des DataStreams
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String str;
            String reponse;

            // Lecture du message d'accueil (Le Menu Help)
            while (true) {
                reponse = in.readUTF();
                if (reponse.equals("END_OF_MESSAGE")) break;
                System.out.println(reponse);
            }

            do {
                System.out.print("\n > ");
                str = userInput.readLine(); // lecture entrée clavier utilisateur

                out.writeUTF(str); // envoie la commande au serveur en UTF
                out.flush();

                // Boucle de réponse du serveur
                while (true) {
                    reponse = in.readUTF();

                    if (reponse.equals("END_OF_MESSAGE")) {
                        break;
                    } else if (reponse.startsWith("File_incomming...")) {

                        // Séparation en 3 parties max pour supporter les noms avec espaces (Bug #3)
                        String[] donnees = reponse.split(" ", 3);
                        long taille_fichier = Long.parseLong(donnees[1]);
                        String nomFichier = donnees[2];

                        System.out.println(ANSI_VERT + "Début du téléchargement : " + ANSI_BLEU + nomFichier + ANSI_RESET);

                        File dossier = new File("downloads");
                        dossier.mkdirs();
                        String chemin_sauvegarde = "downloads/" + nomFichier;

                        FileOutputStream ecriture = new FileOutputStream(chemin_sauvegarde);
                        byte[] buffer = new byte[4096];
                        long total_lu = 0;
                        int quantite_lu;

                        // LECTURE CHIRURGICALE : On ne lit que les octets qui appartiennent au fichier
                        while (total_lu < taille_fichier && (quantite_lu = in.read(buffer, 0, (int)Math.min(buffer.length, taille_fichier - total_lu))) != -1) {
                            ecriture.write(buffer, 0, quantite_lu);
                            total_lu += quantite_lu;
                        }

                        ecriture.close();
                        System.out.println(ANSI_VERT + "Téléchargement terminé ! (" + total_lu + " octets)" + ANSI_RESET);
                        break; // Le fichier est reçu, on sort de la boucle de réponse

                    } else {
                        System.out.println(reponse);
                    }
                }

            } while (!(str.equals("q") || str.equals("-q")));

            System.out.println("\nFermeture du client...");
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());

        }
    }
}
