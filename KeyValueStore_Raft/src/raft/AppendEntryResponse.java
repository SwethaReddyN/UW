package raft;

import java.io.Serializable;

public class AppendEntryResponse implements Serializable {
 
    Integer currentTerm = null;
    boolean success;
    
    public AppendEntryResponse(int term, boolean success) {
        
        currentTerm = term;
        this.success = success;
    }
    
    public AppendEntryResponse(boolean success) {
        
        this.success = success;
    }
}
