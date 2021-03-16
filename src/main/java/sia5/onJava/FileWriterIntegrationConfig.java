package sia5.onJava;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.router.AbstractMessageRouter;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 21:26
 */
@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    public MessageChannel textInChannel(){
        return new DirectChannel();
    }
    @Bean MessageChannel fileWriterChannel(){
        return new DirectChannel();
    }
    @Bean//转换器
    @Transformer(inputChannel = "textInChannel",outputChannel = "fileWriterChannel")
    //指定为集成流中的转换器,接收名为 textInChannel 的通道上的消息，并将消息写入名为 fileWriterChannel 的通道。
    public GenericTransformer<String,String> upperCaseTransformer(){
        return String::toUpperCase;
        //
    }
    @Bean//文件写入消息处理程序
    @ServiceActivator(inputChannel = "fileWriterChannel")
    // @ServiceActivator注解，指示它将接受来自 fileWriterChannel 的消息，并将这些消息传递给由 FileWritingMessageHandler 实例定义的服务。
    public FileWritingMessageHandler fileWriter(){
        //使用消息的 file_name 头中指定的文件名将消息有效负载写入指定目录中的文件。
        FileWritingMessageHandler handler = new FileWritingMessageHandler(new File("/tmp/sia5/files"));
        handler.setExpectReply(false);
        // setExpectReply(false) 来指示服务激活器不应该期望应答通道
        //如果不调用 setExpectReply()，则文件写入 bean 默认为 true，尽管管道仍按预期工作，但将看到记录了一些错误，说明没有配置应答通道。
        handler.setFileExistsMode(FileExistsMode.APPEND);
        handler.setAppendNewLine(true);
        return handler;
        //不需要显式地声明通道。如果不存在具有这些名称的 bean，就会自动创建 textInChannel 和 fileWriterChannel 通道。
    }
    @Bean
    @Transformer(inputChannel = "numberChannel",outputChannel = "romanNumberChannel")
    // @Transformer 注解将 bean 指定为 transformer bean
    //从名为 numberChannel 的通道接收整数值，并使用 toRoman()的静态方法进行转换，得到的结果被发布到名为 romanNumberChannel 的通道中。
    //toRoman在一个名为 RomanNumbers 的类中静态定义的，并在这里通过方法引用进行引用
    public GenericTransformer<Integer,String> romanNumTransformer(){
        return RomanNumbers::toRoman;
    }
    @Bean
    public MessageChannel evenChannel(){
        return new DirectChannel();
    }
    @Bean
    public MessageChannel oddChannel(){
        return new DirectChannel();
    }
    @Bean
    @Router(inputChannel = "numberChannel")
    public AbstractMessageRouter evenOdderRouter(){
        return new AbstractMessageRouter() {
            //AbstractMessageRouter bean 接受来自名为 numberChannel 的输入通道的消息。
            @Override
            protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
                //定义为匿名内部类的实现检查消息有效负载
                Integer number = (Integer) message.getPayload();
                if(number%2==0){
                    return Collections.singleton(oddChannel());
                    //它是偶数，则返回名为 evenChannel 的通道
                }
                return Collections.singleton(oddChannel());
                //否则，通道有效载荷中的数字必须为奇数
            }
        };
    }

}
