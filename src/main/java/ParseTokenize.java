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
import java.io.IOException;

import java.util.*;

public class ParseTokenize {

    String fileName;

    public ParseTokenize() {
        this.fileName = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\BODs\\AcknowledgeAllocateResource.xsd";
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
        for (Map.Entry<String, String> entry : dictionary.entrySet()){
            tokens = entry.getValue().split(" ");
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokens[i].replace(".", "").replace(",", "").replace("\\(", "").replace("\\)", "").replace("\n", "");
            }
            tokenizedDictionary.put(entry.getKey(), normalize(removeStopWords(tokens)));
        }
        return tokenizedDictionary;
    }

    // removes stop words and white space
    private List<String> removeStopWords(String[] tokens){
        List<String> revisedTokens = new ArrayList<>();
        for (String term : tokens){
            if (!isStopWord(term.toLowerCase())){
                revisedTokens.add(term);
            }
        }
        return revisedTokens;
    }

    // checks if term is a stop word
    private boolean isStopWord(String term){
        if (StandardAnalyzer.ENGLISH_STOP_WORDS_SET.contains(term)){
            return true;
        }
        return false;
    }

    // normalizes tokens for camel case
    private List<String> normalize(List<String> tokens){
        for (int i = 0; i < tokens.size(); i++){
            String term = tokens.get(i);
            if (term != term.toLowerCase()){
                String[] termArray = StringUtils.splitByCharacterTypeCamelCase(term);
                tokens.remove(i);
                for (int j = 0; j < termArray.length; j++){
                    tokens.add(i+j, termArray[j]);
                }
                i+=(termArray.length - 1);
            }
        }
        return tokens;
    }

    // checks the spelling of terms
    private void spellCheck(String term) throws SpellCheckException{
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SpellCheckerConfig.class
        );
        SpellChecker spellChecker = applicationContext.getBean(SpellChecker.class);
        try {
            spellChecker.check(term.toLowerCase());
        } catch (SpellCheckException e){
            System.out.println("Incorrect Spelling! " + term);
        }
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ParseTokenize parser = new ParseTokenize();
        //fileName = "";
        Document document = parser.parse(parser.fileName);
        System.out.println("Document URI: " + document.getDocumentURI());
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        HashMap<String, String> dictionary = parser.getDescriptions(document, xPath);
        HashMap<String, List<String>> tokenizedDictionary = parser.tokenize(dictionary);
//        for (key : tokenizedDictionary){
//            HashMap<String, String> spellMistakes = parser.spellCheck(tokenizedDictionary.(key));
//        }
    }
}
