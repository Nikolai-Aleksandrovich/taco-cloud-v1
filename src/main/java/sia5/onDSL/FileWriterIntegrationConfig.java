package sia5.onDSL;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Router;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.router.AbstractMappingMessageRouter;
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
 * @create 2021-03-11 21:52
 */
@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    public IntegrationFlow fileWriterFlow(){
        //捕获整个流,IntegrationFlows 类初始化Builder API
        return IntegrationFlows
                .from(MessageChannels.direct("textInChannel"))
                //textInChannel自动创建并加入容器
                //从名为 textInChannel 的通道接收消息
                .<String,String>transform(t -> t.toUpperCase())
                //转到一个转换器，使消息有效负载大写
                .channel(MessageChannels.direct("fileWriterChannel"))//可以不配置，如果要配置，如此即可
                .handle(Files.outboundAdapter(new File("/tmp/sia5/files"))
                .fileExistsMode(FileExistsMode.APPEND)
                .appendNewLine(true))
                //消息由出站通道适配器处理，该适配器是根据 Spring Integration 的文件模块中提供的文件类型创建的。
                .get();
        //get() 构建要返回的 IntegrationFlow
    }



}
