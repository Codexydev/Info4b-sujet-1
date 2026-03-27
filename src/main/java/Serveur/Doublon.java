package Serveur;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Doublon {
    private String f1;
    private String f2;

    public Doublon(String f1, String f2) {
        this.f1 = f1;
        this.f2 = f2;
    }

    public boolean EstDoublon() throws IOException, NoSuchAlgorithmException {
        byte[] dataFile1 = Files.readAllBytes(Paths.get(f1));
        byte[] hash1 = MessageDigest.getInstance("MD5").digest(dataFile1);
        String checksum1 = new BigInteger(1, hash1).toString(16);

        byte[] dataFile2 = Files.readAllBytes(Paths.get(f2));
        byte[] hash2 = MessageDigest.getInstance("MD5").digest(dataFile2);
        String checksum2 = new BigInteger(1, hash2).toString(16);

        return checksum1.equals(checksum2);
    }
}
