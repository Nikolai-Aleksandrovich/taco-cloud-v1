改变前端为Angular，后端为RestFul编程

### 从服务器检索数据（按条件、按Id）：

首先返回最近创建的三个Taco

首先定义RecentTacosComponent组件：

```typescript

```

其次，还需要一个响应RecentTacosComponent组件的Controller类：

其中，@RestController代表，这是一个RestFul风格的Controller，意味着控制器所有的值直接写入响应体中，而不会再存在Model中再传给另外一个视图，或者给类加@Controller，但反过来，就要给方法加@RespondBody，再或者，给方法的返回值设置为RespondEntity也可以

```java
@RestController
@RequestMapping(path="/design",produces = "application/json")
//produces指明该类所有方法只会处理Accept头信息包含Application/json的请求，限制API只生成Json结果，也会允许其他控制器处理相同路径的请求，只要这些请求不要求Json格式输出即可
@CrossOrigin(origins = "*")
//CrossOrigin允许来自任何域的客户端消费API，因为应用程序的Angular部分会运行在与API独立的主机或者端口，web浏览器会阻止Angular消费该API
public class DesignTacoController {
    private JPATacoRepository jpaTacoRepository;
    @Autowired
    EntityLinks entityLinks;
    public DesignTacoController(JPATacoRepository jpaTacoRepository){
        this.jpaTacoRepository=jpaTacoRepository;
    }
    @GetMapping("/recent")//处理针对"/design/recent"的get请求
    public Iterable<Taco> recentTacos(){
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        return jpaTacoRepository.findAll(page).getContent();
    }
    @GetMapping("/{id}")//这里的{id}为占位符，利用@PathVariable作为参数传给查询方法
    public Taco tacoById(@PathVariable("id")Long id){
        Optional<Taco> optionalTaco = jpaTacoRepository.findById(id);
        //使用Optional<Taco>的原因是因为，可能这个ID查不到taco，所以就在结尾判断optionTaco.isPresent，不然就返回空，但是返回空也会出现问题，这样返回，客户端收到空的响应体和值为200（OK）的HTTP状态码，更好的办法是让他返回404 NOT FOUND
        if(optionalTaco.isPresent()){
            return optionalTaco.get();
        }
        return null;
    }
}
```

改进的按ID查询Taco方法：

```java
@GetMapping("/{id}")
    public ResponseEntity<Taco> tacoById(@PathVariable("id")Long id){
        Optional<Taco> optionalTaco = jpaTacoRepository.findById(id);
        if(optionalTaco.isPresent()){
            return new ResponseEntity<>(optionalTaco.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
    }
```

### 发送数据到服务器端：

```java
@PostMapping(consumes = "application/json")//表示该方法只会处理Content-type与application/json相匹配的请求
@ResponseStatus(HttpStatus.CREATED)
//本来所有的响应码都是200OK，但在这里转换成201CREATED，表示不仅OK而且还创建了资源
public Taco postTaco(@RequestBody Taco taco){
    //方法参数@RequestBody表明请求JSON转换为一个Taco对象并绑定到这个参数上，如果没有@RequestBody，Spring MVC会认为我们希望把请求参数绑定到Taco上
    //PostTaco接收到对象，就用save方法存储
    return designRepo.save(taco);
}

```

### 在服务器端更新数据：

@PutMapping（大规模替换）和@PatchMapping（小规模更新）都可以更新数据，为什么有两种更新？

使用Put的Controller

```java
@PutMapping("/{orderId}")
public Order putOrder(@RequestBody Order order){
	return repo.save(order);
}
```

Put的大规模更新代表，如果一行数据只更新Id，那么其余属性就被null覆盖

使用@PatchMapping：

```java
@PatchMapping(path = "/{orderId}",consumes = "application/json")
    public Order patchOrder(@PathVariable("orderId")Long orderId,@RequestBody Order patch){
        //实际写如何更新的规则还是自己
        Order order = orderRepo.findById(orderId).get();
        if(order.getName()!=null){
            order.setName(patch.getName());
        }
        if(order.getStreet()!=null){
            order.setStreet(patch.getStreet());
        }
        return orderRepo.save(order);
    }
```

### 删除服务器的数据

```java
@DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //返回204No_CONTENT
    public void deleteOrder(@PathVariable("orderId")Long id){
        try{
            orderRepo.deleteById(id);
        }catch (EmptyResultDataAccessException e){
            //什么都不做，因为成功删除和没找到的结果，都是没有这个数据
        }
    }
```

Spring超链接功能

是什么？使用HAL一种超文本应用语言，将向服务器请求的URL从传统硬编码转换为在JSON响应嵌入超链接的通用格式

首先先引入starter：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

HATEOAS版本出现变更，导致很多接口名发生变更：

```

ResourceSupport changed to RepresentationModel
Resource changed to EntityModel
Resources changed to CollectionModel
PagedResources changed to PagedModel
ResourceAssembler changed to RepresentationModelAssembler
ControllerLinkBuilder changed to WebMvcLinkBuilder
ResourceProcessor changed to RepresentationModelProcessor

RepresentationModel.getLinks() now exposes a Links instance (over a List<Link>) as that exposes additional API to concatenate and merge different Links instances using various strategies. Also it has been turned into a self-bound generic type to allow the methods that add links to the instance return the instance itself.

The LinkDiscoverer API has been moved to the client package.

The LinkBuilder and EntityLinks APIs have been moved to the server package.

ControllerLinkBuilder has been moved into server.mvc and deprecated to be replaced by WebMvcLinkBuilder.

RelProvider has been renamed to LinkRelationProvider and returns LinkRelation instances instead of Strings.

VndError has been moved to the mediatype.vnderror package.
```

如何向控制器中添加超链接呢？

重新实现控制器，让他们返回资源类型，而不是领域类型

```java
@GetMapping("/recent")
    public Resources<Resource<Taco>> recentTacos(){
        //把返回类从List<Taco>改为Resources<Resource<Taco>>
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        List<Taco> tacos = jpaTacoRepository.findAll(page).getContent();
        Resources<Resource<Taco>> recentResources = Resources.warp(tacos);
        //利用Resources.warp(tacos),把tacos改写为Resources类
        recentResources.add(
                ControllerLinkBuilder.LinkTo(methodOn(DesignTacoController.class).recentTacos())
                .withRel("recents")
        );
        //再调用连接构建者ControllerLinkBuilder的linkTo，withRel
        return recentResources;
    }
```

如何创建资源装配器？

现在需要给列表中的所有taco资源添加链接，笨办法是遍历Resources中所有的资源，为每一个加上链接，还有一个办法，可以创建一个TacoResource类，负责将Taco转换为TacoResource

```java
public class TacoResource extends RepresentationModel<TacoResource> {
    //拓展了RepresentationModel，继承了一个Link对象的列表和管理
    @Getter
    private final String name;

    @Getter
    private final Date createdAt;

    @Getter
    private final List<Ingredient> ingredientList;

    public TacoResource(Taco taco){
        //接受一个Taco对象，把属性复制过来
        this.name = taco.getName();
        this.createdAt = taco.getCreatedAt();
        this.ingredientList = taco.getIngredients();
    }
}
```

如何把Taco对象转换成TacoResource对象？别说再TacoResource对象内部转，那样还得遍历所有Taco对象，可以创建这样的装配器：

```java
public class TacoResourceAssembler extends RepresentationModelAssemblerSupport<Taco,TacoResource> {
    public TacoResourceAssembler(){
        //一个默认的构造器，告诉超类创建TacoResource中的链接，会利用DesignTacoController确定URL的基础路径
        super(DesignTacoController.class,TacoResource.class);
    }
    @Override
    protected TacoResource instantiateModel(Taco taco){
        //实例化一个TacoResource对象
        //基于给定的Taco实例化TacoResource，如果TacoResource有默认构造器，那就可以不重写
        return new TacoResource(taco);
    }
    @Override
    public TacoResource toModel(Taco taco){
        //不仅要实例化，还要填充链接，要调用上边的TacoResource
        //强制实现，利用Taco创建TacoResource，并设置一个self链接，根据Id衍生
        return createModelWithId(taco.getId(),taco);
    }
}
```

利用这个装配器，那么DesignTacoController的recentTacos方法可以改为：

```java
@GetMapping("/recent")
    public CollectionModel<TacoResource> recentTacos(){
        //返回值为CollectionModel<TacoResource>
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        List<Taco> tacos = jpaTacoRepository.findAll(page).getContent();//获取Taco
        List<TacoResource> tacoResources = new TacoResourceAssembler().toCollectionModel(tacos);//传递Taco对象给toCollectionModel方法，这个方法会循环所有对象，调用它创建列表
        CollectionModel<TacoResource> recentResources = new CollectionModel<TacoResource>(tacoResources);//调用填充链接
        recentResources.add(
                linkTo(methodOn(DesignTacoController.class).recentTacos())
                .withRel("recents")
        );
        return recentResources;
    }
//此时，对"/design/recent"发起GET请求，会生成taco的列表，每个taco都有一个self链接，整体有一个recents链接
```

为配料设置一个资源装配器：

```java
public class IngredientResourceAssembler extends RepresentationModelAssemblerSupport<Ingredient,IngredientResource> {
    public IngredientResourceAssembler(){
        super(IngredientController.class,IngredientResource.class);
    }
    @Override
    public IngredientResource toModel(Ingredient ingredient){
        return createModelWithId(ingredient.getId(),ingredient);
    }
    @Override
    protected IngredientResource instantiateModel(Ingredient ingredient){
        return new IngredientResource(ingredient);
    }
}
```

