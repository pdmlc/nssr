package pdmlc.nssr.process;

import java.io.*;
import java.nio.file.Files;

public class Filter {

    public static void main(String[] args) throws IOException {

        Filter filter = new Filter();
        filter.filterBySpectrum("all_records_", "13C");
        filter.filterBySpectrum("all_records_", "1H");
        filter.filterBySpectrum("all_records_", "17O");
        filter.filterBySpectrum("all_records_", "31P");

    }

    boolean filterSpectrum(String object, String spectrum) {
        return object.contains("\"spectrum\":\""+spectrum+"\"");
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

}
