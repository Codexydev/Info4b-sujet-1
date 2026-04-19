package Serveur;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Recherche {
    private final IndexInverse indexInverse;
    private final StockagesDocuments stockagesDocuments;
    private final IdVersChemin idVersChemin;
    private final String[] motsRecherches;
    private final ArrayList<String> motsNonRecherches = new ArrayList<>();
    private final StopWord stopWord;

    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Recherche(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idVersChemin, String[] motsRecherches, String[] motsNonRecherches, StopWord stopWord) {
        this.indexInverse = indexInverse;
        this.stockagesDocuments = stockagesDocuments;
        this.idVersChemin = idVersChemin;
        this.stopWord = stopWord;
        this.motsRecherches = new String[motsRecherches.length];

        for (int i = 0; i < motsRecherches.length; i++) { // Passage en minuscule
            this.motsRecherches[i] = motsRecherches[i].toLowerCase();
        }
        for (String motExclu : motsNonRecherches) { // Idem avec les mots exclus
            this.motsNonRecherches.add(motExclu.toLowerCase());
        }
    }

    public Recherche(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idVersChemin, String[] motsRecherches, StopWord stopWord) {
        this.indexInverse = indexInverse;
        this.stockagesDocuments = stockagesDocuments;
        this.idVersChemin = idVersChemin;
        this.stopWord = stopWord;
        this.motsRecherches = new String[motsRecherches.length];

        for (int i = 0; i < motsRecherches.length; i++) { // Passage en minuscule
            this.motsRecherches[i] = motsRecherches[i].toLowerCase();
        }
    }

    public String effectuerRecherche() {
        ConcurrentHashMap<String, Double> scoresParChemin = new ConcurrentHashMap<>();
        int totalDocs = stockagesDocuments.getNombreDocuments();

        for (String mot : motsRecherches) {
            if (motsNonRecherches.contains(mot)) continue;

            ConcurrentHashMap<Integer, Integer> indexDuMot = indexInverse.getDocumentsByMot(mot);
            if (indexDuMot == null || indexDuMot.isEmpty()) continue;

            // Calcul IDF
            int nbDocsAvecMot = indexDuMot.size();
            if (nbDocsAvecMot == 0) continue;
            double idf = Math.log((double) totalDocs / nbDocsAvecMot);

            for (Integer id : indexDuMot.keySet()) {
                boolean aExclure = false;
                for (String motExclu : motsNonRecherches) {
                    if (indexInverse.getDocumentsByMot(motExclu).containsKey(id)) {
                        aExclure = true;
                        break;
                    }
                }

                if (!aExclure) {
                    MetaDataDocument metaData = stockagesDocuments.getMetaDataById(id);
                    if (metaData == null) continue;

                    double tf = (double) indexDuMot.get(id) / metaData.getTotalMots();
                    double score = tf * idf;

                    String chemin = idVersChemin.getChemin(id);
                    scoresParChemin.merge(chemin, score, Double::sum);
                }
            }
        }

        List<Map.Entry<String, Double>> listeTriee = new ArrayList<>(scoresParChemin.entrySet());
        listeTriee.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        if (listeTriee.isEmpty()) {
            List<String> motsVidesTrouves = new ArrayList<>();
            for (String mot : motsRecherches) { // Permet de vérifier si les mots sont dans la liste des stopwords
                if (stopWord.getWords().contains(mot)) {
                    motsVidesTrouves.add(mot);
                }
            }
            if (!motsVidesTrouves.isEmpty()) {
                return "Aucun résultat : ce mot appartient à la liste des Stop Words";
            }
            return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
        }

        StringBuilder reponseText = new StringBuilder();
        for (Map.Entry<String, Double> entry : listeTriee) {
            String chemin = entry.getKey();
            Double score = entry.getValue();

            // On récupère les métadonnées pour lire les tags
            int id = idVersChemin.getIdFromPath(chemin);
            MetaDataDocument metaData = stockagesDocuments.getMetaDataById(id);

            // Construction de la ligne de résultat
            String ligne = "Document: " + chemin + ", Score TF-IDF: " + String.format("%.4f", score);

            // Si le document a des tags, on les affiche !
            if (metaData != null && !metaData.getTags().isEmpty()) {
                ligne += " " + ANSI_VERT + "[Tags: " + String.join(", ", metaData.getTags()) + "]" + ANSI_RESET;
            }

            reponseText.append(ligne).append("\n");
        }

        return reponseText.toString();
    }

    public String RechercheAvance(){
        java.util.HashSet<Integer> resultat = new java.util.HashSet<>();
        java.util.HashSet<Integer> resultatFinal = new java.util.HashSet<>();
        String operateur = "ou";
        boolean premierMot = true;

        for(String mot : motsRecherches){
            if(mot.equals("et") || mot.equals("ou") || mot.equals("sauf")){
                operateur = mot;
                continue;
            }
            if(premierMot){
                resultatFinal.addAll(indexInverse.getDocumentsByMot(mot).keySet());
                premierMot = false;
            } else{
                resultat.addAll(indexInverse.getDocumentsByMot(mot).keySet());
                if(operateur.equals("et")){
                    resultatFinal.retainAll(resultat);
                }
                if(operateur.equals("ou")){
                    resultatFinal.addAll(resultat);
                }
                if(operateur.equals("sauf")){
                    resultatFinal.removeAll(resultat);
                }
                operateur = "ou";
                resultat.clear();
            }
            operateur = "ou";
        }

        if(resultatFinal.isEmpty()){
            List<String> motsVidesTrouves = new ArrayList<>();
            for (String mot : motsRecherches) {
                if (stopWord.getWords().contains(mot)) {
                    motsVidesTrouves.add(mot);
                }
            }
            if (!motsVidesTrouves.isEmpty()) {
                return "Aucun résultat : ce mot appartient à la liste des Stop Words.";
            }
            return "Aucun résultat trouvé!";
        }

        String reponse= "";
        for(Integer id: resultatFinal){
            reponse += idVersChemin.getChemin(id) + "\n";
        }
        return ANSI_BLEU + reponse + ANSI_RESET;
    }
}