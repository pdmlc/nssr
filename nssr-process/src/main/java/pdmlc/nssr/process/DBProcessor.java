package pdmlc.nssr.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

class DBProcessor {

    static final int FP_DIM = 1 << 16;

    private static final String recordPath = "records";
    private static final String recordExt = ".json";
    private static final String metaBase = "meta_records";
    private static final String peakBase = "peak_records";
    private static final String smileBase = "smile_records";
    private static final String fingerprintBase = "fingerprint_records";

    private InputStream nmrshiftdb;

    private OutputStream metaRecords;
    private OutputStream peakRecords;
    private OutputStream smileRecords;
    private OutputStream fingerprintRecords;

    private String[] recordBases = new String[]{metaBase, peakBase, smileBase, fingerprintBase};
    private OutputStream[] recordStreams = new OutputStream[4];

    DBProcessor() throws IOException {
        initStreams();
    }

    void initStreams() throws IOException {

    }

    void openStreams() throws IOException {
        for (int i = 0; i < recordBases.length; i++) {
            String recordBase = recordBases[i];
//            String date = new SimpleDateFormat("yyyy-MM-dd'T'HHmmSS").format(new Date());
            String date = "";
            File recordFile = new File(recordPath + "\\" + recordBase + "_" + date + recordExt);
            recordStreams[i] = Files.newOutputStream(recordFile.toPath(),
                                                     StandardOpenOption.WRITE,
                                                     StandardOpenOption.CREATE,
                                                     StandardOpenOption.TRUNCATE_EXISTING);
        }
        nmrshiftdb = Files.newInputStream(new File("nmrshiftdb2.xml").toPath(), StandardOpenOption.READ);
    }

    void flushStreams() throws IOException {
        for (int i = 0; i < recordStreams.length; i++) {
            recordStreams[i].flush();
        }
    }

    void closeStreams() throws IOException {
        for (int i = 0; i < recordStreams.length; i++) {
            recordStreams[i].close();
        }
    }

    void writeBytes(byte[][] bytes) throws IOException {
        if (bytes.length != recordStreams.length) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < bytes.length; i++) {
            recordStreams[i].write(bytes[i]);
        }
    }

    InputStream getNmrshiftdb() {
        return nmrshiftdb;
    }

    OutputStream[] getRecordStreams() {
        return recordStreams;
    }

}
