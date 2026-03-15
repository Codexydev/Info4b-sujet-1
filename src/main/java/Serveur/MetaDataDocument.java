package Serveur;

public class MetaDataDocument {
    private final String chemin;
    private final long poids;
    private final long dateModification;
    private final long totalMots;
    private final int id;

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
        return "DocumentMetaData{" +
                "id=" + id +
                ", chemin='" + chemin + '\'' +
                ", poids=" + poids +
                ", dateModification=" + dateModification +
                ", totalMots=" + totalMots +
                '}';
    }
}