配料对应的Controller：

配料对应的IngredientResource

```java
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
```

在TacoResource上添加@Relation注解

```java
@Relation(value = "taco",collectionRelation = "tacos")
```

可以去掉JSON字段名和java代码中定义资源类名的耦合

### 使用SpringData创建API

SpringDataREST会为Spring DATA创建的repository自动生成REST API

首先加入依赖：

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-data-rest</artifactId>
</dependency>
```

仅仅加入依赖，就能对外暴露REST API

为API设置基础路径,这样API会有不同的端点，避免与已存在的控制器冲突：

```yml
spring:
  data:
  	rest:
  	  base-path: /api
```

有了实体类Taco和TacoRepository，那么Spring的自动配置会为SpringData创建的所有repository自动创建REST API，就可以移除@RestController

通过向API基础路径发送get请求，即读取到所有端点的链接

也可以通过@RestResource(rel="**",path="tacos")来设置关系名和路径

分页和排序

默认集合资源会返回第一页的20个，但也可以设置

想要请求第一页的taco，希望包含5个条目，页数从0开始计数：

```java
$ curl "localhost:8080/api/tacos?size=5"
```

如果超过5个taco，要查看第二页的：

```
$ curl "localhost:8080/api/tacos?size=5&page=1"
```

HATEOAS也提供了链接

添加自定义的端点：

Spring Data能为执行CRUD的Spring Data repository创建端点，但有时候要脱离默认的CRUD API，创建处理核心问题的端点

也可以在@RestController注解的bean中实现任意端点，来补充Data REST，也就是说，DesignTacoController可以和REST Controller一起运行，但编写自己的API Controller会与Spring Data REST脱节：

* 自定义的Controller端点没有映射到Spring Data Rest基础路径下，如果强制映射，就会强耦合
* 自定义的Controller端点，返回资源不会自动包含超链接，意味着，客户端无法通过关系名发现自定义的端点

对基础路径：可以使用Spring Data REST的@RepositoryRestController标注在Controller上，将映射的基础路径设置为与Spring Data REST端点配置的基础路径相同，意味着，控制器所有的映射会有和spring.data.rest.base-path属性值一样的前缀

```java
@RepositoryRestController
public class RecentTacosController {
    private JPATacoRepository jpaTacoRepository;
    public RecentTacosController(JPATacoRepository jpaTacoRepository){
        //注入SPRING DATA JPA CRUD
        this.jpaTacoRepository=jpaTacoRepository;
    }
    @GetMapping(path = "/tacos/recent")
    public ResponseEntity<CollectionModel<TacoResource>> recentTaco(){
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        List<Taco> tacos = jpaTacoRepository.findAll(page).getContent();
        CollectionModel<TacoResource> tacoResources = new TacoResourceAssembler().toCollectionModel(tacos);
        CollectionModel<TacoResource> recentResources= CollectionModel.of(tacoResources);
        recentResources.add(
                linkTo(methodOn(RecentTacosController.class).recentTaco()).withRel("recents")
        );
        return  new ResponseEntity<>(recentResources, HttpStatus.OK);
    }
```

为端点添加自定义的超链接：

```java
@Configuration
public class SpringDataRestConfiguration {
    @Bean
    public RepresentationModelProcessor<PagedModel<EntityModel<Taco>>> tacoProcessor(EntityLinks links){
        return model -> {
            model.add(
                    links.linkFor(Taco.class)
                            .slash("recent")
                            .withRel("recents")
            );
            return model;
        };
    }
}
```

添加一个配置类

## 使用API

Spring有三种方式使用 REST API

* RestTemplate：Spring核心框架的同步REST客户端
* Traverson：Spring HAREOAS的支持超链接、同步的REST客户端
* WebClient：反应式，异步REST客户端

使用RestTemplate：

要使用 RestTemplate，需要创建一个实例：

```java
RestTemplate rest = new RestTemplate();
```

或是将它声明为一个 bean，在需要它的时候将其注入：

```java
@Bean
public RestTemplate restTemplate() {
    return new RestTemplate();
}
```

#### GET 资源:

使用 getForObject() 来获取 Ingredient

```java
public Ingredient getIngredientById(String ingredientId) {
    return rest.getForObject("http://localhost:8080/ingredients/{id}",
                             Ingredient.class, ingredientId);
}
```

getForObject() 变量接受一个字符串 URL 并为 URL 变量使用一个变量列表。传递给 getForObject() 的 ingredientId 参数用于填充给定 URL 中的 `{id}` 占位符。第二个参数是响应应该绑定的类型

使用映射来指定 URL 变量：

```java
public Ingredient getIngredientById(String ingredientId) {
    Map<String, String> urlVariables = new HashMap<>();
    urlVariables.put("id", ingredientId);
    return rest.getForObject("http://localhost:8080/ingredient/{id}",
                            Ingredient.class, urlVariables);
}
```

使用 URI 参数,在调用 getForObject() 之前构造一个 URI 对象

```java
public Ingredient getIngredientById(String ingredientId) {
    Map<String,String> urlVariables = new HashMap<>();
    urlVariables.put("id", ingredientId);
    URI url = UriComponentsBuilder
        .fromHttpUrl("http://localhost:8080/ingredients/{id}")
        .build(urlVariables);
    return rest.getForObject(url, Ingredient.class);
}
```

,另外，getForEntity() 的工作方式与 getForObject() 非常相似，但它返回的不是表示响应有效负载的域对象，而是包装该域对象的 ResponseEntity 对象。ResponseEntity 允许访问附加的响应细节，比如响应头。

```java
public Ingredient getIngredientById(String ingredientId) {
    ResponseEntity<Ingredient> responseEntity =
        rest.getForEntity("http://localhost:8080/ingredients/{id}",
                          Ingredient.class, ingredientId);
    
    log.info("Fetched time: " +
             responseEntity.getHeaders().getDate());
    
    return responseEntity.getBody();
}
```

####  PUT 资源

put() 的所有三个重载方法都接受一个将被序列化并发送到给定 URL 的对象。

用来自一个新的 Ingredient 对象的数据来替换配料资源

```java
public void updateIngredient(Ingredient ingredient) {
    rest.put("http://localhost:8080/ingredients/{id}",
            ingredient,
            ingredient.getId());
}
```

URL 以 String 的形式给出，并有一个占位符，该占位符由给定的 Ingredient 对象的 id 属性替换。要发送的数据是 Ingredient 对象本身。

#### DELETE 资源

```java
public void deleteIngredient(Ingredient ingredient) {
    rest.delete("http://localhost:8080/ingredients/{id}",
               ingredient.getId());
}
```

#### POST 资源

RestTemplate 有三种发送 POST 请求的方法

如果想在 POST 请求后收到新创建的 Ingredient 资源，可以像这样使用 postForObject()：

```java
public Ingredient createIngredient(Ingredient ingredient) {
    return rest.postForObject("http://localhost:8080/ingredients",
                             ingredient,
                             Ingredient.class);
}
```

postForObject() 方法的这种形式采用 String 作为 URL 规范，要发送到服务器的对象以及响应主体应该绑定到的域类型。

调用 postForLocation()，请求资源的位置：它返回的是新创建资源的 URI，而不是资源对象本身。

```java
public URI createIngredient(Ingredient ingredient) {
    return rest.postForLocation("http://localhost:8080/ingredients",
                                ingredient);
}
```

同时需要位置和响应负载，可以调用 postForEntity()：

```java
public Ingredient createIngredient(Ingredient ingredient) {
    ResponseEntity<Ingredient> responseEntity =
        rest.postForEntity("http://localhost:8080/ingredients",
                           ingredient,
                           Ingredient.class);
    
    log.info("New resource created at " +
             responseEntity.getHeaders().getLocation());
    
    return responseEntity.getBody();
}
```

#### 使用 Traverson 引导 REST API

要使用 Traverson，首先需要实例化一个 Traverson 对象和一个 API 的基础 URI：

```java
Traverson traverson = new Traverson(
    URI.create("http://localhost:8080/api"), MediaType.HAL_JSON);
```

这里将 Traverson 指向 Taco Cloud 的基本 URL（在本地运行），这是需要给 Traverson 的唯一 URL。从这里开始，将通过链接关系名称来引导 API。还将指定 API 将生成带有 HAL 风格的超链接的 JSON 响应，以便 Traverson 知道如何解析传入的资源数据。与 RestTemplate 一样，可以选择在使用 Traverson 对象之前实例化它，或者将它声明为一个 bean，以便在需要的地方注入它。

想检索所有 Ingredient 的列表。Ingredient 链接有一个链接到配料资源的 href 属性，需要跟踪这个链接：

```java
ParameterizedTypeReference<Resources<Ingredient>> ingredientType =
    new ParameterizedTypeReference<Resources<Ingredient>>() {};
Resources<Ingredient> ingredientRes =
    traverson.follow("ingredients").toObject(ingredientType);
Collection<Ingredient> ingredients = ingredientRes.getContent();
```

通过调用 Traverson 对象上的 follow() 方法，可以引导到链接关系名称为 ingredients 的资源。现在客户端已经引导到 ingredients，需要通过调用 toObject() 来提取该资源的内容。

假设想获取最近创建的 tacos，从 home 资源开始，可以引导到最近的 tacos 资源：

```java
ParameterizeTypeReference<Resources<Taco>> tacoType = 
    new ParameterizedTypeReference<Resources<Taco>>() {};
Resources<Taco> tacoRes = 
    traverson.follow("tacos").follow("recents").toObject(tacoType);
Collection<Taco> tacos = tacoRes.getContent();
```

简化follow：

```java
Resources<Taco> tacoRes =
    traverson.follow("tacos", "recents").toObject(tacoType);
```

Traverson 可以轻松地引导启用了 HATEOAS 的 API 并调用其资源。但有一件事它没有提供任何方法来编写或删除这些 API。相比之下，RestTemplate 可以编写和删除资源，但不便于引导 API。

举例：addIngredient() 方法将 Traverson 和 RestTemplate 组合起来，向 API POST 一个新 Ingredient：

```java
private Ingredient addIngredient(Ingredient ingredient) {
    String ingredientsUrl = traverson.follow("ingredients")
        .asLink().getHref();
    return rest.postForObject(ingredientsUrl,
                             ingredient,
                             Ingredient.class);
}
```

## 异步消息：

#### JMS 发送消息

Spring 通过称为 JmsTemplate 的基于模板的抽象来支持 JMS。使用 JmsTemplate，很容易从生产者端跨队列和主题发送消息，并在消费者端接收这些消息。Spring 还支持消息驱动 POJO 的概念：简单的 Java 对象以异步方式对队列或主题上到达的消息做出响应。

##### 设置 JMS

首先决定使用 Apache ActiveMQ，还是使用较新的 Apache ActiveMQ Artemis Broker。

如果使用 ActiveMQ，需要添加以下依赖到项目的 pom.xml 文件中：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
```

如果选择 ActiveMQ Artemis，starter 如下所示：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-artemis</artifactId>
</dependency>
```

Artemis 是 ActiveMQ 的下一代重新实现，实际上这让 ActiveMQ 成为一个遗留选项。因此，对于 Taco Cloud，将选择 Artemis。但是，这种选择最终对如何编写发送和接收消息的代码几乎没有影响。唯一显著的区别在于**如何配置 Spring 来创建与 Broker 的连接。**

默认情况下，Spring 假设 Artemis Broker 正在监听 localhost 的 61616 端口。但需要设置一些配置值以适应生产或其他环节

```yml
spring:
  artemis:
    host: artemis.tacocloud.com
    port: 61617
    user: tacoweb
    password: 13tm31n
```

这将设置 Spring，以创建到监听 artemis.tacocloud.com（端口 61617）的 Artemis Broker 的 broker 连接。它还设置将与该 broker 交互的应用程序的凭据，凭据是可选的，但建议用于生产部署。

如果要使用 ActiveMQ：

```yml
spring:
  activemq:
    broker-url: tcp://activemq.tacocloud.com
    user: tacoweb
    password: 13tm31n
```

无论选择 Artemis 还是ActiveMQ，当 Broker 在本地运行时，都不需要为开发环境配置这些属性。

但是，如果使用 ActiveMQ，则需要设置 spring.activemq.in-memory 属性为 false，以防止 Spring 启动内存中的 Broker。内存中的 Broker可能看起来很有用，但它只在发布和消费同一个应用的消息时有用（这一点用处有限）。

#### JmsTemplate 发送消息

msTemplate 有几个发送消息的有用方法，包括：

```java
/ 发送原始消息
void send(MessageCreator messageCreator) throws JmsException;
void send(Destination destination, MessageCreator messageCreator) throws JmsException;
void send(String destinationName, MessageCreator messageCreator) throws JmsException;
// 发送转换自对象的消息
void convertAndSend(Object message) throws JmsException;
void convertAndSend(Destination destination, Object message) throws JmsException;
void convertAndSend(String destinationName, Object message) throws JmsException;
// 发送经过处理后从对象转换而来的消息
void convertAndSend(Object message, MessagePostProcessor postProcessor) throws JmsException;
void convertAndSend(Destination destination, Object message, MessagePostProcessor postProcessor) throws JmsException;
void convertAndSend(String destinationName, Object message, MessagePostProcessor postProcessor) throws JmsException;
```

实际上只有两个方法，send() 和 convertAndSend()，每个方法都被重载以支持不同的参数。

有以下细分：

- send() 方法需要一个 MessageCreator 来制造一个 Message 对象。
- convertAndSend() 方法接受一个 Object，并在后台自动将该 Object 转换为一条 Message。
- 三种 convertAndSend() 方法会自动将一个 Object 转换成一条 Message，但也会接受一个 MessagePostProcessor，以便在 Message 发送前对其进行定制。

此外，这三个方法类别中的每一个都由三个重载的方法组成，它们是通过指定 JMS 目的地（队列或主题）的方式来区分的：

- 一个方法不接受目的地参数，并将消息发送到默认目的地。
- 一个方法接受指定消息目的地的目标对象。
- 一个方法接受一个 String，该 String 通过名称指定消息的目的地。

要使这些方法工作起来，下面程序清单中的 JmsOrderMessagingService，它使用 send() 方法的最基本形式。

使用send方法的例子：

```java
@Service
public class JmsOrderMessingService implements OrderMessagingService{
    private JmsTemplate jmsTemplate;
    @Autowired
    public JmsOrderMessingService(JmsTemplate jmsTemplate){
        this.jmsTemplate=jmsTemplate;
    }
    @Override
    public void sendOrder(Order order){
        jmsTemplate.send(new MessageCreator() {
            //这里没有指明目的地，所以可以在.yml文件设置属性
            @Override
            public Message createMessage(Session session) throws JMSException {
                return session.createObjectMessage((Serializable) order);
            }
        });
    }
}
```

application.yml

```yml
spring:
  jms:
    template:
      default-destination: tacocloud.order.queue
```

在许多情况下，使用缺省目的地是最简单的选择。它让你指定一次目的地名称，允许代码只关心发送消息，而不关心消息被发送到哪里。但是，如果需要将消息发送到缺省目的地之外的目的地，则需要将该目的地指定为 send() 方法的参数。

一种方法是传递目标对象作为 send() 的第一个参数。最简单的方法是声明一个 Destination bean，然后将其注入执行消息传递的 bean。 声明Taco Cloud 订单队列 Destination：

```java
public Destination orderQueue() {
    return new ActiveMQQueue("tacocloud.order.queue");
    //ActiveMQQueue 实际上来自于 Artemis（来自 org.apache.activemq.artemis.jms.client 包)
    //这样就可以在send前自动注入，并在send中使用destination
    //或者直接将名称作为参数："tacocloud.order.com"
}
```

#### 在发送前转换消息

JmsTemplates 的 convertAndSend() 方法不需要提供 MessageCreator，简化了消息发布。相反，将要直接发送的对象传递给 convertAndSend()，在发送之前会将该对象转换为消息。

sendOrder() 的以下重新实现使用 convertAndSend() 将 Order 发送到指定的目的地：

```java
@Override
public void sendOrder(Order order) {
    jms.convertAndSend("tacocloud.order.queue", order);
    //接受 Destination 或 String 值来指定目的地，或者可以完全忽略目的地来将消息发送到默认目的地。
    //传递给 convertAndSend() 的 Order 都会在发送之前转换为消息。
}
```

#### 配置消息转换器

MessageConverter 是 Spring 定义的接口，它只有两个用于实现的方法：

```java
public interface MessageConverter {
    Message toMessage(Object object, Session session)
        throws JMSException, MessageConversionException;
    Object fromMessage(Message message);
}
```

这个接口的实现很简单，都不需要创建自定义实现。Spring 已经提供了一些有用的实现

![image-20210311011543748](C:%5CUsers%5Clenovo%5CAppData%5CRoaming%5CTypora%5Ctypora-user-images%5Cimage-20210311011543748.png)

使用 MappingJackson2MessageConverter ：

```java
@Bean
    public MappingJackson2MessageConverter messageConverter(){
        //这样会让接收者知道他要接受什么类型
        MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();
        messageConverter().setTypeIdPropertyName("_typeId");
        return mappingJackson2MessageConverter;
    }
```

为了实现更大的灵活性，可以通过调用消息转换器上的 setTypeIdMappings() 将合成类型名称映射到实际类型。

```java
@Configuration
public class messageConfig {
    @Bean
    public org.springframework.jms.support.converter.MappingJackson2MessageConverter messageConverter(){
        //这样会让接收者知道他要接受什么类型
        MappingJackson2MessageConverter messageConverter = new MappingJackson2MessageConverter();
        messageConverter.setTypeIdPropertyName("_typeId");
        Map<String,Class<?>> typeIdMappings = new HashMap<String,Class<?>>();
        typeIdMappings.put("order", Order.class);
        messageConverter.setTypeIdMappings(typeIdMappings);
        return messageConverter;
        //与在消息的 _typeId 属性中发送完全限定的类名不同，将发送值 order。在接收应用程序中，将配置类似的消息转换器，将 order 映射到它自己对 order 的理解。
    }
}
```

##### 后期处理消息

厨房如何区分web订单和实体店订单？如何处理此类问题？

解决方案是在消息中添加一个自定义头信息

如果正在使用 send() 方法发送 taco 订单，这可以通过调用消息对象上的 setStringProperty() 轻松实现：

```java
jms.send("tacocloud.order.queue",
        session -> {
            Message message = session.createObjectMessage(order);
            message.setStringProperty("X_ORDER_SOURCE", "WEB");
        });
```

但如果使用 convertAndSend()，Message 对象是在幕后创建的，并且不能访问它。

```java
@Override
    public void sendOrder(Order order){
        jmsTemplate.convertAndSend("TacoCloud.order.queue", order, message -> {
            message.setStringProperty("X_ORDER_SOURCE","WEB");
            return message;
        });
        //对象自动转换为消息
    }
```

使用同一个 MessagePostProcessor 来处理对 convertAndSend() 的几个不同调用。在这些情况下，也许方法引用是比 lambda 更好的选择，避免了不必要的代码重复：

```java
@GetMapping("/convertAndSend/order")
    public String convertAndSendOrder(){
        Order order = buildOrder();
        jmsTemplate.convertAndSend("tacoCloud.order.queue",order,this::addOrderSource);
        return "Convert and sent order";
    }
    private Message addOrderSource(Message message) throws JMSException{
        message.setStringProperty("X_ORDER_SOURCE","WEB");
        return message;
    }
```

#### 接收消息

在消费消息时，可以选择 *拉模型*（代码请求消息并等待消息到达）或 *推模型*（消息可用时将消息传递给代码）。

拉模型调用JmsTemplate接收消息的其中一个方法来请求消息，线程会发生阻塞，直到消息可用为止

推模型定义了一个消息监听器，它在消息可用时被调用。

推模型是最佳选择，因为它不会阻塞线程。但是在某些用例中，如果消息到达得太快，侦听器可能会负担过重。拉模型允许使用者声明他们已经准备好处理新消息。

#### 使用 JmsTemplate 接收

JmsTemplate 提供多个用于拉模式的方法：

```java
Message receive() throws JmsException;
Message receive(Destination destination) throws JmsException;
Message receive(String destinationName) throws JmsException;

Object receiveAndConvert() throws JmsException;
Object receiveAndConvert(Destination destination) throws JmsException;
Object receiveAndConvert(String destinationName) throws JmsException;
```

receive() 方法接收原始消息，而 receiveAndConvert() 方法使用配置的消息转换器将消息转换为域类型。对于其中的每一个，可以指定 Destination 或包含目的地名称的 String，也可以从缺省目的地获取一条消息。

使用 JmsTemplate.receive() 接收 Order 数据的服务组件:

```java
@Service
public class JmsOrderReceiver implements OrderReceiver{
    private JmsTemplate jmsTemplate;
    private MessageConverter messageConverter;

