package raft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class Log {

    ConcurrentHashMap<Integer, LogEntry> log = new ConcurrentHashMap<>();
    private Queue<LogEntry> pendingEntries = new LinkedList<>();
    private Queue<LogEntry> commitedEntries = new LinkedList<>();

    public synchronized void addPendingLogEntry(int logIndex, LogEntry logEntry) {

        log.put(logIndex, logEntry);
        pendingEntries.add(logEntry);
    }

    public synchronized void commitPendingLogEntry(int logIndex) {

        LogEntry logEntry = getLogEntry(logIndex);
        pendingEntries.remove(logEntry);
        commitedEntries.add(logEntry);
    }

    public synchronized void commitLogEntry(int logIndex, LogEntry logEntry) {

        log.put(logIndex, logEntry);
        commitedEntries.add(logEntry);
    }

    public synchronized void removeLogEntry(int logIndex) {

        LogEntry logEntry = getLogEntry(logIndex);
        log.remove(logIndex);
        pendingEntries.remove(logEntry);
    }

    public boolean isLogEmpty() {

        return log.isEmpty();
    }

    public LogEntry getLogEntry(int index) {

        return log.get(index);
    }

    public LogEntry getPreviousLogEntry(int currentIndex) {

        System.err.println("log current index " + currentIndex);
        int[] cEntriesIndex = new int[log.size()];
        ArrayList<LogEntry> entries = new ArrayList(pendingEntries);
        int index = 0;
        for (; index < entries.size(); index++) {

            cEntriesIndex[index] = entries.get(index).getLogIndex();
        }

        entries = new ArrayList(commitedEntries);
        for (int i = 0; i < entries.size(); i++, index++) {

            cEntriesIndex[index] = entries.get(i).getLogIndex();
        }

        Arrays.sort(cEntriesIndex);
        return log.get(currentIndex);
    }

    public LogEntry getLastLogEntry() {

        if (pendingEntries.isEmpty() && commitedEntries.isEmpty()) {
            return null;
        }

        int pIndex = 0;
        if (pendingEntries.size() > 0) {

            pIndex = pendingEntries.peek().getLogIndex();
        }

        int cIndex = 0;
        if (commitedEntries.size() > 0) {

            ArrayList<LogEntry> cEntries = new ArrayList(commitedEntries);
            int[] cEntriesIndex = new int[cEntries.size()];
            for (int i = 0; i < cEntries.size(); i++) {

                cEntriesIndex[i] = cEntries.get(i).getLogIndex();
            }

            Arrays.sort(cEntriesIndex);
            cIndex = cEntriesIndex[cEntriesIndex.length - 1];
        }
        if (pIndex == cIndex) {
            return null;
        }
        if (pIndex > cIndex) {
            return log.get(pIndex);
        } else {
            return log.get(cIndex);
        }
    }

    public Queue<LogEntry> getCommittedLog() {

        return commitedEntries;
    }
}
