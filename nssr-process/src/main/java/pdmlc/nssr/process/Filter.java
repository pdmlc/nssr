package pdmlc.nssr.process;

import java.io.*;
import java.nio.file.Files;

public class Filter {

    public static void main(String[] args) throws IOException {

        String record = "all_records_";

        Filter filter = new Filter();

        filter.filterBySpectrum(record, "13C");
        filter.filterBySpectrum(record, "1H");
        filter.filterBySpectrum(record, "17O");
        filter.filterBySpectrum(record, "31P");

        filter.filterBySolvent(record, "Chloroform-D1 (CDCl3)");

    }

    boolean filterSpectrum(String object, String spectrum) {
        return object.contains("\"spectrum\":\""+spectrum+"\"");
    }

    boolean filterSolvent(String object, String solvent) {
        return object.contains("\"solvent\":\""+solvent+"\"");
    }

    void filterBySpectrum(String in, String spectrum) throws IOException {

        String out = in + "filtered_" + spectrum;

        File ifile = new File("records/"+in+".json");
        File ofile = new File("records/filtered/"+out+".json");

        BufferedWriter o = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(ofile.toPath())));
        BufferedReader i = new BufferedReader(new InputStreamReader(Files.newInputStream(ifile.toPath())));
        i.lines()
         .filter(obj -> filterSpectrum(obj, spectrum))
         .forEach(str -> {
             try {
                 o.write(str+'\n');
             } catch (IOException e) {
                 e.printStackTrace();
             }
         });

    }

    void filterBySolvent(String in, String solvent) throws IOException {

        String out = in + "filtered_" + solvent;

        File ifile = new File("records/"+in+".json");
        File oFile = new File("records/filtered/"+out+".json");

        BufferedWriter o = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(oFile.toPath())));
        BufferedReader i = new BufferedReader(new InputStreamReader(Files.newInputStream(ifile.toPath())));
        i.lines()
         .filter(obj -> filterSolvent(obj, solvent))
         .forEach(str -> {
             try {
                 o.write(str+'\n');
             } catch (IOException e) {
                 e.printStackTrace();
             }
         });

    }

}
