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

#### 2、与数据库的交互使用Jdbc Template

使用JdbcTemplate前，先将starter加入POM文件中

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

在本项目中使用了H2嵌入式数据库（为什么不是用别的数据库？有几种选择？为什么使用这个数据库？）

```xml
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>
```

定义Ingredient repository，完成这样几个功能：

1、查询所有配料信息，返回一个Ingredient对象的集合中，查

2、根据id查询单个Ingredient，查

3、保存Ingredient对象，增

另外，定义TacoCloud的数据库模式，如何放置schema.sql?预加载一些配料数据，taco数据，如何放置data.sql?

放在src/main/resource/下边

如此，先定义IngredientRepository接口：

```java
package tacos.data;
import tacos.Ingredient;
public interface IngredientRepository{
	Iterable<Ingredient> findAll();
	Ingredient findOne(String id);
	Ingredient save(Ingredient ingredient);
}
```

再编写实现类：

```java
package tacos.data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tacos.Ingredient;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * @author Yuyuan Huang
 * @create 2021-01-21 22:54
 */
@Repository//添加该注解，spring组件扫描会发现这个类，并把它初始化为bean并加入到容器中，类似于构造性注解：@Controller@Component
public class JdbcIngredientRepository implement IngredientRepository{
    private JdbcTemplate jdbc;
    @Autowired//自动注入JdbcTemplate
    public JdbcIngredientRepository(JdbcTemplate jdbcTemplate){
        this.jdbc=jdbcTemplate;
    }
    @Override
    public Iterable<Ingredient> findAll() {
        return jdbc.query("select id,name,type from Ingredient",this::mapRowToIngredient);
    }
    @Override
    public Ingredient findOne(String id){
        return jdbc.queryForObject("select id,name,type from Ingredient where id = ?",this::mapRowToIngredient,id);
    }
    private Ingredient mapRowToIngredient(ResultSet rs,int rowNum)throws SQLException{
        return new Ingredient(rs.getString("id"),rs.getString("name"),Ingredient.Type.valueOf(rs.getString("type")));
    }
    @Override
    public Ingredient save(Ingredient ingredient){
        jdbc.update("insert into Ingredient (id,name,type) values(?,?,?)",
        ingredient.getId(),
        ingredient.getName(),
        ingredient.getType().toString());
        return ingredient;
    }
}

```

其实借助JdbcTemplate，有两种保存数据的方法：

1、直接使用jdbc.update方法

如何使用JdbcTemplate保存对象：

TacoRepository：

```java
package tacos.data;
import tacos.Taco;
public interface TacoRepository{
	Taco save(Taco design);
}
```

OrderRepository:

```java
package tacos.data;
import tacos.Order;
public interface OrderRepository{
	Order save(Order order);
}
```



2、使用SimpleJdbcInsert包装类（****Insert.executeAndReturn(map<String,Object>)）

#### 另外，使用SpringDataJPA：

流行的springData项目包含：

```
SpringData JPA：基于关系数据库进行JPA持久化
SpringData MongoDB：持久化到Mongo文档数据库
SpringData Neo4j：持久化到Neo4j图数据库
SpringData Redis：持久化到Redis key-value存储
SpringData Classandra：持久化到Classandra存储
```

那么就要重新加入Starter：

会引入SpringData JPA，也会顺带把Hibernate引进来作为JPA

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

如果想用别的JPA，也可以把Hibernate exclude出去，引别的比如EclipseLink进来

在创建repository方面，SpringData很方便，但代价就是需要在普通对象上添加一部分注解：

@Entity @Id

```java
@Data//本来@Data回家一个有参构造进来，但是因为调用了@No，就没有有参构造了，所以要再加一个@Re
@RequiredArgsConstructor
@NoArgsConstructor(access=AccessLevel.PRIVATE,force = true)//JPA需要一个无参构造器
//因为不想直接使用这个无参构造器，所以设置为私有的，force为true将final属性设置为null
@Entity//声明Ingredient为JPA实体
public class Ingredient {
    @Id //指示id为数据库中唯一标识该实体的属性
    private final String id;
    private final String name;
    private final Type type;

    public static enum Type {
        WRAP, PROTEIN, VEGGIES, CHEESE, SAUCE
    }

}
```

Taco类：同理也有Order类

