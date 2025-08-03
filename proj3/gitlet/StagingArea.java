package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

public class StagingArea implements Serializable {
    /** TreeMap consisting of staged files ready for commit. */
    private TreeMap<String, String> added;

    /** Simple array list of removed files. */
    private ArrayList<String> removed;

    public StagingArea() {
        added = new TreeMap<>();
        removed = new ArrayList<>();
    }

    public void add(String file, String id) {
        added.put(file, id);
    }

    public void remove(String file) {
        removed.add(file);
    }

    public void clear() {
        added = new TreeMap<>();
        removed = new ArrayList<>();
    }

    public TreeMap<String, String> addedFiles() {
        return added;
    }

    public ArrayList<String> removedFiles() {
        return removed;
    }
}
