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

                String str;
                do {
                    System.out.print("\n > ");
                    str = userInput.readLine();

                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(str);

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String response = in.readLine();
                    System.out.println("Response from server: " + response);
                }
                while(!str.equals("q"));

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
}
