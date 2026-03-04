public class DocumentMetaData {
    private String chemin;
    private long poids;
    private long dateModification;

    public DocumentMetaData(String chemin, int poids, int dateModification) {
        this.chemin = chemin;
        this.poids = poids;
        this.dateModification = dateModification;
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