```java
@Data
@Entity
public class Taco {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)//自动生成ID
    private Long id;
    private Date createdAt;
    @NotNull
    @Size(min=5, message="Name must be at least 5 characters long")
    private String name;
    @ManyToMany(targetEntity = Ingredient.class)//表示每一个Taco可以有多个ingredient，每一个ingredient也可以表示多个Taco
    @Size(min=1, message="You must choose at least 1 ingredient")
    private List<Ingredient> ingredients;
    @PrePersist//之前使用JDBCTemplate使，将这个创建时间设置为了当前时间，在这里可以用PrePersist
    void createdAt(){
        this.createdAt=new Date();
    }
}
```

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
    @Id//作为数据库中的id列
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
@SessionAttributes("order")//指定名为order的模型中对象Order可以保存在session中，跨请求使用。
public class DesignTacoController {
    @ModelAttribute(name="order")//此注解在Model中创建一个模型对象Order，名为order
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
        //@@ModelAttribute Order order标识Order order的值来自模型，springmvc不会将值绑定在他上
        //Spring MVC将会validate this data,校验时机在绑定表单数据后，调用processDesign之前，Java Bean Validation API的@Valid注解，如果出错，那么会捕获一个Errors对象并传递给processDesign()
        if(errors.hasErrors()){
            return "design";
        }
        Taco saved = designRepo.save(design);
        //利用注入的taco repository保存对象，将taco对象保存在session的Order里边
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



#### 启动SpringSecurity

首先加入starter：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

仅仅这个starter就实现了每次登入端口，不论哪个视图，都需要登录，用户名为user，密码为日志文件中随机生成的一串序列，但如何更人性化一点？

基于Java配置：

```java
@Configuration
@EnableWebSecurity
public class Test extends WebSecurityConfigurerAdapter {
}
```

这样会使用spring的登录页

Spring Security为配置用户存储提供了多个方案：

```
基于内存的用户存储
基于JDBC的用户存储
基于LDAP作为后端的用户存储
自定义用户详情服务
```

任何一种存储方式都需要覆盖WebSecurityConfigurerAdapter基础配置类中的configure()方法

```java
@Configuration
@EnableWebSecurity
public class Test extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception{
	
	}
}
```

基于内存的用户存储：

优缺点：方便测试和简单应用，但如果要新增，删除或变更用户，就要重新修改代码，部署

```java
@Configuration
@EnableWebSecurity
public class Test extends WebSecurityConfigurerAdapter {
    @Overridejava
    protected void configure(AuthenticationManagerBuilder auth)throws Exception{
        auth.inMemoryAuthentication()
                .withUser("buzz")
                .password("infinity")
                .authorities("ROLE_USER")
                .and()
                .withUser("woody")
                .password("bullseye")
                .authorities("ROLE_USER");

    }
}
```

基于JDBC的用户存储

```java
@Configuration
@EnableWebSecurity
public class Test extends WebSecurityConfigurerAdapter {
@Autowired//
    DataSource dataSource;
    @Override
    protected void configure(AuthenticationManagerBuilder auth)throws Exception{
        auth.jdbcAuthentication()
                .dataSource(dataSource);
                //这样的话对数据库中的table有一定的要求，比如，需要有用户名，密码，是否启用？用户权限，群组权限等
                //也可以自定义自己的查询，重写认证和基本权限的查询语句：
                .usersByUsernameQuery(
                        "select username,password,enable from Users"+"where username=?"
                ).usersByUsernameQuery(
                        "select username,authority from UserAuthorities"+"where username=?"
        ).passwordEncoder(new SCryptPasswordEncoder());
        //使用转码过的密码，因为明文存储密码会导致密码泄露
     }
	
}
```

密码转码的passwordEncoder方法可以接受PasswordEncoder接口的任意实现

```java
.passwordEncoder(new SCryptPasswordEncoder())//使用scrypt加密
.passwordEncoder(new BCryptPasswordEncoder())//使用bcrypt加密
.passwordEncoder(new Pbkdf2PasswordEncoder())//PBKDF2加密
.passwordEncoder(new NoOpPasswordEncoder())//不建议使用，无加密
.passwordEncoder(new StandardPasswordEncoder())//不建议使用，SHA-256哈希加密
```

也可以自定义加密方法，因为PassWord接口很简单：

```
public interface PassWordEncoder{
	String encode(CharSequence rawPassword)
	boolean matches(CharSequence rawPassword,String encodedPassword)
}
```

#### 以LDAP作为后端的用户存储，使用其中的ldapAuthentication方法，类似于jdbcAuthentication

```java
@Configuration
@EnableWebSecurity
public class Test extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth)throws Exception{
        auth.ldapAuthentication()
                .userSearchFilter("uid={0}")
                .groupSearchFilter("member={0}")
            //基于LDAP认证的默认策略是绑定操作，直接用LDAP服务器认证用户，另一种方式是进行比对操作，将输入的密码发送到LDAP目录上，在LDAP服务器上比对，可以保证私密
                .passwordCompare()//加入以便验证密码
                .passwordEncoder(new BCryptPasswordEncoder())//输入密码进行转码
                .passwordAttribute("passcode")//如果password不是“password”而是“passcode”之类的，可以更换密码属性的名称
        	   .contextSource()//默认条件下，LDAP服务器和数据在远端监听本机的33389端口，可以使用contextSource()配置，contextSource返回一个ContextSourceBuilder的对象，提供url指定LDAP地址
                .url("ldap://tacocloud.com:389/dc=tacocloud,dc=com");
        		//或者使用本地嵌入式服务器,用root方法指定服务器根前缀
        //	.root("dc=tacocloud,dc=com");
        		.ldif("classPath:user.ldif");//这样可以指定ldif文件搜索路径
        
    }
}
```

当LDAP服务器启动，他会在类路径下寻找LDIF文件来加载数据，LDIF文件有一行或者多行每项包含一个name：value信息

#### 如何自定义基于JDBC的用户认证（把用户信息也利用Spring Data JPA存储）

首先创建用户类

实现了Spring Security的UserDetail接口，能提供更多信息给框架，（目前视为所有的账号都为可用）

```java
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE,force = true)
@RequiredArgsConstructor
@Entity
public class User implements UserDetails{
    private static  final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    
    private final String username;
    private final String password;
    private final String fullName;
    private final String street;
    private final String city;
    private final String state;
    private final String zip;
    private final String phoneNumber;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //返回用户被授予权限的集合
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

其次创建展现和持久化用户信息的repository接口

拓展Spring Data JPA的CRUDRepository接口，就可以自动生成这个接口的实现类

```java
public interface UserRepository extends CrudRepository<User,Long>{
    User findByUsername(String userName);
}
```

创建自定义用户服务

```java
@Service//类似于@Controller@Component@Repository
public class UserRepositoryUserDetailsService implements UserDetailsService{

