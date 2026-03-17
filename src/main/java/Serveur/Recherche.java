package Serveur;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Recherche {
    private final IndexInverse indexInverse;
    private final StockagesDocuments stockagesDocuments;
    private final IdVersChemin idVersChemin;
    private final String[] motsRecherches;
    private final ArrayList<String> motsNonRecherches = new ArrayList<>();

    public static final String ANSI_BLEU = "\u001B[34m";
    public static final String ANSI_VERT = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";

    public Recherche(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idVersChemin, String[] motsRecherches, String[] motsNonRecherches) {
        this.indexInverse = indexInverse;
        this.stockagesDocuments = stockagesDocuments;
        this.idVersChemin = idVersChemin;
        this.motsRecherches = motsRecherches;
        this.motsNonRecherches.addAll(Arrays.asList(motsNonRecherches));
    }

    public Recherche(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idVersChemin, String[] motsRecherches) {
        this.indexInverse = indexInverse;
        this.stockagesDocuments = stockagesDocuments;
        this.idVersChemin = idVersChemin;
        this.motsRecherches = motsRecherches;
    }

    /**
     * Effectue une recherche ordonne les resultats selon TF-IDF.<br>
     * IDF -> log( totalDocsConcerné / nbDocsAvecMotDedans )<br>
     * TF -> occurrences du mot dans ce doc / total mots du doc<br>
     * TF-IDF = Score -> IDF * TF
     *
     * @return String
     */
    public String effectuerRecherche() {
        TreeMap<Double, String> scores = new TreeMap<>(Collections.reverseOrder());
        ConcurrentHashMap<String, Double> scoresParChemin = new ConcurrentHashMap<>();
        int totalDocs = stockagesDocuments.getNombreDocuments();

        for (String mot : motsRecherches) {
            if (motsNonRecherches.contains(mot)) continue;

            ConcurrentHashMap<Integer, Integer> indexDuMot = indexInverse.getDocumentsByMot(mot);

            // calcul IDF
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
                    double scoreArrondi = Math.round(score * 10000.0) / 10000.0;
                    // Changement pr pas écraser le score à chaque fois
                    String chemin = idVersChemin.getChemin(id);
                    scoresParChemin.merge(chemin, score, Double::sum);
                }
            }
        }
        for (ConcurrentHashMap.Entry<String, Double> entry : scoresParChemin.entrySet()) {
            double scoreArrondi = Math.round(entry.getValue() * 10000.0) / 10000.0;
            scores.put(entry.getValue(), "Document: " + ANSI_VERT + entry.getKey() + ANSI_RESET + ", Score TF-IDF: " + ANSI_BLEU + scoreArrondi + ANSI_RESET);
        }
        //Itération sur score.values (TreeMap) au lieu de l'ArrayList
        String reponseText = "";
        for (String resultat : scores.values()) {
            reponseText += resultat + "\n";
        }

        if (reponseText.equals("")) return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
        return reponseText;
    }

    public String RechercheAvance(){
        java.util.HashSet<Integer> resultat = new java.util.HashSet<>(); //le HashSet évite les doublons
        java.util.HashSet<Integer> resultatFinal = new java.util.HashSet<>();
        String operateur = "OU"; //je passe le OU comme opérateur par défauts
        boolean premierMot = true;
        for(String mot : motsRecherches){
            if(mot.equals("ET") || mot.equals("OU") || mot.equals("SAUF")){
                operateur = mot;
                continue;
            }
            if(premierMot){
                resultatFinal.addAll(indexInverse.getDocumentsByMot(mot).keySet());
                premierMot = false;
            } else{
                resultat.addAll(indexInverse.getDocumentsByMot(mot).keySet());
                if(operateur.equals("ET")){
                    resultatFinal.retainAll(resultat);
                }
                if(operateur.equals("OU")){
                    resultatFinal.addAll(resultat);
                }
                if(operateur.equals("SAUF")){
                    resultatFinal.removeAll(resultat);
                }
                operateur = "OU";
                resultat.clear();
            }


            operateur = "OU"; //remet le ou par defaut
        }
        if(resultatFinal.isEmpty()){
            return "Aucun resultat trouvé!";
        }
        String reponse= "";
        for(Integer id: resultatFinal){
            reponse += idVersChemin.getChemin(id) + "\n";
        }
        return reponse;

    }
}