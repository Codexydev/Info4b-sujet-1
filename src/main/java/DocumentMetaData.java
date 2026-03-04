public class DocumentMetaData {
    private String chemin;
    private long poids;
    private long dateModification;
    private long totalMots;

    public DocumentMetaData(String chemin, long poids, long dateModification, long totalMots) {
        this.chemin = chemin;
        this.poids = poids;
        this.dateModification = dateModification;
        this.totalMots = totalMots;
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
}