    @Autowired
    public JmsOrderReceiver(JmsTemplate jmsTemplate,MessageConverter messageConverter){
        this.jmsTemplate = jmsTemplate;
        this.messageConverter = messageConverter;
    }
    public Order receiveOrder() throws JMSException {
        Message message = jmsTemplate.receive("tacocloud.order.queue");
        //String 来指定从何处拉取订单,receive() 方法返回一个未转换的 Message。接下来使用注入的消息转换器来转换消息。消息中的类型 ID 属性将指导转换器将其转换为 Order
        return (Order) messageConverter.fromMessage(message);
    }
}
```

接收原始 Message 对象在某些需要检查消息属性和标题的情况下可能很有用，但是通常只需要有效载荷。将有效负载转换为域类型需要两个步骤，需要将消息转换器注入组件。

```java
@Service
public class JmsOrderReceiver implements OrderReceiver{
    private JmsTemplate jmsTemplate;
    @Autowired
    public JmsOrderReceiver(JmsTemplate jmsTemplate){
        this.jmsTemplate=jmsTemplate;
    }
    //使用receiveAndConvert就不需要MessageConverter
    public Order receiverOrder() throws  JMSException{
        return (Order)jmsTemplate.receiveAndConvert("tacoloud.order.queue");
    }

}
```

receiveOrder() 将被调用，而对 receive() 或 receiveAndConvert() 的调用将被阻塞。在订单消息准备好之前，不会发生任何其他事情。一旦订单到达，它将从 receiveOrder() 中返回，其信息用于显示订单的详细信息，以便食品加工人员开始工作。

##### 推模型：声明消息监听器

消息监听器是一个被动组件，在消息到达之前是空闲的。

创建对 JMS 消息作出响应的消息监听器，使用 @JmsListener 对组件中的方法进行注解。如下OrderListener 组件，它被动地监听消息，而不是主动地请求消息。

```java
@Component
public class OrderListener {
    private KitchenUI ui;

    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui = ui;
    }
    @JmsListener(destination = "tacocloud.order.queue")
    public void receiverOrder(Order order){
        //receiveOrder() 方法由 JmsListener 注解，以监听 tacocloud.order.queue 目的地的消息。它不处理 JmsTemplate，也不被应用程序代码显式地调用。相反，Spring 中的框架代码将等待消息到达指定的目的地，当消息到达时，receiveOrder() 方法将自动调用，并将消息的 Order 有效负载作为参数
        ui.displayOrder(order);
    }
}
```

@JmsListener 注解类似于 Spring MVC 的请求映射注释 @GetMapping 或 @PostMapping。在 Spring MVC 中，用一个请求映射方法注解的方法对指定路径的请求做出响应。类似地，使用 @JmsListener 注解的方法对到达目的地的消息做出响应。

### 使用 RabbitMQ 和 AMQP

RabbitMQ 可以说是 AMQP 最优秀的实现, JMS 消息使用接收方将从中检索它们的目的地的名称来寻址，而 AMQP 消息使用交换器的名称和路由键来寻址，它们与接收方正在监听的队列解耦。

![image-20210311111121707](C:%5CUsers%5Clenovo%5CAppData%5CRoaming%5CTypora%5Ctypora-user-images%5Cimage-20210311111121707.png)

交换方式：

*Default* —— 一种特殊的交换器，通过 broker 自动创建。它将消息路由到与消息的路由键的值同名的队列中。所有的队列将会自动地与交换器绑定。

*Direct* —— 路由消息到消息路由键的值与绑定值相同的队列。

*Topic* —— 将消息路由到一个或多个队列，其中绑定键（可能包含通配符）与消息的路由键匹配。

*Fanout* —— 将消息路由到所有绑定队列，而不考虑绑定键或路由键。

*Headers* —— 与 topic 交换器类似，只是路由基于消息头值而不是路由键。

*Dead letter* —— 对无法交付的消息（意味着它们不匹配任何已定义的交换器与队列的绑定）的全部捕获。

#### 添加依赖：

将 Spring Boot 的 AMQP starter 依赖项添加到构建中，以取代在前一节中添加的 Artemis 或 ActiveMQ starter：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

之后，自动配置将创建 AMQP 连接工厂和 RabbitTemplate bean，以及其他支持组件。只需添加此依赖项，就可以开始使用 Spring 从 RabbitMQ broker 发送和接收消息

配置 RabbitMQ broker 的位置和凭据的属性：

![image-20210311112817714](C:%5CUsers%5Clenovo%5CAppData%5CRoaming%5CTypora%5Ctypora-user-images%5Cimage-20210311112817714.png)

出于开发目的，可能有一个 RabbitMQ Broker，它不需要在本地机器上运行身份验证，监听端口 5672。当还在开发阶段时，这些属性可能不会有太大的用处，但是当应用程序进入生产环境时，它们无疑会很有用。

例如，假设在进入生产环境时，RabbitMQ Broker 位于一个名为 rabbit.tacocloud.com 的服务器上，监听端口 5673，并需要凭据。在这种情况下，应用程序中的以下配置。当 prod 配置文件处于活动状态时，yml 文件将设置这些属性：

```yml
spring:
  profiles: prod
  rabbitmq:
    host: rabbit.tacocloud.com
    port: 5673
    username: tacoweb
    password: l3tm31n
