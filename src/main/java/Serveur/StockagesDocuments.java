package Serveur;

import java.util.concurrent.ConcurrentHashMap;

public class StockagesDocuments {
    private final ConcurrentHashMap<String, DocumentMetaData> stockagesDocuments = new ConcurrentHashMap<>();

    public void ajouterDocument(int idDocument, String cheminRepertoire, long poids, long dateModification, long nombreTotalMots) {
        DocumentMetaData metaData = new DocumentMetaData(idDocument, cheminRepertoire, poids, dateModification, nombreTotalMots);
        stockagesDocuments.put(cheminRepertoire, metaData); // ajouter les metaData d'un document au stockages de tous les documents
    }

    public DocumentMetaData getMetaData(String cheminRepertoire) {
        return stockagesDocuments.get(cheminRepertoire);
    }
    
    public DocumentMetaData getMetaDataById(int id) {
        for (DocumentMetaData metaData : stockagesDocuments.values()) {
            if (metaData.getId() == id) {
                return metaData;
            }
        }
        return null;
    }

    public ConcurrentHashMap<String, DocumentMetaData> getStockagesDocuments() {
        return stockagesDocuments;
    }

    public void supprimerDocument(String chemin) {
        stockagesDocuments.remove(chemin);
    }

    public int getNombreDocuments() {
        return stockagesDocuments.size();
    }

}
