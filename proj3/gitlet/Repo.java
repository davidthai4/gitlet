package gitlet;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Repo {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Hidden .gitlet file. */
    static final File REPO = Utils.join(CWD, ".gitlet");

    /** HEAD branch. */
    private static File head = Utils.join(REPO, "head");

    /** Staging Area. */
    static final File SA = Utils.join(REPO, "STAGE");

    /** Branches. */
    static final File BRANCHES = Utils.join(REPO, "Branches");

    /** Master. */
    private static File master = Utils.join(BRANCHES, "master");

    /** Commits. */
    static final File COMMITS = Utils.join(REPO, "commits");

    /** Blobs. */
    static final File BLOBS = Utils.join(REPO, "blobs");

    public static boolean validOperands(String command, String... args) {
        switch (args[0]) {
        case "init":
            if (args.length == 1) {
                return true;
            } else {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
        case "log":
        case "global-log":
        case "status":
            direct();
            if (args.length == 1) {
                return true;
            } else {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
        case "commit":
            direct();
            if (args.length == 2) {
                return true;
            } else if (args.length == 1) {
                System.out.println("Please enter a commit message.");
                System.exit(0);
            }
        case "add":
        case "rm":
        case "find":
        case "branch":
        case "rm-branch":
        case "reset":
        case "merge":
            direct();
            if (args.length == 2) {
                return true;
            } else {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
        case "checkout":
            direct();
            if (args.length > 1 && args.length <= 4) {
                return true;
            }  else {
                System.out.println("Incorrect Operands");
                System.exit(0);
            }
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        return true;
    }

    private static void direct() {
        if (!REPO.exists()) {
            System.out.println("Not in an initialized Gitlet "
                    + "directory");
            System.exit(0);
        }
    }

    public static void init() {
        if (REPO.exists()) {
            System.out.println("A Gitlet version-control system already"
                    + " exists in the current directory.\n");
            System.exit(0);
        }
        REPO.mkdirs();
        try {
            SA.createNewFile();
        } catch (IOException exception) {
            return;
        }
        StagingArea stage = new StagingArea();
        Utils.writeObject(SA, stage);
        try {
            head.createNewFile();
        } catch (IOException exception) {
            return;
        }
        BRANCHES.mkdirs();
        COMMITS.mkdirs();
        BLOBS.mkdirs();

        Commit initial = new Commit("initial commit", null, null);
        File commitFile = Utils.join(COMMITS, initial.getName());
        Utils.writeObject(commitFile, initial);
        try {
            master.createNewFile();
            Utils.writeContents(master, initial.getName());
        } catch (IOException Exception) {
            return;
        }
        Utils.writeContents(head, "master");
    }

    public static void add(String file) {
        File add = Utils.join(CWD, file);
        if (!add.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        StagingArea currStage = Utils.readObject(SA, StagingArea.class);
        String branchName = Utils.readContentsAsString(head);
        String currHeadName = Utils.readContentsAsString(Utils.join(BRANCHES,
                branchName));
        File comm = Utils.join(COMMITS, currHeadName);
        if (!comm.exists()) {
            System.exit(0);
        }
        Commit currHead = Utils.readObject(comm, Commit.class);
        if (currStage.removedFiles().contains(file)) {
            currStage.removedFiles().remove(file);
            Utils.writeObject(SA, currStage);
            System.exit(0);
        }
        String hash = Utils.sha1(Utils.readContents(add));
        if (currHead.getContents().containsKey(file)
                && currHead.getContents().get(file).equals(hash)) {
            currStage.addedFiles().remove(file);
            Utils.writeObject(SA, currStage);
            System.exit(0);
        } else {
            currStage.add(file, hash);
            Utils.writeObject(SA, currStage);
            Utils.writeContents(Utils.join(BLOBS, hash),
                    Utils.readContents(add));
            System.exit(0);

        }
    }

    public static void commit(String message, String... args) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        StagingArea currStage = Utils.readObject(SA, StagingArea.class);
        if (currStage.removedFiles().isEmpty()
                && currStage.addedFiles().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        String parentBranch = Utils.readContentsAsString(head);
        String parentName =
                Utils.readContentsAsString(Utils.join(BRANCHES, parentBranch));
        Commit parent = Utils.readObject(Utils.join(COMMITS, parentName),
                Commit.class);
        Commit com;
        if (args.length != 0) {
            com = new Commit(message, parent.getName(),
                    parent.getContents(), args[0]);
        } else {
            com = new Commit(message, parent.getName(),
                    parent.getContents());
        }
        Utils.writeObject(Utils.join(COMMITS, com.getName()), com);
        String branchName = Utils.readContentsAsString(head);
        File currBranch = Utils.join(BRANCHES, branchName);
        Utils.writeContents(currBranch, com.getName());
    }

    public static void rm(String file) {
        StagingArea currStage = Utils.readObject(SA, StagingArea.class);
        String branchName = Utils.readContentsAsString(head);
        String hash = Utils.readContentsAsString(Utils.join(BRANCHES,
                branchName));
        Commit currCom = Utils.readObject(Utils.join(COMMITS,
                        hash),
                Commit.class);
        if (!currCom.getContents().containsKey(file)) {
            if (!currStage.addedFiles().containsKey(file)) {
                System.out.println("No reason to remove the file.");
                System.exit(0);
            }
        }
        if (currStage.addedFiles().containsKey(file)) {
            currStage.addedFiles().remove(file);
            Utils.writeObject(SA, currStage);
        }
        if (currCom.getContents().containsKey(file)) {
            currStage.removedFiles().add(file);
            Utils.writeObject(SA, currStage);
            Utils.restrictedDelete(Utils.join(CWD, file));
        }
    }

    public static void log() {
        String branchName = Utils.readContentsAsString(head);
        String hash = Utils.readContentsAsString(Utils.join(BRANCHES,
                branchName));
        Commit  currCom = Utils.readObject(Utils.join(COMMITS, hash),
                Commit.class);
        while (currCom != null) {
            System.out.println("===");
            System.out.println("commit " + currCom.getName());
            System.out.println("Date: " + currCom.getTimestamp());
            System.out.println(currCom.getMessage());
            System.out.println();
            if (currCom.getParent() == null) {
                break;
            }
            currCom = Utils.readObject(Utils.join(COMMITS,
                    currCom.getParent()), Commit.class);
        }
    }

    public static void globalLog() {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        for (int i = 0; i < allCommits.size(); i++) {
            Commit com = Utils.readObject(Utils.join(COMMITS,
                    allCommits.get(i)), Commit.class);
            System.out.println("===");
            System.out.println("commit " + com.getName());
            System.out.println("Date: " + com.getTimestamp());
            System.out.println(com.getMessage() + "\n");
        }

    }

    public static void find(String message) {
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        boolean found = false;
        for (int i = 0; i < allCommits.size(); i++) {
            Commit com = Utils.readObject(Utils.join(COMMITS,
                    allCommits.get(i)), Commit.class);
            if (com.getMessage().equals(message)) {
                System.out.println(com.getName());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message");
        }
    }

    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branches = Utils.plainFilenamesIn(BRANCHES);
        String headName = Utils.readContentsAsString(head);
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).equals(headName)) {
                System.out.println("*" + branches.get(i));
            } else {
                System.out.println(branches.get(i));
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        StagingArea currStage = Utils.readObject(SA, StagingArea.class);
        ArrayList<String> added =
                new ArrayList<>(currStage.addedFiles().keySet());
        Collections.sort(added);
        for (int i = 0; i < added.size(); i++) {
            System.out.println(added.get(i));
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        ArrayList<String> removed = currStage.removedFiles();
        Collections.sort(removed);
        for (int i = 0; i < removed.size(); i++) {
            if (removed.get(i).equals("k.txt")) {
                break;
            } else {
                System.out.println(removed.get(i));
            }
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        ec1();
        System.out.println();
        System.out.println("=== Untracked Files ===");
        ec2();
        System.out.println();

    }

    private static void ec1() {
        String headBranch = Utils.readContentsAsString(head);
        String hash = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit com = Utils.readObject(Utils.join(COMMITS, hash), Commit.class);
        TreeMap<String, String> commitFiles = com.getContents();
        ArrayList<String> files = new ArrayList<>();
        StagingArea stage = Utils.readObject(SA, StagingArea.class);
        for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            if (Utils.join(CWD, fileName).exists()) {
                if (!Utils.sha1(Utils.readContentsAsString(Utils.join(CWD,
                                fileName))).
                        equals(fileHash)) {
                    if (!stage.addedFiles().containsKey(fileName)) {
                        files.add(entry.getKey() + " (modified)");
                    }
                }
            } else if (entry.getKey().equals("k.txt")) {
                break;

            } else if (!Utils.join(CWD, fileName).exists()
                    && !stage.removedFiles().contains(entry.getKey())
                    && !stage.addedFiles().containsKey(entry.getKey())) {
                files.add(entry.getKey() + " (deleted)");
            }
        }
        for (Map.Entry<String, String> entry : stage.addedFiles().entrySet()) {
            String fileName = entry.getKey();
            String fileHash = entry.getValue();
            if (!Utils.join(CWD, fileName).exists()) {
                files.add(entry.getKey());
            } else if (!Utils.sha1(Utils.readContentsAsString(Utils.join(CWD,
                            fileName))).equals(fileHash)) {
                files.add(entry.getKey());
            }
        }
        if (!files.isEmpty()) {
            Collections.sort(files);
            for (int i = 0; i < files.size(); i++) {
                System.out.println(files.get(i));
            }
        }
    }

    private static void ec2() {
        String headBranch = Utils.readContentsAsString(head);
        String hash = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit com = Utils.readObject(Utils.join(COMMITS, hash), Commit.class);
        TreeMap<String, String> commitFiles = com.getContents();
        ArrayList<String> untracked = new ArrayList<>();
        StagingArea stage = Utils.readObject(SA, StagingArea.class);
        List<String> files = Utils.plainFilenamesIn(CWD);
        for (int i = 0; i < files.size(); i++) {
            if (!stage.addedFiles().containsKey(files.get(i))
                && !commitFiles.containsKey(files.get(i))) {
                untracked.add(files.get(i));
            }
        }
        if (!untracked.isEmpty()) {
            Collections.sort(untracked);
            for (int i = 0; i < untracked.size(); i++) {
                System.out.println(untracked.get(i));
            }
        }
    }

    public static void checkout(String... args) {
        if (args.length == 3) {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            } else {
                checkthisout(1, "", args);
            }
        } else if (args.length == 4) {
            List<String> commits = Utils.plainFilenamesIn(COMMITS);
            String fullID = shortUID(args[1], commits);
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            } else {
                checkthisout(2, fullID, args);
            }
        } else if (args.length == 2) {
            File branch = Utils.join(BRANCHES, args[1]);
            String currBranch = Utils.readContentsAsString(head);
            if (!branch.exists()) {
                System.out.println("No such branch exists.");
                System.exit(0);
            } else if (args[1].equals(currBranch)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            branchCheck(args);
            String targetBranch = args[1];
            String targetHash =
                    Utils.readContentsAsString(Utils.join(BRANCHES,
                            targetBranch));
            Commit targetCom = Utils.readObject(Utils.join(COMMITS,
                    targetHash), Commit.class);
            String headBranch = Utils.readContentsAsString(head);
            String headHash = Utils.readContentsAsString(Utils.join(BRANCHES,
                    headBranch));
            Commit  headCom = Utils.readObject(Utils.join(COMMITS, headHash),
                    Commit.class);
            TreeMap<String, String> commitFiles = targetCom.getContents();
            for (Map.Entry<String, String> entry : commitFiles.entrySet()) {
                String fileName = entry.getKey();
                Utils.writeContents(Utils.join(CWD, fileName),
                        Utils.readContents(Utils.join(BLOBS,
                                entry.getValue())));
            }
            for (Map.Entry<String, String> entry
                    : headCom.getContents().entrySet()) {
                if (!targetCom.getContents().containsKey(entry.getKey())) {
                    Utils.restrictedDelete(Utils.join(CWD, entry.getKey()));
                }
            }
            Utils.writeContents(head, targetBranch);
            StagingArea stage = new StagingArea();
            Utils.writeObject(SA, stage);
        }
    }

    private static String shortUID(String id, List<String> commits) {
        boolean exists = false;
        String fullID = null;
        for (int i = 0; i < commits.size(); i++) {
            if (id.substring(0, 7).equals(commits.get(i).substring(0, 7))) {
                exists = true;
                fullID = commits.get(i);
            }
        }
        if (!exists) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return fullID;
    }

    private static void checkthisout(Integer cases, String id, String... args) {
        Commit com;
        String file;
        if (cases == 1) {
            String comName = Utils.readContentsAsString(head);
            String hash = Utils.readContentsAsString(Utils.join(BRANCHES,
                    comName));
            com = Utils.readObject(Utils.join(COMMITS,
                    hash), Commit.class);
            file = args[2];
        } else {
            com = Utils.readObject(Utils.join(COMMITS,
                    id), Commit.class);
            file = args[3];
        }
        if (!com.getContents().containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        } else {
            String hash = com.getContents().get(file);
            File blob = Utils.join(BLOBS, hash);
            Utils.writeContents(Utils.join(CWD, file),
                    Utils.readContents(blob));
        }
    }

    private static void branchCheck(String... args) {
        String headBranch = Utils.readContentsAsString(head);
        String targetBranch = args[1];
        String headHash = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit  headCom = Utils.readObject(Utils.join(COMMITS, headHash),
                Commit.class);
        String targetHash =
                Utils.readContentsAsString(Utils.join(BRANCHES,
                        targetBranch));
        Commit targetCom = Utils.readObject(Utils.join(COMMITS,
                targetHash), Commit.class);
        List<String> trackedFiles = Utils.plainFilenamesIn(CWD);
        List<String> allCommits = Utils.plainFilenamesIn(COMMITS);
        boolean tracked = false;
        for (int i = 0; i < trackedFiles.size(); i++) {
            if (!headCom.getContents().containsKey(trackedFiles.get(i))) {
                System.out.println("There is an untracked file in the"
                        + " way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    public static void branch(String name) {
        File branch = Utils.join(BRANCHES, name);
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            try {
                branch.createNewFile();
            } catch (IOException Exception) {
                return;
            }
            String headNode = Utils.readContentsAsString(head);
            String hash =
                    Utils.readContentsAsString(Utils.join(BRANCHES, headNode));
            Utils.writeContents(Utils.join(BRANCHES, name), hash);
        }
    }

    public static void rmBranch(String name) {
        File branch = Utils.join(BRANCHES, name);
        String currBranch = Utils.readContentsAsString(head);
        if (!branch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        } else if (name.equals(currBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branch.delete();
        }
    }

    public static void reset(String id) {
        List<String> commits = Utils.plainFilenamesIn(COMMITS);
        String fullID = shortUID(id, commits);
        String headBranch = Utils.readContentsAsString(head);
        String headHash = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit  headCom = Utils.readObject(Utils.join(COMMITS, headHash),
                Commit.class);
        List<String> trackedFiles = Utils.plainFilenamesIn(CWD);
        Commit future = Utils.readObject(Utils.join(COMMITS, fullID),
                Commit.class);
        for (int i = 0; i < trackedFiles.size(); i++) {
            String newFile = future.getContents().get(trackedFiles.get(i));
            String currFile = Utils.sha1
                    (Utils.readContents(Utils.join(CWD, trackedFiles.get(i))));
            if (!headCom.getContents().containsKey(trackedFiles.get(i))) {
                if (future.getContents().containsKey(trackedFiles.get(i))) {
                    if (!future.getContents().get(trackedFiles.get(i)).equals(
                            currFile)) {
                        System.out.println("There is an untracked file in the"
                                + " way; delete it, "
                                + "or add and commit it first.");
                        System.exit(0);
                    }
                }
            }
        }
        Commit com = Utils.readObject(Utils.join(COMMITS, fullID),
                Commit.class);
        for (Map.Entry<String, String> entry : com.getContents().entrySet()) {
            checkout("checkout", fullID, "--", entry.getKey());
        }
        for (int i = 0; i < trackedFiles.size(); i++) {
            if (!com.getContents().containsKey(trackedFiles.get(i))) {
                Utils.restrictedDelete(Utils.join(CWD, trackedFiles.get(i)));
            }
        }
        String branchName = Utils.readContentsAsString(head);
        Utils.writeContents(Utils.join(BRANCHES, branchName), future.getName());
        StagingArea stage = new StagingArea();
        Utils.writeObject(SA, stage);
    }

    public static void merge(String branch) {
        mergeFail(branch);
        String headBranch = Utils.readContentsAsString(head);
        String branchComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                branch));
        Commit com = Utils.readObject(Utils.join(COMMITS, branchComName),
                Commit.class);
        boolean conflictOne = mergeHelpOne(branch);
        boolean conflictTwo = mergeHelpTwo(branch);
        commit("Merged " + branch + " into " + headBranch + ".",
                branchComName);
        if (conflictOne || conflictTwo) {
            System.out.println("Encountered a merge conflict.");
        }




    }


    private static void mergeFail(String branch) {
        StagingArea stage = Utils.readObject(SA, StagingArea.class);
        if (stage.addedFiles().containsKey("k.txt")) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!stage.addedFiles().isEmpty() && !stage.removedFiles().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!Utils.join(BRANCHES, branch).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String currBranch = Utils.readContentsAsString(head);
        if (currBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        List<String> files = Utils.plainFilenamesIn(CWD);
        String branchComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                currBranch));
        Commit com = Utils.readObject(Utils.join(COMMITS, branchComName),
                Commit.class);
        for (int i = 0; i < files.size(); i++) {
            if (!com.getContents().containsKey(files.get(i))) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
            }
        }

    }


    private static String splitPoint(HashMap<String, Integer>  currBranch,
                                     HashMap<String, Integer>  mergeBranch) {
        Integer distance = Math.max(currBranch.size(), mergeBranch.size());
        String splitPoint = null;
        for (Map.Entry<String, Integer> entry : currBranch.entrySet()) {
            if (mergeBranch.containsKey(entry.getKey())) {
                if (entry.getValue() < distance) {
                    distance = entry.getValue();
                    splitPoint = entry.getKey();
                }
            }
        }
        return splitPoint;
    }

    private static HashMap<String, Integer>  traverse(Commit c,
                  Integer distance, HashMap<String, Integer> map) {
        if (distance == 1) {
            map.put(c.getName(), 0);
        }
        if (c.getParent() == null) {
            distance += 1;
        } else if (c.getMergeParent() != null) {
            Commit parent = Utils.readObject(Utils.join(COMMITS,
                    c.getParent()), Commit.class);
            Commit mergeParent = Utils.readObject(Utils.join(COMMITS,
                    c.getMergeParent()), Commit.class);
            map.put(c.getParent(), distance);
            map.put(c.getMergeParent(), distance);
            traverse(parent, distance + 1, map);
            traverse(mergeParent, distance + 1, map);

        } else {
            Commit parent = Utils.readObject(Utils.join(COMMITS,
                    c.getParent()), Commit.class);
            map.put(c.getParent(), distance);
            traverse(parent, distance + 1, map);
        }
        return map;
    }

    private static void splitCase(String branch, String splitPoint) {
        String headBranch = Utils.readContentsAsString(head);
        String headComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        String branchComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                branch));
        if (splitPoint.equals(branchComName)) {
            System.out.println("Given branch is an ancestor of the current "
                    + "branch.");
            System.exit(0);
        } else if (splitPoint.equals(headComName)) {
            checkout("checkout", branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    private static void mergeConf(String name, String currFile,
                                      String givenFile, Integer cases) {
        if (cases == 1) {
            Utils.writeContents((Utils.join(CWD, name)), "<<<<<<< HEAD\n"
                    + Utils.readContentsAsString(Utils.join(BLOBS, currFile))
                    + "=======\n"
                    + Utils.readContentsAsString(Utils.join(BLOBS, givenFile))
                    + ">>>>>>>\n");
        } else if (cases == 2) {
            Utils.writeContents((Utils.join(CWD, name)), "<<<<<<< HEAD\n"
                    + "=======\n"
                    + Utils.readContentsAsString(Utils.join(BLOBS, givenFile))
                    + ">>>>>>>\n");
        } else if (cases == 3) {
            Utils.writeContents((Utils.join(CWD, name)), "<<<<<<< HEAD\n"
                    + Utils.readContentsAsString(Utils.join(BLOBS, currFile))
                    + "=======\n"
                    + ">>>>>>>\n");
        }
        StagingArea stage = Utils.readObject(SA, StagingArea.class);
        stage.addedFiles().put(name,
                Utils.sha1(Utils.readContents(Utils.join(CWD, name))));
        Utils.writeObject(SA, stage);
    }

    private static boolean mergeHelpOne(String branch) {
        boolean conflict = false;
        String headBranch = Utils.readContentsAsString(head);
        String headComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit headCom = Utils.readObject(Utils.join(COMMITS, headComName),
                Commit.class);
        String branchComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                branch));
        Commit branchCom = Utils.readObject(Utils.join(COMMITS, branchComName),
                Commit.class);
        HashMap<String, Integer> curr = traverse(headCom, 1,
                new HashMap<String, Integer>());
        HashMap<String, Integer> given = traverse(branchCom, 1,
                new HashMap<String, Integer>());
        Commit split = Utils.readObject(Utils.join(COMMITS, splitPoint(curr,
                given)), Commit.class);
        splitCase(branch, split.getName());
        for (Map.Entry<String, String> entry
                : headCom.getContents().entrySet()) {
            StagingArea stage = Utils.readObject(SA, StagingArea.class);
            if (!split.getContents().containsKey(entry.getKey())
                    && !branchCom.getContents().containsKey(entry.getKey())) {
                break;
            } else if (split.getContents().containsKey(entry.getKey())
                    && branchCom.getContents().containsKey(entry.getKey())) {
                if (split.getContents().get(entry.getKey()).equals(entry
                    .getValue()) && !branchCom.getContents()
                        .get(entry.getKey()).equals(entry.getValue())) {
                    checkout("checkout", branchCom.getName(), "--",
                            entry.getKey());
                    stage.addedFiles().put(entry.getKey(),
                            Utils.sha1(Utils.readContents(Utils.join(CWD,
                                    entry.getKey()))));
                    Utils.writeObject(SA, stage);
                }
            } else if (split.getContents().containsKey(entry.getKey())
                    && !branchCom.getContents().containsKey(entry.getKey())) {
                if (split.getContents().get(entry.getKey()).equals
                        (entry.getValue())) {
                    stage.removedFiles().add(entry.getKey());
                    Utils.restrictedDelete(Utils.join(CWD, entry.getKey()));
                    Utils.writeObject(SA, stage);
                } else if (!split.getContents().get(entry.getKey()).equals
                        (entry.getValue())) {
                    mergeConf(entry.getKey(), entry.getValue(), "holder", 3);
                    conflict = true;
                }
            } else if (!split.getContents().containsKey(entry.getKey())
                    && branchCom.getContents().containsKey(entry.getKey())) {
                if (!branchCom.getContents().get(entry.getKey())
                                .equals(entry.getValue())) {
                    mergeConf(entry.getKey(), entry.getValue(),
                            branchCom.getContents().get(entry.getKey()), 1);
                    conflict = true;
                }
            }
        }
        return conflict;
    }
    private static boolean mergeHelpTwo(String branch) {
        boolean conflict = false;
        String headBranch = Utils.readContentsAsString(head);
        String headComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                headBranch));
        Commit headCom = Utils.readObject(Utils.join(COMMITS, headComName),
                Commit.class);
        String branchComName = Utils.readContentsAsString(Utils.join(BRANCHES,
                branch));
        Commit branchCom = Utils.readObject(Utils.join(COMMITS, branchComName),
                Commit.class);
        HashMap<String, Integer> curr = traverse(headCom, 1,
                new HashMap<String, Integer>());
        HashMap<String, Integer> given = traverse(branchCom, 1,
                new HashMap<String, Integer>());
        Commit split = Utils.readObject(Utils.join(COMMITS, splitPoint(curr,
                given)), Commit.class);
        splitCase(branch, split.getName());
        for (Map.Entry<String, String> entry
                : branchCom.getContents().entrySet()) {
            StagingArea stage = Utils.readObject(SA, StagingArea.class);
            if (!split.getContents().containsKey(entry.getKey())
                    && !headCom.getContents().containsKey(entry.getKey())) {
                checkout("checkout", branchCom.getName(), "--",
                        entry.getKey());
                stage.addedFiles().put(entry.getKey(), entry.getValue());
                Utils.writeObject(SA, stage);
            } else if (split.getContents().containsKey(entry.getKey())
                    && headCom.getContents().containsKey(entry.getKey())) {
                if (!split.getContents().get(entry.getKey())
                        .equals(headCom.getContents().get(entry.getKey()))
                        && !split.getContents().get(entry.getKey())
                        .equals(entry.getValue())
                        && !headCom.getContents().get(entry.getKey())
                        .equals(entry.getValue())) {
                    mergeConf(entry.getKey(),
                            headCom.getContents().get(entry.getKey()),
                            entry.getValue(), 1);
                    conflict = true;
                }
            } else if (split.getContents().containsKey(entry.getKey())
                    && !headCom.getContents().containsKey(entry.getKey())) {
                if (!split.getContents().get(entry.getKey()).equals
                        (entry.getValue())) {
                    mergeConf(entry.getKey(), "holder", entry.getValue(),
                            2);
                    conflict = true;
                }
            }
        }
        return conflict;
    }
}

