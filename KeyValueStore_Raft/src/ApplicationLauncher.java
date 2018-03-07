/***
 * 
 * Starts client or server components based on the command line arguments 
 * Supports TCP client and server creation,
 *          UDP client and server creation,
 *          RMI client and server (service) creation
 * 
 * Prints usage messages if no arguments are given (or info required to
 *   start the components is missing
 * 
 */

import keyvaluestore.GenericNode;

public class ApplicationLauncher {

    static GenericNode genericNode = new GenericNode();
    static UsageMessages usageMessages = new UsageMessages();
    
    public static void main(String args[]) {

        if (args.length == 0) {

            usageMessages.printClientUsageMessage();
            usageMessages.printServerUsageMessage();
            return;
        }
        
        String protocol = args[0];

        if (protocol.equalsIgnoreCase("rmic")) {

            new ClientLauncher().createClient(args);
        } else if (protocol.equalsIgnoreCase("rmis")) {

            new ServerLauncher().createServer(args, genericNode);
        } else {
            
            System.out.println(protocol + " NOT SUPPORTED");
            System.out.println("Usage :");
            usageMessages.printClientUsageMessage();
            usageMessages.printServerUsageMessage();
        }
    }
}