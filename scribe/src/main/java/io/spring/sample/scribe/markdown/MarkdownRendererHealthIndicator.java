package io.spring.sample.scribe.markdown;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class MarkdownRendererHealthIndicator extends AbstractHealthIndicator {

	private final String TEST_MARKUP = "This is a *test*.";

	private final String EXPECTED_HTML = "<p>This is a <em>test</em>.</p>";

	private final MarkdownRenderer markdownRenderer;

	public MarkdownRendererHealthIndicator(MarkdownRenderer markdownRenderer) {
		this.markdownRenderer = markdownRenderer;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		String result = this.markdownRenderer.renderToHtml(TEST_MARKUP);
		if (EXPECTED_HTML.equals(result)) {
			builder.up();
		}
		else {
			builder.outOfService().withDetail("result", result);
		}
	}
}
