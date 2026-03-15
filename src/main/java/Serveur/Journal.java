package Serveur;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Journal {

    private final String chemin;
    private final BufferedWriter writer;

    // buffer borné
    private final ArrayList<String> fileOperations = new ArrayList<>();
    private static final int CAPACITE_MAX = 100;
    private volatile boolean actif = true;

    private final Thread threadEcrivain;


    public Journal(String chemin) throws IOException {
        this.chemin = chemin;
        this.writer = new BufferedWriter(new FileWriter(chemin, true));

        threadEcrivain = new Thread(new EcrivainJournal(), "Thread-Journal-Ecrivain");
        threadEcrivain.setDaemon(true);
        threadEcrivain.start();
    }

    /**
     * Tâche exécutée par le thread Consommateur.
     * Lit les opérations dans le buffer mémoire et les écrit physiquement sur le disque.
     */
    private class EcrivainJournal implements Runnable {
        @Override
        public void run() {
            while (true) {
                String operation = null;

                synchronized (fileOperations) {
                    while (fileOperations.isEmpty() && actif) {
                        try {
                            fileOperations.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (!actif && fileOperations.isEmpty()) {
                        break;
                    }

                    // On consomme le premier élément
                    operation = fileOperations.remove(0);

                    fileOperations.notifyAll();
                }

                // Écriture sur disque
                if (operation != null) {
                    try {
                        writer.write(operation);
                        writer.newLine();
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Ajoute une opération dans le buffer borné. Bloque le thread appelant si le buffer est plein.
     *
     * @param operation La chaîne de caractères formatée représentant l'opération à loguer.
     */
    private void ajouterOperation(String operation) {
        // Synchronized(o) pr assurer exclusion mutuelle sur le buffer partagé
        synchronized (fileOperations) {
            // Si buffer plein -> producteurs bloqués
            // o.wait() pr attente passive tant que buffer plein
            while (fileOperations.size() >= CAPACITE_MAX && actif) {
                try {
                    fileOperations.wait();
                } catch (InterruptedException e) {
                    // InterruptedException à capturer lors d'une attente
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            // si le journal a été fermé pendant l'ajout on annule l'ajout
            if (!actif) return;

            fileOperations.add(operation);

            // réveille le consommateur
            fileOperations.notifyAll();
        }
    }

    /**
     * Formatage map
     *
     * @param mots La map contenant les mots et leurs occurrences.
     * @return (ex: "mot1:2,mot2:5") ou une chaîne vide si la map est nulle/vide.
     */
    public String formaterMots(ConcurrentHashMap<String, Integer> mots) {
        if (mots == null || mots.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, Integer> entry : mots.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    public void ecrireAjout(String chemin, long datemodif, long taille, ConcurrentHashMap<String, Integer> mots) {
        ajouterOperation("AJOUT;" + chemin + ";" + datemodif + ";" + taille + ";" + formaterMots(mots));
    }

    public void ecrireSuppression(String chemin, long datemodif) {
        ajouterOperation("SUPPRESSION;" + chemin + ";" + datemodif + ";0;");
    }

    public void ecrireMiseAJour(String chemin, long datemodif, long taille, ConcurrentHashMap<String, Integer> mots) {
        ajouterOperation("MISE_A_JOUR;" + chemin + ";" + datemodif + ";" + taille + ";" + formaterMots(mots));
    }

    public void fermer() {
        // notifyAll() pr débloquer thread s'il est en wait()
        synchronized (fileOperations) {
            actif = false;
            fileOperations.notifyAll();
        }

        try {
            threadEcrivain.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Lit le journal.csv au démarrage pour restaurer l'état en mémoire.
     * Reconstruit le StockageDocument et l'IndexInverse
     *
     * @param cheminJournal      chemin journal
     * @param stockagesDocuments stockageDocuments
     * @param indexInverse       indexinversé
     * @param idVersChemin       idVersChemin
     */
    public static void restaurerDepuisJournal(String cheminJournal, StockagesDocuments stockagesDocuments, IndexInverse indexInverse, IdVersChemin idVersChemin) {
        File fichier = new File(cheminJournal);
        if (!fichier.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(cheminJournal))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] champs = ligne.split(";");

                if (champs.length < 2) continue;

                String typeAction = champs[0];
                String chemin = champs[1];

                if (typeAction.equals("AJOUT") || typeAction.equals("MISE_A_JOUR")) {
                    long dateModif = Long.parseLong(champs[2]);
                    long taille = Long.parseLong(champs[3]);

                    int docId;
                    MetaDataDocument meta = stockagesDocuments.getMetaData(chemin);

                    if (meta != null) {
                        docId = meta.getId();
                    } else {
                        idVersChemin.addPath(chemin);
                        docId = idVersChemin.getIdCourant();
                    }

                    ConcurrentHashMap<String, Integer> frequence = new ConcurrentHashMap<>();
                    if (champs.length > 4 && !champs[4].isEmpty()) {
                        for (String motFreq : champs[4].split(",")) {
                            String[] paire = motFreq.split(":");
                            if (paire.length == 2) {
                                int freq = Integer.parseInt(paire[1]);
                                frequence.put(paire[0], freq);
                                indexInverse.restaurerFrequenceMot(paire[0], docId, freq);
                            }
                        }
                    }

                    stockagesDocuments.ajouterDocument(docId, chemin, taille, dateModif, frequence.size());

                } else if (typeAction.equals("SUPPRESSION")) {
                    stockagesDocuments.supprimerDocument(chemin);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Détecte les fichiers supprimés ou modifiés hors ligne et met à jour le système
     *
     * @param stockagesDocuments stockageDocument
     * @param indexInverse       indexinverse
     * @param journal            journal
     */
    public static synchronized void reconcilier(StockagesDocuments stockagesDocuments, IndexInverse indexInverse, Journal journal) {
        for (String chemin : new ArrayList<>(stockagesDocuments.getStockagesDocuments().keySet())) {
            File fichier = new File(chemin);
            if (!fichier.exists()) {
                stockagesDocuments.supprimerDocument(chemin);
                journal.ecrireSuppression(chemin, 0); // appel producteur
            } else {
                long dateOS = fichier.lastModified();
                MetaDataDocument meta = stockagesDocuments.getMetaData(chemin);

                if (meta != null) {
                    long dateStockee = meta.getDateModification();

                    if (dateOS > dateStockee) {
                        int vraiId = meta.getId();

                        Main.indexerFichier(vraiId, chemin, stockagesDocuments, indexInverse, journal, false);
                    }
                }
            }
        }
    }
}