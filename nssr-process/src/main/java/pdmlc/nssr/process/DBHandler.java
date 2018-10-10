package pdmlc.nssr.process;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.vecmath.Point2d;
import java.util.Stack;
import java.util.logging.Logger;

class DBHandler extends DefaultHandler {

    private static final Logger logger = Logger.global;

    private final DBProcessor dbProcessor;
    private ProcessQueue queue;
    private Stack<String> stack = new Stack<>();

    private Entry activeEntry;
    private int entryID = 0;
    private boolean negShift = false;


    DBHandler(DBProcessor dbProcessor) {
        super();
        this.dbProcessor = dbProcessor;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        queue = new ProcessQueue(dbProcessor);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        queue.processRemaining();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (localName) {

            case "atomArray":   stack.push(localName);                              break;
            case "bondArray":   stack.push(localName);                              break;
            case "peakList":    stack.push(localName);                              break;
            case "molecule":    stack.push(localName);  startMolecule(attributes);  break;
            case "spectrum":    stack.push(localName);  startSpectrum(attributes);  break;
            case "atom":        stack.push(localName);  addAtom(attributes);        break;
            case "bond":        stack.push(localName);  addBond(attributes);        break;
            case "peak":        stack.push(localName);  addPeak(attributes);        break;
            case "metadata":    stack.push(localName);  metadata(attributes);       break;
            case "substance":   stack.push(localName);  substance(attributes);      break;
            default:                                                                break;

        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String p = localName;
        switch (localName) {

            case "atomArray":   p = stack.pop();                    break;
            case "bondArray":   p = stack.pop();                    break;
            case "peakList":    p = stack.pop();                    break;
            case "molecule":    p = stack.pop();    endMolecule();  break;
            case "spectrum":    p = stack.pop();    endSpectrum();  break;
            case "atom":        p = stack.pop();                    break;
            case "bond":        p = stack.pop();                    break;
            case "peak":        p = stack.pop();                    break;
            case "metadata":    p = stack.pop();                    break;
            case "substance":   p = stack.pop();                    break;
            default:                                                break;

        }

        if (!p.equals(localName)) {
            throw new SAXException();
        }
    }

    private void startMolecule(Attributes attributes) {
        activeEntry = new Entry(negShift ? entryID : ++entryID);
        negShift = false;
        for (int i = 0; i < attributes.getLength(); i++) {
            if (attributes.getLocalName(i).equals("title")) {
                activeEntry.dbName = attributes.getValue(i);
            }
        }
    }

    private void endMolecule() {

    }

    private void startSpectrum(Attributes attributes) {
        if (!activeEntry.peaks.isEmpty()) {
            activeEntry = activeEntry.wipeSpectrum();
            activeEntry.id = negShift ? entryID : ++entryID;
            negShift = false;
        }
    }

    private void endSpectrum() {
        if (!negShift) {
            queue.push(activeEntry);
        }
    }

    private void addAtom(Attributes attributes) {
        if (attributes == null || activeEntry == null) return;
        String id = attributes.getValue("id");
        String symbol = attributes.getValue("isotopeNumber") + attributes.getValue("elementType");
        int formalCharge = Integer.valueOf(attributes.getValue("formalCharge"));
        double x2 = Double.parseDouble(attributes.getValue("x2"));
        double y2 = Double.parseDouble(attributes.getValue("y2"));
        IAtom atom = new Atom(symbol, new Point2d(x2, y2));
        atom.setID(id);
        atom.setFormalCharge(formalCharge);
        activeEntry.atoms.add(atom);
    }

    private void addBond(Attributes attributes) {
        if (attributes == null || activeEntry == null) return;
        String id = attributes.getValue("id");
        String[] refs = attributes.getValue("atomRefs2").split("\\s");
        int ref1 = Integer.parseInt(refs[0].substring(1)) - 1;
        int ref2 = Integer.parseInt(refs[1].substring(1)) - 1;
        IAtom atom1 = activeEntry.atoms.get(ref1);
        IAtom atom2 = activeEntry.atoms.get(ref2);
        IBond.Order order;
        switch (attributes.getValue("order")) {
            case "S": order = IBond.Order.SINGLE; break;
            case "D": order = IBond.Order.DOUBLE; break;
            case "T": order = IBond.Order.TRIPLE; break;
            default: order  = IBond.Order.UNSET;  break;
        }
        IBond bond = new Bond(atom1, atom2, order);
        bond.setID(id);
        activeEntry.bonds.add(bond);
    }

    private void metadata(Attributes attributes) {
        if (attributes == null || activeEntry == null) return;
        if (attributes.getValue("name").equals("nmr:OBSERVENUCLEUS")) {
            activeEntry.observeNucleus = attributes.getValue("content");
        }
    }

    private void substance(Attributes attributes) {
        if (attributes == null || activeEntry == null) return;
        if (attributes.getValue("role").equals("subst:solvent")) {
            activeEntry.solvent = attributes.getValue("title");
        }
    }

    private void addPeak(Attributes attributes) {
        if (attributes == null || activeEntry == null) return;
        float xValue = Float.parseFloat(attributes.getValue("xValue"));
        negShift |= xValue < 0;
        String multiplicity = attributes.getValue("peakMultiplicity");
        String shape = attributes.getValue("peakShape");
        String id = attributes.getValue("id");
        String refStr = attributes.getValue("atomRefs");
        String[] atomRefs = refStr == null ? new String[]{""} : refStr.split("\\s");
        Entry.Peak peak = new Entry.Peak(xValue, multiplicity, shape, id, atomRefs);
        activeEntry.peaks.add(peak);
    }


}