     private UserRepository userRepository;
     @Autowired
     public UserRepositoryUserDetailsService(UserRepository userRepository){
         this.userRepository=userRepository;
     }//自动注入


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(s);
        if(user!=null){
            return user;
        }
        throw new UsernameNotFoundException("User '"+s+"' not found");//不能返回空，就先判断，再抛出异常
    }

}
```

此时，就可以在configure中使用自定义的User

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {  
    @Autowired
    private UserDetailsService userDetailsService;
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(userDetailsService)
        .passwordEncoder(new BCryptPasswordEncoder());
    }
}
```

##### 注册用户：

Controller类

```java
@Controller
@RequestMapping("/register")
public class RegistrationController {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    public  RegistrationController(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }
    @GetMapping
    public String registerForm(){
        return "registration";
    }
    @PostMapping
    public String processRegistration(RegistrationForm form){
        userRepository.save(form.toUser(passwordEncoder));
        return "redirect:/login";
    }
}
```

Post请求需要一个RegistrationForm类来绑定请求的数据，并需要一个registration表单

该类将表单提交的数据转化为User对象，再由注入的UserRepository保存该对象

```java
@Data
public class RegistrationForm {
  private String username;
  private String password;
  private String fullname;
  private String street;
  private String city;
  private String state;
  private String zip;
  private String phone;
  public User toUser(PasswordEncoder passwordEncoder){
    return new User(username,passwordEncoder.encode(password),fullname,street,city,state,zip,phone);
  }
}
```

#### web请求的保护：

本应用应该对设计taco和提交订单之前认证登录，而仅仅浏览不需要登陆

介绍WebSecurityConfigAdapter的configure()方法

```java
@Override
protected void configure(HttpSecurity http) throws Exception{
	...
}
```

该方法首先接受一个HttpSecurity对象，配置Web级别安全性，比如

* controller处理某个RequestMapping时，先验证某条件
* 自定义登陆页面
* 支持用户退出应用
* 防止跨站请求伪造

第一：只有认证过的用户才可以发起"/design""/orders"的请求

