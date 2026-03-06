package Serveur;

public class DocumentMetaData {
    private String chemin;
    private long poids;
    private long dateModification;
    private long totalMots;
    private int id;

    public DocumentMetaData(int id,String chemin, long poids, long dateModification, long totalMots) {
        this.chemin = chemin;
        this.poids = poids;
        this.dateModification = dateModification;
        this.totalMots = totalMots;
        this.id = id;
    }

    public int getId() {
        return id;
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