```

#### 使用 RabbitTemplate 发送消息

使用 RabbitTemplate 发送消息，send() 和 convertAndSend() 方法与来自 JmsTemplate 的同名方法并行。但是它只将消息路由到给定的队列或主题，RabbitTemplate 方法根据交换和路由键发送消息。

```java
// 发送原始消息
void send(Message message) throws AmqpException;
void send(String routingKey, Message message) throws AmqpException;
void send(String exchange, String routingKey, Message message) throws AmqpException;
// 发送从对象转换过来的消息
void convertAndSend(Object message) throws AmqpException;
void convertAndSend(String routingKey, Object message) throws AmqpException;
void convertAndSend(String exchange, String routingKey, Object message) throws AmqpException;
// 发送经过处理后从对象转换过来的消息
void convertAndSend(Object message, MessagePostProcessor mPP) throws AmqpException;
void convertAndSend(String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;
void convertAndSend(String exchange, String routingKey, Object message, MessagePostProcessor messagePostProcessor) throws AmqpException;
```

使用 RabbitTemplate.send() 发送消息

```java
public class RabbitOrderMessagingService implements OrderMessagingService {
    private RabbitTemplate rabbitTemplate;
    @Autowired
    public RabbitOrderMessagingService(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }
    @Override
    public void sendOrder(Order order) {
        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        MessageProperties messageProperties = new MessageProperties();
        Message message = messageConverter.toMessage(order,messageProperties);
        rabbitTemplate.send("tacocloud.order",message);
        //交换键省略，路由键提供
    }
}
```

默认交换，默认交换名称是 “”（一个空 String ），它对应于 RabbitMQ Broker 自动创建的默认交换。同样，默认的路由键是 “”（其路由取决于所涉及的交换和绑定）。

可以通过设置 spring.rabbitmq.template.exchange 和 spring.rabbitmq.template.routing-key 属性来覆盖这些缺省值：

```yml
spring:
  rabbitmq:
    template:
      exchange: tacocloud.orders
      routing-key: kitchens.central
```

在这种情况下，所有发送的消息都将自动发送到名为 tacocloud.orders 的交换器。如果在 send() 或 convertAndSend() 调用中也未指定路由键，则消息将有一个 kitchens.central 的路由键。

使用 convertAndSend() 让 RabbitTemplate 处理所有的转换工作更加简单

```java
public void sendOrder(Order order) {
    rabbit.convertAndSend("tacocloud.order", order);
}
```

##### 配置消息转换器

默认情况下，使用 SimpleMessageConverter 执行消息转换，SimpleMessageConverter 能够将简单类型（如 String）和可序列化对象转换为消息对象。

Spring 为 RabbitTemplate 提供了其他几个消息转换器，包括：

```
Jackson2JsonMessageConverter —— 使用Jackson 2 JSON处理器将对象与 JSON 进行转换
MarshallingMessageConverter —— 使用 Spring 的序列化和反序列化抽象转换 String 和任何类型的本地对象
SimpleMessageConverter —— 转换 String、字节数组和序列化类型
ContentTypeDelegatingMessageConverter —— 基于 contentType 头信息将对象委托给另一个 MessageConverter
MessagingMessageConverter —— 将消息转换委托给底层 MessageConverter，将消息头委托给 AmqpHeaderConverter
```

如果需要修改消息转换器，需要做的是配置 MessageConverter bean：

```java
@Bean
public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
}
```

Spring Boot 的自动配置将会发现这个 bean 并 RabbitTemplate 的缺省的消息转换器那里。

##### 设置消息属性

与 JMS 一样，可能需要在发送的消息中设置一些标题。例如，假设需要为通过 Taco Cloud 网站提交的所有订单发送一个 X_ORDER_SOURCE。在创建 Message 对象时，可以通过提供给消息转换器的 MessageProperties 实例设置消息头。

```java
public void sendOrder(Order order) {
    MessageConverter converter = rabbit.getMessageConverter();
    MessageProperties props = new MessageProperties();
    props.setHeader("X_ORDER_SOURCE", "WEB");
    //在setOrder中加入一段即可
    Message message = converter.toMessage(order, props);
    rabbit.send("tacocloud.order", message);
}
```

在使用 convertAndSend() 时，不能快速访问 MessageProperties 对象。不过，利用MessagePostProcessor 可以做到

```java
@Override
    public void sengOrder(Order order){
        rabbitTemplate.convertAndSend("tacocloud.order", order, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                MessageProperties messageProperties = message.getMessageProperties();
                messageProperties.setHeader("X_ORDER_SOURCE", "WEB");
                return message;
            }
        });
    }