```java
@Override
    protected void configure(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .authorizeRequests()
                .antMatchers("/design","/orders")
                .hasRole("ROLE_USER")//对以上两个请求，必须要具备ROLE_USER才可以访问
                .antMatchers("/","/**")
                .permitAll();//所有请求均可访问
        //后边的会不会覆盖前边的？不会，因为定义访问限制有优先级，前边的优先级更高
        //当然可以把判断语句换成Spring表达式，但就不赘述了access(String 表达式)
        		.and()//使用and链接前后的配置
                .formLogin()
                .loginPage("/login");//当Spring判定用户没登陆，却需要登陆，就重定向到这个路径
    }
```

使用access()和Spring SPEL表达式能更清晰地过滤请求

```java
@Override
    protected void configure(HttpSecurity httpSecurity) throws  Exception{
        //使用Spring SPEL表达式
        httpSecurity
                .authorizeRequests()
                .antMatchers("/design","/orders")
                .access("hasRole('ROLE_USER')")
                .antMatchers("/","/**")
                .access("permitAll");
    }
    @Override
    protected void configure(HttpSecurity httpSecurity)throws Exception{
        //使用Spring SPEL表达式，规定只有周二才能下单
        httpSecurity
                .authorizeRequests()
                .antMatchers("/design","order")
                .access("hasRole('ROLE_USER')&&"+"T(java.util.Calendar).getInstance().get("+"T(java.util.Calendar).DAY_OF_WEEK)=="+"T(java.util.Calendar).TUESDAY")
                .antMatchers("/","/**")
                .access("permitAll");
    }
```

既然要重定向到“/login”，那么就需要一个控制器，来响应登陆页面，因为这个控制器很简单，只需要返回视图，那么可以使用addViewControllers

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/login");
    }
}
```

login视图定义如下：

```html
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <title>Taco Cloud</title>
</head>
<body>
    <h1>Login</h1>
    <img th:src="@{/images/TacoCloud.png}"/>
    <div th:if="${error}">
        Unable to login.Check your username and password.
    </div>

<p>New here? Click
<a th:href="@{/register}">here</a> to register.</p>
<!-- tag::thAction[] -->
<form method="post" th:action="@{/login}" id="loginForm">
    <!--end::thAction[] -->
    <label for="username">Username:</label>
    <label type="text" name="username" id="username"/><br/>
    <label for="password">Password:</label>
    <label type="password" name="password" id="password"/><br/>

    <input type="submit" value="Login"/>
</form>
</body>
</html>
```

注意，这个视图导致，在默认情况下，spring Security监听登录请求，并验证用户名和密码输入域名称为username和password，当然这也可以自己配置别的

比如声明Spring Security监听对/authenticate的请求来处理提交，并且修改了字段名

```java
.and()//使用and链接前后的配置
                .formLogin()
                .loginPage("/login");//当Spring判定用户没登陆，却需要登陆，就重定向到这个路径				
                .loginProcessingUrl("/authenticate")
                .usernameParameter("user")
                .passwordParameter("pwd")
                .defaultSuccessUrl("/design");//默认重定向到设计页面
```

退出功能需要在配置类上加上一个配置块

```java
.and()//and连接配置块，并设置退出的页面
                .logout()
                .logoutSuccessUrl("/");
//这样会搭建一个安全过滤器，拦截对“/logout”请求
```

为了提供退出功能，原视图也需要加一个退出表单和按钮

```html
<form method="post" th:action="@{/logout}">
      <input type="submit" value="Logout"/>
</form>
```

点击按钮，用户的session被清除，重定向到登陆页面，在这里我把他们重定向到主页。

防止跨站请求伪造（CSRF），这种攻击会误导用户在一个恶意的Web页面上填写信息，然后将表单Post到应用上，比如银行转账系统

预防的方法是，表单从服务器发给用户时，生成一个Token，用户填写并Post给服务器，服务器首先对比Token，如果相同，则继续，不同则认为是攻击

在Spring Security中开启这个功能非常方便，使用Thymeleaf可以在隐藏域渲染CSRF token：

```html
<input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
```

在JSP标签库或者Thymeleaf中，其实这个隐藏域是自动生效的，只要使用th:action即可：

```html
<form method="POST" th:action="@{/login}" id="loginForm">
```

保存默认地址、默认身份信息的操作：

应该将Order实体和创建订单的用户关联起来：

```java
@Data
@Entity
@Table(name="Taco_Order")
public class Order implements Serializable{
...
	@ManyToOne//表示一个用户可以有多个订单，但一个订单只能属于一个用户
	private User user;
	...
}
```

在Order Controller中，processOrder方法之前起到验证错误，存储order并且重定向的作用，但现在需要确认当前的用户是谁，所以需要加调用Order对象的setUser方法

有多种方法确定当前Order对应的User：

```java
注入Principal对象到Controller对象到Controller方法中
注入Authentication对象到Controller方法中
注入SecurityContextHolder来获取安全上下文
注入@AuthenticationPrincipal注解标注方法
```

修改processOrder()，让他接受一个java.security.Principal类型参数，就可以使用Principal从UserRepository查找用户：

```java
@PostMapping
    public String precessOrder(@Valid Order order, Errors errors,
                               SessionStatus sessionStatus, Principal principal){
        if(errors.hasErrors()){
            return "orderForm";
        }
        User user = userRepository.findByUsername(principal.getName());
        order.setUser(user);//问题是，这样在与安全无关的代码中混入的安全代码
        orderRepo.save(order);
        sessionStatus.setComplete();//重置session
        return "redirect:/";
    }
