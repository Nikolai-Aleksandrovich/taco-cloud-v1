package sia5.channelAdapter;

import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;

import java.io.File;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 16:11
 */
public class onDSL {
    @Bean
    public IntegrationFlow fileReaderFlow(){
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(INPUT_DIR))
                .patternFilter(FILE_PATTERN))
                .get();
    }
}
