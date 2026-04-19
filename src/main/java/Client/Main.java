package Client;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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

            Scanner scanner = new Scanner(System.in);
            System.out.print("Adresse IP du serveur > ");
            String ip = scanner.nextLine().trim();
            System.out.print("Port (12345 default) > ");
            String port = scanner.nextLine().trim();
            Socket socket = new Socket(ip, Integer.parseInt(port));

            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String str;
            String reponse;

            while (true) {
                reponse = in.readUTF();
                if (reponse.equals("END_OF_MESSAGE")) break;
                System.out.println(reponse);
            }

            do {
                str = lineReader.readLine("\n > ");
                out.writeUTF(str);
                out.flush();

                if (str.equals("-q") || str.equals("q")) break;
                while (true) {
                    reponse = in.readUTF();

                    if (reponse.equals("END_OF_MESSAGE")) {
                        break;
                    } else if (reponse.startsWith("File_incoming...")) {
                        String[] donnees = reponse.split(" ", 3);
                        long taille_fichier = Long.parseLong(donnees[1]);
                        String nomFichier = donnees[2];

                        System.out.println(ANSI_VERT + "Début du téléchargement : " + ANSI_BLEU + nomFichier + ANSI_RESET);

                        String racine = System.getProperty("user.home"); // Permet de récupérer le dossier racine de l'utilisateur
                        File dossier = new File(racine, "Downloads"); // On se met ensuite sur le dossier des téléchargements
                        if (!dossier.exists()) {
                            dossier.mkdirs();
                        }
                        String chemin_sauvegarde = dossier.getAbsolutePath() + File.separator + nomFichier; // Construit le chemin absolu final avec File.separator pour gérer les \ ou /

                        FileOutputStream ecriture = new FileOutputStream(chemin_sauvegarde);
                        byte[] buffer = new byte[4096];
                        long total_lu = 0;
                        int quantite_lu;

                        int dernierPourcentage = -1;
                        long debutTemps = System.currentTimeMillis();
                        // Boucle bornée par la taille exacte déclarée par le serveur
                        while (total_lu < taille_fichier && (quantite_lu = in.read(buffer, 0, (int)Math.min(buffer.length, taille_fichier - total_lu))) != -1) {
                            ecriture.write(buffer, 0, quantite_lu);
                            total_lu += quantite_lu;

                            int pourcentage = (int) ((total_lu * 100) / taille_fichier);

                            if (pourcentage != dernierPourcentage) {
                                int nbBlocsRemplis = pourcentage / 5;
                                int nbBlocsVides = 20 - nbBlocsRemplis;

                                String barreRemplie = new String(new char[nbBlocsRemplis]).replace("\0", "█");
                                String barreVide = new String(new char[nbBlocsVides]).replace("\0", "░");

                                System.out.print("\r\033[K" + ANSI_BLEU + "Progression : [" + ANSI_VERT + barreRemplie + ANSI_RESET + barreVide + ANSI_BLEU + "] " + pourcentage + "%" + ANSI_RESET);
                                dernierPourcentage = pourcentage;
                            }

                        }

                        ecriture.close();

                        long tempsEcoule = System.currentTimeMillis() - debutTemps;
                        System.out.println("\n" + ANSI_VERT + "Téléchargement terminé ! (" + total_lu + " octets en " + tempsEcoule + " ms)" + ANSI_RESET);
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
