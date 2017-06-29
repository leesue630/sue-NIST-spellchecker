/**
 * Created by lns16 on 6/29/2017.
 */
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import gov.nist.surf2017.sue.config.SpellCheckerConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.xml.sax.SAXException;
import java.io.File;
import java.io.IOException;

public class SpellChecker {

    // ns: namespace
    private static final String ns = "http://www.openapplications.org/oagis/10";
    File file;
    XSOMParser parser = new XSOMParser();
    XSSchemaSet schemaSet;

    private String parse(File file){
        try{
            parser.parse(file);
            schemaSet = parser.getResult();
        } catch (SAXException | IOException e){
            throw new IllegalArgumentException("Invalid schema file: " + file, e);
        } finally {
            return schemaSet.getSchema(0).getTargetNamespace();
        }
    }

    public static void main(String[] args){
        SpellChecker spellchecker = new SpellChecker();
        File file = new File("C:\\Users\\lns16\\Documents\\OAGi-Semantic-Refinement-and-Tooling\\data\\OAGIS_10_3_EnterpriseEdition\\OAGi-BPI-Platform\\org_openapplications_oagis\\10_3\\Model\\BODs\\AcknowledgeAllocateResource.xsd");
        System.out.println(file.getName());
        System.out.println(spellchecker.parse(file));


        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(
                SpellCheckerConfig.class
        );

        gov.nist.surf2017.sue.spellchecker.SpellChecker spellChecker =
                applicationContext.getBean(gov.nist.surf2017.sue.spellchecker.SpellChecker.class);
        spellChecker.check("agagagag");

    }

}
