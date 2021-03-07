package tacos.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Yuyuan Huang
 * @create 2021-01-13 17:02
 */
@Data
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE,force = true)
@Entity
public class Ingredient {
    @Id //指示id为数据库中唯一标识的标签
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }

}