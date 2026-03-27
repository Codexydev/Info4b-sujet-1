package Serveur;

import java.util.LinkedHashMap;
import java.util.Map;

//  le K et V c'est key et value
public class CacheLRU<K, V> extends LinkedHashMap<K, V> {
    private final int capaciteMax; // création de ma limite (modifiable depuis DocumentStore)

    public CacheLRU(int capacite) {
        // le (super) c'est le constructeur de linkedhashmap(le truc qui permet de mettre une liste d'attente pour acceder a la map qui est en gros un tableau)
        // le true c'est pour l'ordre d'accès
        super(capacite, 0.75f, true);
        this.capaciteMax = capacite;
    }

    @Override //reecriture
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capaciteMax;
    }
}

/*
classe qui sert a gerer la quantité de fichier en ram, donc au lieu de faire une liste infini sans limite avec le
concurrent hash map dans le DocumentStore, la on a creer une limite (je l'ai mise a 100 dans le document store
mais on avisera si besoin), et bah dès que c'est plein le fichier le plus ancien est effacé (comme en tp avec les pages)
*/