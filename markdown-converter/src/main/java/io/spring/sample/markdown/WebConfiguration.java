package io.spring.sample.markdown;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.accept;
import static org.springframework.web.servlet.function.RequestPredicates.contentType;

@Configuration(proxyBeanMethods = false)
class WebConfiguration {

	@Bean
	public RouterFunction<ServerResponse> routerFunction(MarkdownHandler markdownHandler) {

		return RouterFunctions.route()
				.POST("/convert", contentType(MediaType.TEXT_MARKDOWN).and(accept(MediaType.TEXT_HTML)), markdownHandler::convert)
				.build();
	}
}
