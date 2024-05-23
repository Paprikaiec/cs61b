package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;
//import java.util.Collections;

// TODO: any imports you need here
import static gitlet.Utils.*;
import java.text.SimpleDateFormat;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author wyw
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits file. */
    public static final File COMMITS_FILE = join(GITLET_DIR, "commits");
    /** The current head pointer file(store branch name). */
    public static final File CURRENTBRANCH_FILE = join(GITLET_DIR, "currentBranch");
    /** The branches file(store hash map for branch pointer name and reference). */
    public static final File BRANCHES_FILE = join(GITLET_DIR, "branches");
    public static final File CURRENTCOMMITHASH_FILE = join(GITLET_DIR, "CurrentCommitHash");
    /** The blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The staging file for addition references map(store hash map for staging blobs) */
    public static final File ADDMAP_FILE = join(GITLET_DIR, "addMap");
    /** The staging file for removal name set */
    public static final File RMSET_FILE = join(GITLET_DIR, "rmSet");

    /** The hash map of commits<commit hash, commit>(need prepareRef() to create). */
    private static HashMap<String, Commit> commits;
    /** The hash map of branches<branch name, commit hash>(need prepareRef() to create). */
    private static HashMap<String, String> branches;
    /** The current branch name(need prepareRef() to create). */
    private static String currentBranch;
    /** The current commit hash(need prepareRef() to create). */
    private static String currentCommitHash;
    /** The current commit object(need prepareRef() to create). */
    private static Commit currentCommit;
    /** The hash map of Staging references and file for addition<file name, blob hash>(need prepareRef() to create). */
    private static HashMap<String, String> addMap;
    /** The hash set of Staging file name for removal. */
    private static HashSet<String> rmSet;
    /* TODO: fill in the rest of this class. */
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

        if (!BRANCHES_FILE.exists()) {
            try {
                BRANCHES_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!CURRENTBRANCH_FILE.exists()) {
            try {
                CURRENTBRANCH_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!CURRENTCOMMITHASH_FILE.exists()) {
            try {
                CURRENTCOMMITHASH_FILE.createNewFile();
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
        Commit iniCommit = new Commit(new Date(0), "initial commit", new HashMap<>(), null, null);
        String iniHash = sha1Hash(iniCommit);
        commits.put(iniHash, iniCommit);

        // Set new HEAD point to the initial commit.
        currentBranch = "master";
        currentCommitHash = iniHash;

        // Set new hash map to store branches reference name.
        branches = new HashMap<>();
        branches.put(currentBranch, currentCommitHash);

        // Set addMap file to store file for addition.
        addMap = new HashMap<>();

        // Set rmSet file to store file for removal.
        rmSet = new HashSet<>();

        saveRefs();
    }

    /** Read references from the corresponding files. */
    private static void loadRefs() {
        commits = readObject(COMMITS_FILE, HashMap.class);
        branches = readObject(BRANCHES_FILE, HashMap.class);
        currentBranch = readContentsAsString(CURRENTBRANCH_FILE);
        currentCommitHash = readContentsAsString(CURRENTCOMMITHASH_FILE);
        currentCommit = commits.get(currentCommitHash);
        addMap = readObject(ADDMAP_FILE, HashMap.class);
        rmSet = readObject(RMSET_FILE, HashSet.class);

    }

    private static void saveRefs() {
        writeObject(COMMITS_FILE, commits);
        writeObject(BRANCHES_FILE, branches);
        writeContents(CURRENTBRANCH_FILE, currentBranch);
        writeContents(CURRENTCOMMITHASH_FILE, currentCommitHash);
        writeObject(ADDMAP_FILE, addMap);
        writeObject(RMSET_FILE, rmSet);
    }

    public static void gitletAdd(String fileName) {
        if (!GITLET_DIR.exists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }

        // Judge whether the fileName file exist.
        if (!dirContainsFile(CWD,fileName)) {
            exitWithError("File does not exist.");
        }

        loadRefs();

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

        saveRefs();
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

        // Update commits.
        HashMap<String, String> blobs = currentCommit.blobs;
        for (String key : rmSet) {
            blobs.remove(key);
        }
        blobs.putAll(addMap);

        Commit commit = new Commit(new Date(), message, blobs, currentCommitHash, null);
        String commitHash = sha1Hash(commit);
        commits.put(commitHash, commit);

        // Update branches and commit hash.
        branches.put(currentBranch, commitHash);
        currentCommitHash = commitHash;

        // Update addMap and rmSet(clear).
        addMap.clear();
        rmSet.clear();

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

        Boolean fd = false;

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

    private static void printStatusFiles(String title, TreeSet<String> Files) {
        System.out.println(title);
        for (String fileName : Files) {
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
            if (name.equals(currentBranch)) {
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
        TreeSet<String > deleted = new TreeSet<>();
        TreeSet<String> untracked = new TreeSet<>();
        
        if (currentFiles != null) {
            for (String fileName : currentFiles) {
                // Prepare probably deleted fileName.
                commitedFiles.remove(fileName);
                addFiles.remove(fileName);
                // Judge whether is untracked.
                if (rmSet.contains(fileName) ||
                        !currentCommit.blobs.containsKey(fileName) && !addMap.containsKey(fileName)) {
                    untracked.add(fileName);
                    continue;
                }
                // Judge whether is modified.
                Blob cwdBlob = new Blob(fileName, join(CWD, fileName));
                String cwdBlobHash = sha1Hash(cwdBlob);
                if (addMap.containsKey(fileName) && cwdBlobHash.equals(addMap.get(fileName)) ||
                        !addMap.containsKey(fileName) && cwdBlobHash.equals(currentCommit.blobs.get(fileName))) {
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
        if (!commits.containsKey(commitHash)) {
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

        if (branchName.equals(currentBranch)) {
            exitWithError("No need to checkout the current branch.");
        }

        String checkoutCommitHash = branches.get(branchName);
        Commit checkoutCommit = commits.get(checkoutCommitHash);

        // Judge whether CWD contains(probably when change branch) untracked files.
        List<String> currentFiles = plainFilenamesIn(CWD);
        if (currentFiles != null) {
            for (String fileName : currentFiles) {
                Blob cwdBlob = new Blob(fileName, join(CWD, fileName));
                String cwdBlobHash = sha1Hash(cwdBlob);
                if (!cwdBlobHash.equals(currentCommit.blobs.get(fileName)) &&
                        !cwdBlobHash.equals(checkoutCommit.blobs.get(fileName))) {
                    exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
                }
            }
        }
        // Dump checkout commit files to the CWD(overwrite or create new).
        if (!currentCommitHash.equals(checkoutCommitHash)) {
            for (String checkFileName : checkoutCommit.blobs.keySet()) {
                checkout(checkoutCommitHash, checkFileName);
            }
        }

        // Change the head.
        currentBranch = branchName;
        currentCommitHash = checkoutCommitHash;

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
            checkout(args[0], args[2]);
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

        if (branchName.equals(currentBranch)) {
            exitWithError("Cannot remove the current branch.");
        }

        branches.remove(branchName);

        saveRefs();
    }

    
}
