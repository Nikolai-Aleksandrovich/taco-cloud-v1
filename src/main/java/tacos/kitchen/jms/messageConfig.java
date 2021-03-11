package tacos.kitchen.jms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.jms.support.converter.MappingJackson2MessageConverter;

import tacos.domain.Order;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 1:40
 */
@Configuration
public class messageConfig {
    @Bean
    public org.springframework.jms.support.converter.MappingJackson2MessageConverter messageConverter(){
        //这样会让接收者知道他要接受什么类型
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTypeIdPropertyName("_typeId");
        Map<String,Class<?>> typeIdMappings = new HashMap<String,Class<?>>();
        typeIdMappings.put("order", Order.class);
        messageConverter.setTypeIdMappings(typeIdMappings);
        return messageConverter;
    }
}
