package per.qy.crawler.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

import per.qy.crawler.entity.UrlTask;
import per.qy.crawler.util.GsonUtil;

@Component
public class TaskProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.consumer.crawler_topic}")
    private String taskTopic;

    public void sendUrlTask(UrlTask task) {
        String data = GsonUtil.toJson(task);
        ListenableFuture<SendResult<String, String>> listenableFuture = kafkaTemplate
                .send(taskTopic, data);
        // 发送成功回调
        SuccessCallback<SendResult<String, String>> successCallback =
				new SuccessCallback<SendResult<String, String>>() {
            @Override
            public void onSuccess(SendResult<String, String> result) {
            }
        };
        // 发送失败回调
        FailureCallback failureCallback = new FailureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                // 失败业务逻辑
                throw new RuntimeException(ex);
            }
        };
        listenableFuture.addCallback(successCallback, failureCallback);
    }
}
