package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.Ingredient;

public interface JPAIngredientRepository extends CrudRepository<Ingredient, String> {

}