```

也可以这样去掉安全代码：

```java
@PostMapping
    public String precessOrder(@Valid Order order, Errors errors,
                               SessionStatus sessionStatus, Authentication authentication){
        if(errors.hasErrors()){
            return "orderForm";
        }
        User user = (User)authentication.getPrincipal();//因为返回一个Object对象
        order.setUser(user);
        orderRepo.save(order);
        sessionStatus.setComplete();//重置session
        return "redirect:/";
    }
```

最方便的方法是直接加上注解@AuthenticationPrincipal User user

```java
@PostMapping
    public String precessOrder(@Valid Order order, Errors errors,
                               SessionStatus sessionStatus, @AuthenticationPrincipal User user){//也不需要类型转换
        if(errors.hasErrors()){
            return "orderForm";
        }
        order.setUser(user);
        orderRepo.save(order);
        sessionStatus.setComplete();//重置session
        return "redirect:/";
    }
```

还有另外一个充满安全性代码，繁琐的方法，但优势是不仅仅可以用在控制器的处理器中，可以用在低级别的方法中

```java
Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
User user = (User)authentication.getPrincipal();
```

### SpringBoot的Configuration Property

配置属性修改的是上下文中的bean属性，可以通过JVM系统属性，命令行参数，操作系统环境变量，Application.properties，Application.yml改变

首先Spring有两种不同的配置：

* bean装配：声明Spring容器应该创建什么组件以及如何互相注入的配置
* 属性注入：设置Spring容器bean值的配置、

如果不使用SpringBoot，手动配置为：在基于java的配置，声明@bean后，会立即为他设置属性值，比如：

```java
@Bean
public DataSource dataSource(){
	return new EmbeddedDataSourceBuilder()
		.setType(H2)
		.addScript("taco_schema.sql")//添加数据库配置完毕后要使用的脚本文件
		.addScripts("user_data.sql","ingredient_data.sql")
		.build();
}
```

但因为自动配置，Spring已经为我做好

也可以在Applicants.yml文件中进行配置

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost/tacocloud
    username: tacodb
    password: tacopassword
    driver-class-name: com.mysql.jdbc.Driver
```

spring会自动推断驱动类，也会在自动配置DataSource bean时，使用这个连接，如果类路径中存在Tomcat的JDBC连接池，就会用，否则会自动使用HikariCP或者Commons DBCPs

反过来说，用显式连接DataSource bean也行

对sql脚本的加载也可以显示配置

```yml
spring:
  datasource:
    schema:
      - order-schema.sql
      - ingredient-schema.sql
      - taco-schema.sql
      - user-schema.sql
    data:
      - ingredients.sql
```

如果设置JNDI数据源并让Spring去查找，那么Spring就会忽略已有的数据源配置

```yml
spring:
  datasource:
  	jndi-name: java:/comp/env/jdbc/tacoCloudDS
```

配置日志：

不配置的话，Spring会用Logback配置日志，以INFO级别写入到控制台

我想把它 root logging设置为WARN级别，把Srping Security日志级别设置为DEBUG

首先在src/main/resources创建logback.xml文件

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>
                %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
            </pattern>
        </encoder>
    </appender>
    <logger name="root" level="INFO"/>
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

再去Application.yml配置参数，规定root logging为WARN，Spring Security为DEBUG，写入/var/logs/下的TacoCloud.log

```yml
logging:
  filepath: /var/logs/
  file: TacoCloud.log
  level:
    root: WARN
    org:
      springframework:
        security: DEBUG
```

外部配置bean的属性：

假设想加入一个功能给OrderController，列出当前用户的历史订单：

