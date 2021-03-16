package tacos.kitchen;

import tacos.domain.Order;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 0:16
 */
public interface OrderMessagingService {
    void sendOrder(Order order);
}
