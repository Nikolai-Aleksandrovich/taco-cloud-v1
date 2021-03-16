package tacos.email.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 20:35
 */
@Component
@Data
@ConfigurationProperties(prefix = "tacocloud.api")
public class ApiProperties {
    private String url;
}
