package tacos.web.api;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.transaction.support.ResourceHolderSupport;
import tacos.domain.Ingredient;
import tacos.domain.Taco;

import java.util.Date;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-08 10:37
 */
@Relation(value = "taco",collectionRelation = "tacos")
public class TacoResource extends RepresentationModel<TacoResource> {
    private static final IngredientResourceAssembler ingredientResourceAssembler = new IngredientResourceAssembler();
    @Getter
    private final String name;

    @Getter
    private final Date createdAt;

    @Getter
    private final CollectionModel<IngredientResource> ingredients;

    public TacoResource(Taco taco){
        this.name = taco.getName();
        this.createdAt = taco.getCreatedAt();
        this.ingredients = ingredientResourceAssembler.toCollectionModel(taco.getIngredients());
    }
}
