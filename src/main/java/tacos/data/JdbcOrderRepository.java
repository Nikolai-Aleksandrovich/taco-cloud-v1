package tacos.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import tacos.Order;
import tacos.Taco;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Yuyuan Huang
 * @create 2021-01-23 17:21
 */
public class JdbcOrderRepository implements OrderRepository{
    private SimpleJdbcInsert orderInsert;
    private SimpleJdbcInsert orderTacoInserter;
    private ObjectMapper objectMapper;
    @Autowired
    public JdbcOrderRepository(JdbcTemplate jdbc){
        this.orderInsert=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order").usingGeneratedKeyColumns("id");
        this.orderTacoInserter=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order_Tacos");
        this.objectMapper=new ObjectMapper();
    }
    @Override
    public Order save(Order order) {
        order.setPlacedAt(new Date());
        long orderId = saveOrderDerails(order);
        order.setId(orderId);
        List<Taco> tacos = order.getTacos();
        for(Taco taco:tacos){
            saveTacoToOrder(taco,orderId);
        }
        return order;
    }
    private long saveOrderDerails(Order order){
        @SuppressWarnings("unchecked")
        Map<String,Object> values = objectMapper.convertValue(order,Map.class);
        values.put("placedAt",order.getPlacedAt());
        long orderId = orderInserter.
    }
}
