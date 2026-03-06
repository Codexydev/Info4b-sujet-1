package Serveur;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

public class test {
    public static void main(String[] args) {
        Path start = Paths.get("/Users/antoine/Documents/etude/l2/s4/info4b/projet/src/main");

        try {
            Files.walk(start)
                    .forEach(
                            path -> {
                                if (Files.isRegularFile(path)) {
                                    System.out.println(path.toString());
                                }
                            }
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
