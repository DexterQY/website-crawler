package per.qy.crawler.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebsiteStateUpdateJob {

    @Autowired
    private WebsiteTaskService websiteTaskService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void execute(){
        websiteTaskService.updateFinishState();
    }
}