```

在 convertAndSend() 中使用 MessagePostProcessor 的匿名内部类进行实现 。在 postProcessMessage() 方法中，首先从消息中获取 MessageProperties，然后调用 setHeader() 来设置 X_ORDER_SOURCE 头信息。 

##### 从 RabbitMQ 接收消息

与 JMS 一样，有两个选择：

- 使用 RabbitTemplate 从队列中拉取消息
- 获取被推送到 @RabbitListener 注解的方法中的消息

```
// 接收消息
Message receive() throws AmqpException;
Message receive(String queueName) throws AmqpException;
Message receive(long timeoutMillis) throws AmqpException;
Message receive(String queueName, long timeoutMillis) throws AmqpException;

// 接收从消息转换过来的对象
Object receiveAndConvert() throws AmqpException;
Object receiveAndConvert(String queueName) throws AmqpException;
Object receiveAndConvert(long timeoutMillis) throws AmqpException;
Object receiveAndConvert(String queueName, long timeoutMillis) throws AmqpException;

// 接收从消息转换过来的类型安全的对象
<T> T receiveAndConvert(ParameterizedTypeReference<T> type) throws AmqpException;
<T> T receiveAndConvert(String queueName, ParameterizedTypeReference<T> type) throws AmqpException;
<T> T receiveAndConvert(long timeoutMillis, ParameterizedTypeReference<T> type) throws AmqpException;
<T> T receiveAndConvert(String queueName, long timeoutMillis, ParameterizedTypeReference<T> type) throws AmqpException;
```

首先，这些方法都不以交换键或路由键作为参数。这是因为交换和路由键用于将消息路由到队列，但是一旦消息在队列中，它们的下一个目的地就是将消息从队列中取出的使用者。使用应用程序不需要关心交换或路由键，队列是在消费应用程序是仅仅需要知道一个东西。

许多方法接受一个 long 参数来表示接收消息的超时。默认情况下，接收超时为 0 毫秒。也就是说，对 receive() 的调用将立即返回，如果没有可用的消息，则可能返回空值。这与 receive() 方法在 JmsTemplate 中的行为有明显的不同。通过传入超时值，可以让 receive() 和 receiveAndConvert() 方法阻塞，直到消息到达或超时过期。但是，即使使用非零超时，代码也要准备好处理返回的 null 值。

```java
@Component
public class RabbitOrderReceiver {
    private RabbitTemplate rabbitTemplate;
    private MessageConverter messageConverter;
    @Autowired
    public RabbitOrderReceiver(RabbitTemplate rabbitTemplate,MessageConverter messageConverter){
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    public Order receiveOrder(){
        Message message = rabbitTemplate.receive("tacocloud.order.queue",30000);
        //这个值也可以在yml中设置
        return message!=null? (Order)messageConverter.fromMessage(message) : null;
    }

}
```

```yml
spring:
  rabbitmq:
    template:
      receive-timeout: 30000
```

也可以直接使用receiveAndConvert

```java
public Order receiveOrder(){
        return (Order) rabbitTemplate.receiveAndConvert("tacocloud.order.queue");
    }
```

可以在类型转换的地方使用更类型安全的PTR

```java
 public Order receiveOrder(){
        return rabbitTemplate.receiveAndConvert("tacocloud.order.queue", new ParameterizedTypeReference<Order>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });
    }
```

##### 使用监听器处理 RabbitMQ 消息

要指定当消息到达 RabbitMQ 队列时应该调用某个方法，在相应的 bean 方法上使用 @RabbitTemplate 进行注解 。

```java
@Component
public class OrderListener {
    private KitchenUI ui;
    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui = ui;
    }
    @RabbitListener(queues = "tacocloud.order.queue")
    public void receiveOrder(Order order){
        ui.displayOrder(order);
    }
}
```

### Kafka 发送消息

Kafka 被设计为在集群中运行，通过将其 topic 划分到集群中的所有实例中，它具有很强的弹性。RabbitMQ 主要处理 exchange 中的队列，而 Kafka 仅利用 topic 来提供消息的发布/订阅。

Kafka topic 被复制到集群中的所有 broker 中。集群中的每个节点充当一个或多个 topic 的 leader，负责该 topic 的数据并将其复制到集群中的其他节点。

更进一步说，每个 topic 可以分成多个分区。在这种情况下，集群中的每个节点都是一个 topic 的一个或多个分区的 leader，但不是整个 topic 的 leader。该 topic 的职责由所有节点分担。

#####  Spring 中设置 Kafka

Kafka 没有 Spring Boot starter。不过还是只需要一个依赖：

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

它的存在将触发 Kafka 的 Spring Boot 自动配置，它将在 Spring 应用程序上下文中生成一个 KafkaTemplate。你所需要做的就是注入 KafkaTemplate 并开始发送和接收消息。

KafkaTemplate 默认在 localhost 上运行 Kafka broker，并监听 9092 端口。在开发应用程序时，在本地启动 Kafka broker 是可以的，但是在进入生产环境时，需要配置不同的主机和端口。

spring.kafka.bootstrap-servers 属性设置一个或多个 Kafka 服务器的位置，用于建立到 Kafka 集群的初始连接。例如，如果集群中的 Kafka 服务器之一运行在 Kafka .tacocloud.com 上，并监听 9092 端口，那么可以在 YAML 中像这样配置它的位置：

```yml
spring:
  kafka:
    bootstrap-servers:
    - kafka.tacocloud.com:9092
    - kafka.tacocloud.com:9093
    - kafka.tacocloud.com:9094
