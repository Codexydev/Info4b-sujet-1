public class DocumentMetaData {
    private String chemin;
    private int poids;
    private int dateModification;

    public DocumentMetaData(String chemin, int poids, int dateModification) {
        this.chemin = chemin;
        this.poids = poids;
        this.dateModification = dateModification;
    }

    public String getChemin() {
        return chemin;
    }

    public int getPoids() {
        return poids;
    }

    public int getDateModification() {
        return dateModification;
    }
}
