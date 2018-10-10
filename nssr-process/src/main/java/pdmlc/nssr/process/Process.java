package pdmlc.nssr.process;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;

public class Process {

    public static void main(String[] args) throws IOException {

        process();

    }

    static void process() throws IOException {

        try {

            DBProcessor dbProcessor = new DBProcessor();

            dbProcessor.openStreams();

            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader xmlReader = saxParser.getXMLReader();
            xmlReader.setContentHandler(new DBHandler(dbProcessor));
            xmlReader.parse(new InputSource(dbProcessor.getNmrshiftdb()));

            dbProcessor.flushStreams();
            dbProcessor.closeStreams();

        } catch (ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

    }


}
