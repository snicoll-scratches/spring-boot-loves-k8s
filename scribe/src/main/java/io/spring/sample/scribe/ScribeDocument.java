package io.spring.sample.scribe;

import java.util.List;

import io.spring.sample.scribe.spell.Typo;

public class ScribeDocument {

	private String markup;

	private String html;

	private List<Typo> typos;

	public String getMarkup() {
		return markup;
	}

	public void setMarkup(String markup) {
		this.markup = markup;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public List<Typo> getTypos() {
		return typos;
	}

	public void setTypos(List<Typo> typos) {
		this.typos = typos;
	}
}
