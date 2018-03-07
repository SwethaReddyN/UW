package raft;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import keyvaluestore.GenericNode;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import server.RmiServer;
import server.nodemembership.Server;

/**
 *
 * @author sweth
 */
public class RaftServer extends RmiServer implements RaftGenericNodeInterface {

    public enum State {
        FOLLOWER, CANDIDATE, LEADER
    }

    private VotedFor votedFor;
    private int currentTerm = 0;
    private State state = State.FOLLOWER;
    private int numberOfServersInCluster = 3;
    private final int ELECTION_TIMEOUT;
    private final int HEARTBEAT_TIMEOUT = 10;
    private static final ScheduledExecutorService timeoutExecutor
            = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture timeoutEvent;
    private String leaderIpAddress = "localhost";
    private int leaderPort = -1;
    private HashMap<String, Integer> nextEntry;
    private Log logs = new Log();
    private int lastLogIndex = 0, commitIndex = 0, lastLogTerm = 0;

    public RaftServer(int serverPort, String memberShipServerIp, int memberShipServerPort)
            throws RemoteException {
        super(serverPort, memberShipServerIp, memberShipServerPort);
        votedFor = new VotedFor();
        ELECTION_TIMEOUT = ThreadLocalRandom.current().nextInt(
                15, 30 + 1);
        serverIpAddress = "localhost";
    }

