package io.spring.sample.scribe.markdown;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
class MarkdownRendererHealthIndicator extends AbstractHealthIndicator {

	private final MarkdownRenderer markdownRenderer;

	public MarkdownRendererHealthIndicator(MarkdownRenderer markdownRenderer) {
		this.markdownRenderer = markdownRenderer;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		String result = this.markdownRenderer.renderToHtml("This is a *test*.");
		if ("<p>This is a <em>test</em>.</p>".equals(result)) {
			builder.up();
		}
		else {
			builder.outOfService().withDetail("result", result);
		}
	}

}
