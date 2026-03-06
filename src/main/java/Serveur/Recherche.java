package Serveur;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Recherche {
    private InvertedIndex invertedIndex;
    private DocumentStore documentStore;
    private IdToPath idToPath;
    private String[] motsRecherches;

    public Recherche(InvertedIndex invertedIndex, DocumentStore documentStore, IdToPath idToPath, String[] motsRecherches) {
        this.invertedIndex = invertedIndex;
        this.documentStore = documentStore;
        this.idToPath = idToPath;
        this.motsRecherches = motsRecherches;
    }

        public String effectuerRecherche() {
            ArrayList<String> reponse = new ArrayList<>();
            String reponseText = "";

            for (String mot : motsRecherches) {
                ConcurrentHashMap<Integer, Integer> indexDuMot = invertedIndex.getIndexDuMot(mot);
                for (Integer id : indexDuMot.keySet()) {

                    reponse.add("Document: " + idToPath.getPath(id) + ", Occurrences du mot: " + indexDuMot.get(id)+"\n");
                }
            }

            for (String res : reponse) {
                reponseText+=res;
            }
            if (reponseText.equals("")) return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
            return reponseText;
        }
}