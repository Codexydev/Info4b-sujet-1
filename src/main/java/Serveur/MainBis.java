package Serveur;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MainBis {

    public static void main(String[] args) {

        String cheminJournal = "journal.csv";

        DocumentStore documentStore = new DocumentStore();
        InvertedIndex invertedIndex = new InvertedIndex();

        // ============================================================
        // TEST 1 — ecrireAjout()
        // TP4 - Exercice 1 : écriture dans un fichier texte via BufferedWriter
        // TP2 - Exercice 2 : ecrireAjout() est un PRODUCTEUR qui dépose dans le buffer
        // TP1 - Exercice 1 : le thread EcrivainJournal (CONSOMMATEUR) lit et écrit en parallèle
        // ============================================================
        System.out.println("=== TEST 1 : ecrireAjout() ===");
        try {
            // TP1 - Exercice 1 question 4 : le constructeur démarre le thread écrivain via start()
            Journal journal = new Journal(cheminJournal);

            // ConcurrentHashMap : structure thread-safe pour les mots (TP2 - accès concurrent)
            ConcurrentHashMap<String, Integer> mots1 = new ConcurrentHashMap<>();
            mots1.put("linux", 12);
            mots1.put("systeme", 5);
            mots1.put("memoire", 1);

            // TP2 - Exercice 2 : appel producteur, dépose l'opération dans le buffer borné
            journal.ecrireAjout("/home/user/tp.pdf", 1713560000L, 204800L, mots1);
            System.out.println("✅ AJOUT écrit dans le journal");

            // ============================================================
            // TEST 2 — ecrireMiseAJour()
            // TP4 - Exercice 1 : écriture dans un fichier texte
            // TP2 - Exercice 2 : appel producteur
            // ============================================================
            System.out.println("\n=== TEST 2 : ecrireMiseAJour() ===");
            ConcurrentHashMap<String, Integer> mots2 = new ConcurrentHashMap<>();
            mots2.put("linux", 15);
            mots2.put("systeme", 5);

            // TP2 - Exercice 2 : appel producteur, dépose dans le buffer
            journal.ecrireMiseAJour("/home/user/tp.pdf", 1713999999L, 204800L, mots2);
            System.out.println("✅ MISE_A_JOUR écrite dans le journal");

            // ============================================================
            // TEST 3 — ecrireSuppression()
            // TP4 - Exercice 1 : écriture dans un fichier texte
            // TP2 - Exercice 2 : appel producteur
            // ============================================================
            System.out.println("\n=== TEST 3 : ecrireSuppression() ===");

            // TP2 - Exercice 2 : appel producteur, dépose dans le buffer
            journal.ecrireSuppression("/home/user/vieux.txt", 1713560000L);
            System.out.println("✅ SUPPRESSION écrite dans le journal");

            // TP1 - Annexe 1 : arrêt propre du thread sans stop()
            // TP1 - Encadré 2 : join() attend la fin du thread écrivain avant de fermer le fichier
            journal.fermer();
            System.out.println("\n✅ Journal fermé proprement");

        } catch (IOException e) {
            System.out.println("❌ Erreur lors de l'écriture : " + e.getMessage());
        }

        // ============================================================
        // TEST 4 — restaurerDepuisJournal()
        // TP4 - Exercice 1 question 1 : lecture ligne à ligne avec BufferedReader
        // TP4 - Exercice 3 question 4 : restauration de l'état depuis un fichier persistant
        // ============================================================
        System.out.println("\n=== TEST 4 : restaurerDepuisJournal() ===");
        Journal.restaurerDepuisJournal(cheminJournal, documentStore, invertedIndex,new IdToPath());

        System.out.println("Documents restaurés : " + documentStore.getNombreDocuments());
        System.out.println("Contenu du DocumentStore : " + documentStore.getDocumentStore());
        System.out.println("Contenu de l'InvertedIndex : " + invertedIndex.getIndexGlobal());

        if (documentStore.getNombreDocuments() > 0) {
            System.out.println("✅ Restauration réussie");
        } else {
            System.out.println("❌ Restauration échouée, DocumentStore vide");
        }

        // ============================================================
        // TEST 5 — reconcilier()
        // TP2 - page 10-11 : reconcilier() est static synchronized,
        // verrou sur la classe pour éviter les accès concurrents
        // TP2 - Exercice 2 : les ecrireSuppression/ecrireMiseAJour appelés en interne
        // sont des appels producteurs qui déposent dans le buffer
        // ============================================================
        System.out.println("\n=== TEST 5 : reconcilier() ===");
        try {
            // TP1 - Exercice 1 question 4 : start() relance un nouveau thread écrivain
            Journal journal2 = new Journal(cheminJournal);
            Journal.reconcilier(documentStore, invertedIndex, journal2);
            System.out.println("✅ Réconciliation terminée");
            System.out.println("Documents après réconciliation : " + documentStore.getNombreDocuments());

            // TP1 - Annexe 1 + Encadré 2 : fermeture propre avec join()
            journal2.fermer();
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de la réconciliation : " + e.getMessage());
        }
    }
}