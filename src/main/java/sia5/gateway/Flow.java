package sia5.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 15:47
 */
public class Flow {
    @Bean
    public IntegrationFlow uppercaseFlow(){
        return IntegrationFlows
                .from("inChannel")
                .<String,String>transform(s -> s.toUpperCase())
                .channel("ouyChannel")
                .get();
    }
}
