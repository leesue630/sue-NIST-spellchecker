package gov.nist.surf2017.sue.spellchecker;

public interface SpellChecker {

    public void check(String term) throws SpellCheckException;

}
