package io.spring.sample.scribe;

import io.spring.sample.scribe.markdown.MarkdownRenderer;
import io.spring.sample.scribe.spell.SpellChecker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class EditorController {

	private final SpellChecker spellChecker;

	private final MarkdownRenderer markdownRenderer;

	public EditorController(SpellChecker spellChecker, MarkdownRenderer markdownRenderer) {
		this.spellChecker = spellChecker;
		this.markdownRenderer = markdownRenderer;
	}

	@PostMapping("/")
	public String spellcheck(@ModelAttribute("document") ScribeDocument document, Model model) {
		document.setHtml(this.markdownRenderer.renderToHtml(document.getMarkup()));
		document.setTypos(this.spellChecker.spellCheck(document.getMarkup()));
		model.addAttribute("document", document);
		return "index";
	}

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("document", new ScribeDocument());
		return "index";
	}
}
