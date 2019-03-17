package per.qy.crawler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import per.qy.crawler.entity.WebsiteTask;

public interface WebsiteTaskDao extends JpaRepository<WebsiteTask, Integer> {
}
