package Serveur;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

public class Journal {
    private final String chemin;
    private final BufferedWriter writer;

    public Journal(String chemin) throws IOException {
        this.chemin = chemin;
        writer =  new BufferedWriter(new FileWriter(chemin, true));
    }

    // Liste de mots + fréquence
    public String formaterMots(ConcurrentHashMap<String, Integer> mots){
        StringBuilder builder = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, Integer> entry : mots.entrySet()) {
            builder.append(entry.getKey() + ":" + entry.getValue() + ",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    //sauvegarde immédiate sur disque dur
    public void ecrireAjout(String chemin, long datemodif, long taille, ConcurrentHashMap<String, Integer> mots) throws IOException {
        String motsformat = formaterMots(mots) ; // transforme en string pr pouboir etre ecrit proprement ensuite
        writer.write("AJOUT" + ";" + chemin +";" + datemodif + ";" + taille + ";" + motsformat); // écrit dans journal.csv
        writer.newLine();
        writer.flush(); //force écriture sur le disque
    }

    // sauvegarde suppression
    public void ecrireSuppression(String chemin, long datemodif) throws IOException {
        writer.write("SUPPRESSION" + ";" + chemin +";" + datemodif + ";0;");
        writer.newLine();
        writer.flush();
    }

    // sauvegarde MAJ
    public void ecrireMiseAJour(String chemin, long datemodif, long taille, ConcurrentHashMap<String, Integer> mots) throws IOException {
        String motsformat = formaterMots(mots) ;
        writer.write("MISE_A_JOUR" + ";" + chemin +";" + datemodif + ";" + taille + ";" + motsformat);
        writer.newLine();
        writer.flush();
    }

    public void fermer(){
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void restaurerDepuisJournal(String cheminJournal, DocumentStore documentStore, InvertedIndex invertedIndex) {
        File fichier = new File(cheminJournal);
        if (!fichier.exists()) {
            return;
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(cheminJournal));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            String ligne;
            int id = 0;
            while ((ligne = br.readLine()) != null) {
                String[] champs = ligne.split(";");
                if (champs[0].equals("AJOUT") || champs[0].equals("MISE_A_JOUR")) {
                    String[] motsFréquences = champs[4].split(",");
                    ConcurrentHashMap<String, Integer> frequence = new ConcurrentHashMap<>();
                    for (String motFreq : motsFréquences) {
                        String[] paire = motFreq.split(":");
                        frequence.put(paire[0], Integer.parseInt(paire[1]));
                    }
                    documentStore.ajouterDocument(id, champs[1], Long.parseLong(champs[3]), Long.parseLong(champs[2]), frequence.size());
                    for (ConcurrentHashMap.Entry<String, Integer> entry : frequence.entrySet()) {
                        invertedIndex.indexerMot(entry.getKey(), Integer.parseInt(champs[2]));
                    }
                    id++;
                } else if (champs[0].equals("SUPPRESSION")) {
                    documentStore.supprimerDocument(champs[1]);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void reconcilier(DocumentStore documentStore, InvertedIndex invertedIndex, Journal journal){
    for(String chemin : documentStore.getDocumentStore().keySet()){
        File fichier = new File(chemin);
        if (!fichier.exists()) {
            documentStore.supprimerDocument(chemin);
            try {
                journal.ecrireSuppression(chemin, 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            long dateOS = fichier.lastModified();
            long dateStockee = documentStore.getDocumentMetaData(chemin).getDateModification();
            if (dateOS > dateStockee) {
                Main.indexFile(0, chemin, documentStore, invertedIndex);
                try {
                    journal.ecrireMiseAJour(chemin, dateOS, fichier.length(), new ConcurrentHashMap<>());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    }
}




