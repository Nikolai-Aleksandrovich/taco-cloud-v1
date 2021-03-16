package sia5.onJava;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @author Yuyuan Huang
 * @create 2021-03-11 18:27
 */
@MessagingGateway(defaultRequestChannel = "textInChannel")
//由 @MessagingGateway 注解,Spring Integration 在运行时生成这个接口的实现
//@MessagingGateway 的 defaultRequestChannel 属性表示，对接口方法的调用产生的任何消息都应该发送到textInChannel的消息通道。
public interface FileWriterGateway {
    void writeToFile(
            @Header(FileHeaders.FILENAME) String filename,String data
            //接受一个文件名作为字符串，另一个字符串包含应该写入文件的文本。
            //@Header 注解指示传递给 filename 的值应该放在消息头中（指定为 FileHeaders），解析为 file_name 的文件名，而不是在消息有效负载中。另一方面，数据参数值则包含在消息有效负载中。
    );
}
