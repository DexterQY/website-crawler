package per.qy.crawler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class CrawlerExecutorConfig {

    @Value("${executor.crawler.core_pool_size}")
    private int corePoolSize;
    @Value("${executor.crawler.keep_alive_seconds}")
    private int keepAliveSeconds;
    @Value("${executor.crawler.queue_capacity}")
    private int queueCapacity;

    @Bean
    public ThreadPoolTaskExecutor crawlerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 配置核心线程数
        executor.setCorePoolSize(corePoolSize);
        // 配置最大线程数
        executor.setMaxPoolSize(corePoolSize);
        // 配置允许的空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        // 配置队列大小
        executor.setQueueCapacity(queueCapacity);
        // 执行初始化
        executor.initialize();
        return executor;
    }
}
