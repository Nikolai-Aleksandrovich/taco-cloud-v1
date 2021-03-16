package sia5.channelAdapter;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.messaging.support.GenericMessage;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 15:53
 */
public class onJava {
//    @Bean
//    @InboundChannelAdapter(
//            poller = @Poller(fixedRate = "1000"),channel = "numberChannel"
//    )
//    //每 1 秒（1000 ms）从注入的 AtomicInteger 提交一个数字到 numberChannel 的通道中
//    //入站信道适配器
//    public MessageSource<Integer> numberSource(AtomicInteger source){
//
//        return () -> {
//            return new GenericMessage<>(source.getAndIncrement());
//        };
//    }
    @Bean
    @InboundChannelAdapter(channel = "file-channel",poller = @Poller(fixedDelay = "1000"))
    public MessageSource<File> fileReadingMessageSource(){
        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
        sourceReader.setDirectory(new File(INPUT_DIR));
        sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
        return sourceReader;
    }
}
