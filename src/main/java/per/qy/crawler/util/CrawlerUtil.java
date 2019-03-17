package per.qy.crawler.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.concurrent.TimeUnit;

public class CrawlerUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerUtil.class);
    public final static String UTF_8 = "UTF-8";

    /**
     * 线程按秒休眠
     */
    public static void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
    }

    public static URI urlConvertToUri(String url) {
        try {
            URL urlObj = new URL(url);
            URI uri = new URI(urlObj.getProtocol(), urlObj.getUserInfo(), IDN.toASCII(urlObj.getHost()),
                    urlObj.getPort(), urlObj.getPath(), urlObj.getQuery(), null);
            return uri;
        } catch (MalformedURLException e) {
            LOG.error("", e);
        } catch (URISyntaxException e) {
            LOG.error("", e);
        }
        return null;
    }

    public static String judgeCharset(String content) {
        if (content == null || "".equals(content.trim())) {
            return null;
        }
        String[] charsets = {"UTF-8", "GBK", "GB2312", "GB18030", "BIG5", "ISO8859-1", "ASCII", "UTF-16", "UTF-32"};
        String upperContent = content.toUpperCase();
        for (String charset : charsets) {
            if (upperContent.contains(charset)) {
                return charset;
            }
        }
        return null;
    }

    public static String getCharsetFromMeta(Document document) {
        Elements metas = document.select("meta[http-equiv=Content-Type]");
        if (metas != null && metas.size() > 0) {
            for (Element meta : metas) {
                String charset = judgeCharset(meta.attr("content"));
                if (charset != null) {
                    return charset;
                }
            }
        }
        metas = document.select("meta[charset]");
        if (metas != null && metas.size() > 0) {
            for (Element meta : metas) {
                String charset = judgeCharset(meta.attr("charset"));
                if (charset != null) {
                    return charset;
                }
            }
        }
        return null;
    }

    /**
     * 判断url对于rootUrl是否外链
     */
    public static boolean isOuterUrl(String rootUrl, String url) {
        String rootUrlDomain = getDomain(rootUrl);
        String urlDomain = getDomain(url);
        return rootUrlDomain.contains(urlDomain) || urlDomain.contains(rootUrlDomain);
    }

    /**
     * 获取网址域名部分
     */
    private static String getDomain(String url) {
        String domain = url.toLowerCase().trim();
        if (domain.contains("http://")) {
            domain = domain.replace("http://", "");
        } else if (domain.contains("https://")) {
            domain = domain.replace("https://", "");
        }
        int index = domain.indexOf("/");
        if (index > -1) {
            domain = domain.substring(0, index);
        }
        return domain;
    }
}
