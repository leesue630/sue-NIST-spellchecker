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

    private static final String DIRECTORY_PREFIX = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\";
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    private int mispelledFileCount = 0;
    private String currentFileName = "";
    private static final List<String> EXTENDED_STOP_WORDS = Arrays.asList(
            "against", "although", "among", "amongst", "because", "cannot", "could", "during", "etc", "from", "his", "how", "multi", "others", "per", "pre", "should", "since", "than", "them", "those", "toward", "unapproved", "unless", "until", "upon", "versa", "we", "what", "when", "where", "whether", "which", "whom", "whose", "without", "would", "you", "your",
            // these are not in the WordNet database
            "customization", "reenter", "reseller", "geocoded", "geocoding", "proactively", "subline", "via", "asynchronously", "inline", "itself", "something", "meta", "designator",
            // unsure about these words, but they appear often
            "sublot", "nonconformant", "datetime", "timeperiod", "timestamp", "teardown", "predefined", "datetimes"
    );
    private static final List<String> BOD_ACRONYMS = Arrays.asList(
            "BOD", "WIP", "IST", "MRP", "ERP", "BOM", "RFQ", "ERP", "HRMS", "CSM", "PDM", "OAGIS", "BSR", "PDC", "CMMS", "GL", "OA", "UOM", "XML", "CNC",
            "RFID", "EDI", "WIPSTATUS", "CHK", "AP", "FDIS", "LTL", "LIMS", "ORIGEF", "PN", "xsd",
            "BO" // because BODs becomes BO and Ds when normalized for camel case
    );
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
            SpellCheckerConfig.class
    );
    SpellChecker spellChecker = applicationContext.getBean(SpellChecker.class);

    // create document from file
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

    // TODO: fix
    private HashMap<String, String> getCCTSDescriptions(Document doc){
        HashMap<String, String> dictionary = new HashMap<>();
        NodeList nodeList1 = doc.getElementsByTagName("xsd:documentation");

        for (int i = 0; i < nodeList1.getLength() && nodeList1.getLength() != 0; i++) {
            String CCTS_Name;
            String CCTS_Description;

            try {
                CCTS_Name = nodeList1.item(i).getChildNodes().item(1).getFirstChild().getNodeValue();
                CCTS_Description = nodeList1.item(i).getChildNodes().item(3).getFirstChild().getNodeValue();
                dictionary.put("Name: _" + CCTS_Name + "_", CCTS_Description);
            } catch (NullPointerException typeErr) {
                System.err.println("ERROR: empty documentation tag: #" + i);
            }
        }
        return dictionary;
    }

    // TODO: fix
    private HashMap<String, String> getCCTSDataTypeDescriptions(Document doc){
        HashMap<String, String> dictionary = new HashMap<>();
        NodeList nodeList1 = doc.getElementsByTagName("ccts_Definition");

        for (int i = 0; i < nodeList1.getLength() && nodeList1.getLength() != 0; i++) {
            String CCTS_Name = "unknown name";
            String CCTS_Definition = nodeList1.item(i).getFirstChild().getNodeValue();

            try {
                CCTS_Name = nodeList1.item(i).getParentNode().getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                String nodeType = nodeList1.item(i).getParentNode().getParentNode().getParentNode().getNodeName();
            } catch (NullPointerException firstErr) {
                System.err.println("ERROR: broad name is not at third parent " + i);
                try {
                    CCTS_Name = nodeList1.item(i).getParentNode().getParentNode().getParentNode().getParentNode().getParentNode().getParentNode().getAttributes().getNamedItem("name").getNodeValue();
                } catch (NullPointerException secondErr) {
                    System.err.println("ERROR: broad name is not at sixth parent " + i);
                    System.err.println("ERROR: Failed to find name " + i);
                }
            } finally {
                dictionary.put("Name: _" + CCTS_Name + "_", CCTS_Definition);
            }
        }
        return dictionary;
    }

    // creates a hash map of key(element types/names/refs) with documentation
    private HashMap<String, String> getDescriptions(Document doc){
        HashMap<String, String> hashMap = new HashMap<>();

        // nodeList: list of all documentation nodes
        NodeList nodeList = doc.getElementsByTagName("xsd:documentation");

        // iterate through all documentation nodes
        // attempt to find correct parent node with appropriate type, name, or ref attribute
        for (int i = 0; i < nodeList.getLength() && nodeList.getLength() != 0; i++) {
            Node parentNode;
            String typeNameRef = null;

            try {
                parentNode = nodeList.item(i).getParentNode().getParentNode();
                typeNameRef = getTypeNameRef(parentNode);
                if (typeNameRef == "Unknown"){ // try going to fourth ancestor to find type, name or ref
                    System.out.println("Parent not at second level");
                    parentNode = parentNode.getParentNode().getParentNode();
                    typeNameRef = getTypeNameRef(parentNode);
                    if (typeNameRef == "Unknown"){
                        System.out.println("Parent not at fourth level");
                    }
                }
            } catch (NullPointerException typeErr) {
                System.err.println("Error while finding parent");
            } finally {
                // check to prevent errors when dealing with empty documentation tags
                if (nodeList.item(i).hasChildNodes() != false) {
                    // find documentation and input entry into hashMap
                    String description = nodeList.item(i).getFirstChild().getNodeValue();
                    hashMap.put(typeNameRef, description);
                } else {
                    // print if documentation tag is empty
                    System.err.println("ERROR: empty documentation tag: " + currentFileName);
                }
            }
        }
        return hashMap;
    }

    // Type, Name, or Ref Value formatted
    private String getTypeNameRef(Node node){
        String typeNameRef = "Unknown";
        Node attribute = node.getAttributes().getNamedItem("type");
        if (attribute != null){
            typeNameRef = "Type: _" + attribute.getNodeValue() + "_";
        } else {
            attribute = node.getAttributes().getNamedItem("name");
            if (attribute != null){
                typeNameRef = "Name: _" + attribute.getNodeValue() + "_";
            } else {
                attribute = node.getAttributes().getNamedItem("ref");
                if (attribute != null) {
                    typeNameRef = "Ref : _" + attribute.getNodeValue() + "_";
                }
            }
        }
        return typeNameRef;
    }

    // tokenize descriptions: split by spaces, remove stop words, normalize camel case
    private HashMap<String, List<String>> tokenize(HashMap<String, String> hashMap) {
        HashMap<String, List<String>> tokenizedHashMap = new HashMap<>();
        List<String> tokens;

        // iterate through hash map and tokenize all descriptions
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            // tokenize using spaces, whitespace, backslash, dash, and underscore
            // \\b: non-word boundary TODO: acts weird here
            // \\B: word boundary
            tokens = new LinkedList<>(Arrays.asList(entry.getValue().split(" |\\n|(\\b/\\b)|(\\b-\\b)|(\\B_\\B)")));

            // cut off non-word characters at the beginning and end of tokens
            // cut off 's and (s) at the end of words
            for (int i = 0; i < tokens.size(); i++) {
                // need to replace ".,()/·:'-" and "'s"
                tokens.set(i, tokens.get(i).replaceAll("^\\W|\\W*$", "").replaceAll("('s$)|(\\(s$)", ""));
            }

            // normalize tokens for camel case and remove special words
            tokens = normalize(tokens);
            tokens = removeSpecialWords(tokens);

            // insert keys and token lists into tokenized hash map
            tokenizedHashMap.put(entry.getKey(), tokens);
        }
        return tokenizedHashMap;
    }

    // normalizes tokens for camel case
    private List<String> normalize(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String term = tokens.get(i);

            // checks if term is fully lowercase
            if (!term.equals(term.toLowerCase())) {
                String[] termArray = StringUtils.splitByCharacterTypeCamelCase(term);

                // checks if term was camel case
                if (termArray.length != 1) {
                    // remove original token
                    tokens.remove(i);

                    // insert new separated tokens
                    for (int j = 0; j < termArray.length; j++) {
                        tokens.add(i + j, termArray[j]);
                    }
                    i += (termArray.length - 1);
                }
            }
        }
        return tokens;
    }

    // removes special words, white space, and numbers
    private List<String> removeSpecialWords(List<String> tokens) {
        List<String> revisedTokens = new ArrayList<>();

        // iterate through tokens
        for (String term : tokens) {

            // if term is a number
            if (!isNumeric(term.replaceAll("\\.", ""))) {

                // if term is not empty, a tab space, a period, or a colon, add term to revised token list
                if (!isSpecialWord(term) && !term.equals("") && !term.equals("\t") && !term.equals(".") && !term.equals(":")) {
                    revisedTokens.add(term);
                }
            }
        }
        return revisedTokens;
    }

    // return true if string is numeric
    public static boolean isNumeric(String str)
    {
        try {
            double d = Double.parseDouble(str);
        } catch(NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // checks if term is a stop word or BOD Acronym
    private boolean isSpecialWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term.toLowerCase()) || EXTENDED_STOP_WORDS.contains(term.toLowerCase()) || BOD_ACRONYMS.contains(term)) {
            return true;
        }
        return false;
    }

    // creates hash map of all spelling mistakes with type names
    // if type has no typos, key is not added to hash map
    private HashMap<String, List<String>> spellCheck(HashMap<String, List<String>> tokenizedHashMap) {
        HashMap<String, List<String>> mispelledHashMap = new HashMap<>();
        List<String> mispelledTerms = new ArrayList<>();;

        // iterate through entries in tokenized hash map
        for (Map.Entry<String, List<String>> entry : tokenizedHashMap.entrySet()) {

            // iterate through tokenized terms and add terms which are spelled correctly
            for (String term : entry.getValue()) {
                if (!spellCheckWord(term)) {
                    mispelledTerms.add(term);
                }
            }

            // put new entry into hash map if mispelled terms exist
            if (mispelledTerms.size() != 0) {
                mispelledHashMap.put(entry.getKey(), mispelledTerms);
            }
        }
        return mispelledHashMap;
    }

    // checks spelling of single term using WordNet database
    private boolean spellCheckWord(String term) throws SpellCheckException {
        try {
            spellChecker.check(term.toLowerCase());
            return true;
        } catch (SpellCheckException e) {
            return false;
        }
    }

    // writes spelling mistakes for a single file
    // writes nothing if file has no typos
    private void printSpellingMistakes(File file, ParseTokenize parser, String directoryName) {
        try {
            Document document = parser.parseFile(file);
            HashMap<String, String> dictionary = parser.getDescriptions(document);

            if (dictionary.size() != 0){ // check for empty document
                HashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
                HashMap<String, List<String>> spellingMistakes = parser.spellCheck(tokenizedDictionary);
                if (spellingMistakes.size() != 0) {
                    mispelledFileCount++;
                    bw.write(mispelledFileCount + ".");
                    bw.newLine();
                    bw.write(directoryName + "/" + file.getName() + " Spelling Mistakes");
                    bw.newLine();
                    // write Type Name and typos onto log file
                    for (Map.Entry<String, List<String>> entry : spellingMistakes.entrySet()) {
                        bw.write(entry.getKey());
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
            } else {
                System.err.println("ERROR: empty document: " + currentFileName);
            }

            System.out.println("FILE: " + file.getName() + " DONE");
        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.err.print(e + "; Invalid file path");
        }
    }

    // writes onto log file
    private void loadDirectory(String directoryName, String directory, ParseTokenize parser) {
        try {
            // write Directory name
            mispelledFileCount = 0;
            bw.write("Directory: " + directoryName);
            bw.newLine();
            bw.newLine();

            // iterate through files in directory
            for (File file : new File(directory).listFiles()) {
                // write file name
                currentFileName = file.getName();
                if (!currentFileName.contains("IST")) {

                    parser.printSpellingMistakes(file, parser, directoryName);
                } else {
                    System.out.println("Skipped IST file: " + currentFileName);
                }
            }
            System.out.println("DIRECTORY: " + directoryName + " DONE");
            bw.write(mispelledFileCount + " files with spelling mistakes.");
            bw.newLine();
        } catch (IOException e) {
            System.err.println(e + "; Invalid Directory Path");
        }
    }

    private void writeLogfile(String prefix, ParseTokenize parser) throws IOException {
        String directory = DIRECTORY_PREFIX + prefix;
        String directoryName = "Model/BODs";
        String logfileName = "./SpellingMistakes/" + prefix.toLowerCase() + "_logfile.txt";
        parser.writeLogfile(directory, directoryName, logfileName, parser);
    }

    private void writeLogfile(String directory, String directoryName, String logfileName, ParseTokenize parser) throws IOException {

        try {
            parser.fw = new FileWriter(logfileName);
            parser.bw = new BufferedWriter(parser.fw);
            // Test single file
//            parser.printSpellingMistakes(new File(DIRECTORY + "BODs\\CancelAcknowledgeCostingActivity.xsd"), parser, "CancelAcknowledgeCostingActivity.xsd");
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
        parser.writeLogfile("Acknowledge", parser);
        parser.writeLogfile("Cancel", parser);
        parser.writeLogfile("Change", parser);
        parser.writeLogfile("Get", parser);
        parser.writeLogfile("Load", parser);
        parser.writeLogfile("Notify", parser);
        parser.writeLogfile("Post", parser);
        parser.writeLogfile("Process", parser);
        parser.writeLogfile("Show", parser);
        parser.writeLogfile("Sync", parser);

//        parser.writeLogfile(DIRECTORY + "\\BODs", "Model/BODs", "./SpellingMistakes/test_logfile.txt", parser);
        //        parser.writeLogfile(DIRECTORY + "Nouns", "Model/Nouns", "./SpellingMistakes/Model_Nouns_logfile.txt", parser);
//        parser.writeLogfile(DIRECTORY + "Platform\\2_3\\Common\\CodeLists", "Model/Platform/2_3/Common/CodeLists", "./SpellingMistakes/Model_Platform_Common_CodeLists_logfile.txt", parser);
//        parser.writeLogfile(DIRECTORY + "Platform\\2_3\\Extension", "Model/Platform/2_3/Extension", "./SpellingMistakes/Model_Platform_Extension_logfile.txt", parser);
    }
}
