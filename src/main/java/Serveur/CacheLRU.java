package Serveur;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheLRU<K, V> extends LinkedHashMap<K, V> {
    private final int capaciteMax; // Création de ma limite (modifiable depuis DocumentStore)

    public CacheLRU(int capacite) {
        // Le (super) c'est le constructeur de LinkedHashMap
        // Le true est pour l'ordre d'accès
        super(capacite, 0.75f, true);
        this.capaciteMax = capacite;
    }

    @Override // Réécriture
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capaciteMax;
    }
}

/*
classe qui sert à gérer la quantité de fichier en ram, donc au lieu de faire une liste infini sans limites avec le
ConcurrentHashMap dans le DocumentStore, on crée une limite, dès que c'est plein, le fichier le plus ancien est effacé
*/