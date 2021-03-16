package sia5.serviceActivator;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.handler.GenericHandler;
import tacos.data.JPAOrderRepository;
import tacos.data.OrderRepository;
import tacos.domain.Order;

import javax.websocket.MessageHandler;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 15:02
 */


public class TacoServiceActivator {
    @Bean
    @ServiceActivator(inputChannel = "someChannel")
    //@ServiceActivator 注解 bean，将其指定为一个服务激活器
    public MessageHandler sysOutHandler(){
        return message -> {
            System.out.println("Message payload:" + message.getPayload());
            //给定的消息时，它发出其有效载荷的标准输出流
        };
    }
    @Bean
    @ServiceActivator(inputChannel = "orderChannel",outputChannel = "completeOrder")
    public GenericHandler<Order> orderHandler(JPAOrderRepository orderRepository){
        return (payload,headers) -> {
            return orderRepository.save(payload);
            //返回一个新的有效载荷之前处理传入的消息
        };
    }
}
