package sia5.gateway;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.stereotype.Component;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 15:42
 */
@Component
@MessagingGateway(defaultRequestChannel = "inChannel",defaultReplyChannel = "outChannel")
//没有必要实现这个接口,Spring Integration 自动提供运行时实现，这个实现会使用特定的通道进行数据的发送与接收。
public interface UpperCaseGateway {
    String uppercase(String in);
    //当 uppercase() 被调用时，给定的 String 被发布到名为 inChannel 的集成流通道中。而且，不管流是如何定义的或是它是做什么的，在当数据到达名为 outChannel 通道时，它从 uppercase() 方法中返回。
}
