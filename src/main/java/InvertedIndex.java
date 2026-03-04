import java.util.concurrent.ConcurrentHashMap;

public class InvertedIndex {
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> indexGlobal = new ConcurrentHashMap<>();

    public void indexerMot(String mot, String cheminFichier) {
        ConcurrentHashMap<String, Integer> indexDuMot = indexGlobal.computeIfAbsent(mot, k -> new ConcurrentHashMap<>());
        indexDuMot.merge(cheminFichier, 1, Integer::sum);
    }

    public ConcurrentHashMap<String, Integer> getIndexDuMot(String mot) {
        return indexGlobal.getOrDefault(mot, new ConcurrentHashMap<>());
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> getIndexGlobal() {
        return indexGlobal;
    }
}