#### 响应式流

响应式流的规范可以通过四个接口定义来概括：Publisher，Subscriber，Subscription 和 Processor。

Publisher 为每一个 Subscription 的 Subscriber 生产数据。Publisher 接口声明了一个 subscribe() 方法，通过这个方法 Subscriber 可以订阅 Publisher：

```
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}
```

Subscriber 一旦进行了订阅，就可以从 Publisher 中接收消息，这些消息都是通过 Subscriber 接口中的方法进行发送：

```
public interface Subscriber<T> {
    void onSubscribe(Subscription sub);
    //Subscriber 通过调用 onSubscribe() 函数将会收到第一个消息。
    // Publisher 调用 onSubscribe()，它通过一个 Subscription 对象将消息传输给 Subscriber
    void onNext(T item);
    void onError(Throwable ex);
    void onComplete();
}
```

Subscriber 可以管理他自己的订阅内容：

```
public interface Subscription {
    void request(long n);
    //请求被被发送了的数据
    //Subscriber 通过传递一个 long 值的参数来表示它将会接收多少数据。这时就会引进 backpressure，用以阻止 Publisher 发送的数据超过 Subscriber 能够处理的数据。
    //在 Publisher 发送了足够的被请求的数据后，Subscriber 可以再次调用 request() 来请求更多的数据。
    void cancel();
    //取消订阅
}
```

一旦 Subcriber 已经接收到数据，数据就通过流开始流动了。每一个 Publisher 发布的项目都会通过调用 onNext() 方法将数据传输到 Subscriber。如果出现错误，onError() 方法将被调用。如果 Publisher 没有更多的数据需要发送了，同时也不会再生产任何数据了，将会调用 onComplete() 方法来告诉 Subscriber，它已经结束了。

对于 Processor 接口而言，它连接了 Subscriber 和 Publisher：

```
public interface Processor<T, R> extends Subscriber<T>, Publisher<R> {}
```

Processor接受Publisher的数据，处理过后发送给Subscriber

##### Reactor

响应式编程接受姓名并返回问候语：

```
Mono.just("Craig")//创建第一个Mono
    .map(n -> n.toUpperCase())//map接收值并创建第二个Mono
    .map(cn -> "Hello, " + cn + "!")//接收值创建第三个Mono
    .subscribe(System.out::println);//对Mono的subscribe调用，接收数据并打印
```

该例子是一个用于数据流的管道。在管道的每个阶段，数据被以某种方式修改了，但是不能知道哪一步操作被哪一个线程执行了的。它们可能在同一个线程也可能不是。

Mono 是 Reactor 的两个核心类型之一，另一个是 Flux。两者都是响应式流的 Publisher 的实现

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M1ygCsHZNfaddCr8xpQ%2F-M1ygyoBIVAjQGjWnocn%2F%E5%9B%BE%2010.1.jpg?alt=media&token=d27394ec-a182-4ea8-aaa2-70ca5a2f0e29)

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M1ygCsHZNfaddCr8xpQ%2F-M1yh4P2dILgZLuGEk1S%2F10.2.jpg?alt=media&token=8db85d70-506e-4e79-b009-d3d328f3aa43)



添加Reactor依赖：

```
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
```

Reactor测试依赖：

```
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-test</artifactId>
    <scope>test</scope>
</dependency>
```

在非SpringBoot项目添加依赖：

```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-bom</artifactId>
            <version>Bismuth-RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Flux 和 Mono 是 Reactor 提供的最重要的组成部分，而这两个响应式类型所提供的操作将它们结合在一起，来创建数据流动的通道。这些操作可以被分为：

* 创建
* 联合
* 传输
* 逻辑处理

#### 创建反应式类型

在Spring中使用反应式类型时，通常将从repository或者service中获取Flux或者Mono，不需要自行创建

##### 根据对象创建反应式

根据一个或多个对象创建Flux或者Mono，可以使用Flux上的静态just()方法创建反应式类型：

```
@Test
public void createAFlux_just() {
    Flux<String> fruitFlux = Flux
        .just("Apple", "Orange", "Grape", "Banana", "Strawberry");
        //没有订阅者，数据流就不会流动
}
```

添加一个订阅者，可以调用 Flux 中的 subscribe() 方法

```
fruitFlux.subscribe(
    f -> System.out.println("Here's some fruit: " + f);
);
```

测试 Flux 或 Mono 更好的方式是使用 Reactor 中的 StepVerifier。给定一个 Flux 或 Mono，StepVerifier 订阅这个响应式类型，然后对流中流动的数据应用断言，最后验证流以预期方式完成。

```
StepVerifier.create(fruitFlux)
    .expectNext("Apple")
    .expectNext("Orange")
    .expectNext("Grape")
    .expectNext("Banana")
    .expectNext("Strawberry")
    .verifyComplete();
