package io.spring.sample.scribe.spell;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DictionaryLoader implements ApplicationRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SpellChecker spellChecker;

	public DictionaryLoader(SpellChecker spellChecker) {
		this.spellChecker = spellChecker;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Initializing dictionary");
		Path wordsPath = Paths.get(getClass().getResource("/words.txt").toURI());
		Files.lines(wordsPath).forEach(spellChecker::addWordToDictionary);
	}
}
