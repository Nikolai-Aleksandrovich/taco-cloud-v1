package tacos.web;

/**
 * @author Yuyuan Huang
 * @create 2021-01-22 20:02
 */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import tacos.domain.Ingredient;
import tacos.data.IngredientRepository;
import tacos.data.JPAIngredientRepository;

@Component
public class IngredientByIdConverter implements Converter<String, Ingredient> {
    private JPAIngredientRepository JIR;
    private IngredientRepository ingredientRepo;

    @Autowired
    public IngredientByIdConverter(IngredientRepository ingredientRepo) {
        this.ingredientRepo = ingredientRepo;
    }
    @Autowired
    public IngredientByIdConverter(JPAIngredientRepository jpaIngredientRepository){
        this.JIR = jpaIngredientRepository;
    }
    @Override
    public Ingredient convert(String id) {
        return ingredientRepo.findOne(id);
    }

}
