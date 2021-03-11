package tacos.kitchen.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import tacos.domain.Order;
import tacos.kitchen.OrderReceiver;

import javax.jms.JMSException;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 9:40
 */
@Service
public class JmsOrderReceiver implements OrderReceiver {
    private JmsTemplate jmsTemplate;
    @Autowired
    public JmsOrderReceiver(JmsTemplate jmsTemplate){
        this.jmsTemplate=jmsTemplate;
    }

    public Order receiverOrder() throws  JMSException{
        return (Order)jmsTemplate.receiveAndConvert("tacoloud.order.queue");
    }

}
//@Service
//public class JmsOrderReceiver implements OrderReceiver{
//    private JmsTemplate jmsTemplate;
//    private MessageConverter messageConverter;
//
//    @Autowired
//    public JmsOrderReceiver(JmsTemplate jmsTemplate,MessageConverter messageConverter){
//        this.jmsTemplate = jmsTemplate;
//        this.messageConverter = messageConverter;
//    }
//    public Order receiveOrder() throws JMSException {
//        Message message = jmsTemplate.receive("tacocloud.order.queue");
//        //String 来指定从何处拉取订单,receive() 方法返回一个未转换的 Message。接下来使用注入的消息转换器来转换消息。消息中的类型 ID 属性将指导转换器将其转换为 Order
//        return (Order) messageConverter.fromMessage(message);
//    }
//
//}
