package tacos.data;

import tacos.domain.Ingredient;

/**
 * @author Yuyuan Huang
 * @create 2021-01-21 22:52
 */
public interface IngredientRepository {
    //使用JDBC时，要在接口显式的定义方法
    Iterable<Ingredient> findAll();
    Ingredient findOne(String id);
    Ingredient save(Ingredient ingredient);
    //但是使用SPRING Data，可以扩展Crud Repository接口

}
