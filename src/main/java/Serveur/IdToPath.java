package Serveur;

import java.util.ArrayList;

public class IdToPath {
    private ArrayList idToPath = new ArrayList<>();
    private int currentId = 0;

    public IdToPath() {
    }

    public void addPath(String path) {
        idToPath.add(path);
        currentId+=1;
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

}
