package gov.nist.surf2017.sue.config;

import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.dictionary.Dictionary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"gov.nist.surf2017.sue.spellchecker.impl"})
public class SpellCheckerConfig {

    @Bean
    public Dictionary dictionary() throws JWNLException {
        Dictionary dictionary = Dictionary.getDefaultResourceInstance();
        return dictionary;
    }
}
