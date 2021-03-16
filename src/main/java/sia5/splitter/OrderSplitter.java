package sia5.splitter;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.router.MessageRouter;
import org.springframework.integration.router.PayloadTypeRouter;
import sia5.BillingInfo;
import sia5.LineItem;
import sia5.PurchaseOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 14:11
 */
public class OrderSplitter {
    public Collection<Object> splitOrderIntoParts(PurchaseOrder po){
        ArrayList<Object> parts = new ArrayList<>();
        //将携带购买订单的消息拆分为两条消息：一条携带账单信息，另一条携带项目列表。
        parts.add(po.getBillingInfo());
        parts.add(po.getLineItems());
        return parts;
    }
    @Bean
    @Splitter(inputChannel = "poChannel",outputChannel = "splitOrderChannel")
    //@Splitter 注解将 OrderSplitter bean 声明为集成流的一部分
    public OrderSplitter orderSplitter(){
        return new OrderSplitter();
    }
    //购买订单到达名为 poChannel 的通道，并被 OrderSplitter 分割。然后，将返回集合中的每个项作为集成流中的单独消息发布到名为 splitOrderChannel 的通道。
    @Bean
    @Router(inputChannel = "splitOrderChannel")
    public MessageRouter splitOrderRouter(){
        PayloadTypeRouter router = new PayloadTypeRouter();
        //将消息路由到不同的通道
        router.setChannelMapping(
                BillingInfo.class.getName(),"billingInfoChannel"
        );
        //将有效负载为类型为 BillingInfo 的消息路由到一个名为 billingInfoChannel 的通道进行进一步处理。
        router.setChannelMapping(List.class.getName(),"lineItemChannel");
        //将 List 类型的有效负载映射到名为 lineItemsChannel 的通道中。
        return router;
    }
    @Splitter(inputChannel = "lineItemsChannel",outputChannel = "lineItemChannel")
    public List<LineItem> lineItemSplitter(List<LineItem> lineItems){
        return lineItems;
        //当携带 List<LineItem> 的有效负载的消息到达名为 lineItemsChannel 的通道时，它将传递到 lineItemSplitter() 方法。
        //集合中的每个 LineItem 都以其自己的消息形式发布到名为 lineItemChannel 的通道。
    }
}
