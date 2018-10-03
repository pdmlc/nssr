package pdmlc.nssr.process;

import org.openscience.cdk.interfaces.IAtomContainer;

import java.util.BitSet;
import java.util.List;

class NMRShiftDBEntry {

    private IAtomContainer      atomContainer;
    private String              iupacName;
    private String              smile;
    private String              observeNucleus;
    private String              solvent;
    private List<Peak>          peaks;
    private long[]              hashes;
    private BitSet              fingerprint;

    class Peak {

        private float       xValue;
        private char        multiplicity;
        private String      shape;
        private String      id;
        private String[]    atomRefs;

        Peak(float xValue, char multiplicity,
             String shape, String id, String[] atomRefs) {
            this.xValue         = xValue;
            this.multiplicity   = multiplicity;
            this.shape          = shape;
            this.id             = id;
            this.atomRefs       = atomRefs;
        }

    }

}
