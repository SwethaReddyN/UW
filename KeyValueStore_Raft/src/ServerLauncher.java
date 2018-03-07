
/** *
 *
 * Starts the server component. Supported protocols - TCP, UDP, RMI
 * If port number is missing, prints error message and stops
 * Otherwise starts server component and passes Server port to the component
 *
 */

import java.rmi.RemoteException;
import keyvaluestore.GenericNode;
import raft.RaftServer;

public class ServerLauncher {

    public void createServer(String serverDetails[], GenericNode genericNode) {

        String protocol = serverDetails[0].toLowerCase().trim();
        if (protocol.equals("rmis") && serverDetails.length < 3) {

            new UsageMessages().printServerUsageMessage();
            return;
        }

        try {

            startRmiServer(genericNode, Integer.parseInt(serverDetails[1]), serverDetails[2], 9055);
            
        } catch (NumberFormatException ex) {

            System.out.println("Illegal port number");
            new UsageMessages().printServerUsageMessage();
        }
    }

    protected void startRmiServer(GenericNode genericNode, 
            int serverPort, String memberShipServerIp, int memberShipServerPort) {

        try {
            System.out.println(new RaftServer(serverPort, memberShipServerIp,
                    memberShipServerPort).startServer(genericNode));
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }
    }
}