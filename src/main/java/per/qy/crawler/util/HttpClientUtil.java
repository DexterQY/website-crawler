package per.qy.crawler.util;

import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpClientUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpClientUtil.class);

    private static PoolingHttpClientConnectionManager createConnectionManager() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        ConnectionSocketFactory plainFactory = PlainConnectionSocketFactory.getSocketFactory();
        registryBuilder.register("http", plainFactory);

        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任任何链接
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(sslContext,
                    NoopHostnameVerifier.INSTANCE);
            registryBuilder.register("https", sslFactory);
        } catch (KeyManagementException e) {
            LOG.error("", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("", e);
        } catch (KeyStoreException e) {
            LOG.error("", e);
        }

        Registry<ConnectionSocketFactory> registry = registryBuilder.build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 最大连接数
        connectionManager.setMaxTotal(200);
        // 每个路由基础的连接
        connectionManager.setDefaultMaxPerRoute(100);
        // 可用空闲连接过期时间，重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
        connectionManager.setValidateAfterInactivity(60000);
        return connectionManager;
    }

    private static RequestConfig createRequestConfig() {
        RequestConfig requestConfig = RequestConfig.custom()
                // 设置连接超时
                .setConnectTimeout(10000)
                // 设置读取超时
                .setSocketTimeout(10000)
                // 设置从连接池获取连接实例的超时
                .setConnectionRequestTimeout(5000).setCookieSpec(CookieSpecs.STANDARD).build();
        return requestConfig;
    }

    /**
     * 创建httpclient
     */
    public static CloseableHttpClient createHttpClient() {
        return HttpClients.custom().setConnectionManager(createConnectionManager())
                .setDefaultRequestConfig(createRequestConfig()).build();
    }

    public static void setHeader(HttpRequestBase method, String url) {
        method.setHeader("Connection", "close");
        method.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 " +
                        "Safari/537.36");
        method.addHeader("Accept-Charset", "UTF-8,GBK,GB2312,GB18030,BIG5,ISO-8859-1,ASCII,UTF-16,UTF-32");
        method.addHeader("Referer", url);
    }

    public static String getOctetStreamFileExt(String url, CloseableHttpResponse response) {
        Header disposition = response.getFirstHeader("Content-Disposition");
        if (disposition == null) {
            disposition = response.getFirstHeader("Content-disposition");
        }
        if (disposition != null) {
            HeaderElement[] elements = disposition.getElements();
            if (elements.length > 0) {
                NameValuePair param = elements[0].getParameterByName("filename");
                if (param != null) {
                    String name = param.getValue();
                    if (name != null && name.contains(".")) {
                        return name.substring(name.lastIndexOf("."));
                    }
                }
            }
        } else {
            if (url.lastIndexOf("/") > 8) {
                String name = url.substring(url.lastIndexOf("/"));
                if (name.contains(".")) {
                    return name.substring(name.lastIndexOf("."));
                }
            }
        }
        return null;
    }

    public static void exportDataAsFile(byte data[], String filePath) {
        File file = new File(filePath);
        file.setExecutable(false);// 禁用可执行权限
        file.setWritable(false);// 禁用可写权限
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(data, 0, data.length);
        } catch (FileNotFoundException e) {
            LOG.error("",e);
        } catch (IOException e) {
            LOG.error("",e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOG.error("",e);
                }
            }
        }
    }

    public static void exportEntityAsFile(HttpEntity entity, String filePath) {
        File file = new File(filePath);
        file.setExecutable(false);// 禁用可执行权限
        file.setWritable(false);// 禁用可写权限
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = entity.getContent();
            if (inputStream == null) {
                file.delete();
                return;
            }
            outputStream = new FileOutputStream(file);
            byte b[] = new byte[1024];
            int j = 0;
            while ((j = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, j);
            }
        } catch (FileNotFoundException e) {
            LOG.error("",e);
        } catch (IllegalStateException e) {
            LOG.error("",e);
        } catch (IOException e) {
            LOG.error("",e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LOG.error("",e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOG.error("",e);
                }
            }
        }
    }
}
