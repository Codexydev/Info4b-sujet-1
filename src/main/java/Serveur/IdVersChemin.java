package Serveur;

import java.util.ArrayList;

public class IdVersChemin {
    private final ArrayList<String> idVersChemin = new ArrayList<>();
    private int idCourant = -1;

    public IdVersChemin() {
    }

    public void addPath(String path) {
        if (!idVersChemin.contains(path)) {
            idVersChemin.add(path);
            idCourant = idVersChemin.size() - 1;
        }
    }

    public int getIdCourant() {
        return idCourant;
    }

    public String getChemin(int id) {
        if (id >= 0 && id < idVersChemin.size()) {
            return (String) idVersChemin.get(id);
        }
        return null;
    }

    public ArrayList getIdVersChemin() {
        return this.idVersChemin;
    }

    public int getIdFromPath(String path) {
        return idVersChemin.indexOf(path);
    }

    public void clear() {
        idVersChemin.clear();
        idCourant = -1;
    }
}
