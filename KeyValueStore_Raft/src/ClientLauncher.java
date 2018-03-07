
/** *
 *
 * Starts the client component. Supported protocols - TCP, UDP, RMI
 * If server IP or port number or request command is missing,
 * prints error message and stops. Otherwise starts the client component and
 * passes Server IP, Server port and request string (command to be executed on
 * the server like put, get etc.) to the component
 *
 */

import client.RmiClient;

public class ClientLauncher {

    public void createClient(String clientRequestTokens[]) {

        //Checks if user provided all the info required to contact
        //the server and request a service. Prints usage message and stops
        //if not provided. ,
        if ((clientRequestTokens[0].equalsIgnoreCase("rmic")
                && clientRequestTokens.length < 4)) {

            new UsageMessages().printClientUsageMessage();
            return;
        }

        //convert request tokens into request string and start client
        try {
            startRmiClient(clientRequestTokens);
        } catch (NumberFormatException ex) {

            System.out.println("Illegal port number");
            new UsageMessages().printClientUsageMessage();
        }
    }

    protected void startRmiClient(String clientRequestTokens[]) {

        new RmiClient().invokeRemoteMethods(clientRequestTokens);
    }
}
