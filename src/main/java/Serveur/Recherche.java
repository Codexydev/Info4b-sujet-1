package Serveur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class Recherche {
    private final InvertedIndex invertedIndex;
    private final StockagesDocuments documentStore;
    private final IdToPath idToPath;
    private final String[] motsRecherches;
    private final ArrayList<String> motsNonRecherches = new ArrayList<>();

    public Recherche(InvertedIndex invertedIndex, StockagesDocuments documentStore, IdToPath idToPath, String[] motsRecherches, String[] motsNonRecherches) {
        this.invertedIndex = invertedIndex;
        this.documentStore = documentStore;
        this.idToPath = idToPath;
        this.motsRecherches = motsRecherches;
        this.motsNonRecherches.addAll(Arrays.asList(motsNonRecherches));
    }

    public Recherche(InvertedIndex invertedIndex, StockagesDocuments documentStore, IdToPath idToPath, String[] motsRecherches) {
        this.invertedIndex = invertedIndex;
        this.documentStore = documentStore;
        this.idToPath = idToPath;
        this.motsRecherches = motsRecherches;
    }

    // chgmt de effectuerRecherche afin d'implémenter TF-IDF
    public String effectuerRecherche() {
        TreeMap<Double, String> scores = new TreeMap<>(Collections.reverseOrder()); //Trie au lieu de ArrayList
        int totalDocs = documentStore.getNombreDocuments(); // IDF : log(totalDocs / nbDocsAvecMot)
        ConcurrentHashMap<String, Double> scoresParChemin = new ConcurrentHashMap<>(); // pr accumuler les scores
        for (String mot : motsRecherches) {
            if (motsNonRecherches.contains(mot)) continue;

            ConcurrentHashMap<Integer, Integer> indexDuMot = invertedIndex.getIndexDuMot(mot);
            //Ajout pr calcul IDF
            int nbDocsAvecMot = indexDuMot.size();
            if (nbDocsAvecMot == 0) continue;
            double idf = Math.log((double) totalDocs / nbDocsAvecMot);

            for (Integer id : indexDuMot.keySet()) {
                boolean aExclure = false;

                for (String motExclu : motsNonRecherches) {
                    if (invertedIndex.getIndexDuMot(motExclu).containsKey(id)) {
                        aExclure = true;
                        break;
                    }
                }

                if (!aExclure) {
                    MetaDataDocument metaData = documentStore.getMetaDataById(id);
                    if (metaData == null) continue;
                    double tf = (double) indexDuMot.get(id) / metaData.getTotalMots(); // TF = occurrences du mot dans ce doc / total mots du doc
                    double score = tf * idf; // TF * IDF
                    double scoreArrondi = Math.round(score * 10000.0) / 10000.0; // arrondi 4 décimales (affichage)
                    // Chgment pr pas écraser le score a chaque fois
                    String chemin = idToPath.getPath(id);
                    scoresParChemin.merge(chemin, score, Double::sum);
                }
            }
        }
        for (ConcurrentHashMap.Entry<String, Double> entry : scoresParChemin.entrySet()) {
            double scoreArrondi = Math.round(entry.getValue() * 10000.0) / 10000.0;
            scores.put(entry.getValue(), "Document: " + entry.getKey() + ", Score TF-IDF: " + scoreArrondi);
        }
        //Itération sur score.values (TreeMap) au lieu de l'ArrayList
        String reponseText = "";
        for (String resultat : scores.values()) {
            reponseText += resultat;
        }
        if (reponseText.equals("")) return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
        return reponseText;
    }
}