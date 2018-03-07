package raft;

public class VotedFor {

    int term = 0;
    int votedForCanditate = -1;
    
    public void updateVote(int term, int candidateId) {
        
        this.term = term;
        votedForCanditate = candidateId;
    }
    
    public void incrementTerm() {
        
        term++;
        votedForCanditate = -1;
    }
}
