package tacos.email.domain;

import lombok.Data;
import tacos.domain.Taco;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-12 17:31
 */
@Data
public class Order {
    private final String email;
    private List<Taco> tacos = new ArrayList<>();
    //这个order类只携带了电子邮件
    public void addTaco(Taco taco){
        this.tacos.add(taco);
    }
}
