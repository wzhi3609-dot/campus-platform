package com.campus.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * HTML 内容清洗工具，使用白名单过滤防止 XSS 攻击
 */
public class HtmlSanitizer {

    private static final Safelist SAFELIST;

    static {
        SAFELIST = Safelist.basic()
                .addTags("h1", "h2", "h3", "h4", "img", "span", "div", "hr")
                .addAttributes("img", "src", "alt", "width", "height")
                .addAttributes("span", "style")
                .addAttributes("div", "style")
                .addAttributes("a", "target", "rel")
                .addProtocols("img", "src", "http", "https", "data");
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return Jsoup.clean(html, SAFELIST);
    }
}
