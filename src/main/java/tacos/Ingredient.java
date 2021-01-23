package tacos;

import lombok.*;

/**
 * @author Yuyuan Huang
 * @create 2021-01-13 17:02
 */
@Data
@RequiredArgsConstructor
public class Ingredient {

    private final String id;
    private final String name;
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }

}