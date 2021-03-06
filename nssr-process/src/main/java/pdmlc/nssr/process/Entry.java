package pdmlc.nssr.process;

import org.json.simple.JSONObject;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.CircularFingerprinter;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import uk.ac.cam.ch.wwmm.opsin.NameToStructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

class Entry {

    private static final NameToStructure NTS_INSTANCE = NameToStructure.getInstance();
    private static final SmilesGenerator SG_INSTANCE = new SmilesGenerator(SmiFlavor.Canonical);
    private static final CircularFingerprinter FP_INSTANCE =
            new CircularFingerprinter(CircularFingerprinter.CLASS_ECFP4, DBProcessor.FP_DIM);

    IAtomContainer      atomContainer;
    List<IAtom>         atoms;
    List<IBond>         bonds;
    List<Peak>          peaks;
    BitSet              fingerprint;
    String              dbName;
    String              iupacName;
    String              smile;
    String              observeNucleus;
    String              solvent;
    long[]              hashes;
    boolean             processed;
    int                 id;

    static class Peak {

        float       xValue;
        String      multiplicity;
        String      shape;
        String      id;
        String[]    atomRefs;

        Peak(float xValue, String multiplicity,
             String shape, String id, String[] atomRefs) {
            this.xValue         = xValue;
            this.multiplicity   = multiplicity;
            this.shape          = shape;
            this.id             = id;
            this.atomRefs       = atomRefs;
        }

    }

    {
        processed = false;
        atomContainer = new AtomContainer();
        peaks = new ArrayList<>();
        atoms = new ArrayList<>();
        bonds = new ArrayList<>();
        observeNucleus = "1H";
    }

    Entry() {
        this.id = 0;
    }

    Entry(int id) {
        this.id = id;
    }

    Entry process() throws CDKException {

        if (processed) {
            return this;
        }

        atomContainer.setAtoms(atoms.toArray(new IAtom[]{}));
        atomContainer.setBonds(bonds.toArray(new IBond[]{}));

        smile = SG_INSTANCE.create(atomContainer);
        fingerprint = FP_INSTANCE.getBitFingerprint(atomContainer).asBitSet();
//        hashes = FP_INSTANCE.getCountFingerprint(atomContainer).

        processed = true;
        return this;

    }

    void export(DBProcessor dbProcessor) throws IOException {

        Map<String, Object> metaMap = new HashMap<>();
        metaMap.put("id", id);
        metaMap.put("name", dbName);
        metaMap.put("solvent", solvent);
        metaMap.put("spectrum", observeNucleus);
        String metaJson = JSONObject.toJSONString(metaMap) + '\n';
        byte[] metaBytes = metaJson.getBytes(StandardCharsets.UTF_8);

        Map<String, Object> peakMap = new HashMap<>();
        peakMap.put("id", id);
        peakMap.put("peaks", peaksToCondensedBitSet());
        String peakJson = JSONObject.toJSONString(peakMap) + '\n';
        byte[] peakBytes = peakJson.getBytes(StandardCharsets.UTF_8);

        Map<String, Object> smileMap = new HashMap<>();
        smileMap.put("id", id);
        smileMap.put("smile", smile);
        String smileJson = JSONObject.toJSONString(smileMap) + '\n';
        byte[] smileBytes = smileJson.getBytes(StandardCharsets.UTF_8);

        Map<String, Object> fingerprintMap = new HashMap<>();
        fingerprintMap.put("id", id);
        fingerprintMap.put("fingerprint", fingerprintsToCondensedBitSet());
        String fingerprintJson = JSONObject.toJSONString(fingerprintMap) + '\n';
        byte[] fingerprintBytes = fingerprintJson.getBytes(StandardCharsets.UTF_8);

        Map<String, Object> allMap = new HashMap<>();
        allMap.put("id", id);
        allMap.put("name", dbName);
        allMap.put("solvent", solvent);
        allMap.put("spectrum", observeNucleus);
        allMap.put("peaks", peaksToCondensedBitSet());
        allMap.put("smile", smile);
        allMap.put("fingerprint", fingerprintsToCondensedBitSet());
        String allJson = JSONObject.toJSONString(allMap) + '\n';
        byte[] allBytes = allJson.getBytes(StandardCharsets.UTF_8);

        byte[][] bytes = new byte[][]{metaBytes, peakBytes, smileBytes, fingerprintBytes, allBytes};

        dbProcessor.writeBytes(bytes);
    }

    Entry wipeSpectrum() {
        Entry result = new Entry();

        result.atoms = new ArrayList<>(this.atoms);
        result.bonds = new ArrayList<>(this.bonds);
        result.dbName = this.dbName;
        result.iupacName = this.iupacName;
        result.id = this.id;

        result.observeNucleus = "1H";
        result.solvent = "";
        result.peaks = new ArrayList<>();

        return result;
    }

    private List<Long> peaksToCondensedBitSet() {
        try {
            return Arrays.stream(peaks.stream()
                                      .map(peak -> peak.xValue)
                                      .collect(() -> new BitSet(1 << 12),
                                               (s, f) -> s.set((int) (f * 10)),
                                               BitSet::or)
                                      .toLongArray())
                         .boxed()
                         .collect(Collectors.toList());
        } catch (IndexOutOfBoundsException e) {
            System.err.println("id: "+id);
            return new ArrayList<>();
        }

    }

    private List<Long> fingerprintsToCondensedBitSet() {
        return Arrays.stream(fingerprint.toLongArray())
                     .boxed()
                     .collect(Collectors.toList());
    }

}
