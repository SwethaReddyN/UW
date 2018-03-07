/**
 *
 * Methods to communicate with nodedirectory.
 * Sends put <ip> <port> command to nodedirectory when server is started Fetches
 * latest directory from nodesirectory every 10 secs Sends del <ip> to
 * nodedirectory when server gets exit request
 *
 */
package server.nodemembership;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import keyvaluestore.GenericNodeInterface;
import raft.RaftGenericNodeInterface;

public class Directory implements Runnable {

    public Server[] directory;
    String memberShipServerIp;
    int memberShipServerPort;
    ScheduledExecutorService executor;

    public Directory(String memberShipServerIp, int memberShipServerPort) {

        this.memberShipServerIp = memberShipServerIp;
        this.memberShipServerPort = memberShipServerPort;
    }

    @Override
    public void run() {

        createDirectory();
    }

    public Server[] getDirectory() {

        return directory;
    }

    public void createDirectory() {

        createDirectory(sendRequestToMembershipServer("store"));
    }

    protected void createDirectory(String directoryAsString) {

        if (directoryAsString.equals("ERROR")) {
            return;
        }
        String serverInfoAsString[] = directoryAsString.split(System.lineSeparator());
        ArrayList<Server> serverList = new ArrayList<>();

        for (String serverInfoAsString1 : serverInfoAsString) {
            if (serverInfoAsString1.trim().length() == 0) {
                continue;
            }
            String[] tokens = serverInfoAsString1.split(",");
            serverList.add(new Server(tokens[1].split("=")[1].trim(),
                    Integer.parseInt(tokens[0].split("=")[1].trim())));
        }

//        System.out.println("response from server"); 
//        printList(serverList);
//        if (directory != null && directory.length > 0) {
//            ArrayList<Server> currentList = new ArrayList<Server>(Arrays.asList(directory));
//            ArrayList<Server> removeList = new ArrayList<Server>(Arrays.asList(directory));
//
//            removeList.removeAll(serverList);
//            System.out.println("remove list"); 
//            printList(removeList);
//            serverList.removeAll(currentList);
//            System.out.println("add list"); 
//            printList(serverList);
//
//            currentList.removeAll(removeList);
//            currentList.addAll(serverList);
//            System.out.println("final list"); 
//            printList(currentList);
//            directory = currentList.toArray(new Server[currentList.size()]);
//        } else {
        directory = serverList.toArray(new Server[serverList.size()]);
//        }
    }

    public int getNumberOfServer() {
    
        return directory.length;
    }
    
    public void startDirectoryService(String serverIp, int serverPort) {

        String comm = "put " + serverPort + " " + serverIp;
        sendRequestToMembershipServer(comm);
        createDirectory();
        scheduleCreateDirectoryTask();
    }

    public int getLeaderPort() {

        if (directory == null || directory.length == 0) {
            return -1;
        }

        try {

            for (int i = 0; i < directory.length; i++) {

                Registry registry = LocateRegistry.getRegistry(directory[i].getIpAddress(), directory[i].getPort());
                RaftGenericNodeInterface keyStoreStub = (RaftGenericNodeInterface) registry.lookup("RaftKeyValueStore");
                int port = keyStoreStub.getLeaderPort();
                if(port != -1)
                    return port;
            }

        } catch (ConnectException ex) {
        } catch (RemoteException ex) {
        } catch (NotBoundException ex) {
            System.out.println(ex.getMessage());
        }
        return -1;
    }

    protected void scheduleCreateDirectoryTask() {

        executor = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> result = executor.scheduleAtFixedRate(this, 0, 10,
                TimeUnit.SECONDS);
    }

    public void shutDownDirectoryService(String serverIp, int serverPort) {

        executor.shutdownNow();
        sendRequestToMembershipServer("del " + serverPort);
    }

    public String sendRequestToMembershipServer(String command) {

        try {

            String commandTokens[] = command.split("\\s+");

            Registry registry = LocateRegistry.getRegistry(memberShipServerIp, 9055);
            GenericNodeInterface keyStoreStub = (GenericNodeInterface) registry.lookup("KeyValueStore");

            if (commandTokens[0].trim().equals("put")) {

                keyStoreStub.put(commandTokens[1], commandTokens[2]);
            } else if (commandTokens[0].trim().equals("del")) {

                keyStoreStub.delete(commandTokens[1]);
            } else {
                return keyStoreStub.store();
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
        }
        return "";
    }
}
