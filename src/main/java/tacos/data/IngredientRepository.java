package tacos.data;

import tacos.Ingredient;

/**
 * @author Yuyuan Huang
 * @create 2021-01-21 22:52
 */
public interface IngredientRepository {
    Iterable<Ingredient> findAll();
    Ingredient findById(String id);
    Ingredient save(Ingredient ingredient);
}
