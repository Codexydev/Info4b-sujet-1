package Serveur;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class Recherche {
    private final InvertedIndex invertedIndex;
    private final DocumentStore documentStore;
    private final IdToPath idToPath;
    private final String[] motsRecherches;
    private final ArrayList<String> motsNonRecherches = new ArrayList<>();

    public Recherche(InvertedIndex invertedIndex, DocumentStore documentStore, IdToPath idToPath, String[] motsRecherches, String[] motsNonRecherches) {
        this.invertedIndex = invertedIndex;
        this.documentStore = documentStore;
        this.idToPath = idToPath;
        this.motsRecherches = motsRecherches;
        this.motsNonRecherches.addAll(Arrays.asList(motsNonRecherches));
    }

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
            if (motsNonRecherches.contains(mot)) continue;

            ConcurrentHashMap<Integer, Integer> indexDuMot = invertedIndex.getIndexDuMot(mot);
            for (Integer id : indexDuMot.keySet()) {
                boolean aExclure = false;

                for (String motExclu : motsNonRecherches) {
                    if (invertedIndex.getIndexDuMot(motExclu).containsKey(id)) {
                        aExclure = true;
                        break;
                    }
                }

                if (!aExclure) {
                    reponse.add("Document: " + idToPath.getPath(id) + ", Occurrences du mot: " + indexDuMot.get(id) + "\n");
                }
            }
        }

        for (String res : reponse) {
            reponseText += res;
        }
        if (reponseText.equals("")) return "Aucun résultat trouvé pour le(s) mot(s) recherché(s).";
        return reponseText;
    }
}