package tacos.email.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 16:44
 */
@Data
@Component
@ConfigurationProperties(prefix = "tacocloud.email")
public class EmailProperties {
    private String username;
    private String password;
    private String host;
    private String mailbox;
    private long pollRate = 30000;

    public String getImapUrl(){
        return String.format("imaps://%s:%s%s/%s",this.username,this.password,this.host,this.mailbox);
        //使用 get() 方法来产生一个 IMAP URL。流就使用这个 URL 连接到 Taco Cloud 的电子邮件服务器，然后轮询电子邮件。所捕获的属性中包括，用户名、密码、IMAP服务器的主机名、轮询的邮箱和该邮箱被轮询频率
    }
}
