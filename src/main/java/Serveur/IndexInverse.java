package Serveur;

import java.util.concurrent.ConcurrentHashMap;

/*
 InvertedIndex permet le fonctionnement de l'index inversée
 Structure de données : ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> indexGlobal
    clé    -> mot (String)
    valeur -> sousDico (ConcurrentHashMap)
            clé    -> id (Int)
            valeur -> fréquence du mot dans le fichier (int)
*/
public class IndexInverse {
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> indexInverse = new ConcurrentHashMap<>();

    public void indexerMot(String mot, int idDocument) {
        // crée sous dico si nouveau mot
        ConcurrentHashMap<Integer, Integer> sousDico = indexInverse.computeIfAbsent(mot, k -> new ConcurrentHashMap<>());
        // fait +1 à la fréquence si mot déjà connu dans le fichier sinon init à 1.
        sousDico.merge(idDocument, 1, Integer::sum);
    }

    /**
     * Retourne sousDico (ConcurrentHashMap des id des documents et des frequences du mot dans le document)<br>
     * <b>sousDico :</b>
     * <ul>
     * <li><b>Clé</b> : ID du document</li>
     * <li><b>Valeur</b> : Fréquence du mot dans ce document</li>
     * </ul>
     * @param mot (String)
     * @return ConcurrentHashMap (Int, Int)
     */
    public ConcurrentHashMap<Integer, Integer> getDocumentsByMot(String mot) {
        return indexInverse.getOrDefault(mot, new ConcurrentHashMap<>());
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> getIndexInverse() {
        return indexInverse;
    }

    /**
     * Récupère tous les mots indexés présents dans un document spécifique avec leur fréquence
     * @param id d'un document (fait le lien avec IdVersChemin).
     * @return ConcurrentHashMap(String, Integer) ([mot] -> [frequence])
     */
    public ConcurrentHashMap<String, Integer> getMotsDocument(int id) {
        ConcurrentHashMap<String, Integer> motsEtFreq = new ConcurrentHashMap<>();
        for (String mot : indexInverse.keySet()) {
            //obtenir le sous dico à partir d'un mot
            ConcurrentHashMap<Integer, Integer> sousDico = indexInverse.get(mot);
            if (sousDico.containsKey(id)) {
                motsEtFreq.put(mot, sousDico.get(id));
            }
        }
        return motsEtFreq;
    }

    /**
     * Permet de mettre à jour le sousDico. Si le sousDico éxiste pas pour le mot sa le crée sinon sa met
     * simplement pour le mot concerné l'id du document et la fréquence du mot.
     * @param mot
     * @param id
     * @param frequence
     */
    public void restaurerFrequenceMot(String mot, int id, int frequence) {
        ConcurrentHashMap<Integer, Integer> sousDico = indexInverse.computeIfAbsent(mot, k -> new ConcurrentHashMap<>());
        sousDico.put(id, frequence);
    }

    /**
     * Supprime totalement un mot de l'index global (rétroactivité des mots vides).
     */
    public void supprimerMot(String mot) {
        indexInverse.remove(mot);
    }

    public void reinitialiserIndex() {
        indexInverse.clear();
    }
}