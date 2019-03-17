package per.qy.crawler.util;

import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class UrlExtract {

    private Document document;
    private String rootUrl;
    private Set<String> urls = new HashSet<>();

    public UrlExtract(String html, String rootUrl) {
        this.document = Jsoup.parse(html);
        this.rootUrl = rootUrl;
    }

    public UrlExtract(Document document, String rootUrl) {
        this.document = document;
        this.rootUrl = rootUrl;
    }

    public Set<String> getUrls() {
        return urls;
    }

    /**
     * 提取a标签链接
     */
    public UrlExtract extractFromA() {
        extractFromTag("a", "href");
        return this;
    }

    /**
     * 提取img标签链接
     */
    public UrlExtract extractFromImg() {
        extractFromTag("img", "src");
        return this;
    }

    /**
     * 提取frame标签链接
     */
    public UrlExtract extractFromFrame() {
        extractFromTag("frame", "src");
        return this;
    }

    /**
     * 提取iframe标签链接
     */
    public UrlExtract extractFromIframe() {
        extractFromTag("iframe", "src");
        return this;
    }

    /**
     * 提取标签链接
     */
    public UrlExtract extractFromTag(String tagName, String urlAttributeName) {
        Elements elements = document.select(tagName);
        if (elements != null && elements.size() > 0) {
            for (Element element : elements) {
                if (element.hasAttr(urlAttributeName)) {
                    String url = element.attr(urlAttributeName);
                    url = completeSrcByRootUrl(rootUrl, url);
                    urls.add(url);
                }
            }
        }
        return this;
    }

    private String completeSrcByRootUrl(String rootUrl, String src) {
        String allSrc = rootUrl;
        if (src != null && !"".equals(src.trim())) {
            rootUrl = rootUrl.replaceAll("\\s", "");
            if (rootUrl.lastIndexOf("/") == rootUrl.length() - 1) {
                rootUrl = rootUrl.substring(0, rootUrl.lastIndexOf("/"));
            }
            if (src.toLowerCase().startsWith("http")) {// 全路径
                allSrc = src;
            } else if (src.startsWith("//")) {// 全路径
                if (rootUrl.startsWith("https")) {
                    allSrc = "https:" + src;
                } else {
                    allSrc = "http:" + src;
                }
            } else if (src.startsWith("/")) {// 绝对路径
                if (rootUrl.lastIndexOf("/") < 9) {
                    allSrc = rootUrl + src;
                } else {
                    allSrc = rootUrl.substring(0, rootUrl.indexOf("/", 9)) + src;
                }
            } else {// 相对路径
                int num = 0;
                while (src.startsWith("../")) {
                    num++;
                    src = src.substring(3);
                }
                for (int i = 0; i <= num; i++) {
                    if (rootUrl.lastIndexOf("/") > 8) {
                        rootUrl = rootUrl.substring(0, rootUrl.lastIndexOf("/"));
                    }
                }
                allSrc = rootUrl + "/" + src;
            }
        }
        return allSrc;
    }
}
