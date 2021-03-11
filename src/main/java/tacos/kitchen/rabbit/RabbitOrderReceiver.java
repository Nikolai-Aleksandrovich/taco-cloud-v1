package tacos.kitchen.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import tacos.domain.Order;

import java.lang.reflect.Type;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 16:04
 */
@Component
public class RabbitOrderReceiver {
    private RabbitTemplate rabbitTemplate;
    private MessageConverter messageConverter;
    @Autowired
    public RabbitOrderReceiver(RabbitTemplate rabbitTemplate,MessageConverter messageConverter){
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    public Order receiveOrder(){
        Message message = rabbitTemplate.receive("tacocloud.order.queue",30000);
        return message!=null? (Order)messageConverter.fromMessage(message) : null;
    }
//    public Order receiveOrder(){
//        return (Order) rabbitTemplate.receiveAndConvert("tacocloud.order.queue");
//    }
//    public Order receiveOrder(){
//        return rabbitTemplate.receiveAndConvert("tacocloud.order.queue", new ParameterizedTypeReference<Order>() {
//            @Override
//            public Type getType() {
//                return super.getType();
//            }
//        });
//    }

}
