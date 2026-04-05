package Client;

import java.io.*;
import java.net.Socket;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

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
            /*Socket socket = new Socket(ip, Integer.parseInt(port));*/

            Terminal terminal = TerminalBuilder.builder().system(true).build();
            LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String str;
            String reponse;

            while ((reponse = in.readLine()) != null) {
                if (reponse.equals("END_OF_MESSAGE")) { // vérifie la fin de la réponse du server
                    break;
                }
                System.out.println(reponse);
            }

            do {
                System.out.print("\n > ");
                str = lineReader.readLine("\n > ");

                out.println(str); // envoie str au server

                // reponse server
                while ((reponse = in.readLine()) != null) {
                    if (reponse.equals("END_OF_MESSAGE")) { // vérifie la fin de la réponse du server
                        break;
                    } else if (reponse.startsWith("File_incomming...")) {
                        String[] donnees = reponse.split(" ");
                        String unité = "octets";
                        String taille_fichier_affiche_fin = "";
                        double taille_fichier = Long.parseLong(donnees[1]);
                        double taille_fichier_affiche = taille_fichier;
                        if (taille_fichier < 1000) {
                            taille_fichier_affiche_fin = String.format("%.0f", taille_fichier);
                        }
                        if (1000 <= taille_fichier && taille_fichier < 1000000) {
                            taille_fichier_affiche = taille_fichier / (double) 1000;
                            taille_fichier_affiche_fin = String.format("%.1f", taille_fichier_affiche);
                            unité = "ko";
                        } else if (1000000 <= taille_fichier && taille_fichier < 1000000000) {
                            taille_fichier_affiche = taille_fichier / (double) 1000000;
                            taille_fichier_affiche_fin = String.format("%.1f", taille_fichier_affiche);
                            unité = "Mo";
                        } else if (1000000000 <= taille_fichier) {
                            taille_fichier_affiche = taille_fichier / (double) 1000000000;
                            taille_fichier_affiche_fin = String.format("%.1f", taille_fichier_affiche);
                            unité = "Go";
                        }
                        System.out.println(ANSI_VERT + "taille du fichier téléchargé : " + ANSI_BLEU + taille_fichier_affiche_fin + unité + "\n" + ANSI_VERT + "nom du fichier : " + ANSI_BLEU + donnees[2] + ANSI_RESET);
                        File dossier = new File("downloads");
                        dossier.mkdirs();
                        String chemin_sauvegarde = "downloads/" + donnees[2];
                        FileOutputStream ecriture = new FileOutputStream(chemin_sauvegarde);
                        InputStream tuyau_reception = socket.getInputStream();
                        byte[] buffer = new byte[4096];
                        long total_lu = 0;
                        int quantite_lu = 0;
                        while(total_lu < taille_fichier && (quantite_lu = tuyau_reception.read(buffer)) != -1){
                            ecriture.write(buffer, 0, quantite_lu);
                            total_lu += quantite_lu;
                        }
                        ecriture.close();
                        System.out.println("Télechargement terminé!");


                        break;
                    } else {
                        System.out.println(reponse);
                    }

                }

            }
            while (!(str.equals("q") || str.equals("-q")));
            System.out.println("\nFermeture du client...");
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());

        }
    }
}
