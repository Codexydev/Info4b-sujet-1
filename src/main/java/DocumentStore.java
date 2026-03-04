import java.util.concurrent.ConcurrentHashMap;

public class DocumentStore {
        private final ConcurrentHashMap<String, DocumentMetaData> documentStore = new ConcurrentHashMap<>();

        public void ajouterDocument(String chemin, long poids, long dateModification, long totalMots) {
            DocumentMetaData metaData = new DocumentMetaData(chemin, poids, dateModification, totalMots); // totalMots à revoir
            documentStore.put(chemin, metaData);
        }

        public DocumentMetaData getDocumentMetaData(String chemin) {
            return documentStore.get(chemin);
        }

        public ConcurrentHashMap<String, DocumentMetaData> getDocumentStore() {
            return documentStore;
        }

        public void supprimerDocument(String chemin) {
            documentStore.remove(chemin);
        }

        public int getNombreDocuments() {
            return documentStore.size();
        }

}
