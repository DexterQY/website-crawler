package per.qy.crawler.kafka;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import per.qy.crawler.dao.UrlTaskDao;
import per.qy.crawler.entity.UrlTask;
import per.qy.crawler.service.WebsiteTaskService;
import per.qy.crawler.task.CrawlerTask;
import per.qy.crawler.util.CrawlerUtil;
import per.qy.crawler.util.GsonUtil;

@Component
@EnableKafka
public class TaskConsumerListener {

    private final static Log LOG = LogFactory.getLog(TaskConsumerListener.class);
    private static AtomicInteger count = new AtomicInteger();

    @Autowired
    private WebsiteTaskService websiteTaskService;

    @Autowired
    private UrlTaskDao urlTaskDao;

    @Autowired
    private TaskProducer taskProducer;

    @Autowired
    private ThreadPoolTaskExecutor crawlerTaskExecutor;// 爬虫线程池

    @KafkaListener(topics = "${kafka.consumer.crawler_topic}", containerFactory = "kafkaListenerContainerFactory")
    public void listenTaskScan(List<ConsumerRecord<String, String>> records, Acknowledgment ack) {
        LOG.info(String.format("本次加载任务数=%s，累计加载任务数=%s", records.size(), count.addAndGet(records.size())));
        ack.acknowledge();
        for (ConsumerRecord<String, String> record : records) {
            String json = record.value();
            UrlTask item = GsonUtil.fromJson(json, UrlTask.class);
            // 若线程池工作队列过大则暂停添加任务
            while (crawlerTaskExecutor.getThreadPoolExecutor().getQueue().size() > 500) {
                CrawlerUtil.sleepSeconds(10);
                LOG.info("爬取任务队列已满，暂停添加任务！");
            }
            CrawlerTask task = new CrawlerTask(item, websiteTaskService, urlTaskDao, taskProducer);
            crawlerTaskExecutor.execute(task);
        }
    }
}
