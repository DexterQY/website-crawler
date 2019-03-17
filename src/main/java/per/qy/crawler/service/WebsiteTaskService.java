package per.qy.crawler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import per.qy.crawler.dao.WebsiteTaskDao;
import per.qy.crawler.entity.WebsiteTask;
import per.qy.crawler.filter.UrlDuplicateFilter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebsiteTaskService {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteTaskService.class);
    private Map<Integer, UrlDuplicateFilter> dupFilterMap = new HashMap<>();
    private Map<Integer, WebsiteTask> websiteTaskMap = new HashMap<>();
    private static final Object LOCK = new Object();

    @Autowired
    private WebsiteTaskDao websiteTaskDao;

    public void put(WebsiteTask item) {
        websiteTaskDao.saveAndFlush(item);
        dupFilterMap.put(item.getId(), new UrlDuplicateFilter(item.getMaxCount()));
        item.setRanges(Arrays.asList(item.getRange().split(",")));
        websiteTaskMap.put(item.getId(), item);
        LOG.info("添加爬虫任务，website=" + item.getUrl());
    }

    public UrlDuplicateFilter getUrlDuplicateFilter(int id) {
        return dupFilterMap.get(id);
    }

    public WebsiteTask getWebsiteTask(int id) {
        return websiteTaskMap.get(id);
    }

    public void addTaskCount(int id, int addCount) {
        WebsiteTask item = websiteTaskMap.get(id);
        synchronized (LOCK) {
            item.setTaskCount(item.getTaskCount() + addCount);
        }
    }

    public void addFinishCount(int id) {
        WebsiteTask item = websiteTaskMap.get(id);
        synchronized (LOCK) {
            item.setFinishCount(item.getFinishCount() + 1);
        }
    }

    public void updateFinishState() {
        for (WebsiteTask website : websiteTaskMap.values()) {
            if (website.getTaskCount() == website.getFinishCount()) {
                website.setState(2);
                website.setFinishTime(new Date());
                websiteTaskDao.save(website);
                websiteTaskMap.remove(website.getId());
                dupFilterMap.remove(website.getId());
                LOG.info(String.format("爬取完成，链接总数=%s，website=%s", website.getFinishCount(), website.getUrl()));
            }
        }
    }
}
