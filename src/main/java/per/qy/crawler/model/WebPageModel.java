package per.qy.crawler.model;

import org.jsoup.nodes.Document;
import per.qy.crawler.util.ContentTypeUtil;

public class WebPageModel {

    public int respCode = 200;//响应状态码
    public String message;//异常信息
    public Document document;//html页面Document对象
    public String encoding;//页面编码
    public String contentType;//网页类型
    public long contentLength;//内容长度
    public String filePath;//文件路径
    public String fileExt;//文件后缀

    public PageFormat format = PageFormat.OTHER;

    public enum PageFormat {
        HTML, IMAGE, AUDIO, VIDEO, TXT, WORD, EXCEL, PPT, PDF, COMPRESS, APK, IPA, OTHER
    }

    public void updateFormat() {
        String type = contentType;
        if (ContentTypeUtil.OCTET_STREAM_TYPE.equalsIgnoreCase(contentType)) {
            type = ContentTypeUtil.getContentType(fileExt);
        }
        if (ContentTypeUtil.isHtml(type)) {
            format = PageFormat.HTML;
        } else if (ContentTypeUtil.isImage(type)) {
            format = PageFormat.IMAGE;
        } else if (ContentTypeUtil.isAudio(type)) {
            format = PageFormat.AUDIO;
        } else if (ContentTypeUtil.isVideo(type)) {
            format = PageFormat.VIDEO;
        } else if (ContentTypeUtil.isTxt(type)) {
            format = PageFormat.TXT;
        } else if (ContentTypeUtil.isWord(type)) {
            format = PageFormat.WORD;
        } else if (ContentTypeUtil.isExcel(type)) {
            format = PageFormat.EXCEL;
        } else if (ContentTypeUtil.isPpt(type)) {
            format = PageFormat.PPT;
        } else if (ContentTypeUtil.isPdf(type)) {
            format = PageFormat.PDF;
        } else if (ContentTypeUtil.isCompress(type)) {
            format = PageFormat.COMPRESS;
        } else if (ContentTypeUtil.isApk(type)) {
            format = PageFormat.APK;
        } else if (ContentTypeUtil.isIpa(type)) {
            format = PageFormat.IPA;
        }
    }
}
