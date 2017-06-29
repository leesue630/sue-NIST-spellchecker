package gov.nist.surf2017.sue.spellchecker.impl;

import gov.nist.surf2017.sue.spellchecker.SpellCheckException;
import gov.nist.surf2017.sue.spellchecker.SpellChecker;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.IndexWordSet;
import net.sf.extjwnl.dictionary.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WordnetSpellChecker implements SpellChecker {

    @Autowired
    private Dictionary dictionary;

    @Override
    public void check(String term) throws SpellCheckException {
        IndexWordSet indexWordSet;
        try {
            indexWordSet = dictionary.lookupAllIndexWords(term);
        } catch (JWNLException e) {
            throw new SpellCheckException(e);
        }

        if (indexWordSet.size() == 0) {
            throw new SpellCheckException("'" + term + "' can't be found in the database.");
        }
    }

}
