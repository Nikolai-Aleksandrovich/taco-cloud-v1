package tacos.email;

import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tacos.email.domain.ApiProperties;
import tacos.email.domain.Order;

import java.util.Map;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 17:49
 */
@Component
public class OrderSubmitMessageHandler implements GenericHandler<Order> {
    private RestTemplate restTemplate;
    private ApiProperties apiProperties;

    public OrderSubmitMessageHandler(RestTemplate restTemplate,ApiProperties apiProperties){
        this.apiProperties=apiProperties;
        this.restTemplate = restTemplate;
    }
    @Override
    public Object handle(Order order, Map<String,Object> headers) {
        restTemplate.postForObject(apiProperties.getUrl(),order,String.class);
        return null;
    }
}
