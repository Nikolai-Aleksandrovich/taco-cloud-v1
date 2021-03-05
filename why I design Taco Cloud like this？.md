# Taco Cloud Note

1、首先展示项目层级，并介绍各个包分别包含什么？

![image-20210305160844804](C:%5CUsers%5Clenovo%5CAppData%5CRoaming%5CTypora%5Ctypora-user-images%5Cimage-20210305160844804.png)

##### src/main/java包含所有的package：controller、pojo、service、config等

##### src/main/resources包含所有的静态资源、动态资源、数据库资源等



1、首先设置homepage欢迎页，homepage需要什么呢？

- 一个处理主页请求的控制器类
- 一个视图模板，定义了主页的外观

首先，设置控制器类：

```java
@Controller//表示这是一个控制类，将他加入到容器中
@RequestMapping
public class HomeController{
    @GetMapping("/")//处理的是get请求
    public String home(){
        return("home")
    }
}
```

注：像这样只转发请求，不处理其他事情的控制器，其实可以用视图控制器来做：

```java
package tacos.web;

/**
 * @author Yuyuan Huang
 * @create 2021-01-22 20:00
 */


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
    }

}
```

模板名称由逻辑视图名称派生而来，它的前缀是 /templates/，后缀是 .html。模板的结果路径是 /templates/home.html。因此，需要将模板放在项目的 /src/main/resources/templates/home.html 中。

其次，设置视图模板：

```html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
    <head>
        <title>Taco Cloud</title>
    </head>
    
    <body>
        <h1>Welcome to...</h1>
        <img th:src="@{/images/TacoCloud.png}"/>
    </body>
</html>
```

使用一个 Thymeleaf 的 th:src 属性和一个 @{…} 表达式引用具有上下文相对路径的图片。



2、如何可以让用户在浏览器定制玉米饼，填写地址，保存数据以便配送呢？

如何保证用户提交的信息符合要求呢？需要加入数据校验：

1、要在被校验的类上声明校验规则

2、要在controller上声明校验，捕捉错误返回的String也不一样

3、要在前端表单显示校验信息，不能让用户猜测错误

使用Hibernate的Validation API来进行校验（@NotNull@Size），Hibernate Validator（@NotBlank），JavaBean Validation API。

首先编写玉米饼的类：

```java
package tacos;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Ingredient {
    @Id //指示id为数据库中唯一标识的标签
    private final String id;
    private final String name;
    private final Type type;
    
    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }
}
```

要使用@Data和@RequiredArgsConstructor就必须在pom文件导入Lombok

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

创建Controller类，已处理“design”的http请求，意味着也需要一个专门负责design的html界面，既然要design Taco，那么就必须要有一个Taco类

Taco类

```java
import taco.Ingredient
@Data
public class Taco(){
    @Id//对id生成自增id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Date createdAt;
    @NotNull
    @Size(min=5, message="Name must be at least 5 characters long")
	private String name;//design中使用一个文本值来命名taco
    @ManyToMany(targetEntity = Ingredient.class)
    @Size(min=1, message="You must choose at least 1 ingredient")
	private List<Ingredient> ingredients;
    @PrePersist
    void createdAt(){
        this.createdAt=new Date();
    }
     
}
```

DesignTacoController类：