```

####  KafkaTemplate 发送消息

```java
ListenableFuture<SendResult<K, V>> send(String topic, V data);
ListenableFuture<SendResult<K, V>> send(String topic, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, K key, V data);
ListenableFuture<SendResult<K, V>> send(String topic, Integer partition, Long timestamp, K key, V data);
ListenableFuture<SendResult<K, V>> send(ProducerRecord<K, V> record);
ListenableFuture<SendResult<K, V>> send(Message<?> message);
ListenableFuture<SendResult<K, V>> sendDefault(V data);
ListenableFuture<SendResult<K, V>> sendDefault(K key, V data);
ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, K key, V data);
ListenableFuture<SendResult<K, V>> sendDefault(Integer partition, Long timestamp, K key, V data);
```

没有 convertAndSend() 方法。这是因为 KafkaTemplate 是用的泛型，同时能够在发送消息时直接处理域类型。在某种程度上，所有的 send() 方法都在做 convertAndSend() 的工作。

当使用 Kafka 发送消息时，可以指定以下参数来指导如何发送消息：

- 发送消息的 topic（send() 方法必要的参数）
- 写入 topic 的分区（可选）
- 发送记录的键（可选）
- 时间戳（可选；默认为 System.currentTimeMillis()）
- payload（必须）

对于 send() 方法，还可以选择发送一个 ProducerRecord，它与在单个对象中捕获所有上述参数的类型差不多。也可以发送 Message 对象，但是这样做需要将域对象转换为 Message。通常，使用其他方法比创建和发送 ProducerRecord 或 Message 对象更容易。

使用 KafkaTemplate 及其 send() 方法:

```java
@Service
public class KafkaOrderMessagingService implements OrderMessagingService {

    private KafkaTemplate<String,Order> kafkaTemplate;
    
    @Autowired
    public KafkaOrderMessagingService(KafkaTemplate<String, Order> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }

    @Override
    public void sendOrder(Order order) {
        kafkaTemplate.send("tacocloud.orders.topic",order);
    }
}
```

简化：通过设置 spring.kafka.template.default-topic 属性，将默认主题设置为 tacocloud.orders.topic：

```yml
spring:
  kafka:
    template:
      default-topic: tacocloud.orders.topic
```

然后，在 sendOrder() 方法中，可以调用 sendDefault() 而不是 send()，并且不指定主题名称：

```java
@Override
public void sendOrder(Order order) {
    kafkaTemplate.sendDefault(order);
}
```

##### 编写 Kafka 监听器

kafka不提供任何接收消息的方法，使用 Spring 消费来自 Kafka 主题的消息的唯一方法是编写消息监听器。

```java
@Component
public class OrderListener {
    private KitchenUI ui;
    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    }
    @KafkaListener(topics = "tacocloud.orders.topic")
    public void handle(Order order){
        ui.displayOrder(order);
    }
}
```

只为 handle() 方法提供了一个 Order（payload）参数 。但是，如果需要来自消息的其他元数据，它也可以接受一个 ConsumerRecord 或 Message 对象。

```java
@Component
@Slf4j
public class OrderListener {
    private KitchenUI ui;
    @Autowired
    public OrderListener(KitchenUI ui){
        this.ui=ui;
    }
    @KafkaListener(topics = "tacocloud.orders.topic")
    public void handle(Order order, Message<Order> message){
        MessageHeaders headers = message.getHeaders();
        log.info("Received from partition {} with timestamp {}",headers.get(KafkaHeaders.RECEIVED_PARTITION_ID),headers.get(KafkaHeaders.RECEIVED_TIMESTAMP));
        ui.displayOrder(order);
    }
}
```

#### Spring 集成

#####  声明集成流

创建一个向文件系统写入数据的集成流。

首先，需要将 Spring Integration 添加到项目构建中。对于 Maven，必要的依赖关系如下：

第一个依赖项是 Spring Integration 的 Spring Boot starter。无论 Spring Integration 流可能与什么集成，这种依赖关系都是开发 Spring Integration 流所必需的。

第二个依赖项是 Spring Integration 的文件端点模块。文件端点模块提供了将文件从文件系统提取到集成流或将数据从流写入文件系统的能力

```xml
	<dependency>
		<groupId>org.springframework.integration</groupId>
		<artifactId>spring-integration-core</artifactId>
		<version>5.4.4</version>
	</dependency>
	<dependency>
		<groupId>org.springframework.integration</groupId>
		<artifactId>spring-integration-file</artifactId>
		<version>5.3.4.RELEASE</version>
	</dependency>
```

需要为应用程序创建一种将数据发送到集成流的方法，以便将数据写入文件。为此，将创建一个网关接口

```java
@MessagingGateway(defaultRequestChannel = "textInChannel")
//由 @MessagingGateway 注解,Spring Integration 在运行时生成这个接口的实现
//@MessagingGateway 的 defaultRequestChannel 属性表示，对接口方法的调用产生的任何消息都应该发送到textInChannel的消息通道。
public interface FileWriterGateway {
    void writeToFile(
            @Header(FileHeaders.FILENAME) String filename,String data
            //接受一个文件名作为字符串，另一个字符串包含应该写入文件的文本。
            //@Header 注解指示传递给 filename 的值应该放在消息头中（指定为 FileHeaders），解析为 file_name 的文件名，而不是在消息有效负载中。另一方面，数据参数值则包含在消息有效负载中。
    );
}
```

仍然需要编写额外的配置来定义满足应用程序需求的流。声明集成流的三个配置选项包括：

- XML 配置
- Java 配置
- 使用 DSL 进行 Java 配置

使用java配置编写集成流

```java
@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    public MessageChannel textInChannel(){
        return new DirectChannel();//直接把Channel配置为bean，更方便配置
    }
    @Bean MessageChannel fileWriterChannel(){
        return new DirectChannel();
    }
    @Bean//转换器
    @Transformer(inputChannel = "textInChannel",outputChannel = "fileWriterChannel")
    //指定为集成流中的转换器,接收名为 textInChannel 的通道上的消息，并将消息写入名为 fileWriterChannel 的通道。
    public GenericTransformer<String,String> upperCaseTransformer(){
        return String::toUpperCase;
        //
    }
    @Bean//文件写入消息处理程序
    @ServiceActivator(inputChannel = "fileWriterChannel")
    // @ServiceActivator注解，指示它将接受来自 fileWriterChannel 的消息，并将这些消息传递给由 FileWritingMessageHandler 实例定义的服务。
    public FileWritingMessageHandler fileWriter(){
        //使用消息的 file_name 头中指定的文件名将消息有效负载写入指定目录中的文件。
        FileWritingMessageHandler handler = new FileWritingMessageHandler(new File("/tmp/sia5/files"));
        handler.setExpectReply(false);
        // setExpectReply(false) 来指示服务激活器不应该期望应答通道
        //如果不调用 setExpectReply()，则文件写入 bean 默认为 true，尽管管道仍按预期工作，但将看到记录了一些错误，说明没有配置应答通道。
        handler.setFileExistsMode(FileExistsMode.APPEND);
        handler.setAppendNewLine(true);
        return handler;
        //不需要显式地声明通道。如果不存在具有这些名称的 bean，就会自动创建 textInChannel 和 fileWriterChannel 通道。
    }
}
```

#### 使用 Spring Integration 的 DSL 配置

```java
@Configuration
public class FileWriterIntegrationConfig {
    @Bean
    public IntegrationFlow fileWriterFlow(){
        //捕获整个流,IntegrationFlows 类初始化Builder API
        return IntegrationFlows
                .from(MessageChannels.direct("textInChannel"))
                //textInChannel自动创建并加入容器
                //从名为 textInChannel 的通道接收消息
                .<String,String>transform(t -> t.toUpperCase())
                //转到一个转换器，使消息有效负载大写
                .channel(MessageChannels.direct("fileWriterChannel"))//可以不配置，如果要配置，如此即可
                .handle(Files.outboundAdapter(new File("/tmp/sia5/files"))
                .fileExistsMode(FileExistsMode.APPEND)
                .appendNewLine(true))
                //消息由出站通道适配器处理，该适配器是根据 Spring Integration 的文件模块中提供的文件类型创建的。
                .get();
        //get() 构建要返回的 IntegrationFlow
    }
}

