package per.qy.crawler.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import per.qy.crawler.entity.UrlTask;

public interface UrlTaskDao extends JpaRepository<UrlTask, Integer> {
}
