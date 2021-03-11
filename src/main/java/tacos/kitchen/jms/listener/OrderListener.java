package tacos.kitchen.jms.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import tacos.domain.Order;
import tacos.kitchen.KitchenUI;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 10:44
 */
@Component
public class OrderListener {
    private KitchenUI ui;

    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui = ui;
    }
    @JmsListener(destination = "tacocloud.order.queue")
    public void receiverOrder(Order order){
        //receiveOrder() 方法由 JmsListener 注解，以监听 tacocloud.order.queue 目的地的消息。它不处理 JmsTemplate，也不被应用程序代码显式地调用。相反，Spring 中的框架代码将等待消息到达指定的目的地，当消息到达时，receiveOrder() 方法将自动调用，并将消息的 Order 有效负载作为参数
        ui.displayOrder(order);
    }
}
