package pdmlc.nssr.process;

import org.openscience.cdk.exception.CDKException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

class ProcessQueue {

    private Queue<Entry> queue = new LinkedList<>();

    private DBProcessor dbProcessor;
    private int processThreshhold = 20;

    ProcessQueue(DBProcessor dbProcessor) {
        this.dbProcessor = dbProcessor;
    }

    void push(Entry entry) {
        boolean offering = queue.add(entry);
        if (!offering || queue.size() >= processThreshhold) {
            processRemaining();
        }
    }

    void processRemaining() {
        while (!queue.isEmpty()) {
            processNext();
        }
        try {
            dbProcessor.flushStreams();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void processNext() {
        try {
            queue.remove().process().export(dbProcessor);
        } catch (CDKException | IOException e) {
            e.printStackTrace();
        }
    }

}
