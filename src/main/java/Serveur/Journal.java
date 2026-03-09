package Serveur;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Journal {

    private final String chemin;
    private final BufferedWriter writer;

    // buffer borné à N places, partagé entre producteurs et consommateur
    private final ArrayList<String> fileOperations = new ArrayList<>();
    private static final int CAPACITE_MAX = 100; // taille max du buffer borné

    // sans utiliser stop() qui est déprécié depuis Java 1.5
    private boolean actif = true;

    // créa thread via "implements Runnable"
    // setName() pour nommer le thread
    private final Thread threadEcrivain;

    public Journal(String chemin) throws IOException {
        this.chemin = chemin;
        this.writer = new BufferedWriter(new FileWriter(chemin, true));

        // Constructeur Thread(Runnable, String) avec nom du thread
        // Démarrage du thread avec start()
        threadEcrivain = new Thread(new EcrivainJournal(), "Thread-Journal-Ecrivain");
        threadEcrivain.setDaemon(true);
        threadEcrivain.start();
    }

    // Strat "implements Runnable" pour déf code du thread
    // Ce thread est le CONSOMMATEUR du buffer
    private class EcrivainJournal implements Runnable {
        @Override
        public void run() {
            while (actif || !fileOperations.isEmpty()) {
                String operation = null;

                // Synchronized protège l'accès concurrent au buffer partagé
                // Syntaxe synchronized(o) { <section critique> }
                synchronized (fileOperations) {

                    // si buffer vide -> consommateur attend que producteurs produisent
                    // o.wait() libère accès exclusif et bloque le thread en attente d'un notify()
                    while (fileOperations.isEmpty() && actif) {
                        try {
                            fileOperations.wait(); // attente passive
                        } catch (InterruptedException e) {
                            // InterruptedException à capturer lors d'une attente
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (!fileOperations.isEmpty()) {
                        operation = fileOperations.remove(0); // consomme le 1er élmt du buffer

                        // o.notifyAll() réveille tt les threads bloqués sur objt
                        // ici -> réveil les producteurs potentiellement bloqués car buffer était plein
                        fileOperations.notifyAll();
                    }
                }

                // Écriture effectuée hors du verrou sur fileOperations pr minimiser section critique
                // Synchronized sur writer = section critique pr l'accès fichier
                if (operation != null) {
                    synchronized (writer) {
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
    }

    // Méth PRODUCTEUR
    // ajt une opération dans buffer borné, bloque si buffer plein
    private void ajouterOperation(String operation) {

        // Synchronized(o) pr assurer exclusion mutuelle sur le buffer partagé
        synchronized (fileOperations) {

            // Si buffer plein -> producteurs bloqués
            // o.wait() pr attente passive tant que buffer plein
            while (fileOperations.size() >= CAPACITE_MAX) {
                try {
                    fileOperations.wait();
                } catch (InterruptedException e) {
                    // InterruptedException à capturer lors d'une attente
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            fileOperations.add(operation);

            // notifyAll() réveille le thread consommateur bloqué sur wait()
            fileOperations.notifyAll();
        }
    }

    // formate les occurrences de mots + utilise ConcurrentHashMap
    public String formaterMots(ConcurrentHashMap<String, Integer> mots) {
        if (mots == null || mots.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, Integer> entry : mots.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    // écriture dans fichier texte via BufferedWriter
    // appel producteur, l'opération est mise dans le buffer
    public void ecrireAjout(String chemin, long datemodif, long taille,
                            ConcurrentHashMap<String, Integer> mots) {
        ajouterOperation("AJOUT;" + chemin + ";" + datemodif + ";" + taille + ";" + formaterMots(mots));
    }

    // écriture dans un fichier texte (again)
    public void ecrireSuppression(String chemin, long datemodif) {
        ajouterOperation("SUPPRESSION;" + chemin + ";" + datemodif + ";0;");
    }

    // écriture dans un fichier texte (again again)
    public void ecrireMiseAJour(String chemin, long datemodif, long taille,
                                ConcurrentHashMap<String, Integer> mots) {
        ajouterOperation("MISE_A_JOUR;" + chemin + ";" + datemodif + ";" + taille + ";" + formaterMots(mots));
    }

    // arrêt propre d'un thread sans utiliser stop() (again)
    // join() bloque le thread appelant jusqu'à la fin du thread écrivain
    public void fermer() {

        // notifyAll() pr débloquer thread s'il est en wait()
        synchronized (fileOperations) {
            actif = false;
            fileOperations.notifyAll();
        }

        // t1.join() bloque le thread appelant jusqu'à la terminaison du thread cible
        // garantit que ttes les opérations en attente sont écrites avant de fermer le fichier
        try {
            threadEcrivain.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // synchronized pour protéger la fermeture du writer
        synchronized (writer) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // lecture ligne à ligne avec BufferedReader
    // restauration de l'état depuis un fichier persistant (sérialisation)
    public static void restaurerDepuisJournal(String cheminJournal,
                                              DocumentStore documentStore,
                                              InvertedIndex invertedIndex) {
        File fichier = new File(cheminJournal);
        if (!fichier.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(cheminJournal))) {
            String ligne;
            int id = 0;
            while ((ligne = br.readLine()) != null) {
                String[] champs = ligne.split(";");
                if (champs[0].equals("AJOUT") || champs[0].equals("MISE_A_JOUR")) {

                    // ConcurrentHashMap : structure thread-safe pr stocker les fréquences
                    // justifiée ici car documentStore et invertedIndex peuvent être accédés par plusieurs threads (TP2)
                    ConcurrentHashMap<String, Integer> frequence = new ConcurrentHashMap<>();
                    if (champs.length > 4 && !champs[4].isEmpty()) {
                        for (String motFreq : champs[4].split(",")) {
                            String[] paire = motFreq.split(":");
                            if (paire.length == 2) {
                                frequence.put(paire[0], Integer.parseInt(paire[1]));
                            }
                        }
                    }
                    documentStore.ajouterDocument(id, champs[1],
                            Long.parseLong(champs[3]),
                            Long.parseLong(champs[2]),
                            frequence.size());
                    for (ConcurrentHashMap.Entry<String, Integer> entry : frequence.entrySet()) {
                        invertedIndex.indexerMot(entry.getKey(), Integer.parseInt(champs[2]));
                    }
                    id++;
                } else if (champs[0].equals("SUPPRESSION")) {
                    documentStore.supprimerDocument(champs[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // "static synchronized" = verrou sur classe entière
    // empêche 2 appels simultanés à reconcilier() de provoquer des incohérences sur documentStore
    public static synchronized void reconcilier(DocumentStore documentStore,
                                                InvertedIndex invertedIndex,
                                                Journal journal) {
        for (String chemin : documentStore.getDocumentStore().keySet()) {
            File fichier = new File(chemin);
            if (!fichier.exists()) {
                documentStore.supprimerDocument(chemin);
                journal.ecrireSuppression(chemin, 0); // TP2 - Exercice 2 : appel producteur
            } else {
                long dateOS = fichier.lastModified();
                long dateStockee = documentStore.getDocumentMetaData(chemin).getDateModification();
                if (dateOS > dateStockee) {
                    Main.indexFile(0, chemin, documentStore, invertedIndex);
                    journal.ecrireMiseAJour(chemin, dateOS, fichier.length(), new ConcurrentHashMap<>());
                }
            }
        }
    }
}