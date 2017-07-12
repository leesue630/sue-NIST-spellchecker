/**
 * Created by lns16 on 6/29/2017.
 */

import gov.nist.surf2017.sue.Lists.WordLists;
import gov.nist.surf2017.sue.config.SpellCheckerConfig;
import gov.nist.surf2017.sue.spellchecker.SpellCheckException;
import gov.nist.surf2017.sue.spellchecker.SpellChecker;
import net.sf.extjwnl.data.Word;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.apache.commons.lang.StringUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTokenize {

    private static final String DIRECTORY_PREFIX = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\";
    private BufferedWriter bw = null;
    private FileWriter fw = null;
    private int mispelledFileCount = 0;
    private String currentFileName = "";
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

    // gets CCTS Description/Definition for single documentation node
    private Map.Entry<String, String> getCCTSData(Node node) {

        String CCTS_Name = "Unknown";
        // tag <ccts_Definition> or <ccts_Description>
        String CCTS_Definition = "Unknown";
        Node secondChild;
        Node fourthChild;

        try {
            secondChild = node.getChildNodes().item(1);
            if (secondChild.getNodeName() == "ccts_Name") {
                CCTS_Name = secondChild.getFirstChild().getNodeValue();
                fourthChild = node.getChildNodes().item(3);
                if (fourthChild.getNodeName() == "ccts_Definition" || fourthChild.getNodeName() == "ccts_Description") {
                    CCTS_Definition = fourthChild.getFirstChild().getNodeValue();
                }
            } else if (secondChild.getNodeName() == "ccts_Definition" || secondChild.getNodeName() == "ccts_Description") {
                CCTS_Definition = secondChild.getFirstChild().getNodeValue();
            }
        } catch (NullPointerException typeErr) {
            System.err.println("ERROR: while finding CCTS Name/Definition/Description");
        }
        return new AbstractMap.SimpleEntry<String, String>("CCTS Name: _" + CCTS_Name + "_", CCTS_Definition);
    }

    // for CodeList_CurrencyCode_ISO_7_04.xsd
    private Map.Entry<String, String> getCodeName(Node node) {

        String value = "Unknown";
        String codeName = "Unknown";

        try {
            value = node.getParentNode().getParentNode().getAttributes().getNamedItem("value").getNodeValue();
            codeName = node.getChildNodes().item(1).getFirstChild().getNodeValue();
        } catch (NullPointerException typeErr) {
            System.err.println("ERROR: no CodeName");
        }

        return new AbstractMap.SimpleEntry<String, String>("Value: _" + value + "_", codeName);
    }

    // creates a hash map of key(element types/names/refs) with documentation
    private LinkedHashMap<String, String> getDescriptions(Document doc) {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>("Unknown", "Unknown");

        // nodeList: list of all documentation nodes
        NodeList nodeList = doc.getElementsByTagName("xsd:documentation");

        // iterate through all documentation nodes
        // attempt to find correct parent node with appropriate type, name, or ref attribute
        for (int i = 0; i < nodeList.getLength() && nodeList.getLength() != 0; i++) {
            Node currentNode = nodeList.item(i);
            int childCount = currentNode.getChildNodes().getLength();
            switch (childCount) {
                case 0:
                    System.err.println("ERROR: empty documentation tag: " + currentFileName);
                    break;
                case 1:
                    entry = getDocumentation(currentNode);
                    break;
                default:
                    String firstChildTagName = currentNode.getChildNodes().item(1).getNodeName();
                    if (firstChildTagName == "CodeName") {
                        entry = getCodeName(currentNode);
                    } else if (firstChildTagName.contains("ccts")) {
                        entry = getCCTSData(currentNode);
                    }
                    break;
            }
            hashMap.put(entry.getKey(), entry.getValue()); // typeNameRef, description
        }
        return hashMap;
    }

    private Map.Entry<String, String> getDocumentation(Node node) {
        Node parentNode;
        String typeNameRef = null;
        String description = "Unknown"; // nodeList.item(i).getFirstChild().getNodeValue();
        try {
            parentNode = node.getParentNode().getParentNode();
            typeNameRef = getTypeNameRef(parentNode);
            if (typeNameRef == "Unknown") { // try going to fourth ancestor to find type, name or ref
                // System.out.println("Parent not at second level");
                parentNode = parentNode.getParentNode().getParentNode();
                typeNameRef = getTypeNameRef(parentNode);
                if (typeNameRef == "Unknown") {
                    System.err.println("Parent not at fourth level");
                }
            }
        } catch (NullPointerException typeErr) {
            System.err.println("Error while finding parent");
        }
        try {
            description = node.getFirstChild().getNodeValue();
        } catch (NullPointerException typeErr) {
            System.err.println("Error while finding documentation text");
        }
        return new AbstractMap.SimpleEntry<>(typeNameRef, description);
    }

    private LinkedHashMap<String, String> sampleGetCCTSDefinition(Document doc) {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        // nodeList: list of all documentation nodes
        NodeList nodeList = doc.getElementsByTagName("ccts_Definition");

        // iterate through all documentation nodes
        // attempt to find correct parent node with appropriate type, name, or ref attribute
        for (int i = 0; i < nodeList.getLength() && nodeList.getLength() != 0; i++) {
            hashMap.put("Unknown", nodeList.item(i).getFirstChild().getNodeValue()); // typeNameRef, description
        }
        return hashMap;
    }

    // Type, Name, or Ref Value formatted
    private String getTypeNameRef(Node node) {
        String typeNameRef = "Unknown";
        Node attribute = node.getAttributes().getNamedItem("type");
        if (attribute != null) {
            typeNameRef = "Type: _" + attribute.getNodeValue() + "_";
        } else {
            attribute = node.getAttributes().getNamedItem("name");
            if (attribute != null) {
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
    private LinkedHashMap<String, List<String>> tokenize(LinkedHashMap<String, String> hashMap) {
        LinkedHashMap<String, List<String>> tokenizedHashMap = new LinkedHashMap<>();
        List<String> tokens;

        // iterate through hash map and tokenize all descriptions
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            // tokenize using spaces, whitespace, backslash, dash, and underscore
            // \\b: non-word boundary TODO: acts weird here
            // \\B: word boundary
            tokens = new LinkedList<>(Arrays.asList(entry.getValue().split(" |\n|(\\b/\\b)|(\\b-\\b)|(\\B_\\B)")));

            // cut off non-word characters at the beginning and end of tokens
            // cut off 's and (s) at the end of words
            for (int i = 0; i < tokens.size(); i++) {
                // need to replace ".,()/·:'-" and "'s"
                tokens.set(i, tokens.get(i).trim().replaceAll("^\\W|\\W*$", "").replaceAll("('s$)|(\\(s$)", ""));
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
            if (!isNumeric(term.replaceAll("\\.|,|:", ""))) {

                // if term is not empty or non-word characters add term to revised token list
                if (!WordLists.isSpecialWord(term) && !term.equals("") && !regexChecker("^[\\W]*$", term)) {
                    revisedTokens.add(term);
                }
            }
        }
        return revisedTokens;
    }

    // return true if string is numeric
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    // return true if regex pattern is found in string
    public static Boolean regexChecker(String theRegex, String str2Check) {
        Pattern checkRegex = Pattern.compile(theRegex);
        Matcher regexMatcher = checkRegex.matcher(str2Check);
        return regexMatcher.find();
    }

    // creates hash map of all spelling mistakes with type names
    // if type has no typos, key is not added to hash map
    private LinkedHashMap<String, List<String>> spellCheck(LinkedHashMap<String, List<String>> tokenizedHashMap) {
        LinkedHashMap<String, List<String>> mispelledHashMap = new LinkedHashMap<>();
        List<String> mispelledTerms;

        // iterate through entries in tokenized hash map
        for (Map.Entry<String, List<String>> entry : tokenizedHashMap.entrySet()) {
            mispelledTerms = new ArrayList<>();
            // iterate through tokenized terms and add terms which are spelled correctly
            for (String term : entry.getValue()) {
                if (!spellCheckWord(term) || term.equals("Acknowledgeed")) {
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
    private boolean spellCheckWord(String term) {
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
            LinkedHashMap<String, String> dictionary = parser.getDescriptions(document);

            if (dictionary.size() != 0) { // check for empty document
                LinkedHashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
                LinkedHashMap<String, List<String>> spellingMistakes = parser.spellCheck(tokenizedDictionary);

                // if there are spelling mistakes
                if (spellingMistakes.size() != 0) {
                    mispelledFileCount++;
                    bw.write(mispelledFileCount + ". ");

                    // write file path and Spelling Mistakes
                    bw.write(file.getName() + " Spelling Mistakes");
                    bw.newLine();

                    // writes typos onto log file
                    for (Map.Entry<String, List<String>> entry : spellingMistakes.entrySet()) {
                        bw.write(entry.getKey());
                        bw.newLine();
                        String autocorrectTerm;
                        // prints each typo
                        for (String mispell : entry.getValue()) {
                            bw.write("- " + mispell);
                            autocorrectTerm = WordLists.autocorrectTerm(mispell);
                            if (autocorrectTerm != null){
                                bw.write(" => " + autocorrectTerm);
                            }
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
    private void loadDirectory(String directory, String directoryName, ParseTokenize parser) {
        try {
            mispelledFileCount = 0;

            // write Directory name
            bw.write("Directory: " + directoryName);
            bw.newLine();
            bw.newLine();

            // iterate through files in directory
            for (File file : new File(directory).listFiles()) {
                // write file name
                currentFileName = file.getName();

                // ignore IST files
                if (!currentFileName.contains("IST")) {
                    parser.printSpellingMistakes(file, parser);
                } else {
                    System.out.println("Skipped IST file: " + currentFileName);
                }
            }

            System.out.println("DIRECTORY: " + directoryName + " DONE");
            bw.write(mispelledFileCount + " file(s) with spelling mistakes.");
            bw.newLine();
        } catch (IOException e) {
            System.err.println(e + "; Invalid Directory Path");
        }
    }

    // write log file using suffix
    private void writeLogfile(String suffix, ParseTokenize parser) throws IOException {
        String directory = DIRECTORY_PREFIX + suffix;
        String directoryName = "Model\\" + suffix;

        // create or write over logfile in SpellingMistakes folder
        String logfileName = "./SpellingMistakes/" + suffix.toLowerCase().replaceAll("\\\\", "_") + "_logfile.txt";
        parser.writeLogfile(directory, directoryName, logfileName, parser);
    }

    // write log file
    private void writeLogfile(String directory, String directoryName, String logfileName, ParseTokenize parser) throws IOException {
        try {
            parser.fw = new FileWriter(logfileName);
            parser.bw = new BufferedWriter(parser.fw);

            // Test directory
            parser.loadDirectory(directory, directoryName, parser);

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

    // write log file for single file
    private void writeLogfileForSingleFile(String suffix, ParseTokenize parser) {
        String filePath = DIRECTORY_PREFIX + suffix;
        String directoryName = "Model\\" + suffix;
        String logfileName = "./SpellingMistakes/" + suffix.toLowerCase().replace(".xsd", "").replaceAll("\\\\", "_") + "_logfile.txt";

        // create or write over logfile in SpellingMistakes folder
        try {
            parser.fw = new FileWriter(logfileName);
            parser.bw = new BufferedWriter(parser.fw);
            parser.printSpellingMistakes(new File(filePath), parser);

        } catch (IOException ex) {
            ex.printStackTrace();
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
//        String term = "SEARCHTERM";
//        System.out.println(!WordLists.isSpecialWord(term) && !term.equals("") && !regexChecker("^[\\W]*$", term));
//        parser.writeLogfile("BODs", parser); // DONE
//        parser.writeLogfile("Nouns", parser); // DONE
//        parser.writeLogfile("Platform\\2_3\\BODs", parser); // DONE
//        parser.writeLogfileForSingleFile("Platform\\2_3\\Extension\\Extensions.xsd", parser); // No spelling mistakes
//        parser.writeLogfileForSingleFile("Platform\\2_3\\Common\\CodeLists\\CodeLists_1.xsd", parser);
//        parser.writeLogfileForSingleFile("Platform\\2_3\\Common\\CodeLists\\CodeList_CurrencyCode_ISO_7_04.xsd", parser);
//        parser.writeLogfile("Platform\\2_3\\Common\\CodeLists", parser); // DONE
//        parser.writeLogfile("Platform\\2_3\\Common\\Components", parser); // DONE
//        parser.writeLogfile("Platform\\2_3\\Common\\DataTypes", parser); // No spelling mistakes
//        parser.writeLogfileForSingleFile("Platform\\2_3\\Common\\DataTypes\\XMLSchemaBuiltinType_1_patterns.xsd", parser);
        parser.writeLogfile("Platform\\2_3\\Common\\IdentifierScheme", parser);
    }
}