```java
@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
public class OrderController {
    private JPAOrderRepository orderRepo;
    private UserRepository userRepository;

    public OrderController(JPAOrderRepository orderRepo,UserRepository userRepository) {
        this.orderRepo = orderRepo;
        this.userRepository=userRepository;
    }
    @GetMapping
    public String orderForUser(@AuthenticationPrincipal User user, Model model){
        Pageable pageable = (Pageable) PageRequest.of(0,20);//只显示降序创建时间最近20条
        model.addAttribute("order",orderRepo.findByUserOrderByPlaceAtDesc(user,pageable));
        return "orderList";

    }
    }
```

但问题是，当前的20是硬编码的，如何修改？

先加入@ConfigurationProperties，再加入一个setter，然后使用yml文件设置它的值

```java
@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
@ConfigurationProperties(prefix = "taco.orders")
public class OrderController {
    private int pageSize=20;
    private JPAOrderRepository orderRepo;
    private UserRepository userRepository;
    public void setPageSize(int pageSize){
        this.pageSize=pageSize;
    }
    
    public OrderController(JPAOrderRepository orderRepo,UserRepository userRepository) {
        this.orderRepo = orderRepo;
        this.userRepository=userRepository;
    }
    @GetMapping
    public String orderForUser(@AuthenticationPrincipal User user, Model model){
        Pageable pageable = (Pageable) PageRequest.of(0,pageSize);//只显示降序创建时间最近20条
        model.addAttribute("order",orderRepo.findByUserOrderByPlaceAtDesc(user,pageable));
        return "orderList";

    }
```

yml：

```yml
Taco:
  orders:
    pageSize: 10
```

这样的配置方法也更方便把多个bean的通用配置提取出来，比如可以使用OrderProps类来储存订单的通用配置：

```java
@Component
@ConfigurationProperties(prefix = "taco.orders")
@Data
public class OrderProps {
    private int pageSize=20;//把一些通用配置从控制器中抽取出来，放到统一的bean中
}
```

此时只需要把OrderProps自动装配到OrderCOntroller上即可，如果这个值是多个Controller共同使用，那么可以更方便的做修改

比如当前需要给页面大小做限制，就不需要修改控制器，而直接修改OrderProps：

```java
@Component
@ConfigurationProperties(prefix = "taco.orders")
@Data
@Validated
public class OrderProps {
    @Min(value=5,message = "must between 5 and 25")
    @Max(value=25,message = "must between 5 and 25")
    private int pageSize=20;//把一些通用配置从控制器中抽取出来，放到统一的bean中
}
```

开发和调试使用H2数据库，TacoCloud代码的日志级别为DEBUG，但生产环境，就使用外部的MySQL数据库，日志级别为WARN

可设置Profile和它的属性来进行切换

声明一个新的profile有两种方式，直接创建(Application-prod.yml)或者用三个中划线

```yml
sever:
  port: 9090

spring:
  datasource:
    schema:
      - order-schema.sql
      - ingredient-schema.sql
      - taco-schema.sql
      - user-schema.sql
    data:
      - ingredients.sql
    url: jdbc:mysql://localhost/tacocloud
    username: tacodb
    password: tacopassword
    driver-class-name: com.mysql.jdbc.Driver

logging:
  filepath: /var/logs/
  file: TacoCloud.log
  level:
    root: WARN
    org:
      springframework:
        security: DEBUG
        

greeting:
  welcome: ${spring.application.name}
  welcome2: YOU ARE USING ${spring.application.name}

Taco:
  orders:
    pageSize: 10
```

```yml
spring:
  datasource:
    url: jdbc:mysql://localHost/tacocloud
    username: tacouser
    password: tacopasswordyml
  logging:-+
    level:
      tacos: WARN
```

如何激活当前想要的profile呢？

尽量不要用spring.profiles.active:prod，这样切换默认环境不如使用环境变量好：

```
% export SPRING_PROFILES_ACTIVE=prod,audit,ha
```

如果是jar包，那么使用：

```
% java -jar taco-cloud.jar --spring.profiles.active=prod
```

默认条件，容器会创建java配置类所有被声明的bean，如果希望某些bean仅在特定的Profile才会创建，那么使用@Profile,可以修饰在方法，也可以修饰在类上

在开发阶段，每次都回家再配料库和H2数据库，这样很有必要，但是没有必要在生产阶段每次启动都加载双库，所以：

```java
	@Bean
	@Profile("dev","qa")
	//or @Profile("!prod")
	public CommandLineRunner dataLoader(JPAIngredientRepository ingredientRepository, UserRepository userRepository, PasswordEncoder passwordEncoder){
	}
```

