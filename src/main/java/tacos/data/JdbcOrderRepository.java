package tacos.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import tacos.domain.Order;
import tacos.domain.Taco;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yuyuan Huang
 * @create 2021-01-23 17:21
 */
public class JdbcOrderRepository implements OrderRepository{
    //SimpleJdbcInsert是对PSC的方便使用取代
    //使用SimpleJdbcInsert插入数据
    private SimpleJdbcInsert orderInsert; //把值插入TacoOrder
    private SimpleJdbcInsert orderTacoInsert;//把值插入Taco——Order——Taco表
    private ObjectMapper objectMapper;

    @Autowired
    public JdbcOrderRepository(JdbcTemplate jdbc){
        this.orderInsert=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order").usingGeneratedKeyColumns("id");
        //SimpleJDBCInsert有两个方法execute executeAndReturn非常有用，接受一个Map(String,Object)作为参数，String作为数据库的列，Object作为数据库的值
        this.orderTacoInsert=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order_Tacos");
        this.objectMapper=new ObjectMapper();
        //使用Jackson的ObjectMapper作为将order转为mapper的convertValue方法
    }
    @Override
    public Order save(Order order) {
        order.setPlacedAt(new Date());
        long orderId = saveOrderDetails(order);
        order.setId(orderId);
        List<Taco> tacos = order.getTacos();
        for (Taco taco:tacos){
            saveTacoToOrder(taco,orderId);
        }
        return order;
    }
    private long saveOrderDetails(Order order){
        Map<String,Object> values = objectMapper.convertValue(order,Map.class);
        values.put("placedAt",order.getPlacedAt());//因为convertValue将date属性转为long，而导致属性不兼容，所以在转完要换一下placedAt的属性
        long orderId = orderInsert.executeAndReturnKey(values).longValue();
        return orderId;
    }
    private void saveTacoToOrder(Taco taco,long orderId){
        Map<String,Object> values = new HashMap<>();
        values.put("tacoOrder",orderId);
        values.put("taco",taco.getId());
        orderTacoInsert.execute(values);


    }

//    private SimpleJdbcInsert orderInserter;
//    private SimpleJdbcInsert orderTacoInserter;
//    private ObjectMapper objectMapper;
//    @Autowired
//    public JdbcOrderRepository(JdbcTemplate jdbc){
//        this.orderInserter=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order").usingGeneratedKeyColumns("id");
//        this.orderTacoInserter=new SimpleJdbcInsert(jdbc).withTableName("Taco_Order_Tacos");
//        this.objectMapper=new ObjectMapper();
//    }
//    @Override
//    public Order save(Order order) {
//        order.setPlacedAt(new Date());
//        long orderId = saveOrderDerails(order);
//        order.setId(orderId);
//        List<Taco> tacos = order.getTacos();
//        for(Taco taco:tacos){
//            saveTacoToOrder(taco,orderId);
//        }
//        return order;
//    }
//    private long saveOrderDerails(Order order){
//        @SuppressWarnings("unchecked")
//        Map<String,Object> values = objectMapper.convertValue(order,Map.class);
//        values.put("placedAt",order.getPlacedAt());
//        return orderInserter.executeAndReturnKey(values).longValue();
//    }
//    private void saveTacoToOrder(Taco taco,long orderId){
//        Map<String,Object> values=new HashMap<>();
//        values.put("tacoOrder",orderId);
//        values.put("taco",taco.getId());
//        orderTacoInserter.execute(values);
//    }
}
