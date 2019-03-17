package per.qy.crawler.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import per.qy.crawler.dao.UrlTaskDao;
import per.qy.crawler.entity.UrlTask;
import per.qy.crawler.entity.WebsiteTask;
import per.qy.crawler.filter.UrlDuplicateFilter;
import per.qy.crawler.kafka.TaskProducer;
import per.qy.crawler.model.WebPageModel;
import per.qy.crawler.util.PageDownloadUtil;
import per.qy.crawler.service.WebsiteTaskService;
import per.qy.crawler.util.CrawlerUtil;
import per.qy.crawler.util.UrlExtract;

import java.util.Set;

public class CrawlerTask implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CrawlerTask.class);
    private UrlTask task;
    private WebsiteTaskService websiteTaskService;
    private UrlTaskDao urlTaskDao;
    private TaskProducer taskProducer;

    public CrawlerTask(UrlTask task, WebsiteTaskService websiteTaskService, UrlTaskDao urlTaskDao,
                       TaskProducer taskProducer) {
        this.task = task;
        this.websiteTaskService = websiteTaskService;
        this.urlTaskDao = urlTaskDao;
        this.taskProducer = taskProducer;
    }

    @Override
    public void run() {
        long millis = System.currentTimeMillis();
        WebsiteTask website = websiteTaskService.getWebsiteTask(task.getRootId());
        //爬取链接内容
        WebPageModel page = PageDownloadUtil.executeGet(task.getUrl(), website.getRanges());
        task.setContentLength(page.contentLength);
        task.setContentType(page.contentType);
        task.setRespCode(page.respCode);
        task.setRemark(page.message);
        task.setFilePath(page.filePath);
        task.setUseMillis(System.currentTimeMillis() - millis);
        urlTaskDao.saveAndFlush(task);

        if (task.getLevel() < website.getMaxLevel()) {
            Set<String> childUrls = new UrlExtract(page.document, task.getUrl()).extractFromA().extractFromFrame()
                    .extractFromIframe().extractFromImg().getUrls();
            if (!childUrls.isEmpty()) {
                UrlDuplicateFilter dupFilter = websiteTaskService.getUrlDuplicateFilter(task.getRootId());
                int addCount = 0;
                for (String childUrl : childUrls) {
                    if (CrawlerUtil.isOuterUrl(task.getUrl(), childUrl) && task.getLevel() >= website.getOuterLevel()) {
                        continue;
                    }
                    //提取出的子链接去重
                    if (dupFilter.filter(childUrl)) {
                        UrlTask childTask = new UrlTask();
                        childTask.setUrl(childUrl);
                        childTask.setParentId(task.getId());
                        childTask.setRootId(task.getRootId());
                        childTask.setLevel(task.getLevel() + 1);
                        taskProducer.sendUrlTask(childTask);
                        addCount++;
                    }
                }
                //任务数更新
                websiteTaskService.addTaskCount(task.getRootId(), addCount);
            }
        }
        //完成任务数更新
        websiteTaskService.addFinishCount(task.getRootId());
        LOG.info(String.format("爬取用时=%s，url=%s", System.currentTimeMillis() - millis, task.getUrl()));
    }
}
