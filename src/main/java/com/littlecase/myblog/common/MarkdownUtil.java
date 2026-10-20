package com.littlecase.myblog.common;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Markdown 工具：发布时把 Markdown 原文渲染为 HTML 缓存到 article.content_html（PRD 假设 1 / 10.2）
 * <p>
 * 支持 GFM 表格、删除线、任务列表；代码块使用 &lt;pre&gt;&lt;code&gt; 输出，
 * 前端再用 highlight.js 做语法高亮（PRD 7.2）。
 */
public final class MarkdownUtil {

    /** 自动摘要长度：摘要为空时截取正文纯文本前 150 字（PRD 假设 2） */
    private static final int SUMMARY_LENGTH = 150;

    private static final List<Extension> EXTENSIONS = List.of(
            TablesExtension.create(),
            StrikethroughExtension.create(),
            TaskListItemsExtension.create()
    );

    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    private static final Pattern CODE_FENCE = Pattern.compile("(?s)```.*?```");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern IMAGE = Pattern.compile("!\\[[^\\]]*\\]\\([^)]*\\)");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]*)\\]\\([^)]*\\)");
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]*)`");
    private static final Pattern HEADING = Pattern.compile("(?m)^\\s{0,3}#{1,6}\\s*");
    private static final Pattern QUOTE_LIST = Pattern.compile("(?m)^\\s{0,3}(>|[-*+]|\\d+\\.)\\s+");
    /** Markdown 表格分隔行，如 | --- | :--: | */
    private static final Pattern TABLE_SEPARATOR = Pattern.compile("(?m)^\\s*\\|?\\s*:?-{2,}[\\s:|-]*$");
    private static final Pattern EMPHASIS = Pattern.compile("[*_~]{1,3}");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private MarkdownUtil() {
    }

    /** Markdown 原文 → HTML 片段（不可用时返回空串） */
    public static String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }

    /** Markdown 原文 → 纯文本（去语法标记，用于摘要与列表展示） */
    public static String toPlainText(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        String text = markdown;
        text = CODE_FENCE.matcher(text).replaceAll(" ");
        text = TABLE_SEPARATOR.matcher(text).replaceAll("");
        text = IMAGE.matcher(text).replaceAll(" ");
        text = LINK.matcher(text).replaceAll("$1");
        text = INLINE_CODE.matcher(text).replaceAll("$1");
        text = HTML_TAG.matcher(text).replaceAll(" ");
        text = HEADING.matcher(text).replaceAll("");
        text = QUOTE_LIST.matcher(text).replaceAll("");
        text = EMPHASIS.matcher(text).replaceAll("");
        text = text.replace("|", " ");
        text = WHITESPACE.matcher(text).replaceAll(" ");
        return text.trim();
    }

    /** 摘要：手填优先，为空时自动截取正文纯文本前 150 字 */
    public static String resolveSummary(String summary, String markdown) {
        if (summary != null && !summary.isBlank()) {
            return summary.trim();
        }
        String plain = toPlainText(markdown);
        if (plain.length() <= SUMMARY_LENGTH) {
            return plain;
        }
        return plain.substring(0, SUMMARY_LENGTH);
    }
}
