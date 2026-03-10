package Serveur;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class InvertedIndex {
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> indexGlobal = new ConcurrentHashMap<>();

    public void indexerMot(String mot, int id) {

        ConcurrentHashMap<Integer, Integer> indexDuMot = indexGlobal.computeIfAbsent(mot, k -> new ConcurrentHashMap<>());
        indexDuMot.merge(id, 1, Integer::sum);
    }

    public ConcurrentHashMap<Integer, Integer> getIndexDuMot(String mot) {
        return indexGlobal.getOrDefault(mot, new ConcurrentHashMap<>());
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> getIndexGlobal() {
        return indexGlobal;
    }


    // pr chaque mot de l'index global, si ce mot a été trouvé dans le document id
    // -> on l'ajoute à la map "résultat" avec sa fq.
    // On obtient le ConcurrentHashMap<String, Integer> attendu par journal.ecrireAjout().
    public ConcurrentHashMap<String, Integer> getMotsDocument(int id) {
        ConcurrentHashMap<String, Integer> mots = new ConcurrentHashMap<>();
        for (String mot : indexGlobal.keySet()) {
            ConcurrentHashMap<Integer, Integer> docs = indexGlobal.get(mot);
            if (docs.containsKey(id)) {
                mots.put(mot, docs.get(id));
            }
        }
        return mots;
    }}