```java
package tacos.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import tacos.Ingredient;
import tacos.Ingredient.Type;
import tacos.Order;
import tacos.Taco;
import tacos.data.IngredientRepository;
import tacos.data.TacoRepository;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
/**
 * @author Yuyuan Huang
 * @create 2021-01-13 17:09
 */
@Slf4j//lombok产出简单的日志
@Controller//加载时识别为控制器，并作为bean加入容器
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
    private TacoRepository designRepo;
    @Autowired
    public DesignTacoController(IngredientRepository ingredientRepo,TacoRepository tacoRepository) {
        this.ingredientRepo = ingredientRepo;
        this.designRepo=tacoRepository;
    }
    @GetMapping
    //这里Model对象负责在控制器Controller和视图之间传递数据，放到Model上的属性会传递到servlet respond的属性中，就可以传递给视图
    public String showDesignForm(Model model){
    List<Ingredient> ingredients = new ArrayList<>();
        ingredientRepo.findAll().forEach(ingredients::add);

        Type[] types = Ingredient.Type.values();
        for(Type type:types){
            model.addAttribute(type.toString().toLowerCase(),filterByType(ingredients,type));
        }
//    model.addAttribute("design", new Taco());
        return "design";
        //return的design就是视图的名称，会将模型渲染到视图中
    }
    @PostMapping//处理表单提交
    public String processDesign(@Valid Taco design, Errors errors, @ModelAttribute Order order){
        //Spring MVC将会validate this data,校验时机在绑定表单数据后，调用processDesign之前，Java Bean Validation API的@Valid注解，如果出错，那么会捕获一个Errors对象并传递给processDesign()
        if(errors.hasErrors()){
            return "design";
        }
        Taco saved = designRepo.save(design);
        order.addDesign(saved);
        return "redirect:/orders/current";
        //当你提交taco时，会出现一个表单让你选择配送地点
    }
    private List<Ingredient> filterByType(
            List<Ingredient> ingredients, Type type) {
        return ingredients
                .stream()
                .filter(x -> x.getType().equals(type))
                .collect(Collectors.toList());
    }

}
```

控制器完成，就应该设计design视图：

spring有多种定义视图的方式，JSP，Thymeleaf，Freemaker，Groovy模板等，但我用Thymeleaf

首先加入thymeleaf的依赖：

加入后，自动配置功能会为springmvc创建支持Thymeleaf的bean，另外，Thymeleaf设计时与特定的web框架解耦的，无法与controller放到Model的数据工作，但他可以识别Servlet的request。所以，spring会先把model的数据复制到request属性中。

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

设计好的design.html

```html
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
  <head>
    <title>Taco Cloud</title>
    <link rel="stylesheet" th:href="@{/style.css}"/>
  </head>
  <body>
    <h1>Design your taco!</h1>
    <img th:src="@{/images/taco.png}"/>
    <form method="POST" th:object="${design}">
    <span class="validationError"
          th:if="${#fields.hasErrors('ingredients')}"
          th:errors="*{ingredients}">Ingredient Error</span>
    <div class="grid">
      <div class="ingredient-group" id="wraps">
      <h3>Designate your wrap:</h3>
      <div th:each="ingredient:${wrap}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      <div class="ingredient-group" id="proteins">
      <h3>Pick your protein</h3>
      <div th:each="ingredient:${protein}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      <div class="ingredient-group" id="cheeses">
      <h3>Pick your cheese</h3>
      <div th:each="ingredient:${cheese}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      <div class="ingredient-group" id="veggies">
      <h3>Pick your veggies</h3>
      <div th:each="ingredient:${veggies}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      <div class="ingredient-group" id="sauces">
      <h3>Pick your sauces</h3>
      <div th:each="ingredient:${sauce}">
        <input name="ingredients" type="checkbox" th:value="${ingredient.id}"/>
        <span th:text="${ingredient.name}">INGREDIENT</span><br/>
      </div>
      </div>
      </div>
      <div>
      <h3>Name your taco creation:</h3>
      <input type="text" th:field="*{name}"/>
      <span th:text="${#fields.hasErrors('name')}">XXX</span>
      <span class="validationError"
            th:if="${#fields.hasErrors('name')}"
            th:errors="*{name}">Name Error</span>
        <br/>
      <button>Submit your taco</button>
      </div>
    </form>
  </body>
</html>
```

设置order/current的Controller：

```java
@Slf4j
@Controller
@RequestMapping("order")
public class OrderController(){
	@GetMapping("current")
	public String orderForm(Model model){
		model.addAttribute("order",new Order());
		return "orderForm"
	}
    @PostMapping
    public String processOrder(@Valid Order order,Errors errors,SessionStatus sessionStatus){
        //这里也进行了校验
        if(errors.hasErrors()){
            return "orderForm";
        }
        orderRepo.save(order);
        sessionStatus.setComplete();
        return "redirect:/";
    }
}
```

