package tacos.data;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import tacos.Order;

import java.util.Date;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-01-22 16:36
 */
//public interface OrderRepository {
//    Order save(Order order);
//}

public interface OrderRepository extends CrudRepository<Order,Long>{
    //除了CrudRepository提供的基本CRUD外，套添加自定义的方法：
    List<Order> findByDeliveryZip(String deliveryZip);
    List<Order> readOrderByDeliveryZipAndPlacedAtBetween(String deliveryZip, Date startDate,Date endDate);
    List<Order> findByDeliveryToAndDeliveryCityAllIgnoresCase(String deliveryTo,String deliveryCity);
    List<Order> findByDeliveryCityOrderByDeliveryTo(String city);
    @Query("Order o where o.deliveryCity='Seattle'")
    List<Order> readOrdersDeliveredInSeattle();


}