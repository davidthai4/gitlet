package gitlet;


import java.text.SimpleDateFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Repo.SA;

public class Commit implements Serializable {
    /** Commit Message. */
    private String message;
     /** Commit timestamp. */
    private String timestamp;
     /** Commit parent.*/
    private String parent;
     /** Contents of commit. */
    private TreeMap<String, String> contents;
     /** Commit hash code/name. */
    private String name;
    /** Merge parent name. */
    private String mergeParent;

    public Commit(String msg, String prnt,
                  TreeMap<String, String> blob, String... mergePrnt) {
        if (msg.isEmpty()) {
            System.out.println("Please enter a commit message.");
        }
        if (prnt == null) {
            this.timestamp = "Thu Jan 1 00:00:00 1970 -0700";
            this.message = msg;
            this.parent = null;
            this.contents = new TreeMap<>();
            this.name = Utils.sha1(Utils.serialize(this));
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(
                    "EEE MMM d HH:mm:ss yyyy Z");
            Date date = new Date();
            this.timestamp = formatter.format(date);
            this.message = msg;
            this.parent = prnt;
            if (mergePrnt.length != 0) {
                this.mergeParent = mergePrnt[0];
            }
            this.contents = new TreeMap<>(blob);
            StagingArea currStage = Utils.readObject(SA, StagingArea.class);
            if (!currStage.addedFiles().isEmpty()) {
                this.contents.putAll(currStage.addedFiles());
                currStage.addedFiles().clear();
            }
            if (!currStage.removedFiles().isEmpty()) {
                for (String key: currStage.removedFiles()) {
                    this.contents.remove(key);
                }
                currStage.removedFiles().clear();
            }
            Utils.writeObject(SA, currStage);
            this.name = Utils.sha1(Utils.serialize(this));
        }
    }


    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getParent() {
        return this.parent;
    }

    public String getName() {
        return this.name;
    }

    public TreeMap<String, String> getContents() {
        return this.contents;
    }

    public String getMergeParent() {
        return this.mergeParent;
    }
}
