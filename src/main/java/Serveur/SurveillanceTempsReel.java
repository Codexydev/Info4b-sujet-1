package Serveur;

import java.nio.file.*;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.ConcurrentHashMap;

public class SurveillanceTempsReel implements Runnable {

    private final String cheminRepertoire;
    private final StockagesDocuments stockagesDocuments;
    private final IndexInverse indexInverse;
    private final IdVersChemin idVersChemin;
    private final Journal journal;
    private final StopWord stopWord;

    public SurveillanceTempsReel(String cheminRepertoire, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, IdVersChemin idVersChemin, Journal journal, StopWord stopWord) {
        this.cheminRepertoire = cheminRepertoire;
        this.stockagesDocuments = stockagesDocuments;
        this.indexInverse = indexInverse;
        this.idVersChemin = idVersChemin;
        this.journal = journal;
        this.stopWord = stopWord;
    }

    private void enregistrerDossiers(Path racine, WatchService watchService) throws IOException {
        Files.walk(racine)
                .filter(Files::isDirectory)
                .forEach(dossier -> {
                    try {
                        dossier.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
                    } catch (IOException e) {
                        System.err.println("Erreur sur : " + dossier);
                    }
                });
    }

    @Override
    public void run() {
        try{
            WatchService watchservice = FileSystems.getDefault().newWatchService();
            Path chemin = Paths.get(cheminRepertoire); // Il faut un chemin et pas juste un string sinon il ne sera pas utilisable pour faire des actions dessus avec NIO.
            enregistrerDossiers(chemin, watchservice); // Permet de notifier le watchservice en cas de creation, modif ou suppression
            System.out.println("Lancement du watchservice sur le dossier: " + cheminRepertoire);

            while(true){
                WatchKey info = watchservice.take(); // Boucle infinie, mais avec une méthode bloquante
                for(WatchEvent<?> action : info.pollEvents()){
                    WatchEvent.Kind<?> typeAction = action.kind(); // Type d'évènements

                    if (typeAction == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    Path nomDuFichier = (Path) action.context();
                    if (nomDuFichier == null) continue; // Double sécurité
                    Path dossierConcerne = (Path) info.watchable();
                    String cheminComplet = dossierConcerne.resolve(nomDuFichier).toString();
                    if (nomDuFichier.toString().startsWith(".")) {
                        continue; // Condition qui permet d'ignorer les fichiers cachés
                    }
                    try {
                        Thread.sleep(150); // On laisse le temps à l'éditeur de texte de sauvegarder
                    } catch (InterruptedException ignored) {}
                    System.out.println("L'action " + typeAction + " a été réalisée sur " + nomDuFichier);
                    if(typeAction == StandardWatchEventKinds.ENTRY_CREATE){
                        if (Files.isDirectory(Paths.get(cheminComplet))) { // Permet de vérifier si c'est un dossier au lieu d'un fichier
                            enregistrerDossiers(Paths.get(cheminComplet), watchservice);
                            System.out.println("Nouveau dossier  : " + cheminComplet);
                            Main.parcoursFichiers(cheminComplet, stockagesDocuments, indexInverse, idVersChemin, journal, stopWord); // Scan si on ajoute un dossier deja plein
                        }
                        else {
                            if(stockagesDocuments.getMetaData(cheminComplet) == null){
                                int nouvelId;
                                synchronized(idVersChemin){
                                    idVersChemin.addPath(cheminComplet);
                                    nouvelId = idVersChemin.getIdFromPath(cheminComplet);
                                }
                                Main.indexerFichier(nouvelId, cheminComplet, stockagesDocuments, indexInverse, journal, true, stopWord);
                                System.out.println("nouveau fichier ajouté à l'indexeur de fichier");
                            }
                        }

                    } else if (typeAction == StandardWatchEventKinds.ENTRY_MODIFY) {
                        MetaDataDocument meta = stockagesDocuments.getMetaData(cheminComplet);
                        if (meta != null) {
                            int vraiId = meta.getId();
                            ConcurrentHashMap<String, Integer> anciensMots = indexInverse.getMotsDocument(vraiId);
                            for (String mot : anciensMots.keySet()) {
                                ConcurrentHashMap<Integer, Integer> listeDocsDuMot = indexInverse.getDocumentsByMot(mot);
                                if (listeDocsDuMot != null) {
                                    listeDocsDuMot.remove(vraiId);
                                }
                            }
                            Main.indexerFichier(vraiId, cheminComplet, stockagesDocuments, indexInverse, journal, false, stopWord); // On met false, ce n'est pas un ajout mais plutôt une modif
                            System.out.println("fichier mis à jour");
                        }

                    } else if (typeAction == StandardWatchEventKinds.ENTRY_DELETE) {
                        MetaDataDocument meta = stockagesDocuments.getMetaData(cheminComplet);
                        if (meta != null) {
                            int vraiId = meta.getId();
                            ConcurrentHashMap<String, Integer> anciensMots = indexInverse.getMotsDocument(vraiId);
                            for (String mot : anciensMots.keySet()) {
                                ConcurrentHashMap<Integer, Integer> listeDocsDuMot = indexInverse.getDocumentsByMot(mot);
                                if (listeDocsDuMot != null) {
                                    listeDocsDuMot.remove(vraiId);
                                }
                            }

                            stockagesDocuments.supprimerDocument(cheminComplet);
                            journal.ecrireSuppression(cheminComplet, System.currentTimeMillis());
                            System.out.println("Le fichier : " + " ' " +  cheminComplet + " ' " + " a été supprimé de l'indexeur de fichier.");
                        }
                    }
                }
                info.reset();
            }
        }catch (Exception e){
            System.out.println("le watchservice a cesser de fonctionner: " + e.getMessage());
        }
    }
}