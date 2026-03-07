package Serveur;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MainBis {

    public static void main(String[] args) {

        // ============================================================
        // CONFIGURATION
        // ============================================================
        String cheminJournal = "journal.csv"; // fichier créé dans le répertoire courant

        DocumentStore documentStore = new DocumentStore();
        InvertedIndex invertedIndex = new InvertedIndex();

        // ============================================================
        // TEST 1 — Création du journal et écriture d'un AJOUT
        // ============================================================
        System.out.println("=== TEST 1 : ecrireAjout() ===");
        try {
            Journal journal = new Journal(cheminJournal);

            ConcurrentHashMap<String, Integer> mots1 = new ConcurrentHashMap<>();
            mots1.put("linux", 12);
            mots1.put("systeme", 5);
            mots1.put("memoire", 1);

            journal.ecrireAjout("/home/user/tp.pdf", 1713560000L, 204800L, mots1);
            System.out.println("✅ AJOUT écrit dans le journal");

            // ============================================================
            // TEST 2 — Écriture d'une MISE_A_JOUR
            // ============================================================
            System.out.println("\n=== TEST 2 : ecrireMiseAJour() ===");
            ConcurrentHashMap<String, Integer> mots2 = new ConcurrentHashMap<>();
            mots2.put("linux", 15);
            mots2.put("systeme", 5);

            journal.ecrireMiseAJour("/home/user/tp.pdf", 1713999999L, 204800L, mots2);
            System.out.println("✅ MISE_A_JOUR écrite dans le journal");

            // ============================================================
            // TEST 3 — Écriture d'une SUPPRESSION
            // ============================================================
            System.out.println("\n=== TEST 3 : ecrireSuppression() ===");
            journal.ecrireSuppression("/home/user/vieux.txt", 1713560000L);
            System.out.println("✅ SUPPRESSION écrite dans le journal");

            journal.fermer();
            System.out.println("\n✅ Journal fermé proprement");

        } catch (IOException e) {
            System.out.println("❌ Erreur lors de l'écriture : " + e.getMessage());
        }

        // ============================================================
        // TEST 4 — Restauration depuis le journal
        // ============================================================
        System.out.println("\n=== TEST 4 : restaurerDepuisJournal() ===");
        Journal.restaurerDepuisJournal(cheminJournal, documentStore, invertedIndex);

        System.out.println("Documents restaurés : " + documentStore.getNombreDocuments());
        System.out.println("Contenu du DocumentStore : " + documentStore.getDocumentStore());
        System.out.println("Contenu de l'InvertedIndex : " + invertedIndex.getIndexGlobal());

        if (documentStore.getNombreDocuments() > 0) {
            System.out.println("✅ Restauration réussie");
        } else {
            System.out.println("❌ Restauration échouée, DocumentStore vide");
        }

        // ============================================================
        // TEST 5 — Réconciliation
        // ============================================================
        System.out.println("\n=== TEST 5 : reconcilier() ===");
        try {
            Journal journal2 = new Journal(cheminJournal);
            Journal.reconcilier(documentStore, invertedIndex, journal2);
            System.out.println("✅ Réconciliation terminée");
            System.out.println("Documents après réconciliation : " + documentStore.getNombreDocuments());
            journal2.fermer();
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de la réconciliation : " + e.getMessage());
        }
    }
}