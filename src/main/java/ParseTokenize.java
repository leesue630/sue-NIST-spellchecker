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
    private static final String SAMPLE_DIRECTORY = "C:\\Users\\lns16\\Documents\\BODSample";
    private XPathFactory xPathFactory;
    private XPath xPath;
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    private int errorFileCount = 0;
    private static final List<String> EXTENDED_STOP_WORDS = Arrays.asList(
            "among", "could", "during", "etc", "from", "others", "since", "than", "them", "unapproved", "upon", "versa", "we", "what", "when", "where", "which", "without", "would", "you",
            // these are not in the WordNet database
            "reenter", "reseller"
    );
    private static final List<String> BOD_ACRONYMS = Arrays.asList(
            "BOD", "WIP", "IST", "MRP", "ERP", "BOM", "RFQ", "ERP", "HRMS", "CSM", "PDM", "OAGIS", "BSR", "PDC", "CMMS", "GL", "OA"
    );

    public ParseTokenize() {
        this.xPathFactory = XPathFactory.newInstance();
        this.xPath = xPathFactory.newXPath();
    }

    public Document parseFile(File file) throws ParserConfigurationException, IOException, SAXException {
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

    // returns hash map of type names and documentation
    private HashMap<String, String> getDescriptions(Document doc, XPath xPath) {
        List<String> list = new ArrayList<>();

        HashMap<String, String> dictionary = null;
        dictionary = new HashMap<>();
        NodeList nodeList1 = doc.getElementsByTagName("xsd:documentation");

        for (int i = 0; i < nodeList1.getLength() && nodeList1.getLength() != 0; i++) {
            String typeName = null;
            try {
                typeName = nodeList1.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("type").getNodeValue();
            } catch (NullPointerException e) {
                System.err.print("ERROR: no type attribute for ");
                typeName = nodeList1.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            } finally {
                String description = nodeList1.item(i).getFirstChild().getNodeValue();
                dictionary.put(typeName, description);
            }

        }
        return dictionary;
    }

    // tokenize descriptions: split by spaces, remove stop words, normalize camel case
    private HashMap<String, List<String>> tokenize(HashMap<String, String> dictionary) {
        HashMap<String, List<String>> tokenizedDictionary = new HashMap<>();
        List<String> tokens;
        for (Map.Entry<String, String> entry : dictionary.entrySet()) {
            tokens = new LinkedList<>(Arrays.asList(entry.getValue().split(" |\\n|(\\b/\\b)|(\\b-\\b)")));
            for (int i = 0; i < tokens.size(); i++) {
                // need to replace ".,()/·:'-" and "'s"
                tokens.set(i, tokens.get(i).replaceAll("'s$", "").replaceAll("^\\W|\\W*$", ""));
            }
            tokenizedDictionary.put(entry.getKey(), removeStopWords(normalize(tokens)));
        }
        return tokenizedDictionary;
    }

    // removes stop words and white space
    private List<String> removeStopWords(List<String> tokens) {
        List<String> revisedTokens = new ArrayList<>();
        for (String term : tokens) {
            if (!isSpecialWord(term) && !term.equals("")) {
                revisedTokens.add(term);
            }
        }
        return revisedTokens;
    }

    // checks if term is a stop word or BOD Acronym
    private boolean isSpecialWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase()) || BOD_ACRONYMS.contains(term)) {
            return true;
        }
        return false;
    }

    // normalizes tokens for camel case
    private List<String> normalize(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String term = tokens.get(i);
            if (!term.equals(term.toLowerCase())) {
                String[] termArray = StringUtils.splitByCharacterTypeCamelCase(term);
                if (termArray.length != 1) {
                    tokens.remove(i);
                    for (int j = 0; j < termArray.length; j++) {
                        tokens.add(i + j, termArray[j]);
                    }
                    i += (termArray.length - 1);
                }
            }
        }
        return tokens;
    }

    // creates hashmap of all spelling mistakes with type names
    // if type has no typos, key is not added to hash map
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

            if (mispelled.size() != 0) {
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

    // writes spelling mistakes for a single file
    // writes nothing if file has no typos
    private void printSpellingMistakes(File file, ParseTokenize parser) {
        try {
            Document document = parser.parseFile(file);
            HashMap<String, String> dictionary = parser.getDescriptions(document, parser.xPath);
            HashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
            HashMap<String, List<String>> spellingMistakes = parser.spellCheck(tokenizedDictionary);

            if (spellingMistakes.size() != 0) {
                errorFileCount++;
                bw.write(errorFileCount + ".");
                bw.newLine();
                bw.write( file.getName() + " Spelling Mistakes");
                bw.newLine();
                // write Type Name and typos onto log file
                for (Map.Entry<String, List<String>> entry : spellingMistakes.entrySet()) {
                    bw.write("Type Name: _" + entry.getKey() + "_");
                    bw.newLine();
                    for (String mispell : entry.getValue()) {
                        bw.write("- " + mispell);
                        bw.newLine();
                    }
                    bw.newLine();
                }

                // dashes indicate new file
                bw.write("--------------------------");
                bw.newLine();
            }

            System.out.println("FILE: " + file.getName() + " DONE");
        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.err.print(e);
        }
    }

    // writes onto log file
    private void loadDirectory(String directoryName, String directory, ParseTokenize parser) {
        try {
            // write Directory name
            bw.write("Directory: " + directoryName);
            bw.newLine();
            bw.newLine();

            // iterate through files in directory
            for (File file : new File(directory).listFiles()) {
                // write file name
                parser.printSpellingMistakes(file, parser);
            }
            System.out.println("DIRECTORY: " + directoryName + " DONE");
            bw.write(errorFileCount + " files with spelling mistakes.");
            bw.newLine();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void writeLogfile(String logFileName, String directoryName, String directory, ParseTokenize parser) throws IOException {
        try {
            parser.fw = new FileWriter(logFileName);
            parser.bw = new BufferedWriter(parser.fw);
            // Test single file
//        parser.printSpellingMistakes(new File("C:\\Users\\lns16\\Documents\\BODTest\\AcknowledgeBOM.xsd"), parser);
            parser.loadDirectory(directoryName, directory, parser);

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

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ParseTokenize parser = new ParseTokenize();

        parser.writeLogfile("./acknowledge_logfile.txt", "Acknowledge Schemas", SAMPLE_DIRECTORY, parser);
//        parser.writeLogfile("./ModelBODs_logfile.txt", "Model/BODs", DIRECTORY, parser);
//        parser.writeLogfile("./ModelPlatform_logfile.txt", "Model/Platform/2_3/BODs", PLATFORM_DIRECTORY, parser);
    }
}
