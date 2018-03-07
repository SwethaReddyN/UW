package client;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import keyvaluestore.GenericNodeInterface;
import raft.RaftGenericNodeInterface;

public class RmiClient {

    public String invokeRemoteMethods(String[] requestTokens) {

        try {
            Registry registry = LocateRegistry.getRegistry(requestTokens[1], Integer.parseInt(requestTokens[2]));
            GenericNodeInterface keyStoreStub = (RaftGenericNodeInterface) registry
                    .lookup("RaftKeyValueStore");
            return invokeStubMethods(keyStoreStub, requestTokens);
        } catch (NumberFormatException ex) {
            System.out.println("Illegal port");
            System.out.println(ex.getMessage());
        } catch (ConnectException ex) {
            System.out.println("Connection Refused");
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        } catch (NotBoundException ex) {
            System.out.println(ex.getMessage());
        }
        return "ERROR";
    }

    public String invokeRemoteMethods(String[] requestTokens, int port,
            boolean isRaftServer) {

        try {
            Registry registry = LocateRegistry.getRegistry(requestTokens[1], port);
            GenericNodeInterface keyStoreStub = null;
            keyStoreStub = (GenericNodeInterface) registry
                    .lookup("KeyValueStore");
            return invokeStubMethods(keyStoreStub, requestTokens);
        } catch (ConnectException ex) {
            System.out.println("Connection Refused");
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        } catch (NotBoundException ex) {
            System.out.println(ex.getMessage());
        }
        return "ERROR";
    }

    public String invokeStubMethods(GenericNodeInterface keyStoreStub,
            String[] requestTokens) {

        String operation = requestTokens[3].toLowerCase();
        String serverResponse = "";
        try {
            switch (operation) {
                case "put":
                    if (requestTokens.length < 6) {
//                        System.out.println("ERROR:Missing parameters, "
//                                + "please enter key and value");
                        return "ERROR:Missing parameters,please enter key and value";
                    }
                    serverResponse += keyStoreStub.put(requestTokens[4],
                            requestTokens[5]);
                    break;
                case "get":
                    if (requestTokens.length < 5) {
//                        System.out.println("ERROR:Missing parameters, "
//                                + "please enter key");
                        return "ERROR:Missing parameters, please enter key";
                    }
                    serverResponse += keyStoreStub.get(requestTokens[4]);
                    break;
                case "del":
                    if (requestTokens.length < 5) {
//                        System.out.println("ERROR:Missing parameters, "
//                                + "please enter key");
                        return "ERROR:Missing parameters, please enter key";
                    }
                    serverResponse += keyStoreStub.delete(requestTokens[4]);
                    break;
                case "store":
                    serverResponse += keyStoreStub.store();
                    break;
                case "exit":
                    try {
                        keyStoreStub.exit();
                    } catch (UnmarshalException ex) {
                        System.out.println("RMI server shut down...");
                    }
                    return "RMI server shut down...";
                default:
                    serverResponse = "ERROR:Unsupported operation. Supported "
                            + "operations - put, get, del, store, exit";
            }
            System.out.println(serverResponse);
            return serverResponse;
        } catch (RemoteException ex) {
        }
        return serverResponse;
    }
}
