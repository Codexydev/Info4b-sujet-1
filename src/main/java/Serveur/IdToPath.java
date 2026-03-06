package Serveur;

import java.util.ArrayList;

public class IdToPath {
    private ArrayList idToPath = new ArrayList<>();
    private int currentId = -1;

    public IdToPath() {
    }

    public void addPath(String path) {
        idToPath.add(path);
        currentId++;
    }

    public int getCurrentId() {
        return currentId;
    }

    public String getPath(int id) {
        if (id >= 0 && id < idToPath.size()) {
            return (String) idToPath.get(id);
        }
        return null;
    }

    public ArrayList getIdToPath() {
        return this.idToPath;
    }
}
