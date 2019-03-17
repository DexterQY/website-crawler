package per.qy.crawler.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.qy.crawler.model.WebPageModel;

public class PageDownloadUtil {

    private static final Logger LOG = LoggerFactory.getLogger(PageDownloadUtil.class);
    private static final int MAX_HTML_LENGTH = 20 * 1024 * 1024;//html页面限制20M
    private static final int MAX_FILE_LENGTH = 500 * 1024 * 1024;//其它附件类型限制500M
    private static final String FOLDER_NAME = "d:/temp/" + UUID.randomUUID().toString().replace("-", "") + "/";
    private static final AtomicInteger INDEX = new AtomicInteger();
    private static final CloseableHttpClient client = HttpClientUtil.createHttpClient();
    //自定义错误返回值
    private static final Map<Integer,String> CODE_MAP = new HashMap<>();
    static{
        CODE_MAP.put(-501,"uri解析异常");
        CODE_MAP.put(-502,"网络协议异常");
        CODE_MAP.put(-503,"域名解析异常");
        CODE_MAP.put(-504,"http连接异常");
        CODE_MAP.put(-505,"网络IO异常");
        CODE_MAP.put(-506,"页面解析异常");
        CODE_MAP.put(-507,"编码格式异常");
        CODE_MAP.put(-508,"内容长度超出限制");
        CODE_MAP.put(-509,"网页类型超出可爬取范围");
    }

    public static WebPageModel executeGet(String url, List<String> ranges) {
        WebPageModel page = new WebPageModel();
        int redirectTimes = 0;
        boolean redirect;
        URI uri = CrawlerUtil.urlConvertToUri(url);
        if (uri == null) {
            page.respCode = -501;
            page.message = CODE_MAP.get(page.respCode);
            return page;
        }
        do {
            redirectTimes++;
            redirect = false;
            HttpGet method = new HttpGet(uri);
            HttpClientUtil.setHeader(method, url);
            CloseableHttpResponse response = null;
            long millis = System.currentTimeMillis();
            try {
                response = client.execute(method);
                page.respCode = response.getStatusLine().getStatusCode();
                if (page.respCode == HttpStatus.SC_OK) {
                    download(page, url, response, ranges);
                } else if (page.respCode >= 300 && page.respCode < 400) {// 页面跳转
                    Header[] locationHeader = response.getHeaders("location");
                    if (locationHeader != null && locationHeader.length > 0) {
                        String redirectUrl = locationHeader[0].getValue();
                        if (StringUtils.isNotBlank(redirectUrl) && !url.equals(redirectUrl)) {
                            uri = CrawlerUtil.urlConvertToUri(redirectUrl);
                            redirect = true;
                        }
                    }
                }
            } catch (ClientProtocolException e) {
                LOG.error("", e);
                page.respCode = -502;
                page.message = CODE_MAP.get(page.respCode);
            } catch (UnknownHostException e) {
                LOG.error("", e);
                page.respCode = -503;
                page.message = CODE_MAP.get(page.respCode);
            } catch (HttpHostConnectException e) {
                LOG.error("", e);
                page.respCode = -504;
                page.message = CODE_MAP.get(page.respCode);
            } catch (IOException e) {//连接超时尝试重连3次
                redirectTimes++;
                redirect = true;
                LOG.error(String.format("第%s次链接失败，executeusetime=%s", redirectTimes / 2,
                        System.currentTimeMillis() - millis), e);
                page.respCode = -505;
                page.message = CODE_MAP.get(page.respCode);
            } finally {
                if (response != null) {
                    EntityUtils.consumeQuietly(response.getEntity());
                    try {
                        response.close();
                    } catch (IOException e) {
                        LOG.error("responseclose", e);
                    }
                }
                method.releaseConnection();
            }
        } while (redirect && redirectTimes <= 5);
        return page;
    }

