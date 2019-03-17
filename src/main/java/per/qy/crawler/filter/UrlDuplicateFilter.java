package per.qy.crawler.filter;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class UrlDuplicateFilter {

    private final Object lock = new Object();
    private final Set<String> set = new HashSet<>();
    private final int maxCount; // 最大不相同数量

    public UrlDuplicateFilter(int maxCount) {
        this.maxCount = maxCount;
    }

    /**
     * 过滤重复url
     */
    public boolean filter(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        synchronized (lock) {
            if (reachMaxCount() || set.contains(url)) {
                return false;
            }
            set.add(url);
        }
        return true;
    }

    /**
     * 判断数量是否达到上限
     */
    private boolean reachMaxCount() {
        return set.size() >= maxCount;
    }
}
