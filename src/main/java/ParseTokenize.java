/**
 * Created by lns16 on 6/29/2017.
 */

import gov.nist.surf2017.sue.config.SpellCheckerConfig;
import gov.nist.surf2017.sue.spellchecker.SpellCheckException;
import gov.nist.surf2017.sue.spellchecker.SpellChecker;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;

import java.util.*;

public class ParseTokenize {

    private static final String DIRECTORY = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\BODs";
    private static final String PLATFORM_DIRECTORY = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\Platform\\2_3\\BODs";
    private static final String TEST_DIRECTORY = "C:\\Users\\lns16\\Documents\\BODTest";
    private static final String LOGFILE = "./logfile.txt";
    private XPathFactory xPathFactory;
    private XPath xPath;
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    private static final List<String> EXTENDED_STOP_WORDS = Arrays.asList(
            "where", "from", "could", "which"
    );
    public ParseTokenize() {
        this.xPathFactory = XPathFactory.newInstance();
        this.xPath = xPathFactory.newXPath();
    }

    public Document parse(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        Document doc = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            return doc;
        }
    }

    private HashMap<String, String> getDescriptions(Document doc, XPath xPath) {
        List<String> list = new ArrayList<>();
//        String xPathStatement = "//first_name";

        HashMap<String, String> dictionary = null;
        dictionary = new HashMap<>();
        NodeList nodeList1 = doc.getElementsByTagName("xsd:documentation");

        for (int i = 0; i < nodeList1.getLength() && nodeList1.getLength() != 0; i++) {
            String typeName = nodeList1.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("type").getNodeValue();
            String description = nodeList1.item(i).getFirstChild().getNodeValue();
            dictionary.put(typeName, description);
        }
        return dictionary;
    }

    // tokenize descriptions: split by spaces, remove stop words, normalize camel case
    private HashMap<String, List<String>> tokenize(HashMap<String, String> dictionary) {
        HashMap<String, List<String>> tokenizedDictionary = new HashMap<>();
        String[] tokens;
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            tokens = entry.getValue().split(" |\\n");
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].replace(".", "").replace(",", "").replaceAll("[()]", "").replaceAll("\n", "");
            }
            tokenizedDictionary.put(entry.getKey(), normalize(removeStopWords(tokens)));
        }
        return tokenizedDictionary;
    }

    // removes stop words and white space
    private List<String> removeStopWords(String[] tokens) {
        List<String> revisedTokens = new ArrayList<>();
        for (String term : tokens) {
            if (!isStopWord(term) && !term.equals("")) {
                revisedTokens.add(term);
            }
        }
        return revisedTokens;
    }

    // checks if term is a stop word
    private boolean isStopWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase())) {
            return true;
        }
        return false;
    }

    // normalizes tokens for camel case
    private List<String> normalize(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String term = tokens.get(i);
            if (!term.equals(term.toLowerCase())) {
                List<String> termArray = new LinkedList<>(Arrays.asList(StringUtils.splitByCharacterTypeCamelCase(term)));
                if (termArray.size() != 1) {
                    tokens.remove(i);

                    // removes Of because it is a stop word
                    if (termArray.contains("Of")) {
                        termArray.remove(termArray.indexOf("Of"));
                    }
                    for (int j = 0; j < termArray.size(); j++) {
                        tokens.add(i + j, termArray.get(j));
                    }
                    i += (termArray.size() - 1);
                }
            }
        }
        return tokens;
    }

    // creates hashmap of all spelling mistakes with type names
    private HashMap<String, List<String>> spellCheck(HashMap<String, List<String>> dictionary) {
        HashMap<String, List<String>> mistakeTypes = new HashMap<String, List<String>>();
        List<String> mispelled;

        for (Map.Entry<String, List<String>> entry : dictionary.entrySet()) {
            mispelled = new ArrayList<>();

            for (String term : entry.getValue()) {
                if (!spellCheck(term)) {
                    mispelled.add(term);
                }
            }

            if (!(mispelled.size() == 0)) {
                mistakeTypes.put(entry.getKey(), mispelled);
            }
        }

        return mistakeTypes;
    }

    // checks the spelling of terms
    private boolean spellCheck(String term) throws SpellCheckException {
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SpellCheckerConfig.class
        );
        SpellChecker spellChecker = applicationContext.getBean(SpellChecker.class);
        try {
            spellChecker.check(term.toLowerCase());
            return true;
        } catch (SpellCheckException e) {
            return false;
        }
    }

    private void printSpellingMistakes(File file, ParseTokenize parser){
        try {
            bw.write(file.getName());
            bw.newLine();

            Document document = parser.parse(file);
            HashMap<String, String> dictionary = parser.getDescriptions(document, parser.xPath);
            HashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
            HashMap<String, List<String>> spellingMistakes = parser.spellCheck(tokenizedDictionary);

            // write Type Name and typos onto log file
            for (Map.Entry<String, List<String>> entry : spellingMistakes.entrySet()) {
                bw.write("Type Name: " + entry.getKey());
                bw.newLine();
                for (String mispell : entry.getValue()) {
                    bw.write(mispell);
                    bw.newLine();
                }
                bw.newLine();
            }

            // dashes indicate new file
            bw.write("--------------------------");
            bw.newLine();
        } catch (IOException | SAXException | ParserConfigurationException e){
            System.err.println(e);
        }
    }

    // writes onto log file
    private void loadDirectory(String directoryName, String directory, ParseTokenize parser) {
        try {
            // write Directory name
            bw.write("Directory: " + directoryName);
            bw.newLine();

            // iterate through files in directory
            for (File file : new File(directory).listFiles()) {
                // write file name
                parser.printSpellingMistakes(file, parser);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ParseTokenize parser = new ParseTokenize();
        try {
            parser.fw = new FileWriter(LOGFILE);
            parser.bw = new BufferedWriter(parser.fw);
            parser.printSpellingMistakes(new File("C:\\Users\\lns16\\Documents\\BODTest\\AcknowledgeBOM.xsd"), parser);
//            parser.loadDirectory("Test Directory ", TEST_DIRECTORY, parser);
//            parser.loadDirectory("Model/BODs", DIRECTORY, parser);
//            parser.loadDirectory("Model/Platform/2_3/BODs", PLATFORM_DIRECTORY, parser);
        } finally {

            try {

                if (parser.bw != null)
                    parser.bw.close();

                if (parser.fw != null)
                    parser.fw.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