```

##### 根据集合创建

Flux 也可从任何的集合创建，如数组、 Iterable 或是 Java Stream。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LBTpYeutAoFCNARQo%2F-M3LCnAnZsJ0l_EDh-Dz%2F10.3.png?alt=media&token=81771302-f94b-47e1-837f-1c358fd689c6)

```
@Test
public void createAFlux_fromArray() {
    String[] fruits = new String[] {
        "Apple", "Orange", "Grape", "Banana", "Strawberry" };
    
    Flux<String> fruitFlux = Flux.fromArray(fruits);
    //静态方法 fromArray()，然后将数组作为数据源传入
    StepVerifier.create(fruitFlux)
        .expectNext("Apple")
        .expectNext("Orange")
        .expectNext("Grape")
        .expectNext("Banana")
        .expectNext("Strawberry")
        .verifyComplete();
}
```

如果你需要从 java.util.List、java.util.Set 或任何实现了 java.lang.Iterable 接口的类创建 Flux，你可以将它传入静态方法 fromIterable() 中：

```
@Test
public void createAFlux_fromIterable() {
    List<String> fruitList = new ArrayList<>();
    fruitList.add("Apple");
    fruitList.add("Orange");
    fruitList.add("Grape");
    fruitList.add("Banana");
    fruitList.add("Strawberry");
    Flux<String> fruitFlux = Flux.fromIterable(fruitList);
    // ... verify steps
}
```

用Stream作为Flux的源

```
@Test
public void createAFlux_fromStream() {
    Stream<String> fruitStream =
        Stream.of("Apple", "Orange", "Grape", "Banana", "Strawberry");
    Flux<String> fruitFlux = Flux.fromStream(fruitStream);
    // ... verify steps
}
```

##### 生成 Flux 数据

使用 Flux 作为计数器，发出一个随每个新值递增的数字。要创建计数器 Flux，可以使用静态 range() 方法。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LBTpYeutAoFCNARQo%2F-M3LD_PB_HbhPM-Hx2Da%2F10.4.png?alt=media&token=d8af878e-e83c-4c73-89f8-580509e13e0a)

创建一个范围的 Flux

```
@Test
public void createAFlux_range() {
    Flux<Integer> intervalFlux = Flux.range(1, 5);
    StepVerifier.create(intervalFlux)
        .expectNext(1)
        .expectNext(2)
        .expectNext(3)
        .expectNext(4)
        .expectNext(5)
        .verifyComplete();
}
```

类似于range()的有interval(), interval() 创建一个发出递增值的 Flux。但是 interval() 的特殊之处在于，你不必给它一个起始值和结束值，而是指定一个持续时间或一个值的发出频率。：

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LDkHAEFfc4hNSOWgF%2F-M3LEMZzeuAmYLP4VtGO%2F10.5.png?alt=media&token=27b0135c-903d-42fb-acc8-9cc9e2f8589e)

 interval() 方法来创建每秒发送一个值的 Flux

```
@Test
public void createAFlux_interval() {
    Flux<Long> intervalFlux = Flux.interval(Duration.ofSeconds(1)).take(5);
    //间隔 Flux 发出的值以 0 开始，并在每个连续项上递增
    //由于 interval() 没有给定最大值，因此它可能永远运行。使用 take() 操作将结果限制为前 5 个条目
    StepVerifier.create(intervalFlux)
        .expectNext(0L)
        .expectNext(1L)
        .expectNext(2L)
        .expectNext(3L)
        .expectNext(4L)
        .verifyComplete();
}
```

####  Reactor 中 Flux 和 Mono 的结合和分解操作

##### 合并响应式类型

为了将一个 Flux 与另一个合并，可以使用 mergeWith() 操作

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LF9pTiZRjlO_kAWZq%2F-M3LF_ZyjqDnOmOjkdtZ%2F10.6.png?alt=media&token=34d29bf2-78f6-4202-8912-b5dc34e4e517)

```
@Test
public void mergeFluxes() {
    Flux<String> characterFlux = Flux
        .just("Garfield", "Kojak", "Barbossa")
        .delayElements(Duration.ofMillis(500));
        //Flux 会尽可能快地发送数据。因此，需要在创建 Flux 的时候使用 delayElements() 操作，用来将数据发送速度减慢 —— 每 0.5s 发送一个数据。
    
    Flux<String> foodFlux = Flux
        .just("Lasagna", "Lollipops", "Apples")
        .delaySubscription(Duration.ofMillis(250))
        .delayElements(Duration.ofMillis(500));
    
    Flux<String> mergedFlux = characterFlux.mergeWith(foodFlux);
    
    StepVerifier.create(mergedFlux)
        .expectNext("Garfield")
        .expectNext("Lasagna")
        .expectNext("Kojak")
        .expectNext("Lollipops")
        .expectNext("Barbossa")
        .expectNext("Apples")
        .verifyComplete();
}
```

因为 mergeWith() 不能保证源之间的完美交替，所以可能需要考虑使用 zip() 操作。当两个 Flux 对象压缩在一起时，会产生一个新的 Flux，该 Flux 生成一个元组，其中元组包含来自每个源 Flux 的一个项

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LGT5oUgUbK3HQgSd9%2F-M3LGOEKFtlEf68k6jt5%2F10.7.png?alt=media&token=7c1f4eb6-6e02-4128-bd86-9a1bee712b13)

zip()的使用：

```
@Test
public void zipFluxes() {
    Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
    Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");
    
    Flux<Tuple2<String, String>> zippedFlux = Flux.zip(characterFlux, foodFlux);
    
    StepVerifier.create(zippedFlux)
        .expectNextMatches(p ->
            p.getT1().equals("Garfield") &&
            p.getT2().equals("Lasagna"))
        .expectNextMatches(p ->
            p.getT1().equals("Kojak") &&
            p.getT2().equals("Lollipops"))
        .expectNextMatches(p ->
            p.getT1().equals("Barbossa") &&
            p.getT2().equals("Apples"))
        .verifyComplete();
}
```

如果你不想使用 Tuple2，而是想用一些使用其他类型，你可以提供给 zip() 你想产生任何对象的 Function 接口。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LIKVJP4W556vngLnu%2F-M3LITSSI1133lHRvd_o%2F10.8.png?alt=media&token=7da834ea-a4d9-4f12-bdc3-7308f4941490)

如何压缩的 character Flux 和 food Flux，使得它产生 String 类型的的 Flux 对象：

```
@Test
public void zipFluxesToObject() {
    Flux<String> characterFlux = Flux.just("Garfield", "Kojak", "Barbossa");
    Flux<String> foodFlux = Flux.just("Lasagna", "Lollipops", "Apples");
    
    Flux<String> zippedFlux = Flux.zip(characterFlux, foodFlux,
                                   (c, f) -> c + " eats " + f);
                                   //给 zip() 的 Function 接口（这里给出一个 lambda 表达式）简单地把两个值连接成一句话，由压缩后的 Flux 进行数据发送。
    
    StepVerifier.create(zippedFlux)
        .expectNext("Garfield eats Lasagna")
        .expectNext("Kojak eats Lollipops")
        .expectNext("Barbossa eats Apples")
        .verifyComplete();
}
```

##### 选择第一个反应式类型并发出：

first() 操作选择两个 Flux 对象的第一个对象然后输出它的值。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LIKVJP4W556vngLnu%2F-M3LIz1lxY25-KR7gH6E%2F10.9.png?alt=media&token=910c19cf-0d77-4ce2-ad66-5943087b64fc)

使用 first()，它创建了一个新的 Flux，将只会发布从第一个源 Flux 发布的数据：

```
@Test
public void firstFlux() {
    Flux<String> slowFlux = Flux.just("tortoise", "snail", "sloth")
        .delaySubscription(Duration.ofMillis(100));
    Flux<String> fastFlux = Flux.just("hare", "cheetah", "squirrel");
    
    Flux<String> firstFlux = Flux.first(slowFlux, fastFlux);
    
    StepVerifier.create(firstFlux)
        .expectNext("hare")
        .expectNext("cheetah")
        .expectNext("squirrel")
        .verifyComplete();
}
```

#### 转换和过滤响应式流

##### 从响应式类型中过滤数据

当数据从 Flux 中流出时，skip(n) 忽略前几个条目。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJDuDkdWeikUFy4f6%2F-M3LJUQb-r1HoDUu9ip6%2F10.10.png?alt=media&token=04506f4f-2fe4-4885-a282-3f28f44fc044)