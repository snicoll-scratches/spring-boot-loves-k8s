package io.spring.sample.scribe.spell;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
class DictionaryLoader implements ApplicationRunner {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final SpellChecker spellChecker;

	public DictionaryLoader(SpellChecker spellChecker) {
		this.spellChecker = spellChecker;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Initializing dictionary");
		Resource resource = new DefaultResourceLoader().getResource("classpath:words.txt");
		try (InputStream in = resource.getInputStream()) {
			String content = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
			for (String word : content.split("\\r?\\n")) {
				spellChecker.addWordToDictionary(word);
			}
		}
	}

}
