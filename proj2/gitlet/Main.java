package gitlet;



import java.util.Arrays;

import static gitlet.Repository.*;
import static gitlet.Utils.*;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author wyw
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
           exitWithError("Please enter a command.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs(args, 1);
                gitletInit();
                break;
            case "add":
                validateNumArgs(args, 2);
                gitletAdd(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                gitletCommit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                gitletRm(args[1]);
                break;
            case "log" :
                validateNumArgs(args, 1);
                gitletLog();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                gitletGlobalLog();
                break;
            case "find":
                validateNumArgs(args, 2);
                gitletFind(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                gitletStatus();
                break;
            case "checkout":
                if (args.length > 4 || args.length == 1 ||
                        args.length > 2 && !args[args.length - 2].equals("--")) {
                    exitWithError("Incorrect operands.");
                }
                gitletCheckout(Arrays.copyOfRange(args, 1, args.length));
                break;
            case "branch":
                validateNumArgs(args, 2);
                gitletBranch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                gitletRmBranch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                gitletReset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                gitletMerge(args[1]);
                break;
            default:
                exitWithError("No command with that name exists.");
        }
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            exitWithError("Incorrect operands.");
        }
    }
}
