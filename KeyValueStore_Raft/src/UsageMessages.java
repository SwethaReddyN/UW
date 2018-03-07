
public class UsageMessages {

    public void printServerUsageMessage() {

        String serverUsageMessage = "Server Usage:" + System.lineSeparator();
        serverUsageMessage += "rmis <serverPort> <membershipServerIP>"
                + " - run RMI Server." + System.lineSeparator();
        
        System.out.println(serverUsageMessage);
    }

    public void printClientUsageMessage() {

        String clientUsageMessage = "Client Usage:" + System.lineSeparator();
        clientUsageMessage += "rmic <address> <port> put <key> <msg> -  RMI CLIENT: "
                + "Put an object into store" + System.lineSeparator();
        clientUsageMessage += "rmic <address> <port> get <key> - RMI CLIENT: "
                + "Get an object from store by key" + System.lineSeparator();
        clientUsageMessage += "rmic <address> <port> del <key> - RMI CLIENT: "
                + "Delete an object from store by key" + System.lineSeparator();
        clientUsageMessage += "rmic <address> <port> store - RMI CLIENT: Display "
                + "object store" + System.lineSeparator();
        clientUsageMessage += "rmic <address> <port> exit - RMI CLIENT: "
                + "Shutdown server" + System.lineSeparator();
        
        System.out.println(clientUsageMessage);
    }
}
