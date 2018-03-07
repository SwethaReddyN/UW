package raft;

import java.io.Serializable;

public class VoteResponse implements Serializable {

    int term;
    boolean voteGranted;
    
    public VoteResponse(int term, boolean voteGranted) {
        
        this.term = term;
        this.voteGranted = voteGranted;
    }
    
    public int getTerm() {
        
        return term;
    }
    
    public boolean getVoteGranted() {
        
        return voteGranted;
    }
}
