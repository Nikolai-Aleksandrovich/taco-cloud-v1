package tacos.kitchen.rabbit.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListeners;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tacos.domain.Order;
import tacos.kitchen.KitchenUI;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 16:25
 */
@Component
public class OrderListener {
    private KitchenUI ui;
    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui = ui;
    }
    @RabbitListener(queues = "tacocloud.order.queue")
    public void receiveOrder(Order order){
        ui.displayOrder(order);
    }
}
