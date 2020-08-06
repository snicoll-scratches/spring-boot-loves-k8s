package io.spring.sample.scribe.spell;

import java.util.ArrayList;
import java.util.List;

public class Typo {

	private final String original;

	private final List<String> suggestions = new ArrayList<>();

	public Typo(String original) {
		this.original = original;
	}

	public void addSuggestion(String suggestion) {
		this.suggestions.add(suggestion);
	}

	public String getOriginal() {
		return original;
	}

	public List<String> getSuggestions() {
		return suggestions;
	}
}
