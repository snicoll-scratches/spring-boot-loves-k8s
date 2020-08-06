package io.spring.sample.markdown;

import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import org.springframework.stereotype.Component;

@Component
class PegdownConverter {

	private final PegDownProcessor pegdown;

	public PegdownConverter() {
		this.pegdown = new PegDownProcessor(Extensions.ALL ^ Extensions.ANCHORLINKS);
	}

	public String convert(String markup) {
		// synchronizing on pegdown instance since neither the processor nor the
		// underlying parser is thread-safe.
		synchronized (this.pegdown) {
			return this.pegdown.markdownToHtml(markup.toCharArray());
		}
	}

}
