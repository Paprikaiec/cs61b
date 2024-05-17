package gitlet;



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
            // TODO: FILL THE REST IN
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
