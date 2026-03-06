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
}