    @Override
    public String startServer(GenericNode genericNode) {

        String resp = super.startServer(genericNode);
        state = State.FOLLOWER;
        System.out.println("Raft server started in " + state + " mode with timeout "
                + ELECTION_TIMEOUT);

        if (directory.getLeaderPort() != -1) {

            try {
                leaderPort = directory.getLeaderPort();
                RaftGenericNodeInterface keyStoreStub = getRemoteInterface(leaderIpAddress, leaderPort);
                Queue<LogEntry> entries = keyStoreStub.getLeaderCommittedLog();
                if (entries == null || entries.size() == 0) {
                    return resp;
                }
                LogEntry logEntry = null;
                int numberOfLogEntries = entries.size();
                for (int i = 0; i < numberOfLogEntries; i++) {

                    logEntry = entries.remove();
                    logs.commitLogEntry(logEntry.getLogIndex(), logEntry);
                    String[] tokens = logEntry.getCommand().split("\\s+");
                    try {
                        if (tokens[0].trim().equalsIgnoreCase("put")) {
                            System.out.println(super.put(tokens[1], tokens[2]));
                        } else {
                            super.delete(tokens[1]);
                        }
                    } catch (RemoteException ex) {
                        System.out.println(ex.getMessage());
                    }
                }

                this.lastLogIndex = logEntry.getLogIndex();
                this.lastLogTerm = logEntry.getLogTerm();
                this.commitIndex = lastLogIndex;
            } catch (RemoteException ex) {
                Logger.getLogger(RaftServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        timeoutEvent = timeoutExecutor.schedule(this::handleElectionTimeout,
                ELECTION_TIMEOUT, TimeUnit.SECONDS);
        return resp;
    }

    public void startElectionProcess() {

        currentTerm++;
        timeoutEvent.cancel(true);
        timeoutEvent = timeoutExecutor.schedule(this::handleElectionTimeout,
                ELECTION_TIMEOUT, TimeUnit.SECONDS);

        Server[] servers = directory.getDirectory();
        int numberOfVotes = 1;
        votedFor.updateVote(currentTerm, serverPort);

        RaftGenericNodeInterface keyStoreStub;
        for (int i = 0; (i < servers.length
                && state.equals(State.CANDIDATE)); i++) {

            if (servers[i].getPort() == serverPort) {
                continue;
            }

            try {
                keyStoreStub = getRemoteInterface(
                        servers[i].getIpAddress(), servers[i].getPort());

                VoteResponse response = keyStoreStub.requestVote(currentTerm,
                        serverPort, 0, 0);
                if (isVoteGranted(response)) {
                    numberOfVotes++;
                    if(numberOfVotes >= (servers.length / 2) + 1) {
                        
                        becomeLeader();
                        break;
                    }
                    
                }
                if(timeoutEvent.isCancelled())
                    break;
                
            } catch (RemoteException ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (state.equals(State.CANDIDATE)) {
            checkMajorityVotesReceived(numberOfVotes, servers.length);
        }
    }

    public void handleHeartbeatTimeout() {

        Server[] servers = directory.getDirectory();
        for (Server server : servers) {
            if (server.getPort() == serverPort) {
                continue;
            }
            try {
                RaftGenericNodeInterface keyStoreStub = getRemoteInterface(server.getIpAddress(), server.getPort());
                if (keyStoreStub != null) {
                    keyStoreStub.appendEntries(currentTerm, serverPort);
                }
            } catch (RemoteException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    protected boolean isVoteGranted(VoteResponse voteResponse) {

        if (currentTerm < voteResponse.getTerm()) {
            currentTerm = voteResponse.getTerm();
            stopElectionProcess();
        } else {

            if (currentTerm == voteResponse.getTerm()) {

                if (voteResponse.getVoteGranted()) {
                    return true;
                }
            }
        }

        return false;

    }

    protected void checkMajorityVotesReceived(int numberOfVotes, int totalVotes) {

        if (numberOfVotes >= (totalVotes / 2) + 1) {

            becomeLeader();
        } else if (numberOfVotes < (totalVotes / 2)) {
            becomeFollower();
        } else {
            System.out.println("split vote");
        }
    }

    protected void initializeNextIndex() {

        Server[] servers = directory.getDirectory();
        int totalFollowers = servers.length;
        nextEntry = new HashMap<>(totalFollowers);
        for (int i = 0; i < totalFollowers; i++) {

            nextEntry.put(servers[i].getIpAddress(), lastLogIndex + 1);
        }
    }

    public void becomeLeader() {

        state = State.LEADER;
        timeoutEvent.cancel(true);
        System.out.println("state changed to " + state);
        handleHeartbeatTimeout();
        timeoutEvent = timeoutExecutor.scheduleAtFixedRate(
                this::handleHeartbeatTimeout, 0, HEARTBEAT_TIMEOUT,
                TimeUnit.SECONDS);
        leaderIpAddress = serverIpAddress;
        leaderPort = serverPort;

        initializeNextIndex();
    }

    public void becomeFollower() {

        state = State.FOLLOWER;
        timeoutEvent.cancel(true);
        System.out.println("state changed to " + state);
        nextEntry = null;
    }

    public void stopElectionProcess() {

        timeoutEvent.cancel(true);
        state = State.FOLLOWER;
        System.out.println("state changed to " + state);
    }

    public void handleElectionTimeout() {

        if(timeoutEvent.isCancelled() || timeoutEvent.isDone())
            return ;
        switch (state) {

            case FOLLOWER:
                System.out.println("No leader, start new election");
                state = State.CANDIDATE;
                System.out.println("state changed to " + state);
                numberOfServersInCluster = directory.getNumberOfServer();
                startElectionProcess();
                break;
            case LEADER:
                assert false;
            case CANDIDATE:
                System.out.println("No election result, restart election");
                startElectionProcess();
            default:
                throw new IllegalStateException();
        }
    }

    protected RaftGenericNodeInterface getRemoteInterface(String ipAddress, int port) {

        try {
            Registry registry = LocateRegistry.getRegistry(ipAddress, port);
            return (RaftGenericNodeInterface) registry.lookup("RaftKeyValueStore");
        } catch (ConnectException ex) {
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        } catch (NotBoundException ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private boolean isAtLeastUpToDateAsCandidate(int cLlastLogTerm, int cLastLogIndex) {
        // Based on 5.4.2 paragraph of the Raft paper
        return (cLlastLogTerm > lastLogTerm
                || (cLlastLogTerm == lastLogTerm && cLastLogIndex >= lastLogIndex));
    }

    @Override
    public VoteResponse requestVote(int term, int candidatePort,
            int cLastLogIndex, int cLlastLogTerm) throws RemoteException {

        boolean voteGranted = false;
        if (votedFor.term < term) {

            votedFor.updateVote(term, candidatePort);
            currentTerm = term;
            voteGranted = true;
            if(state != state.FOLLOWER)
                System.out.println("state changed to " + state.FOLLOWER);
            state = state.FOLLOWER;
        } else if (votedFor.term == term && (votedFor.votedForCanditate == -1
                || votedFor.votedForCanditate == candidatePort)) {

            if (isAtLeastUpToDateAsCandidate(cLlastLogTerm, cLastLogIndex)) {

                votedFor.updateVote(term, candidatePort);
                currentTerm = term;
                voteGranted = true;
                timeoutEvent.cancel(true);
            }
        }

        return new VoteResponse(currentTerm, voteGranted);
    }

    @Override
    public void appendEntries(int term, int leaderPort) {

        if (term >= currentTerm) {

            timeoutEvent.cancel(true);
            this.leaderPort = leaderPort;
            currentTerm = term;
            timeoutEvent = timeoutExecutor.schedule(this::handleElectionTimeout,
                    ELECTION_TIMEOUT, TimeUnit.SECONDS);
            if (state.equals(state.LEADER)) {
                System.out.println("state changed to " + State.FOLLOWER);
            }
            state = State.FOLLOWER;

        }
    }

    @Override
    public void appendEntries(int term, int leaderPort, int commitIndex) {

        timeoutEvent.cancel(true);
        this.commitIndex = commitIndex;
        LogEntry logEntry = logs.getLogEntry(commitIndex);
        logs.commitPendingLogEntry(commitIndex);
        String[] tokens = logEntry.getCommand().split("\\s+");
        try {
            if (tokens[0].trim().equalsIgnoreCase("put")) {
                System.out.println(super.put(tokens[1], tokens[2]));
            } else {
                super.delete(tokens[1]);
            }
        } catch (RemoteException ex) {
            System.out.println(ex.getMessage());
        }
        timeoutEvent = timeoutExecutor.schedule(this::handleElectionTimeout,
                ELECTION_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public AppendEntryResponse appendEntries(int term, int leaderPort, int prevLogIndex,
            int prevLogTerm, ArrayList<LogEntry> log, int commitIndex) {

        timeoutEvent.cancel(true);
        timeoutEvent = timeoutExecutor.schedule(this::handleElectionTimeout,
                ELECTION_TIMEOUT, TimeUnit.SECONDS);
        if (term < currentTerm) {
            System.out.println("return false term");
            return new AppendEntryResponse(false);
        }

        LogEntry lastLogEntry = logs.getLastLogEntry();
        if (prevLogIndex == 0 || lastLogEntry == null) {

            LogEntry logEntry = log.get(0);
            lastLogIndex = logEntry.getLogIndex();
            lastLogTerm = logEntry.getLogTerm();
            logs.addPendingLogEntry(logEntry.getLogIndex(), logEntry);
            return new AppendEntryResponse(true);
        } else {

            if (lastLogEntry != null && (lastLogEntry.getLogTerm() == prevLogTerm
                    && lastLogEntry.getLogIndex() == prevLogIndex)) {

                for (int i = 0; i < log.size(); i++) {

                    LogEntry logEntry = log.get(i);
                    logs.addPendingLogEntry(logEntry.getLogIndex(), logEntry);
                    lastLogIndex = logEntry.getLogIndex();
                    lastLogTerm = logEntry.getLogTerm();
                }
                return new AppendEntryResponse(true);
            }
        }
        return new AppendEntryResponse(false);
    }

    public void sendCommitEntryToFollowers(int term, int leaderPort, int logIndex) {

        timeoutEvent.cancel(true);
        timeoutEvent = timeoutExecutor.scheduleAtFixedRate(
                this::handleHeartbeatTimeout, 0, HEARTBEAT_TIMEOUT,
                TimeUnit.SECONDS);

        Server[] servers = directory.getDirectory();
        if (servers == null || servers.length == 0) {
            return;
        }
        int numberOfFollowers = servers.length;
        ExecutorService service = Executors.newFixedThreadPool(numberOfFollowers);
        for (int i = 0; i < numberOfFollowers; i++) {

            final String ipAddress = servers[i].getIpAddress();
            final int port = servers[i].getPort();
            service.submit(() -> {
                try {
                    if (leaderPort != port) {

                        RaftGenericNodeInterface keyStoreStub = getRemoteInterface(ipAddress, port);
                        if (keyStoreStub != null) {
                            keyStoreStub.appendEntries(currentTerm, leaderPort,
                                    logIndex);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
    }

    public boolean sendAppendEntriesToFollowers(int prevLogIndex,
            int prevLogTerm, LogEntry logEntry) {

        timeoutEvent.cancel(true);
        timeoutEvent = timeoutExecutor.scheduleAtFixedRate(
                this::handleHeartbeatTimeout, 0, HEARTBEAT_TIMEOUT,
                TimeUnit.SECONDS);

        Server[] servers = directory.getDirectory();
        if (servers == null || servers.length == 0) {
            return true;
        }

        final String leaderIp = leaderIpAddress;
        int numberOfFollowers = servers.length;
        ExecutorService service = Executors.newFixedThreadPool(numberOfFollowers);
        ArrayList<LogEntry> sendLogs = new ArrayList<>();
        List<String> logUpdatedSuccessfully = new CopyOnWriteArrayList<String> ();
        sendLogs.add(logEntry);

        for (int i = 0; i < numberOfFollowers; i++) {

            final String ipAddress = servers[i].getIpAddress();
            final int port = servers[i].getPort();
            service.submit(() -> {
                try {
                    if (port != leaderPort) {

                        RaftGenericNodeInterface keyStoreStub
                                = getRemoteInterface(ipAddress, port);
                        if (keyStoreStub != null) {

                            AppendEntryResponse response = keyStoreStub
                                    .appendEntries(currentTerm, leaderPort,
                                            prevLogIndex, prevLogTerm, sendLogs, commitIndex);

                            if (!response.success) {

                                int tempPrevLogIndex = prevLogIndex,
                                        tempPrevLogTerm = prevLogTerm;

                                while (true) {

                                    LogEntry tempEntry = logs.getPreviousLogEntry(tempPrevLogIndex);
                                    
                                    if (tempEntry == null) {
                                        logUpdatedSuccessfully.add("false");
                                        break;
                                    }
                                    tempPrevLogIndex = tempEntry.getLogIndex();
                                    tempPrevLogTerm = tempEntry.getLogTerm();
                                    sendLogs.add(0, tempEntry);
                                    response = keyStoreStub
                                            .appendEntries(currentTerm, leaderPort,
                                                    tempPrevLogIndex, tempPrevLogTerm, sendLogs, commitIndex);
                                    if (response.success) {
                                        logUpdatedSuccessfully.add("true");
                                        break;
                                    }
                                }
                            } else {
                                
                                logUpdatedSuccessfully.add("true");
                            }
                            
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    logUpdatedSuccessfully.add("false");
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
        }
        
        int successNumber = 1;
        for(int i = 0 ; i < logUpdatedSuccessfully.size(); i++) {
            
            if(logUpdatedSuccessfully.get(i).equals("true"))
                successNumber++;
        }
        if(((numberOfServersInCluster / 2) + 1) > successNumber)
            return false;
        return true;
    }

    @Override
    public String delete(String key) throws RemoteException {

        System.out.println("RMI service request received: del " + key);

        RaftGenericNodeInterface keyStoreStub;
        if (!state.equals(state.LEADER)) {

            if (leaderPort == -1) {

                leaderPort = directory.getLeaderPort();
                if (leaderPort == -1) {
                    System.out.println("ERROR:Leader not yet selected");
                    return "ERROR:Leader not yet selected";
                }
            }
            keyStoreStub = getRemoteInterface(leaderIpAddress, leaderPort);
            return keyStoreStub.delete(key);
        }

        int prevLogIndex = 0, prevLogTerm = 0;

        if (!logs.isLogEmpty()) {

            LogEntry logEntry = logs.getLastLogEntry();
			if (logEntry != null) {
				prevLogIndex = logEntry.getLogIndex();
				prevLogTerm = logEntry.getLogTerm();
			}
        }	

        LogEntry logEntry = new LogEntry(currentTerm, ++lastLogIndex, "del "
                + key);
        logs.addPendingLogEntry(lastLogIndex, logEntry);

        if (sendAppendEntriesToFollowers(prevLogIndex, prevLogTerm, logEntry)) {

            String response = super.delete(key);
            logs.commitPendingLogEntry(lastLogIndex);
            commitIndex = logEntry.getLogIndex();
            sendCommitEntryToFollowers(logEntry.getLogTerm(), leaderPort, logEntry.getLogIndex());
            return response;
        }
        
        logs.removeLogEntry(lastLogIndex);
        return "ERROR:Could not update key value store. Minimum number of servers not updated log successfully";
    }
    
    @Override
    public int getLeaderPort() {

        return leaderPort;
    }

    @Override
    public void leaderDown() {

        leaderPort = -1;
    }

    @Override
    public String put(String key, String value) throws RemoteException {

        System.out.println("RMI service request received: put " + key
                + ", " + value);

        RaftGenericNodeInterface keyStoreStub;
        if (!state.equals(state.LEADER)) {

            if (leaderPort == -1) {

                leaderPort = directory.getLeaderPort();
                if (leaderPort == -1) {
                    System.out.println("ERROR:Leader not yet selected");
                    return "ERROR:Leader not yet selected";
                }

            }
            keyStoreStub = getRemoteInterface(leaderIpAddress, leaderPort);
            return keyStoreStub.put(key, value);
        }

        int prevLogIndex = 0, prevLogTerm = 0;

        if (!logs.isLogEmpty()) {

            LogEntry logEntry = logs.getLastLogEntry();
            if (logEntry != null) {
                prevLogIndex = logEntry.getLogIndex();
                prevLogTerm = logEntry.getLogTerm();
            }
        }

        LogEntry logEntry = new LogEntry(currentTerm, ++lastLogIndex,
                "put " + key + " " + value);
        logs.addPendingLogEntry(lastLogIndex, logEntry);

        if (sendAppendEntriesToFollowers(prevLogIndex, prevLogTerm, logEntry)) {

            String response = super.put(key, value);
            logs.commitPendingLogEntry(lastLogIndex);
            commitIndex = logEntry.getLogIndex();
            sendCommitEntryToFollowers(logEntry.getLogTerm(), leaderPort, logEntry.getLogIndex());
            return response;
        }
        logs.removeLogEntry(lastLogIndex);
        return "ERROR:Could not update key value store. Minimum number of servers not updated log successfully";
    }

    @Override
    public Queue<LogEntry> getLeaderCommittedLog() {

        return logs.getCommittedLog();
    }

    @Override
    public void exit() throws RemoteException {

        System.out.println("RMI service request received: exit");
        leaderPort = -1;
        if (!state.equals(state.LEADER)) {
            super.stopServer();
            return;
        }

        Server[] servers = directory.getDirectory();
        if (servers == null || servers.length == 0) {
            super.stopServer();
            return;
        }
        int numberOfFollowers = servers.length;
        ExecutorService service = Executors.newFixedThreadPool(numberOfFollowers);

        for (int i = 0; i < numberOfFollowers; i++) {

            final String ipAddress = servers[i].getIpAddress();
            final int port = servers[i].getPort();
            service.submit(() -> {
                try {
                    if (serverPort != port) {

                        RaftGenericNodeInterface keyStoreStub
                                = getRemoteInterface(ipAddress, port);
                        if (keyStoreStub != null) {
                            keyStoreStub.leaderDown();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {

        }
        super.stopServer();
    }
}