    private static void download(WebPageModel page, String url, CloseableHttpResponse response,
                                 List<String> ranges) {
        HttpEntity entity = response.getEntity();
        page.contentLength = entity.getContentLength();// 此方法不准确，经常返回-1，后面重新赋值

        // ContentType.getOrDefault(entity).getMimeType()提取可能会因为非支持的charset类型而报错，所以这里改为手工提取mimeType
        Header header = entity.getContentType();
        if (header != null) {
            HeaderElement[] headerElements = header.getElements();
            if (headerElements != null && headerElements.length > 0) {
                page.contentType = headerElements[0].getName();
            }
        }
        if (ContentTypeUtil.OCTET_STREAM_TYPE.equalsIgnoreCase(page.contentType)) {
            page.fileExt = HttpClientUtil.getOctetStreamFileExt(url, response);
        } else if (page.contentType == null) {
            //若未从header中取到contentType，根据url后缀判断
            if (url.lastIndexOf("/") > 8) {
                String name = url.substring(url.lastIndexOf("/"));
                if (name.contains(".")) {
                    page.contentType = ContentTypeUtil.getContentType(name.substring(name.lastIndexOf(".")));
                }
            }
        }
        page.updateFormat();

        if (ranges.contains(page.format.toString())) {
            if (page.format == WebPageModel.PageFormat.HTML) {
                if (page.contentLength == 0 || page.contentLength > MAX_HTML_LENGTH) {
                    page.respCode = -508;
                    page.message = CODE_MAP.get(page.respCode);
                    return;
                }
                try {
                    String html = null;
                    Document document = null;
                    String charset = null;
                    if (header != null) {
                        charset = CrawlerUtil.judgeCharset(header.toString());
                    }
                    if (charset != null) {
                        html = EntityUtils.toString(entity, charset);
                        document = Jsoup.parse(html);
                    } else {
                        byte[] data = EntityUtils.toByteArray(entity);
                        html = new String(data, CrawlerUtil.UTF_8);
                        document = Jsoup.parse(html);
                        charset = CrawlerUtil.getCharsetFromMeta(document);
                        if (charset != null && !CrawlerUtil.UTF_8.equals(charset)) {
                            html = new String(data, charset);
                            document = Jsoup.parse(html);
                        }
                    }
                    byte[] data = html.getBytes(CrawlerUtil.UTF_8);
                    page.contentLength = data.length;
                    if (page.contentLength <= 0 || page.contentLength > MAX_HTML_LENGTH) {
                        page.respCode = -508;
                        page.message = CODE_MAP.get(page.respCode);
                        return;
                    }

                    page.encoding = CrawlerUtil.UTF_8;
                    page.document = document;

                    createFilePath(page);
                    HttpClientUtil.exportDataAsFile(data, page.filePath);
                } catch (ParseException e) {
                    LOG.error("", e);
                    page.respCode = -506;
                    page.message = CODE_MAP.get(page.respCode);
                } catch (UnsupportedEncodingException e) {
                    LOG.error("", e);
                    page.respCode = -507;
                    page.message = CODE_MAP.get(page.respCode);
                } catch (IOException e) {
                    LOG.error("", e);
                    page.respCode = -505;
                    page.message = CODE_MAP.get(page.respCode);
                }
            } else {// 如果是非html页面直接下载
                if (page.contentLength == 0 || page.contentLength > MAX_FILE_LENGTH) {
                    page.respCode = -508;
                    page.message = CODE_MAP.get(page.respCode);
                    return;
                }
                if (page.fileExt == null) {
                    page.fileExt = ContentTypeUtil.getExtendFileName(page.contentType);
                }
                createFilePath(page);
                HttpClientUtil.exportEntityAsFile(entity, page.filePath);
            }
        } else {
            page.respCode = -509;
            page.message = CODE_MAP.get(page.respCode);
        }
    }

    private static void createFilePath(WebPageModel page) {
        String filePath = FOLDER_NAME + page.format.toString() + "/";
        File file = new File(filePath);
        file.mkdirs();
        page.filePath = filePath + INDEX.getAndIncrement() + page.fileExt;
    }
}
