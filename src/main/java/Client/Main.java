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

                while (true) {
                    reponse = in.readUTF();

                    if (reponse.equals("END_OF_MESSAGE")) {
                        break;
                    } else if (reponse.startsWith("File_incomming...")) {
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

                        while (total_lu < taille_fichier && (quantite_lu = in.read(buffer, 0, (int)Math.min(buffer.length, taille_fichier - total_lu))) != -1) {
                            ecriture.write(buffer, 0, quantite_lu);
                            total_lu += quantite_lu;
                        }

                        ecriture.close();
                        System.out.println(ANSI_VERT + "Téléchargement terminé ! (" + total_lu + " octets)" + ANSI_RESET);
                        break;

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
