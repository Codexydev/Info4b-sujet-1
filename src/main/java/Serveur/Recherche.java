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
        this.motsRecherches = new String[motsRecherches.length];
        for (int i = 0; i < motsRecherches.length; i++) { //passage en minuscule de tous les mots recherchés
            this.motsRecherches[i] = motsRecherches[i].toLowerCase();
        }
        for (String motExclu : motsNonRecherches) { //idem avec les mots exclu
            this.motsNonRecherches.add(motExclu.toLowerCase());
        }
    }

    public Recherche(IndexInverse indexInverse, StockagesDocuments stockagesDocuments, IdVersChemin idVersChemin, String[] motsRecherches) {
        this.indexInverse = indexInverse;
        this.stockagesDocuments = stockagesDocuments;
        this.idVersChemin = idVersChemin;
        this.motsRecherches = new String[motsRecherches.length];
        for (int i = 0; i < motsRecherches.length; i++) { //pareil passage en minuscule
            this.motsRecherches[i] = motsRecherches[i].toLowerCase();
        }
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
        ConcurrentHashMap<String, Double> scoresParChemin = new ConcurrentHashMap<>();
        int totalDocs = stockagesDocuments.getNombreDocuments();

        for (String mot : motsRecherches) {
            if (motsNonRecherches.contains(mot)) continue;

            ConcurrentHashMap<Integer, Integer> indexDuMot = indexInverse.getDocumentsByMot(mot);
            if (indexDuMot == null || indexDuMot.isEmpty()) continue;

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
        List<Map.Entry<String, Double>> listeTriee = new ArrayList<>(scoresParChemin.entrySet());

        listeTriee.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        StringBuilder reponseText = new StringBuilder();
        for (Map.Entry<String, Double> entry : listeTriee) {
            double scoreArrondi = Math.round(entry.getValue() * 10000.0) / 10000.0;
            reponseText.append("Document: ").append(ANSI_VERT).append(entry.getKey()).append(ANSI_RESET)
                    .append(", Score TF-IDF: ").append(ANSI_BLEU).append(scoreArrondi).append(ANSI_RESET)
                    .append("\n");
        }

        if (reponseText.length() == 0) return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
        return reponseText.toString();
    }

    public String RechercheAvance(){
        java.util.HashSet<Integer> resultat = new java.util.HashSet<>(); //le HashSet évite les doublons
        java.util.HashSet<Integer> resultatFinal = new java.util.HashSet<>();
        String operateur = "ou"; //je passe le OU comme opérateur par défauts
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

            operateur = "ou"; //remet le ou par defaut
        }
        if(resultatFinal.isEmpty()){
            return "Aucun resultat trouvé!";
        }
        String reponse= "";
        for(Integer id: resultatFinal){
            reponse += idVersChemin.getChemin(id) + "\n";
        }
        return ANSI_BLEU+reponse+ANSI_RESET;

    }
}