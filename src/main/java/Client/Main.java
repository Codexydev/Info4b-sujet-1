package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {

        try {
            System.out.println("Client started...");
            Socket socket = new Socket("localhost", 12345);

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
            while(!str.equals("q"));
            System.out.println("\nFermeture du client...");
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur : " + e.getMessage());

        }
    }
}
