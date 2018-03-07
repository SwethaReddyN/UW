package raft;

import java.io.Serializable;


public class LogEntry implements Serializable {

    private int logTerm;
    private int logIndex;
    private String command;
    
    public LogEntry(int term, int index, String comm) {
    
        logTerm = term;
        logIndex = index;
        command = comm;
    }
    
    public int getLogTerm() {
        
        return logTerm;
    }
    
    public int getLogIndex() {
        
        return logIndex;
    }
    
    public String getCommand() {
        
        return command;
    }
}
