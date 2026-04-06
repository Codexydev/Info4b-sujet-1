package Serveur;

import java.nio.file.*;
import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;

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

    @Override
    public void run() {
        try{
            WatchService watchservice = FileSystems.getDefault().newWatchService();
            Path chemin = Paths.get(cheminRepertoire); //faut un chemin et pas juste un string sinon il sera pas utilisable pour faire des actions dessus avec NIO
            chemin.register(watchservice, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE); // permet de notifier le watchservice en cas de creation, modif ou suppression
            System.out.println("Lancement du watchservice sur le dossier: " + cheminRepertoire);

            while(true){
                WatchKey info = watchservice.take(); //boucle infinie mais avec un emethode bloquante, et on est notifier que si besoin
                for(WatchEvent<?> action : info.pollEvents()){
                    WatchEvent.Kind<?> typeAction = action.kind(); //type d'evenement (si on creeer?modifie?supprime?)
                    Path nomDuFichier = (Path) action.context(); //où est ce que l'action a été faite?
                    String cheminComplet = chemin.resolve(nomDuFichier).toString();
                    if (nomDuFichier.toString().startsWith(".")) {
                        continue; //condition qui permet d'ignorer les fichiers cachés
                    }
                    System.out.println("L'action " + typeAction + " a été réalisée sur " + nomDuFichier);
                    if(typeAction == StandardWatchEventKinds.ENTRY_CREATE){
                        if(stockagesDocuments.getMetaData(cheminComplet) == null){
                            int nouvelId;
                            synchronized(idVersChemin){
                                idVersChemin.addPath(cheminComplet);
                                nouvelId = idVersChemin.getIdCourant();
                            }
                            Main.indexerFichier(nouvelId, cheminComplet, stockagesDocuments, indexInverse, journal, true, stopWord); //la lecture de fichier est longue donc vaut mieux sortir du for pour eviter
                            //de monopoliser la clé du synchronized poour rien
                            System.out.println("nouveau fichier ajouté à l'indexeur de fichier");
                        }

                    } else if (typeAction == StandardWatchEventKinds.ENTRY_MODIFY) {
                        MetaDataDocument meta = stockagesDocuments.getMetaData(cheminComplet);
                        if (meta != null) {
                            int vraiId = meta.getId();
                            Main.indexerFichier(vraiId, cheminComplet, stockagesDocuments, indexInverse, journal, false, stopWord); //on met false car c'est pas un ajout mais une modif
                            System.out.println("fichier mis à jour");
                        }

                    } else if (typeAction == StandardWatchEventKinds.ENTRY_DELETE) {
                        stockagesDocuments.supprimerDocument(cheminComplet);
                        journal.ecrireSuppression(cheminComplet, System.currentTimeMillis());
                        System.out.println("fichier supprimé de l'indexeur de fichier");

                    }
                }
                boolean valide = info.reset();
                if (!valide) {
                    break; // On casse la boucle infinie, et on arrete le watchsrrvice si le dossier a été supprimer le info.reset() enverraa un false
                }
            }
        }catch (Exception e){
            System.out.println("le watchservice a cesser de fonctionner: " + e.getMessage());
        }
    }
}