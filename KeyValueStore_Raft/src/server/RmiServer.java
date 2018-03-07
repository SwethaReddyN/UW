/**
 *
 * Class to create registry and export the services, Stop registry, and
 * call GenericNode methods to handle client requests.
 *
 * Creates a new rmi registry at port 9055 and is stopped when exit request is
 * received from client
 *
 */
package server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import keyvaluestore.GenericNode;
import keyvaluestore.GenericNodeInterface;
import server.nodemembership.Directory;

public class RmiServer extends UnicastRemoteObject
        implements GenericNodeInterface {

    protected static GenericNode genericNode;
    String memberShipServerIp;
    int memberShipServerPort;
    protected Directory directory;
    protected String serverIpAddress;
    protected int serverPort;

    public RmiServer(int serverPort, String memberShipServerIp, int memberShipServerPort)
            throws RemoteException {

        this.serverPort = serverPort;
        this.memberShipServerIp = memberShipServerIp;
        this.memberShipServerPort = memberShipServerPort;
        directory = new Directory(memberShipServerIp, memberShipServerPort);
    }

    public String startServer(GenericNode genericNode) {

        RmiServer server = this;
        RmiServer.genericNode = genericNode;
     
        try {

            //Create an instance of the services to be exported
            directory.startDirectoryService(/*getServerIpAddress()*/ "localhost", serverPort);
            GenericNodeInterface keyStoreStub = null;

            try {
                //Export the instance or in other words create a stub
                keyStoreStub = (GenericNodeInterface) UnicastRemoteObject
                        .exportObject(server, 0);
            } catch (ExportException ex) {

                //If a stub is already created, then get it.
                keyStoreStub = (GenericNodeInterface) UnicastRemoteObject
                        .toStub(server);
            }

            Registry registry;
            try {
                //Create an RMI registry at port serverPort
                registry = LocateRegistry.createRegistry(serverPort);
            } catch (ExportException ex) {
                //If registry is already created, then get that
                registry = LocateRegistry.getRegistry();
            }
            //Bind the services to the registry
            registry.rebind("RaftKeyValueStore", keyStoreStub);
        } catch (ConnectException ex) {
            System.out.println("Connection Refused");
            System.exit(0);
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
            System.exit(0);
        }

        return ("RMI services bound and ready");
    }

//    protected String getServerIpAddress() {
//
//        SubnetInfo subnet = (new SubnetUtils(memberShipServerIp, "255.255.255.0")).getInfo();
//
//        try {
//            Enumeration nis = NetworkInterface.getNetworkInterfaces();
//            while (nis.hasMoreElements()) {
//                NetworkInterface ni = (NetworkInterface) nis.nextElement();
//                Enumeration ias = ni.getInetAddresses();
//                while (ias.hasMoreElements()) {
//                    InetAddress ia = (InetAddress) ias.nextElement();
//                    if (ia.isAnyLocalAddress() || ia.isLinkLocalAddress()
//                            || ia.isLoopbackAddress()) {
//                        continue;
//                    }
//                    if (subnet.isInRange(ia.getHostAddress())) {
//
//                        serverIpAddress = ia.getHostAddress();
//                        return serverIpAddress;
//                    }
//                }
//
//            }
//        } catch (SocketException ex) {
//            System.out.println(ex.getMessage());
//        }
//
//        return serverIpAddress;
//    }

    public void stopServer() {

        try {
            //Get local registry running at 9055, get the exported stub and
            //unbind the resources
            Registry registry = LocateRegistry.getRegistry(serverPort);
            GenericNodeInterface keyStoreStub = (GenericNodeInterface) registry
                    .lookup("RaftKeyValueStore");
            registry.unbind("RaftKeyValueStore");
            UnicastRemoteObject.unexportObject(this, true);
            directory.shutDownDirectoryService(serverIpAddress,
                    serverPort);
        } catch (NotBoundException | AccessException ex) {

            System.out.println(ex.getMessage());
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        } finally {

            System.out.println("RMI Server shut down...");
            System.exit(0);
        }
    }

    @Override
    public String put(String key, String value) throws RemoteException {

        System.out.println("RMI service request received: put " + key
                + ", " + value);
        return genericNode.put(key, value);
    }

    @Override
    public String get(String key) throws RemoteException {

        System.out.println("RMI service request received: get " + key);
        return genericNode.get(key);
    }

    @Override
    public String delete(String key) throws RemoteException {

        System.out.println("RMI service request received: del " + key);
        return genericNode.delete(key);
    }

    @Override
    public String store() throws RemoteException {

        System.out.println("RMI service request received: store");
        return genericNode.store();
    }

    @Override
    public void exit() throws RemoteException {

        System.out.println("RMI service request received: exit");
        stopServer();
    }
}