package Serveur;

import java.util.concurrent.ConcurrentHashMap;

/*
 InvertedIndex permet le fonctionnement de l'index inversée
 Structure de données : ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> indexGlobal
    clé    -> mot (String)
    valeur -> indexDuMot (ConcurrentHashMap)
            clé    -> id (Int)
            valeur -> fréquence du mot dans le fichier (int)
*/
public class InvertedIndex {
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> indexGlobal = new ConcurrentHashMap<>();

    public void indexerMot(String mot, int id) {
        ConcurrentHashMap<Integer, Integer> indexDuMot = indexGlobal.computeIfAbsent(mot, k -> new ConcurrentHashMap<>()); // crée sous dico si nouveau mot
        indexDuMot.merge(id, 1, Integer::sum); // fait +1 à la fréquence si mot déjà connu dans le fichier sinon init à 1.
    }

    public ConcurrentHashMap<Integer, Integer> getIndexDuMot(String mot) { // renvoi la liste des documents et freq associe a un mot
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
    }

    public void restaurerFrequenceMot(String mot, int id, int frequence) {
        ConcurrentHashMap<Integer, Integer> indexDuMot = indexGlobal.computeIfAbsent(mot, k -> new ConcurrentHashMap<>());
        indexDuMot.put(id, frequence);
    }

}

