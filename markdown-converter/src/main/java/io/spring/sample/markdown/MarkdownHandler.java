package io.spring.sample.markdown;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
class MarkdownHandler {

	private final PegdownConverter converter;

	public MarkdownHandler(PegdownConverter converter) {
		this.converter = converter;
	}

	public ServerResponse convert(ServerRequest request) throws Exception {
		String markup = request.body(String.class);
		if (markup.contains("delay")) {
			Thread.sleep(8000);
		}
		return ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(converter.convert(markup));
	}
}
