package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author David Thai
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */

    public static void main(String... args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (Repo.validOperands(args[0], args)) {
            helper(args);
        }
        System.exit(0);
    }
    private static void helper(String... args) {
        switch (args[0]) {
        case "init": {
            Repo.init();
            break;
        }
        case "add": {
            Repo.add(args[1]);
            break;
        }
        case "commit": {
            Repo.commit(args[1]);
            break;
        }
        case "rm": {
            Repo.rm(args[1]);
            break;
        }
        case "log": {
            Repo.log();
            break;
        }
        case "global-log": {
            Repo.globalLog();
            break;
        }
        case "find": {
            Repo.find(args[1]);
            break;
        }
        case "status": {
            Repo.status();
            break;
        }
        case "checkout": {
            Repo.checkout(args);
            break;
        }
        case "branch": {
            Repo.branch(args[1]);
            break;
        }
        case "rm-branch": {
            Repo.rmBranch(args[1]);
            break;
        }
        case "reset": {
            Repo.reset(args[1]);
            break;
        }
        case "merge": {
            Repo.merge(args[1]);
            break;
        }
        default:
            System.out.println("No command with that name exists.");
        }
    }
}