```

#### Spring Integration的基础

集成流由以下一个或多个组件组成。这些组件在集成流中所扮演的角色：

*Channels* —— 将信息从一个元素传递到另一个元素。

*Filters* —— 有条件地允许基于某些标准的消息通过流。

*Transformers* —— 更改消息值或将消息有效负载从一种类型转换为另一种类型。

*Routers* —— 直接将信息发送到几个渠道之一，通常是基于消息头。

*Splitters* —— 将收到的信息分成两条或多条，每条都发送到不同的渠道。

*Aggregators* —— 与分离器相反，它将来自不同渠道的多条信息组合成一条信息。

*Service activators* —— 将消息传递给某个 Java 方法进行处理，然后在输出通道上发布返回值。

*Channel adapters* —— 将通道连接到某些外部系统或传输。可以接受输入，也可以向外部系统写入。

*Gateways* —— 通过接口将数据传递到集成流。

#### 消息通道

Spring Integration 提供了多个管道的实现，包括以下这些：

- PublishSubscribeChannel —— 消息被发布到 PublishSubscribeChannel 后又被传递给一个或多个消费者。如果有多个消费者，他们都将会收到消息。
- QueueChannel —— 消息被发布到 QueueChannel 后被存储到一个队列中，直到消息被消费者以先进先出（FIFO）的方式拉取。如果有多个消费者，他们中只有一个能收到消息。
- PriorityChannel —— 与 QueueChannel 类似，但是与 FIFO 方式不同，消息被冠以 priority 的消费者拉取。
- RendezvousChannel —— 与 QueueChannel 期望发送者阻塞通道，直到消费者接收这个消息类似，这种方式有效的同步了发送者与消费者。
- DirectChannel —— 与 PublishSubscribeChannel 类似，但是是通过在与发送方相同的线程中调用消费者来将消息发送给单个消费者，此通道类型允许事务跨越通道。
- ExecutorChannel —— 与 DirectChannel 类似，但是消息分派是通过 TaskExecutor 进行的，在与发送方不同的线程中进行，此通道类型不支持事务跨通道。
- FluxMessageChannel —— Reactive Streams Publisher 基于 Project Reactor Flux 的消息通道。

在 Java 配置和 Java DSL 样式中，输入通道都是自动创建的，默认是 DirectChannel。但是，如果希望使用不同的通道实现，则需要显式地将通道声明为 bean 并在集成流中引用它。

比如：

```java
@Bean
public MessageChannel orderChannel() {
    return new PublishSubscribeChannel();
}
```

然后在集成流定义中通过名称引用这个通道。

```java
@ServiceActovator(inputChannel="orderChannel")
```

如果使用 Java DSL 配置方式，需要通过调用 channel() 方法引用它

```java
@Bean
public IntegrationFlow orderFlow() {
    return IntegrationFlows
        ...
        .channel("orderChannel")
        ...
        .get();
}
```

如果使用 QueueChannel，则必须为使用者配置一个轮询器poller。例如：

```java
@Bean
public MessageChannel orderChannel() {
    return new QueueChannel();
}
```

需要确保将使用者配置为轮询消息通道。在服务激活器的情况下，@ServiceActivator 注解可能是这样的：

```java
@ServiceActivator(inputChannel="orderChannel", poller=@Poller(fixedRate="1000"))
//服务激活器每秒（或 1,000 ms）从名为 orderChannel 的通道轮询一次
```

##### 过滤器

过滤器可以放置在集成管道的中间，以允许或不允许消息进入流中的下一个步骤

只希望偶数传递到名为 evenNumberChannel 的通道。：

```java
@Bean
public IntegrationFlow evenNumberFlow(AtomicInteger integerSource) {
    return IntegrationFlows
        ...
        .<Integer>filter((p) -> p % 2 == 0)
        ...
        .get();
}
```

如果过滤器过于复杂，可以实现GenericSelector接口作为替代方案，而不是lambda表达式

#### 转换器

转换器对消息执行一些操作，通常会产生不同的消息，并且可能会产生不同的负载类型。

假设正在一个名为 numberChannel 的通道上发布整数值，并且希望将这些数字转换为包含等效罗马数字的 String 字符串

```java
@Bean
    @Transformer(inputChannel = "numberChannel",outputChannel = "romanNumberChannel")
    // @Transformer 注解将 bean 指定为 transformer bean
    //从名为 numberChannel 的通道接收整数值，并使用 toRoman()的静态方法进行转换，得到的结果被发布到名为 romanNumberChannel 的通道中。
    //toRoman在一个名为 RomanNumbers 的类中静态定义的，并在这里通过方法引用进行引用
    public GenericTransformer<Integer,String> romanNumTransformer(){
        return RomanNumbers::toRoman;
    }
```

在 Java DSL 配置风格中，直接使用build风格把方法引用传递给 toRoman() 方法即可

```java
@Bean
public IntegrationFlow transformerFlow() {
    return IntegrationFlows
        ...
        .transform(RomanNumbers::toRoman)
        ...
        .get();
}
```

如果 transformer 比较复杂，需要单独的成为一个 Java 类，可以将它作为 bean 注入流配置，并将引用传递给 transform() 方法：

```java
@Bean
public RomanNumberTransformer romanNumberTransformer() {
    return new RomanNumberTransformer();
    //声明了一个 RomanNumberTransformer 类型的 bean，它本身是 Spring Integration 的 Transformer 或 GenericTransformer 接口的实现
}
@Bean
public IntegrationFlow transformerFlow(
    RomanNumberTransformer romanNumberTransformer) {
    //bean 被注入到 transformerFlow() 方法，并在定义集成流时传递给 transform() 方法。
    return IntegrationFlows
        ...
        .transform(romanNumberTransformer)
        ...
        .get();
}
```

#### 路由

基于某些路由标准的路由器允许在集成流中进行分支，将消息定向到不同的通道。

假设有一个名为 numberChannel 的通道，整数值通过它流动。假设希望将所有偶数消息定向到一个名为 evenChannel 的通道，而将奇数消息定向到一个名为 oddChannel 的通道。要在集成流中创建这样的路由，可以声明一个 AbstractMessageRouter 类型的 bean，并使用 @Router 注解该 bean：

```java
@Bean
    public MessageChannel evenChannel(){
        return new DirectChannel();
    }
    @Bean
    public MessageChannel oddChannel(){
        return new DirectChannel();
    }
    @Bean
    @Router(inputChannel = "numberChannel")
    public AbstractMessageRouter evenOdderRouter(){
        return new AbstractMessageRouter() {
            //AbstractMessageRouter bean 接受来自名为 numberChannel 的输入通道的消息。
            @Override
            protected Collection<MessageChannel> determineTargetChannels(Message<?> message) {
                //定义为匿名内部类的实现检查消息有效负载
                Integer number = (Integer) message.getPayload();
                if(number%2==0){
                    return Collections.singleton(oddChannel());
                    //它是偶数，则返回名为 evenChannel 的通道
                }
                return Collections.singleton(oddChannel());
                //否则，通道有效载荷中的数字必须为奇数
            }
        };
    }
```

在JavaDSL中，路由器是通过在流定义过程中调用 route() 来声明的，

```java
@Bean
public IntegrationFlow numberRoutingFlow(AtomicInteger source) {
    return IntegrationFlows
        ...
        .<Integer, String>route(n -> n%2==0 ? "EVEN":"ODD", mapping ->
            mapping.subFlowMapping("EVEN", sf -> 
               sf.<Integer, Integer>transform(n -> n * 10).handle((i,h) -> { ... }))
                 .subFlowMapping("ODD", sf -> 
                     sf.transform(RomanNumbers::toRoman).handle((i,h) -> { ... }))
            )
        .get();
}
```

如果是偶数，则返回一个偶数的字符串值。如果是奇数，则返回奇数。然后使用这些值来确定哪个子映射将处理消息。

##### 分割器

将消息拆分为多个独立处理的消息，使用splitter的情况：

* 消息有效载荷，包含单个消息有效载荷相同类型的项的集合。

* 信息有效载荷，携带的信息虽然相关，但可以分为两种或两种以上不同类型的信息。

当将消息有效负载拆分为两个或多个不同类型的消息时，通常只需定义一个 POJO 即可，该 POJO 提取传入的有效负载的各个部分，并将它们作为集合的元素返回。

```java
public class OrderSplitter {
    public Collection<Object> splitOrderIntoParts(PurchaseOrder po){
        ArrayList<Object> parts = new ArrayList<>();
        //将携带购买订单的消息拆分为两条消息：一条携带账单信息，另一条携带项目列表。
        parts.add(po.getBillingInfo());
        parts.add(po.getLineItems());
        return parts;
    }
    @Bean
    @Splitter(inputChannel = "poChannel",outputChannel = "splitOrderChannel")
    //@Splitter 注解将 OrderSplitter bean 声明为集成流的一部分
    public OrderSplitter orderSplitter(){
        return new OrderSplitter();
    }
    //购买订单到达名为 poChannel 的通道，并被 OrderSplitter 分割。然后，将返回集合中的每个项作为集成流中的单独消息发布到名为 splitOrderChannel 的通道。
    @Bean
    @Router(inputChannel = "splitOrderChannel")
    public MessageRouter splitOrderRouter(){
        PayloadTypeRouter router = new PayloadTypeRouter();
        //将消息路由到不同的通道
        router.setChannelMapping(
                BillingInfo.class.getName(),"billingInfoChannel"
        );
        //将有效负载为类型为 BillingInfo 的消息路由到一个名为 billingInfoChannel 的通道进行进一步处理。
        router.setChannelMapping(List.class.getName(),"lineItemChannel");
        //将 List 类型的有效负载映射到名为 lineItemsChannel 的通道中。
        return router;
    }
}
```

如果想进一步分割 LineItem 列表，分别处理每个 LineItem，要将列表拆分为多个消息（每个行项对应一条消息），只需编写一个方法（而不是 bean），该方法使用 @Splitter 进行注解，并返回 LineItems 集合

```java
@Splitter(inputChannel = "lineItemsChannel",outputChannel = "lineItemChannel")
    public List<LineItem> lineItemSplitter(List<LineItem> lineItems){
        return lineItems;
        //当携带 List<LineItem> 的有效负载的消息到达名为 lineItemsChannel 的通道时，它将传递到 lineItemSplitter() 方法。
        //集合中的每个 LineItem 都以其自己的消息形式发布到名为 lineItemChannel 的通道。
    }
```

使用 Java DSL 来声明相同的 Splitter/Router 配置，可以调用 split() 和 route()：

```java
return IntegrationFlows
    ...
    .split(orderSplitter())
    .<Object, String> route(p -> {
        if (p.getClass().isAssignableFrom(BillingInfo.class)) {
            return "BILLING_INFO";
        } else {
            return "LINE_ITEMS";
        }
    }, mapping ->
           mapping.subFlowMapping("BILLING_INFO", sf -> 
                      sf.<BillingInfo> handle((billingInfo, h) -> { ... }))
                  .subFlowMapping("LINE_ITEMS", sf -> 
                       sf.split().<LineItem> handle((lineItem, h) -> { ... }))
    )
    .get();
