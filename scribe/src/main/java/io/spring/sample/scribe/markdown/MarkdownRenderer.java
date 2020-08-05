package io.spring.sample.scribe.markdown;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MarkdownRenderer {

	private static final Logger logger = LoggerFactory.getLogger(MarkdownRenderer.class);

	private final RestTemplate restTemplate;

	private final CircuitBreaker circuitBreaker;

	public MarkdownRenderer(RestTemplateBuilder builder, CircuitBreakerFactory circuitBreakerFactory) {
		this.restTemplate = builder.build();
		this.circuitBreaker = circuitBreakerFactory.create("markdown");
	}

	public String renderToHtml(String markup) {
		URI uri = URI.create("http://localhost:8082/convert");
		RequestEntity<String> request = RequestEntity.post(uri)
				.contentType(MediaType.TEXT_MARKDOWN)
				.body(markup);
		return this.circuitBreaker.run(
				() -> this.restTemplate.exchange(request, String.class).getBody(),
				(exception) -> {
					logger.warn("Error while calling markdown converter service", exception);
					return "<p>Could not render markup.</p>";
				});
	}
}
