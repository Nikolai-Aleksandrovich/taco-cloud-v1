package tacos.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Yuyuan Huang
 * @create 2021-03-07 17:06
 */
@Component
@ConfigurationProperties(prefix = "taco.orders")
@Data
@Validated
public class OrderProps {
    @Min(value=5,message = "must between 5 and 25")
    @Max(value=25,message = "must between 5 and 25")
    private int pageSize=20;//把一些通用配置从控制器中抽取出来，放到统一的bean中
}
