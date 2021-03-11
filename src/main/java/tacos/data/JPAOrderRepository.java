package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.domain.Order;
import tacos.domain.User;

import java.awt.print.Pageable;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-06 16:32
 */
public interface JPAOrderRepository extends CrudRepository<Order,Long> {
    //指定Order实体和数据库主键的类型
    List<Order> findByDeliverZip(String deliverZip);
    List<Order> findByUserOrderByPlaceAtDesc(User user, Pageable pageable);
}
