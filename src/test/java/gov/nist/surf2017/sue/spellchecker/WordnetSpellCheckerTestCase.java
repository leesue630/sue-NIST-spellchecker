package gov.nist.surf2017.sue.spellchecker;

import gov.nist.surf2017.sue.config.SpellCheckerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpellCheckerConfig.class})
public class WordnetSpellCheckerTestCase {

    @Autowired
    private SpellChecker spellChecker;

    @Test(expected = SpellCheckException.class)
    public void testBOM() {
        spellChecker.check("BOM");
    }

    @Test
    public void testAcknowledgeing() {
        spellChecker.check("Acknowledgeing");
    }

}
