package raft;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Queue;
import keyvaluestore.GenericNodeInterface;

public interface RaftGenericNodeInterface extends GenericNodeInterface {

    public VoteResponse requestVote(int term, int candidatePort,
            int lastLogIndex, int lastLogTerm) throws RemoteException;
    
    public int getLeaderPort() throws RemoteException;
    
    public Queue<LogEntry> getLeaderCommittedLog() throws RemoteException;
    
    public void leaderDown() throws RemoteException;

    //heartbeat
    public void appendEntries(int term, int leaderPort) throws RemoteException;

    public AppendEntryResponse appendEntries(int term, int leaderPort, int prevLogIndex,
            int prevLogTerm, ArrayList<LogEntry> log, int commitIndex) throws RemoteException;
    
    public void appendEntries(int term, int leaderPort, int commitLogIndex) throws RemoteException;
}