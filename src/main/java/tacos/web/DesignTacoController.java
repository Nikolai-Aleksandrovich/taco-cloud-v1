package tacos.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import tacos.data.JPATacoRepository;
import tacos.domain.Ingredient;
import tacos.domain.Ingredient.Type;
import tacos.domain.Order;
import tacos.domain.Taco;
import tacos.data.IngredientRepository;
import tacos.data.TacoRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yuyuan Huang
 * @create 2021-01-13 17:09
 */
@Slf4j//lombok产出简单的日志
@Controller
@RequestMapping("/design")
@SessionAttributes("order")
public class DesignTacoController {
    @ModelAttribute(name="order")
    public Order order(){
        return new Order();
    }
    @ModelAttribute(name = "taco")
    public Taco taco(){
        return new Taco();
    }
    private final IngredientRepository ingredientRepo;
    private JPATacoRepository designRepo;
    @Autowired
    public DesignTacoController(IngredientRepository ingredientRepo,JPATacoRepository tacoRepository) {
        this.ingredientRepo = ingredientRepo;
        this.designRepo=tacoRepository;
    }
    @GetMapping

    public String showDesignForm(Model model){
        //新建一个以ingredient为元素的list
//        List<Ingredient> ingredients = Arrays.asList(
//                new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
//                new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
//                new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
//                new Ingredient("CARN", "Carnitas", Type.PROTEIN),
//                new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGIES),
//                new Ingredient("LETC", "Lettuce", Type.VEGGIES),
//                new Ingredient("CHED", "Cheddar", Type.CHEESE),
//                new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
//                new Ingredient("SLSA", "Salsa", Type.SAUCE),
//                new Ingredient("SRCR", "Sour Cream", Type.SAUCE)
//        );
//        Type[] types = Ingredient.Type.values();
//        for(Type type:types){
//            model.addAttribute(type.toString().toLowerCase(),
//                    filterByType(ingredients,type));
//        }
//        model.addAttribute("design",new Taco());
//        return "design";
        List<Ingredient> ingredients = new ArrayList<>();
        ingredientRepo.findAll().forEach(ingredients::add);

        Type[] types = Ingredient.Type.values();
        for(Type type:types){
            model.addAttribute(type.toString().toLowerCase(),filterByType(ingredients,type));
        }
//    model.addAttribute("design", new Taco());
        return "design";

    }
    @PostMapping
    public String processDesign(@Valid Taco design, Errors errors, @ModelAttribute Order order){
        //validate this data,校验时机在绑定表单数据后，调用processDesign之前
        if(errors.hasErrors()){
            return "design";
        }
        Taco saved = designRepo.save(design);
        order.addDesign(saved);
        return "redirect:/orders/current";
    }
    @PostMapping(consumes = "application/json")//表示该方法只会处理Content-type与application/json相匹配的请求
    @ResponseStatus(HttpStatus.CREATED)
    //本来所有的响应码都是200OK，但在这里转换成201CREATED，表示不仅OK而且还创建了资源
    public Taco postTaco(@RequestBody Taco taco){
        //方法参数@RequestBody表明请求JSON转换为一个Taco对象并绑定到这个参数上，如果没有@RequestBody，Spring MVC会认为我们希望把请求参数绑定到Taco上
        //PostTaco接收到对象，就用save方法存储
        return designRepo.save(taco);
    }
    private List<Ingredient> filterByType(
            List<Ingredient> ingredients, Type type) {
        return ingredients
                .stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }

}
