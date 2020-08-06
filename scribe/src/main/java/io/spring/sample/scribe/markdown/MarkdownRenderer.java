package io.spring.sample.scribe.markdown;

import java.net.URI;
import java.util.function.Supplier;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MarkdownRenderer {

	private static final Logger logger = LoggerFactory.getLogger(MarkdownRenderer.class);

	private final RestTemplate restTemplate;

	private final CircuitBreaker circuitBreaker;

	public MarkdownRenderer(RestTemplateBuilder builder) {
		this.restTemplate = builder.build();
		this.circuitBreaker = CircuitBreaker.ofDefaults("markdown");
	}

	public String renderToHtml(String markup) {
		URI uri = URI.create("http://localhost:8082/convert");
		RequestEntity<String> request = RequestEntity.post(uri)
				.contentType(MediaType.TEXT_MARKDOWN)
				.body(markup);
		Supplier<String> supplier = this.circuitBreaker.decorateSupplier(
				() -> this.restTemplate.exchange(request, String.class).getBody());
		return Try.ofSupplier(supplier).recover(
				(exception) -> {
					logger.warn("Error while calling markdown converter service", exception);
					return "<p>Could not render markup.</p>";
				}).get();
	}
}
