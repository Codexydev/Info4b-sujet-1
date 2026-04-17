package Serveur;

public class MetaDataDocument {
    private final String chemin;
    private final long poids;
    private final long dateModification;
    private final long totalMots;
    private final int id;

    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public MetaDataDocument(int id, String chemin, long poids, long dateModification, long totalMots) {
        this.chemin = chemin;
        this.poids = poids;
        this.dateModification = dateModification;
        this.totalMots = totalMots;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public long getTotalMots() {
        return totalMots;
    }

    public String getChemin() {
        return chemin;
    }

    public long getPoids() {
        return poids;
    }

    public long getDateModification() {
        return dateModification;
    }

    @Override
    public String toString() {
        return  ANSI_VERT + " id du fichier = " + ANSI_BLEU + id + "\n" + ANSI_RESET +
                ANSI_VERT + " chemin = " + ANSI_BLEU +"'"+ chemin + '\'' + "\n" + ANSI_RESET +
                ANSI_VERT + " poids = " + ANSI_BLEU + poids + "\n" + ANSI_RESET +
                ANSI_VERT + " dateModification = " + ANSI_BLEU + dateModification + "\n" + ANSI_RESET +
                ANSI_VERT + " totalMots = " + ANSI_BLEU + totalMots + "\n" + ANSI_RESET;
    }
}
