package Serveur;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class StockagesDocuments {
    private final Map<String, MetaDataDocument> stockagesDocuments = Collections.synchronizedMap(new CacheLRU<>(100));

    public void ajouterDocument(int idDocument, String cheminRepertoire, long poids, long dateModification, long nombreTotalMots) {
        MetaDataDocument metaData = new MetaDataDocument(idDocument, cheminRepertoire, poids, dateModification, nombreTotalMots);
        stockagesDocuments.put(cheminRepertoire, metaData); // ajouter les metaData d'un document au stockages de tous les documents
    }

    public MetaDataDocument getMetaData(String cheminRepertoire) {
        return stockagesDocuments.get(cheminRepertoire);
    }

    public MetaDataDocument getMetaDataById(int id) {
        for (MetaDataDocument metaData : stockagesDocuments.values()) {
            if (metaData.getId() == id) {
                return metaData;
            }
        }
        return null;
    }

    public ConcurrentHashMap<String, MetaDataDocument> getStockagesDocuments() {
        return new ConcurrentHashMap<>(stockagesDocuments);
    }

    public void supprimerDocument(String chemin) {
        stockagesDocuments.remove(chemin);
    }

    public int getNombreDocuments() {
        return stockagesDocuments.size();
    }

    public void clear() {
        stockagesDocuments.clear();

    }
}