```

##### 服务激活器

服务激活器从输入信道接收消息并发送这些消息给 MessageHandler的实现（调用某个服务）

Spring 集成提供了多种的 MessageHandler 实现开箱即用，但也可以定制实现充当服务激活

如何声明 MessageHandler bean，构成为一个服务激活器

```java
@Bean
    @ServiceActivator(inputChannel = "someChannel")
    //@ServiceActivator 注解 bean，将其指定为一个服务激活器
    public MessageHandler sysOutHandler(){
        return message -> {
            System.out.println("Message payload:" + message.getPayload());
            //给定的消息时，它发出其有效载荷的标准输出流
        };
    }
```

另外，可以声明一个服务激活器，用于在返回一个新的有效载荷之前处理传入的消息。在这种情况下，这个 bean 应该是一个 GenericHandler 而非的 MessageHandler：

```java
@Bean
    @ServiceActivator(inputChannel = "orderChannel",outputChannel = "completeOrder")
    public GenericHandler<Order> orderHandler(JPAOrderRepository orderRepository){
    //服务激活器是一个 GenericHandler，有效载荷为 Order 类型
        return (payload,headers) -> {
        //保存 Order 后产生的结果被发送到名称为 completeChannel 的输出通道。
            return orderRepository.save(payload);
            //当订单到达，它是通过 repository 进行保存；
            //返回一个新的有效载荷之前处理传入的消息
        };
    }
```

同时也可以通过传递了 MessageHandler 或 GenericHandler 去调用在流定义中的 handler() 方法，来使用在 Java DSL 配置式中的服务激活器：

```java
public IntegrationFlow someFlow() {
    return IntegrationFlows
        ...
        .handle(msg -> {
            System.out.println("Message payload: " + msg.getPayload());
            //MessageHandler 是作为一个 lambda，但也可以将它作为一个参考方法甚至是一个类，它实现了 MessageHandler 接口。
        })
        .get();
}
```

如果服务激活器不是流的结束，handler() 可以接受 GenericHandler 。从之前应用订单存储服务激活器来看，可以使用 Java DSL 对流程进行配置

```java
public IntegrationFlow orderFlow(OrderRepository orderRepo) {
    return IntegrationFlows
        ...
        .<Order>handle((payload, headers) -> {
            return orderRepo.save(payload);
        })
        ...
        .get();
}
```

如果选择在一个流程的结束使用 GenericHandler，需要返回 null，否则会得到这表明有没有指定输出通道的错误。

##### 网关

网关是通过一个应用程序可以将数据提交到一个集成信息流，也能可选的接收流的结果作为响应的，会声明为接口，应用借助Spring Integration调用他发送信息到集成流。

编写一个双向网关也很容易。当写网关接口时，确保该方法返回某个值发布到集成流。

假设一个网关处理接受一个 String 的简单集成信息流，并把特定的 String 转成大写。网关接口可能是这个样子：

```java
@Component
@MessagingGateway(defaultRequestChannel = "inChannel",defaultReplyChannel = "outChannel")
//没有必要实现这个接口,Spring Integration 自动提供运行时实现，这个实现会使用特定的通道进行数据的发送与接收。
public interface UpperCaseGateway {
    String uppercase(String in);
    //当 uppercase() 被调用时，给定的 String 被发布到名为 inChannel 的集成流通道中。而且，不管流是如何定义的或是它是做什么的，在当数据到达名为 outChannel 通道时，它从 uppercase() 方法中返回。
}

```

使用 Java DSL 配置的uppercase集成流

```java
@Bean
    public IntegrationFlow uppercaseFlow(){
        return IntegrationFlows
                .from("inChannel")
                .<String,String>transform(s -> s.toUpperCase())
                .channel("ouyChannel")
                .get();
    }
```

##### 通道适配器

通道适配器代表集成信息流的入口点和出口点。数据通过入站信道适配器的方式进入到集成流中，通过出站信道适配器的方式离开集成流。

入站信道的适配器可以采取多种形式，这取决于它们引入到流的数据源。

例如，声明一个入站通道适配器，它采用从 AtomicInteger 到流递增的数字。

```java
@Bean
    @InboundChannelAdapter(
            poller = @Poller(fixedRate = "1000"),channel = "numberChannel"
    )
    public MessageSource<Integer> numberSource(AtomicInteger source){
        return () -> {
            return new GenericMessage<>(source.getAndIncrement());
        };
    } 
```

在 Java DSL 配置中类似的输入通道适配器：

```java
@Bean
public IntegrationFlow someFlow(AtomicInteger integerSource) {
    return IntegrationFlows
        .from(integerSource, "getAndIncrement",
              c -> c.poller(Pollers.fixedRate(1000)))
              //from() 方法就是使用 Java DSL 来定义流的时候，表明它是怎么处理的。
        ...
        .get();
}
```

通常情况下，通道适配器通过的 Spring Integration 的多端点模块之一进行提供。

假设需要一个入站通道适配器，用它来监视指定的目录，同时将任何写入到那个目录中的文件作为消息，提交到名为 file-channel 的通道中。

下面的 Java 配置使用 FileReadingMessageSource 从 Spring Integration 的文件端点模块来实现这一目标：

```java
@Bean
@InboundChannelAdapter(channel="file-channel",
                       poller=@Poller(fixedDelay="1000"))
public MessageSource<File> fileReadingMessageSource() {
    FileReadingMessageSource sourceReader = new FileReadingMessageSource();
    sourceReader.setDirectory(new File(INPUT_DIR));
    sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
    return sourceReader;
}
```

或者基于Java DSL

```java
@Bean
public IntegrationFlow fileReaderFlow() {
    return IntegrationFlows
        .from(Files.inboundAdapter(new File(INPUT_DIR))
              .patternFilter(FILE_PATTERN))
        .get();
}
```

##### 端点模块

Spring Integration 提供了端点模块适配器，包括入站和出站，用于与各种常用外部系统进行集成

| 模块                      | 依赖的 Artifact ID             |
| ------------------------- | ------------------------------ |
| AMQP                      | spring-integration-amqp        |
| Spring application events | spring-integration-event       |
| RSS and Atom              | spring-integration-feed        |
| Filesystem                | spring-integration-file        |
| FTP/FTPS                  | spring-integration-ftp         |
| GemFire                   | spring-integration-gemfire     |
| HTTP                      | spring-integration-http        |
| JDBC                      | spring-integration-jdbc        |
| JPA                       | spring-integration-jpa         |
| JMS                       | spring-integration-jms         |
| Email                     | spring-integration-mail        |
| MongoDB                   | spring-integration-mongodb     |
| MQTT                      | spring-integration-mqtt        |
| Redis                     | spring-integration-redis       |
| RMI                       | spring-integration-rmi         |
| SFTP                      | spring-integration-sftp        |
| STOMP                     | spring-integration-stomp       |
| Stream                    | spring-integration-stream      |
| Syslog                    | spring-integration-syslog      |
| TCP/UDP                   | spring-integration-ip          |
| Twitter                   | spring-integration-twitter     |
| Web                       | Services spring-integration-ws |
| WebFlux                   | spring-integration-webflux     |
| WebSocket                 | spring-integration-websocket   |
| XMPP                      | spring-integration-xmpp        |
| ZooKeeper                 | spring-integration-zookeeper   |

##### 使用Email端点模块，创建Email集成流

实现一个集成信息流，用于轮询 Taco Cloud 收件箱中的 taco 订单电子邮件，并解析邮件订单的详细信息，然后提交订单到 Taco Cloud 进行处理。

总之，将从邮箱端点模块中使用入站通道适配器，用于把 Taco Cloud 收件箱中的邮件提取到集成流中。

在集成信息流中，电子邮件将被解析为订单对象，接着被传递到另外一个向 Taco Cloud 的 REST API 提交订单的处理器中，在那里，它们将如同其他订单一样被处理。

首先，定义一个简单的配置属性的类，来捕获如何处理 Taco Cloud 电子邮件：

```java
@Data
@Component
@ConfigurationProperties(prefix = "tacocloud.email")
//在 application.yml 配置文件中详细配置 email 的信息：
public class EmailProperties {
    private String username;
    private String password;
    private String host;
    private String mailbox;
    private long pollRate = 30000;

    public String getImapUrl(){
        return String.format("imaps://%s:%s%s/%s",this.username,this.password,this.host,this.mailbox);
        //使用 get() 方法来产生一个 IMAP URL。流就使用这个 URL 连接到 Taco Cloud 的电子邮件服务器，然后轮询电子邮件。所捕获的属性中包括，用户名、密码、IMAP服务器的主机名、轮询的邮箱和该邮箱被轮询频率
    }
}
```

application.yml:

```yml
tacocloud:
  email:
    host: imap.tacocloud.com
    mailbox: INBOX
    username: taco-in-flow
    password: 1L0v3T4c0s
    poll-rate: 10000
```

使用 EmailProperties 去配置集成流有两种选择:

- *定义在 Taco Cloud 应用程序本身里面* -- 在流结束的位置，服务激活器将调用定义了的存储库来创建 taco 订单。
- *定义在一个单独的应用程序中* -- 在流结束的位置，服务激活器将发送 POST 请求到 Taco Cloud API 来提交 taco 订单。

无论选择那种服务激活器的实现，集成流的设计没有影响，但需要配置不同的taco类，order类，Ingredient类，所以在一个单独的应用程序中集成信息流，可以避免与现有的域类型混淆进行。