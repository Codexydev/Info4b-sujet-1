package Serveur;

public class MetaModif {
    public void modifierMetadataPhysique(String chemin, String champ, String valeur) {
        try {
            // Utilisation de exiv2 pour modifier le champ Description (IPTC ou EXIF)
            // La syntaxe "set" écrase la valeur précédente
            ProcessBuilder pb = new ProcessBuilder(
                    "exiv2",
                    "-M", "set Iptc.Application2.Caption String " + valeur,
                    chemin
            );

            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Attente de la fin du processus pour garantir l'écriture
            p.waitFor();

        } catch (Exception e) {
            System.err.println("Erreur lors de la modification physique : " + e.getMessage());
        }
    }
}
