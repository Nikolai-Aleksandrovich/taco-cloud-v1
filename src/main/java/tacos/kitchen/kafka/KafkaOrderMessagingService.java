package tacos.kitchen.kafka;

import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tacos.domain.Order;
import tacos.kitchen.OrderMessagingService;
import org.springframework.kafka.core.KafkaTemplate;
/**
 * @author Yuyuan Huang
 * @create 2021-03-11 17:09
 */
@Service
public class KafkaOrderMessagingService implements OrderMessagingService {

    private KafkaTemplate<String,Order> kafkaTemplate;

    @Autowired
    public KafkaOrderMessagingService(KafkaTemplate<String, Order> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    @Override
    public void sendOrder(Order order) {
        kafkaTemplate.send("tacocloud.orders.topic",order);
    }
}
