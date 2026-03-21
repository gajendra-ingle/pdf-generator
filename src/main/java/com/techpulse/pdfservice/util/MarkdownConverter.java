package com.techpulse.pdfservice.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class MarkdownConverter {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownConverter() {
        MutableDataSet options = new MutableDataSet();
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * Converts Markdown content to a full standalone HTML document.
     * Optionally injects custom CSS into the &lt;style&gt; block.
     *
     * @param markdownContent raw Markdown string
     * @param cssContent optional custom CSS (null = use default)
     * @return full HTML document string ready for Gotenberg
     */
    public String toHtml(String markdownContent, String cssContent) {
        log.debug("Converting Markdown to HTML");

        Node document = parser.parse(markdownContent);
        String body = renderer.render(document);
        String css = StringUtils.hasText(cssContent) ? cssContent : defaultCss();

        return """
                <!DOCTYPE html>
                <html>
                  <head>
                    <meta charset="UTF-8"/>
                    <style>
                      %s
                    </style>
                  </head>
                  <body>
                    %s
                  </body>
                </html>
                """.formatted(css, body);
    }

    private String defaultCss() {
        return """
                body {
                  font-family: Arial, sans-serif;
                  font-size: 14px;
                  line-height: 1.6;
                  color: #333;
                  margin: 40px;
                }
                h1, h2, h3, h4 { color: #222; }
                pre {
                  background: #f4f4f4;
                  padding: 10px;
                  border-radius: 4px;
                  overflow-x: auto;
                }
                code {
                  font-family: monospace;
                  background: #f4f4f4;
                  padding: 2px 4px;
                  border-radius: 3px;
                }
                table { border-collapse: collapse; width: 100%; }
                th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                th { background: #f0f0f0; }
                blockquote {
                  border-left: 4px solid #ddd;
                  margin: 0;
                  padding-left: 16px;
                  color: #666;
                }
                """;
    }
}

