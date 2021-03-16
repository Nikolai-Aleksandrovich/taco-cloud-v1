package tacos.web.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import tacos.domain.Ingredient;

/**
 * @author Yuyuan Huang
 * @create 2021-03-08 15:19
 */
public class IngredientResource extends RepresentationModel<IngredientResource> {

    @Getter
    private String name;

    @Getter
    private Ingredient.Type type;

    public IngredientResource(Ingredient ingredient){
        this.name=ingredient.getName();
        this.type=ingredient.getType();
    }
}
