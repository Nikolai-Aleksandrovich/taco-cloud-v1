package tacos.kitchen.jms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import tacos.domain.Order;
import tacos.kitchen.OrderMessagingService;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 0:14
 */
@Service
public class JmsOrderMessingService implements OrderMessagingService {
    private JmsTemplate jmsTemplate;
    private Destination destination;
    @Autowired
    public JmsOrderMessingService(JmsTemplate jmsTemplate,Destination destination){
        this.jmsTemplate=jmsTemplate;
        this.destination=destination;
    }
    @GetMapping("/convertAndSend/order")
    public String convertAndSendOrder(){
        Order order = buildOrder();
        jmsTemplate.convertAndSend("tacoCloud.order.queue",order,this::addOrderSource);
        return "Convert and sent order";
    }
    private Message addOrderSource(Message message) throws JMSException{
        message.setStringProperty("X_ORDER_SOURCE","WEB");
        return message;
    }
//    @Override
//    public void sendOrder(Order order){
//        //这里没有指明目的地，所以可以在.yml文件设置属性
//        jmsTemplate.send(destination, session -> session.createObjectMessage((Serializable) order));
//    }

    @Override
    public void sendOrder(Order order){
        jmsTemplate.convertAndSend("TacoCloud.order.queue", order, message -> {
            message.setStringProperty("X_ORDER_SOURCE","WEB");
            return message;
        });
        //对象自动转换为消息
    }
}
