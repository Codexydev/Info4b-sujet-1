package Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
        static void main(String[] args) {

            try {
                System.out.println("Client started...");
                Socket socket = new Socket("localhost", 12345);

                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String str;

                do {
                    System.out.print("\n > ");
                    str = userInput.readLine();

                    out.println(str);

                    String reponse;
                    while ((reponse = in.readLine()) != null) {
                        if (reponse.equals("END_OF_MESSAGE")) {
                            break;
                        }
                        System.out.println(reponse);
                    }
                }
                while(!str.equals("q"));
                System.out.println("Fermeture du client...");
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
}
