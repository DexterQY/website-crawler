package per.qy.crawler.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContentTypeUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ContentTypeUtil.class);
    public static final String OCTET_STREAM_TYPE = "application/octet-stream";
    // key=MimeType
    private static final Map<String, String> TYPE_MAP = new HashMap<>();
    // key=Ext
    private static final Map<String, String> EXT_MAP = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        InputStream is = ContentTypeUtil.class.getResourceAsStream("/mime-type.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] data = line.split("=");
                if (data.length == 2) {
                    TYPE_MAP.put(data[1].trim(), data[0].trim());
                    EXT_MAP.put(data[0].trim(), data[1].trim());
                }
            }
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            try {
                reader.close();
                isr.close();
                is.close();
            } catch (IOException e) {
                LOG.error("", e);
            }
        }
    }

    public static String getContentType(String ext) {
        if (StringUtils.isNotBlank(ext)) {
            if (EXT_MAP.containsKey(ext.toLowerCase())) {
                return EXT_MAP.get(ext.toLowerCase());
            }
        }
        return "unkown";
    }

    public static String getExtendFileName(String contentType) {
        if (StringUtils.isNotBlank(contentType)) {
            if (TYPE_MAP.containsKey(contentType.toLowerCase())) {
                return TYPE_MAP.get(contentType.toLowerCase());
            }
        }
        return ".unkown";
    }

    public static boolean isHtml(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("text/html");
    }

    public static boolean isImage(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("image/");
    }

    public static boolean isAudio(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("audio/");
    }

    public static boolean isVideo(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("video/") || contentType.startsWith("application/x-shockwave-flash");
    }

    public static boolean isTxt(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("text/plain");
    }

    public static boolean isWord(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/msword") || contentType.startsWith("application/vnd.ms-word")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml");
    }

    public static boolean isExcel(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/vnd.ms-excel")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument.spreadsheetml");
    }

    public static boolean isPpt(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/mspowerpoint")
                || contentType.startsWith("application/vnd.ms-powerpoint")
                || contentType.startsWith("application/ms-powerpoint") || contentType.startsWith("application/x-ppt")
                || contentType.startsWith("application/vnd.openxmlformats-officedocument.presentationml");
    }

    public static boolean isPdf(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/pdf");
    }

    public static boolean isCompress(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/zip") || contentType.startsWith("application/x-rar-compressed")
                || contentType.startsWith("application/x-7z-compressed") || contentType.startsWith("application/x-tar")
                || contentType.startsWith("application/x-gzip") || contentType.startsWith("application/x-bzip2");
    }

    public static boolean isApk(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/vnd.android");
    }

    public static boolean isIpa(String contentType) {
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        return contentType.startsWith("application/vnd.iphone");
    }
}
