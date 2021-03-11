package tacos.kitchen.rabbit;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import tacos.domain.Order;
import tacos.kitchen.OrderMessagingService;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 12:13
 */
public class RabbitOrderMessagingService implements OrderMessagingService {
    private RabbitTemplate rabbitTemplate;
    @Autowired
    public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
//    @Override
//    public void sendOrder(Order order) {
//        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
//        MessageProperties messageProperties = new MessageProperties();
//        Message message = messageConverter.toMessage(order,messageProperties);
//        rabbitTemplate.send("tacocloud.order",message);
//        //交换键省略，路由键提供
//    }
    @Override
    public void sengOrder(Order order){
        rabbitTemplate.convertAndSend("tacocloud.order", order, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setHeader("X_ORDER_SOURCE", "WEB");
                return message;
            }
        });
    }
}
