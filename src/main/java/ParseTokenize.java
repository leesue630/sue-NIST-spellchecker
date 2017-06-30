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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class ParseTokenize {

    private static final String DOCUMENTNAME = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\BODs\\AcknowledgeAllocateResource.xsd";
    private static final String FILENAME = "./logfile.txt";
    private XPathFactory xPathFactory;
    private XPath xPath;

    public ParseTokenize() {
        this.xPathFactory = XPathFactory.newInstance();
        this.xPath = xPathFactory.newXPath();
    }

    public Document parse(String fileName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder;
        Document doc = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            doc = documentBuilder.parse(fileName);
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
        //create XPathExpression object
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
            tokens = entry.getValue().split(" ");
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].replaceAll(".", "").replaceAll(",", "").replaceAll("[()]", "").replaceAll("\n", "");
            }
            tokenizedDictionary.put(entry.getKey(), normalize(removeStopWords(tokens)));
        }
        return tokenizedDictionary;
    }

    // removes stop words and white space
    private List<String> removeStopWords(String[] tokens) {
        List<String> revisedTokens = new ArrayList<>();
        for (String term : tokens) {
            if (!isStopWord(term.toLowerCase())) {
                revisedTokens.add(term);
            }
        }
        return revisedTokens;
    }

    // checks if term is a stop word
    private boolean isStopWord(String term) {
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term)) {
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
                tokens.remove(i);
                for (int j = 0; j < termArray.length; j++) {
                    tokens.add(i + j, termArray[j]);
                }
                i += (termArray.length - 1);
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

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ParseTokenize parser = new ParseTokenize();
        Document document = parser.parse(DOCUMENTNAME);
        String docURI = "Document URI: " + document.getDocumentURI();
        HashMap<String, String> dictionary = parser.getDescriptions(document, parser.xPath);
        HashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
        HashMap<String, List<String>> spellingMistakes = parser.spellCheck(tokenizedDictionary);

        BufferedWriter bw = null;
        FileWriter fw = null;

        try /*OutputStream out = new BufferedOutputStream(Files.newOutputStream(FILENAME, CREATE, APPEND))*/ {
            fw = new FileWriter(FILENAME);
            bw = new BufferedWriter(fw);
            bw.write(docURI);
            bw.newLine();
            for (Map.Entry<String, List<String>> entry : spellingMistakes.entrySet()){
                bw.write("Type Name: " + entry.getKey());
                bw.newLine();
                for (String mispell : entry.getValue()) {
                    bw.write(mispell);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println(e);
        } finally {

            try {

                if (bw != null)
                    bw.close();

                if (fw != null)
                    fw.close();

            } catch (IOException ex) {

                ex.printStackTrace();

            }

        }
    }
}
