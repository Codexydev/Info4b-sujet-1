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

/*      System.out.print("IP > ");
        Scanner scanner = new Scanner(System.in);
        String ip = scanner.nextLine();
        System.out.print("Port > ");
        String port = scanner.nextLine();*/

        try {
            System.out.println("Client started...\n");

            Socket socket = new Socket("localhost", 12345);
            /*Socket socket = new Socket(ip, Integer.parseInt(port));*/

            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
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
                str = userInput.readLine(); // lecture entrée utilisateur

                out.println(str); // envoie str au server

                // reponse server
                while ((reponse = in.readLine()) != null) {
                    if (reponse.equals("END_OF_MESSAGE")) { // vérifie la fin de la réponse du server
                        break;
                    }
                    System.out.println(reponse);
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