这样需要一个Order类来表示，配送所需要的各种信息：

```java
package tacos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-01-14 15:03
 */
@Data
@Entity
@Table(name="Taco_Order")
public class Order {
    private static final long serialVersionUID=1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date placedAt;
    @NotBlank(message = "Name is required")
    private String name;
    @NotBlank(message = "Street is required")
    private String street;
    @NotBlank(message = "city is required")
    private String city;
    @NotBlank(message = "state is required")
    private String state;
    @NotBlank(message = "Zip code is required")
    private String zip;
    @CreditCardNumber(message="Not a valid credit card number")
    private String ccNumber;
    @Pattern(regexp = "^(0[1-9]|1[0-2])([\\/])([1-9][0-9])$",message = "Must be formatted MM/YY")
    private String ccExpiration;
    @Digits(integer=3,fraction = 0,message = "Invalid CVV")
    private String ccCVV;
    @ManyToMany(targetEntity=Taco.class)
    private List<Taco> tacos = new ArrayList<>();

    public void addDesign(Taco design) {
        this.tacos.add(design);
    }
    @PrePersist
    void placedAt(){
        this.placedAt=new Date();
    }

}
```

这样又需要设置关于他的orderForm表单：

```html
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Taco Cloud</title>
    <link rel="stylesheet" th:href="@{/styles.css}"/>
</head>
<body>
    <form method="POST" th:action="@{/orders}" th:object="${order}">
        <h1>Order your taco creations!</h1>

        <img th:src="@{/images/taco.png}"/>
        <a th:href="@{/design}" id="another">Design another taco</a><br/>

        <div th:if="${#fields.hasErrors()}">
            <span class="validationError">
                Please correct the problems below and resubmit
            </span>
        </div>

        <h3>Deliver my taco masterpieces to ...</h3>
        <label for="name">Name:</label>
        <input type="text" th:field="*{name}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('name')}"
              th:errors="*{name}">Name Error</span>
        <br/>

        <label for="street">Street address:</label>
        <input type="text" th:field="*{street}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('street')}"
              th:errors="*{street}">Street Error</span>
        <br/>

        <label for="city">City:</label>
        <input type="text" th:field="*{city}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('city')}"
              th:errors="*{city}">City Error</span>
        <br/>

        <label for="state">State:</label>
        <input type="text" th:field="*{state}">
        <span class="validationError"
              th:if="${#fields.hasErrors('state')}"
              th:errors="*{state}">State Error</span>
        <br/>

        <label for="zip">Zip:</label>
        <input type="text" th:field="*{zip}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('zip')}"
              th:errors="*{zip}">Zip Error</span>
        <br/>

        <h3>Here's how i'll pay...</h3>
        <label for="ccNumber">Credit Card:</label>
        <input type="text" th:field="*{ccNumber}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('ccNumber')}"
              th:errors="*{ccNumber}">CC Num Error</span>
        <br/>

        <label for="ccExpiration">Expiration:</label>
        <input type="text" th:field="*{ccExpiration}">
        <span class="validationError"
              th:if="${#fields.hasErrors('ccExpiration')}"
              th:errors="*{ccExpiration}">CC Exp Error</span>
        <br/>

        <label for="ccCVV">CVV</label>
        <input type="text" th:field="*{ccCVV}"/>
        <span class="validationError"
              th:if="${#fields.hasErrors('ccCVV')}"
              th:errors="*{ccCVV}">CC CVV Error</span>
        <br/>

        <input type="submit" value="Submit order"/>
    </form>

</body>
</html>
```

其中的

```html
<form method="POST" th:action="@{/orders}" th:object="${order}">
```

明确指定action：表单要POST提交到order上，不然就会提交到展现该表单相同的URL上，也就是"order/current"



