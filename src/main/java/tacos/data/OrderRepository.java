package tacos.data;

import tacos.Order;

/**
 * @author Yuyuan Huang
 * @create 2021-01-22 16:36
 */
public interface OrderRepository {
    Order save(Order order);
}
