package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
import java.text.SimpleDateFormat;

/** @author wyw */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits file. */
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    /** The short id file. */
    public static final File SHORTIDS_FILE = join(GITLET_DIR, "shortIds");
    public static final int SHORTIDLENGTH = 8;
    /** The current head pointer file(store branch name). */
    public static final File HEAD_FILE = join(GITLET_DIR, "head");
    /** The branches file(store hash map for branch pointer name and reference). */
    public static final File BRANCHES_FILE = join(GITLET_DIR, "branches");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The staging file for addition references map(store hash map for staging blobs) */
    public static final File ADDMAP_FILE = join(GITLET_DIR, "addMap");
    /** The staging file for removal name set */
    public static final File RMSET_FILE = join(GITLET_DIR, "rmSet");

    /** The hash map of commits<commit hash, commit>(need prepareRef() to create). */
    private static HashMap<String, Commit> commits;
    /** The hash map of the short ids<6-digit hash, commit sha1 hash>(need prepareRef() to create). */
    private static HashMap<String, String> shortIds;
    /** The hash map of branches<branch name, commit hash>(need prepareRef() to create). */
    private static HashMap<String, String> branches;
    /** The current branch name(need prepareRef() to create). */
    private static String head;
    /** The current commit hash(need prepareRef() to create). */
    private static String currentCommitHash;
    /** The current commit object(need prepareRef() to create). */
    private static Commit currentCommit;
    /** The hash map of Staging references and file for addition<file name, blob hash>(need prepareRef() to create). */
    private static HashMap<String, String> addMap;
    /** The hash set of Staging file name for removal. */
    private static HashSet<String> rmSet;
    private static void setupPersistence() {
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }

        if (!COMMITS_FILE.exists()) {
            try {
                COMMITS_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!SHORTIDS_FILE.exists()) {
            try {
                SHORTIDS_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!BRANCHES_FILE.exists()) {
            try {
                BRANCHES_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!HEAD_FILE.exists()) {
            try {
                HEAD_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        if (!BLOBS_DIR.exists()) {
            BLOBS_DIR.mkdir();
        }
        
        if (!ADDMAP_FILE.exists()) {
            try {
                ADDMAP_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!RMSET_FILE.exists()) {
            try {
                RMSET_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void gitletInit() {
        if (GITLET_DIR.exists()) {
            exitWithError("A Gitlet version-control system already exists in the current directory.");
        }

        setupPersistence();

        // Set new hash map to store commits.
        commits = new HashMap<>();
        Commit iniCommit = new Commit(new Date(0), "initial commit", new HashMap<>(),
                null, null);
        String iniHash = sha1Hash(iniCommit);
        commits.put(iniHash, iniCommit);

        // Set short ids hash map.
        shortIds = new HashMap<>();
        shortIds.put(iniHash.substring(0, SHORTIDLENGTH), iniHash);

        // Set new HEAD point to the initial commit.
        head = "master";

        // Set new hash map to store branches reference name.
        branches = new HashMap<>();
        branches.put(head, iniHash);

        // Set addMap file to store file for addition.
        addMap = new HashMap<>();

        // Set rmSet file to store file for removal.
        rmSet = new HashSet<>();

        saveRefs();
    }

    /** Read references from the corresponding files. */
    private static void loadRefs() {
        commits = readObject(COMMITS_FILE, HashMap.class);
        shortIds = readObject(SHORTIDS_FILE, HashMap.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        head = readContentsAsString(HEAD_FILE);
        currentCommitHash = branches.get(head);
        currentCommit = commits.get(currentCommitHash);
        addMap = readObject(ADDMAP_FILE, HashMap.class);
        rmSet = readObject(RMSET_FILE, HashSet.class);

    }

    private static void saveRefs() {
        writeObject(COMMITS_FILE, commits);
        writeObject(SHORTIDS_FILE, shortIds);
        writeObject(BRANCHES_FILE, branches);
        writeContents(HEAD_FILE, head);
        writeObject(ADDMAP_FILE, addMap);
        writeObject(RMSET_FILE, rmSet);
    }

    private static void addCWDFile (String fileName) {
        // If the file name already exists in the rmSet, cancel the removal.
        rmSet.remove(fileName);
        // If the blobs area already exists the staged same name file, delete to overwrite.
        if (addMap.containsKey(fileName)) {
            join(BLOBS_DIR, addMap.get(fileName)).delete();
        }
        // Read file and store blob.
        Blob stagingBlob = new Blob(fileName, join(CWD, fileName));
        String stagingRef = stagingBlob.saveBlob(BLOBS_DIR);
        // Set staging reference.
        addMap.put(fileName, stagingRef);

        // If the staged file is identical to the commited file, delete the addMap references.
        if (stagingRef.equals(currentCommit.blobs.get(fileName))) {
            addMap.remove(fileName);
        }
    }

    public static void gitletAdd(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        // Judge whether the fileName file exist.
        if (!dirContainsFile(CWD, fileName)) {
            exitWithError("File does not exist.");
        }

        loadRefs();

        addCWDFile(fileName);

        saveRefs();
    }

    private static void commit(String message, String secondParent) {
        // Update commits.
        HashMap<String, String> blobs = new HashMap<>(currentCommit.blobs);
        for (String key : rmSet) {
            blobs.remove(key);
        }
        blobs.putAll(addMap);

        Commit commit = new Commit(new Date(), message, blobs, currentCommitHash, secondParent);
        String commitHash = sha1Hash(commit);
        commits.put(commitHash, commit);
        shortIds.put(commitHash.substring(0, SHORTIDLENGTH), commitHash);

        // Update branches.
        branches.put(head, commitHash);

        // Update addMap and rmSet(clear).
        addMap.clear();
        rmSet.clear();
    }
    public static void gitletCommit(String message) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
        if (message.isBlank()) {
            exitWithError("Please enter a commit message.");
        }

        loadRefs();

        if (addMap.isEmpty() && rmSet.isEmpty()) {
            exitWithError("No changes added to the commit.");
        }

        commit(message, null);

        // Store changes.
        saveRefs();
    }

    public static void gitletRm(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();
        if (!addMap.containsKey(fileName) && !currentCommit.blobs.containsKey(fileName)) {
            exitWithError("No reason to remove the file.");
        }

        addMap.remove(fileName);

        if (currentCommit.blobs.containsKey(fileName)) {
            rmSet.add(fileName);
            if (dirContainsFile(CWD, fileName)) {
                restrictedDelete(join(CWD, fileName));
            }

        }

        saveRefs();
    }

    private static void printCommit(String cmtHash, Commit cmt) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z");
        System.out.println("===");
        System.out.printf("commit %s%n", cmtHash);
        if (cmt.secondParent != null) {
            System.out.printf("Merge: %.7s %.7s%n", cmt.parent, cmt.secondParent);
        }
        System.out.println("Date: " + sdf.format(cmt.timestamp));
        System.out.println(cmt.message);
        System.out.println();
    }

    public static void gitletLog() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        Commit cmt = currentCommit;
        String cmtHash = currentCommitHash;
        printCommit(cmtHash, cmt);
        while (cmt.parent != null) {
            cmtHash = cmt.parent;
            cmt = commits.get(cmt.parent);
            printCommit(cmtHash, cmt);
        }
    }

    public static void gitletGlobalLog() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        for (String cmtHash : commits.keySet()) {
            printCommit(cmtHash, commits.get(cmtHash));
        }
    }

    public static void gitletFind(String message) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        boolean fd = false;

        for (String cmtHash : commits.keySet()) {
            if (message.equals(commits.get(cmtHash).message)) {
                System.out.println(cmtHash);
                fd = true;
            }
        }

        if (!fd) {
            exitWithError("Found no commit with that message.");
        }

    }

    private static void printStatusFiles(String title, TreeSet<String> files) {
        System.out.println(title);
        for (String fileName : files) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    public static void gitletStatus() {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        System.out.println("=== Branches ===");
        TreeSet<String> branchesFiles = new TreeSet<>(branches.keySet());
        for (String name : branchesFiles) {
            if (name.equals(head)) {
                System.out.println("*" + name);
            } else {
                System.out.println(name);
            }
        }
        System.out.println();

        TreeSet<String> addFiles = new TreeSet<>(addMap.keySet());
        printStatusFiles("=== Staged Files ===", addFiles);

        TreeSet<String> rmFiles = new TreeSet<>(rmSet);
        printStatusFiles("=== Removed Files ===", rmFiles);

        // Filtering out modified files, deleted files, untracked files.
        List<String> currentFiles = plainFilenamesIn(CWD);
        TreeSet<String> commitedFiles = new TreeSet<>(currentCommit.blobs.keySet());

        TreeSet<String> modified = new TreeSet<>();
        TreeSet<String> deleted = new TreeSet<>();
        TreeSet<String> untracked = new TreeSet<>();
        
        if (currentFiles != null) {
            for (String fileName : currentFiles) {
                // Prepare probably deleted fileName.
                commitedFiles.remove(fileName);
                addFiles.remove(fileName);
                // Judge whether is untracked.
                if (rmSet.contains(fileName)
                        || !currentCommit.blobs.containsKey(fileName) && !addMap.containsKey(fileName)) {
                    untracked.add(fileName);
                    continue;
                }
                // Judge whether is modified.
                Blob cwdBlob = new Blob(fileName, join(CWD, fileName));
                String cwdBlobHash = sha1Hash(cwdBlob);
                if (addMap.containsKey(fileName) && !cwdBlobHash.equals(addMap.get(fileName))
                        || !addMap.containsKey(fileName) && !cwdBlobHash.equals(currentCommit.blobs.get(fileName))) {
                    modified.add(fileName);
                }
            }
        }
        // Judge whether is deleted.
        deleted.addAll(addFiles);
        commitedFiles.removeAll(rmFiles);
        deleted.addAll(commitedFiles);

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : deleted) {
            System.out.println(fileName + " (deleted)");
        }
        for (String fileName : modified) {
            System.out.println(fileName + " (modified)");
        }
        System.out.println();

        printStatusFiles("=== Untracked Files ===", untracked);
    }

    private static void checkout(String commitHash, String fileName) {
        if (commitHash == null || !commits.containsKey(commitHash)) {
            exitWithError("No commit with that id exists.");
        }

        Commit checkCommit = commits.get(commitHash);

        if (!checkCommit.blobs.containsKey(fileName)) {
            exitWithError("File does not exist in that commit.");
        }

        // Read the checked blob file.
        Blob checkBlob = readObject(join(BLOBS_DIR, checkCommit.blobs.get(fileName)), Blob.class);
        // Write the checked blob contents to the CWD.
        checkBlob.writeBlobContents(CWD);
        // Clear the fileName in the addMap or the rmSet(if exists).
        addMap.remove(fileName);
        rmSet.remove(fileName);
    }

    private static void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            exitWithError("No such branch exists.");
        }

        String checkoutCommitHash = branches.get(branchName);
        Commit checkoutCommit = commits.get(checkoutCommitHash);
        HashMap<String, String> checkoutFiles = checkoutCommit.blobs;
        HashMap<String, String> headFiles = currentCommit.blobs;
        HashMap<String, String> cwdFiles = new HashMap<>();

        if (branchName.equals(head) && currentCommitHash.equals(checkoutCommitHash)) {
            exitWithError("No need to checkout the current branch.");
        }

        // Judge whether CWD contains(probably when change branch) untracked files.
        List<String> cwdFileNames = plainFilenamesIn(CWD);
        if (cwdFileNames != null) {
            for (String fileName : cwdFileNames) {
                Blob cwdBlob = new Blob(fileName, join(CWD, fileName));
                String cwdHash = sha1Hash(cwdBlob);
                cwdFiles.put(fileName, cwdHash);
                if (!cwdHash.equals(headFiles.get(fileName)) && !cwdHash.equals(checkoutFiles.get(fileName))) {
                    exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }

        // Delete tracked files.
        for (String fileName : headFiles.keySet()) {
            if (!checkoutFiles.containsKey(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }
        // Overwrite checkout files.
        for (String fileName : checkoutFiles.keySet()) {
            if (!cwdFiles.containsKey(fileName)
                || !checkoutFiles.get(fileName).equals(cwdFiles.get(fileName))
                    && cwdFiles.get(fileName).equals(headFiles.get(fileName))) {
                checkout(checkoutCommitHash, fileName);
            }
        }

        // Change the head.
        head = branchName;

        // Clear staging areas.
        addMap.clear();
        rmSet.clear();
    }

    public static void gitletCheckout(String[] args) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        if (args.length == 1) {
            // Checkout branch.
            checkoutBranch(args[0]);
        } else if (args[0].equals("--")) {
            // Checkout current commit file.
            checkout(currentCommitHash, args[1]);
        } else {
            // Checkout given commit file.
            if (args[0].length() == SHORTIDLENGTH ) {
                checkout(shortIds.get(args[0]), args[2]);
            } else {
                checkout(args[0], args[2]);
            }

        }

        saveRefs();
    }

    public static void gitletBranch(String branchName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        if (branches.containsKey(branchName)) {
            exitWithError("A branch with that name already exists.");
        }

        branches.put(branchName, currentCommitHash);

        saveRefs();
    }

    public static void gitletRmBranch(String branchName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        if (!branches.containsKey(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }

        if (branchName.equals(head)) {
            exitWithError("Cannot remove the current branch.");
        }

        branches.remove(branchName);

        saveRefs();
    }

    public static void gitletReset(String resetHash) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();
        
        if (resetHash.length() == SHORTIDLENGTH) {
            resetHash = shortIds.get(resetHash);
        }

        if (!commits.containsKey(resetHash)) {
            exitWithError("No commit with that id exists");
        }

        // Change the current branch hash to the reset hash.
        branches.replace(head, resetHash);
        checkoutBranch(head);

        saveRefs();
    }

    /** Return the split point hash */
    private static String findSplit(String branchName) {
        HashSet<String> visited = new HashSet<>();
        String cmt1 = currentCommitHash;
        String cmt2 = branches.get(branchName);
        while (true) {
            if (visited.contains(cmt1)) {
                return cmt1;
            } else {
                visited.add(cmt1);
            }

            if (visited.contains(cmt2)) {
                return cmt2;
            } else {
                visited.add(cmt2);
            }

            if (commits.get(cmt1).parent != null) {
                cmt1 = commits.get(cmt1).parent;
            }

            if (commits.get(cmt2).parent != null) {
                cmt2 = commits.get(cmt2).parent;
            }
        }
    }

    private static void addressConflict(String fileName, String headBlobHash, String givenBlobHash) {

        String headContents = (headBlobHash == null ?
                "" : readObject(join(BLOBS_DIR, headBlobHash), Blob.class).contents);
        String givenContents = (givenBlobHash == null ?
                "" : readObject(join(BLOBS_DIR, givenBlobHash),Blob.class).contents);
        String contents = "<<<<<<< HEAD\n" +
                headContents +
                "=======\n" +
                givenContents +
                ">>>>>>>\n";
        writeContents(join(CWD, fileName), contents);
        addCWDFile(fileName);
    }

    public static void gitletMerge(String branchName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        loadRefs();

        if (!addMap.isEmpty() || !rmSet.isEmpty()) {
            exitWithError("You have uncommitted changes.");
        }
        if (!branches.containsKey(branchName)) {
            exitWithError("A branch with that name does not exist.");
        }
        if (head.equals(branchName)) {
            exitWithError("Cannot merge a branch with itself.");
        }

        String splitPointHash = findSplit(branchName);
        String givenCommitHash = branches.get(branchName);
        if (splitPointHash.equals(givenCommitHash)) {
            exitWithError("Given branch is an ancestor of the current branch.");
        }
        if (splitPointHash.equals(currentCommitHash)) {
            checkoutBranch(branchName);
            exitWithError("Current branch fast-forwarded.");
        }

        // Get split point, current commit, given commit blobs hash map.
        HashMap<String, String> splitBlobs = commits.get(splitPointHash).blobs;
        HashMap<String, String> headBlobs = currentCommit.blobs;
        HashMap<String, String> givenBlobs = commits.get(givenCommitHash).blobs;

        // Judge whether there is untracked would be modified files.
        List<String> cwdFiles = plainFilenamesIn(CWD);
        if (cwdFiles != null) {
            for (String fileName : cwdFiles) {
                Blob cwdBlob = new Blob(fileName, join(CWD, fileName));
                String cwdBlobHash = sha1Hash(cwdBlob);
                if (!headBlobs.containsKey(fileName) && givenBlobs.containsKey(fileName) &&
                        (!givenBlobs.get(fileName).equals(splitBlobs.get(fileName)) ||
                                !splitBlobs.containsKey(fileName) && !cwdBlobHash.equals(givenBlobs.get(fileName)))) {
                    exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }

        // Filter out file for addition or removal.
        for (String fileName : splitBlobs.keySet()) {
            if (splitBlobs.get(fileName).equals(headBlobs.get(fileName))) {
                if (!givenBlobs.containsKey(fileName)) {
                    restrictedDelete(join(CWD,fileName));
                    rmSet.add(fileName);
                } else if (!splitBlobs.get(fileName).equals(givenBlobs.get(fileName))) {
                    addMap.put(fileName, givenBlobs.get(fileName));
                }
            }
        }

        // Filter out file for checkout.
        for (String fileName : givenBlobs.keySet()) {
            if (!splitBlobs.containsKey(fileName) && !headBlobs.containsKey(fileName)) {
                addMap.put(fileName, givenBlobs.get(fileName));
            }
        }

        // Filter out file for conflict.
        boolean conflict = false;
        for (String fileName : headBlobs.keySet()) {
            String headFileHash = headBlobs.get(fileName);
            if (givenBlobs.containsKey(fileName) && !headFileHash.equals(givenBlobs.get(fileName))) {
                addressConflict(fileName, headFileHash, givenBlobs.get(fileName));
                conflict = true;
            }
        }

        for (String fileName : splitBlobs.keySet()) {
            String splitBlobHash = splitBlobs.get(fileName);
            String headBlobHash = headBlobs.get(fileName);
            String givenBlobHash = givenBlobs.get(fileName);

            if (headBlobHash == null && givenBlobHash != null && !splitBlobHash.equals(givenBlobHash)
                    || headBlobHash != null && givenBlobHash == null && !splitBlobHash.equals(headBlobHash)) {
                addressConflict(fileName, headBlobHash, givenBlobHash);
                conflict = true;
            }
        }

        // Generate merge commit and checkout corresponding files.
        commit(String.format("Merged %s into %s.", branchName, head), givenCommitHash);
        checkoutBranch(head);
        // Judge whether there is conflict.
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }

        saveRefs();
    }
}
