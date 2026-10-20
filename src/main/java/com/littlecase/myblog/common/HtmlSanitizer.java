package com.littlecase.myblog.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * HTML 清洗：文章正文渲染为 HTML 后、入库前做一次 XSS 过滤（PRD 6 安全）
 * <p>
 * 采用白名单策略，保留 Markdown 常用标签；前端再用 DOMPurify 做二次清洗（双层防护）。
 * 注意：图片走相对路径 /upload/**，故开启 preserveRelativeLinks，避免相对地址被当作非法协议剔除。
 */
public final class HtmlSanitizer {

    /**
     * 占位 base URI（不会出现在输出中）。
     * 坑：jsoup 的 URL 协议白名单只认绝对地址，baseUri 为空时会把 /upload/xxx.png、/article/1
     * 这类相对地址当作“非法协议”直接删掉；给出 baseUri 后相对地址能通过协议校验，
     * 再配合 preserveRelativeLinks(true)，输出仍保持相对路径。
     */
    private static final String BASE_URI = "http://localhost";

    private static final Safelist SAFELIST = Safelist.relaxed()
            // 允许 Markdown 渲染常见的 class（如代码高亮 hljs-*、表格对齐）
            .addAttributes(":all", "class")
            .addAttributes("a", "target", "rel")
            .preserveRelativeLinks(true);

    private HtmlSanitizer() {
    }

    /** 清洗 HTML 片段，返回安全内容 */
    public static String clean(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document.OutputSettings settings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(html, BASE_URI, SAFELIST, settings);
    }
}
