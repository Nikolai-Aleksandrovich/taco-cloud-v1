package tacos.kitchen.kafka.listener;


import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import tacos.domain.Order;
import tacos.kitchen.KitchenUI;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 17:18
 */
@Component
@Slf4j
public class OrderListener {
    private KitchenUI ui;
    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    }
    @KafkaListener(topics = "tacocloud.orders.topic")
    public void handle(Order order, Message<Order> message){
        MessageHeaders headers = message.getHeaders();
        log.info("Received from partition {} with timestamp {}",headers.get(KafkaHeaders.RECEIVED_PARTITION_ID),headers.get(KafkaHeaders.RECEIVED_TIMESTAMP));
        ui.displayOrder(order);
    }
}
