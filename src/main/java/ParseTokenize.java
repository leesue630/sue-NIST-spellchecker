/**
 * Created by lns16 on 6/29/2017.
 */

import com.sun.org.apache.xpath.internal.res.XPATHErrorResources;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import gov.nist.surf2017.sue.config.SpellCheckerConfig;
import gov.nist.surf2017.sue.spellchecker.SpellChecker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseTokenize {

    String fileName;

    public ParseTokenize() {
        this.fileName = "C:\\Users\\lns16\\Documents\\AARsample.xml";
    }

    public Document parse(String fileName) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        Document doc = null;
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
            doc = builder.parse(fileName);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            return doc;
        }
    }

    private List<String> getDescriptions(Document doc, XPath xPath) {
        List<String> list = new ArrayList<>();
        String xPathStatement = "//xsd:annotation/xsd:documentation";
//        String xPathStatement = "//first_name";

        try {
            //create XPathExpression object
            NodeList nodeList1 = doc.getElementsByTagName("xsd:documentation");
            for (int i = 0; i < nodeList1.getLength() && nodeList1.getLength()!= 0; i++) {
                String typeName = nodeList1.item(i).getParentNode().getParentNode().getAttributes().getNamedItem("type").getNodeValue();
                String description = nodeList1.item(i).getFirstChild().getNodeValue();
                System.out.println(typeName + ":");
                System.out.println(description);
                System.out.println("--------------------------------------");
            }
            XPathExpression expression = xPath.compile(xPathStatement);
            //evaluate expression result on XML document
            NodeList nodeList = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                list.add(nodeList.item(i).getFirstChild().getNodeValue());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        ParseTokenize parser = new ParseTokenize();
        //fileName = "C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\BODs\\AcknowledgeAllocateResource.xsd";
        Document document = parser.parse(parser.fileName);
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        List<String> list = parser.getDescriptions(document, xPath);
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SpellCheckerConfig.class
        );
        SpellChecker spellChecker = applicationContext.getBean(SpellChecker.class);
        spellChecker.check("grocery");

    }

}
