package tacos.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.mail.dsl.Mail;
import tacos.email.domain.EmailProperties;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 17:11
 */
@Configuration
public class TacoOrderEmailIntegrationConfig {
    //使用java DSL配置集成流
    @Bean
    public IntegrationFlow tacoOrderEmailFlow(
            EmailProperties emailProperties,
            EmailToOrderTransformer emailToOrderTransformer,
            OrderSubmitMessageHandler orderSubmitMessageHandler){
        return IntegrationFlows
                .from(
                        Mail.imapInboundAdapter(emailProperties.getImapUrl()),e -> e.poller(Pollers.fixedDelay(emailProperties.getPollRate()))
                )
                //入站通道适配器，使用IMP URL创建，根据pollrates进行轮询，传入的email给转换器
                .transform(emailToOrderTransformer)
                //把在 EmailToOrderTransformer 中实现的转换器，注入到 tacoOrderEmailFlow() 方法中。
                // 从转换中所产生的订单通过另外一个通道转到最终组件中。
                .handle(orderSubmitMessageHandler)
                //出站通道适配器，接收一个订单对象，并将其提交到 Taco Cloud 的 REST API
                .get();

    }


}
