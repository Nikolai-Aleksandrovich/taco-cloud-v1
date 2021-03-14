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

```
@Test
public voiad skipAFew() {
    Flux<String> skipFlux = Flux.just(
        "one", "two", "skip a few", "ninety nine", "one hundred")
        .skip(3);
        //该流跳过前三个项，并且只发布最后两个项。
    
    StepVerifier.create(skipFlux)
        .expectNext("ninety nine", "one hundred")
        .verifyComplete();
}
```

skip() 操作的另一种形式是生成一个流，该流在从源流发出项之前等待一段指定的时间。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LJkp4gUPZyxuaWxww%2F10.11.png?alt=media&token=33d9cc73-c2f7-44d6-af55-3b2bea1af49b)

```
@Test
public void skipAFewSeconds() {
    Flux<String> skipFlux = Flux.just(
        "one", "two", "skip a few", "ninety nine", "one hundred")
        .delayElements(Duration.ofSeconds(1))
        .skip(Duration.ofSeconds(4));
    
    StepVerifier.create(skipFlux)
        .expectNext("ninety nine", "one hundred")
        .verifyComplete();
}
```

take() 可以看作是 skip() 的反面。skip() 跳过前几个项，take() 只发出前几个项

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LJzoU_YqtxDfV0_X9%2F10.12.png?alt=media&token=ff8d1d20-3d4a-493f-9e39-8f3e1cfb15b6)

```
@Test
public void take() {
    Flux<String> nationalParkFlux = Flux.just(
        "Yellowstone", "Yosemite", "Grand Canyon","Zion", "Grand Teton")
        .take(3);
    
    StepVerifier.create(nationalParkFlux)
        .expectNext("Yellowstone", "Yosemite", "Grand Canyon")
        .verifyComplete();
}
```

基于时间的take：

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKMJsbtq5k-_m56%2F10.13.png?alt=media&token=b9bfabe2-78a4-47ae-8484-160020e8a10c)

```
@Test
public void take() {
    Flux<String> nationalParkFlux = Flux.just(
        "Yellowstone", "Yosemite", "Grand Canyon","Zion", "Grand Teton")
        .delayElements(Duration.ofSeconds(1))
        .take(Duration.ofMillis(3500));
    
    StepVerifier.create(nationalParkFlux)
        .expectNext("Yellowstone", "Yosemite", "Grand Canyon")
        .verifyComplete();
}
```

filter() 操作允许你根据需要的任何条件有选择地发布

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKNvw_iXEVK4ixj%2F10.14.png?alt=media&token=06427cf2-0519-4e45-a6d3-d192dd1ba64e)

```
@Test
public void filter() {
    Flux<String> nationalParkFlux = Flux.just(
        "Yellowstone", "Yosemite", "Grand Canyon","Zion", "Grand Teton")
        .filter(np -> !np.contains(" "));
        //只接受没有空格的 String
    
    StepVerifier.create(nationalParkFlux)
        .expectNext("Yellowstone", "Yosemite", "Zion")
        .verifyComplete();
}
```

distinct() 操作产生一个只发布源 Flux 中尚未发布的项的 Flux。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKOS3wp2RGGVpH4%2F10.15.png?alt=media&token=39eae767-0ed0-4e41-b195-bae481a67282)

```
@Test
public void distinct() {
    Flux<String> animalFlux = Flux.just(
        "dog", "cat", "bird", "dog", "bird", "anteater")
        .distinct();
    
    StepVerifier.create(animalFlux)
        .expectNext("dog", "cat", "bird", "anteater")
        .verifyComplete();
}
```

##### 映射响应式数据

map() 操作会创建一个 Flux，该 Flux 在重新发布之前，按照给定函数对其接收的每个对象执行指定的转换

关于 map() 的重要理解是，映射是同步执行的，因为每个项都是由源 Flux 发布的。如果要异步执行映射，应考虑使用 flatMap() 操作。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKPmePEHIRLWmQF%2F10.16.png?alt=media&token=4bcd1824-c53d-4e36-80ba-dcba5e729755)

```
@Test
public void map() {
    Flux<Player> playerFlux = Flux
        .just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
        .map(n -> {
            String[] split = n.split("\\s");
            return new Player(split[0], split[1]);
            //使用生成的字符串数组创建 Player 对象
        });
    
    StepVerifier.create(playerFlux)
        .expectNext(new Player("Michael", "Jordan"))
        .expectNext(new Player("Scottie", "Pippen"))
        .expectNext(new Player("Steve", "Kerr"))
        .verifyComplete();
}
```

flatMap() 不是简单地将一个对象映射到另一个对象，而是将每个对象映射到一个新的 Mono 或 Flux。Mono 或 Flux 的结果被压成一个新的 Flux。当与subscribeOn() 一起使用时，flatMap() 可以释放 Reactor 类型的异步能力。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKQemI2XuNdKQ76%2F10.17.png?alt=media&token=81b77c17-74dc-4bbd-a47d-d202cdfd7dc4)

转换映射操作使用中间 Flux 来执行转换，从而允许异步转换

 flatMap() 和 subscribeOn() 的用法：

使用 flatMap() 和 subscribeOn() 的好处是，可以通过将工作分成多个并行线程来增加流的吞吐量。但由于这项工作是并行完成的，无法保证先完成哪项工作，因此无法知道产生的 Flux 中排放的项目的顺序。因此，StepVerifier 只能验证发出的每个项是否存在于 Player 对象的预期列表中，并且在 Flux 完成之前将有三个这样的项。

```
@Test
public void flatMap() {
    Flux<Player> playerFlux = Flux
        .just("Michael Jordan", "Scottie Pippen", "Steve Kerr")
        .flatMap(n -> Mono.just(n).map(p -> {
            String[] split = p.split("\\s");
            return new Player(split[0], split[1]);
            //传入 String 转换为 String 类型的 Mono。然后对 Mono 应用 map() 操作，将 String 转换为 Player
        })
        .subscribeOn(Schedulers.parallel())
        //调用 subscribeOn() 来指示每个订阅应该在一个并行线程中进行。
        //可以异步和并行地执行多个传入 String 对象的映射操作。
        );
    
    List<Player> playerList = Arrays.asList(
        new Player("Michael", "Jordan"),
        new Player("Scottie", "Pippen"Pippen"),
        new Player("Steve", "Kerr"));
    
    StepVerifier.create(playerFlux)
        .expectNextMatches(p -> playerList.contains(p))
        .expectNextMatches(p -> playerList.contains(p))
        .expectNextMatches(p -> playerList.contains(p))
        .verifyComplete();
}
```

subscribe() 订阅一个响应式流并有效地将其启动

subscribeOn() 指定了应该 *如何* 并发地处理订阅，通过 subscribeOn() 可以使用 Schedulers 程序中的一个静态方法指定要使用的并发模型。

| Schedulers 方法 | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| .immediate()    | 在当前线程中执行订阅                                         |
| .single()       | 在单个可重用线程中执行订阅，对所有调用方重复使用同一线程     |
| .newSingle()    | 在每个调用专用线程中执行订阅                                 |
| .elastic()      | 在从无限弹性池中提取的工作进程中执行订阅，根据需要创建新的工作线程，并释放空闲的工作线程（默认情况下 60 秒） |
| .parallel()     | 在从固定大小的池中提取的工作进程中执行订阅，该池的大小取决于 CPU 核心的数量。 |

##### 在响应式流上缓冲数据

buffer() 操作将数据流分解成比特大小的块，缓会产生一个给定最大大小的 List Flux，这些 List 是从传入的 Flux 中收集的，可以不带参数

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKRqmARWdgp3BOv%2F10.18.png?alt=media&token=e3bf7ff8-d4e4-4ed2-ae29-c4f2e3829c68)

```
@Test
public void buffer() {
    Flux<String> fruitFlux = Flux.just(
        "apple", "orange", "banana", "kiwi", "strawberry");
    
    Flux<List<String>> bufferedFlux = fruitFlux.buffer(3);
    
    StepVerifier
        .create(bufferedFlux)
        .expectNext(Arrays.asList("apple", "orange", "banana"))
        .expectNext(Arrays.asList("kiwi", "strawberry"))
        .verifyComplete();
}
```

当将buffer() 与 flatMap() 结合使用时，它可以并行处理每个 List 集合：

```
Flux.just("apple", "orange", "banana", "kiwi", "strawberry")
    .buffer(3)
    .flatMap(x -> 
         Flux.fromIterable(x)
         //获取每个 List 缓冲区并从其元素创建一个新的 Flux
             .map(y -> y.toUpperCase())
             //然后对其应用 map() 操作
             .subscribeOn(Schedulers.parallel())
             //每个缓冲 List 在单独的线程中进一步并行处理
             .log()
    ).subscribe();
```

buffer不带参数：

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKS0foypb-r4B4k%2F10.19.png?alt=media&token=86206ff0-9bbb-45a0-a4de-1436e9521b74)

collectList() 生成一个发布 List 的 Mono，而不是生成一个发布 List 的 Flux

```
@Test
public void collectList() {
    Flux<String> fruitFlux = Flux.just(
        "apple", "orange", "banana", "kiwi", "strawberry");
    Mono<List<String>> fruitListMono = fruitFlux.collectList();
    
    StepVerifier
        .create(fruitListMono)
        .expectNext(Arrays.asList(
            "apple", "orange", "banana", "kiwi", "strawberry"))
        .verifyComplete();
}
```

collectMap() 操作产生一个 Mono，它发布一个 Map，其中填充了由给定 Function 计算其键值的条目

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKTybkVNKWIGmSo%2F10.20.png?alt=media&token=711cea10-fa8f-4c88-9e74-08cb425e33ae)

```
@Test
public void collectMap() {
    Flux<String> animalFlux = Flux.just(
        "aardvark", "elephant", "koala", "eagle", "kangaroo");
    Mono<Map<Character, String>> animalMapMono =
        animalFlux.collectMap(a -> a.charAt(0));
    
    StepVerifier
        .create(animalMapMono)
        .expectNextMatches(map -> {
            return
                map.size() == 3 &&
                map.get('a').equals("aardvark") &&
                map.get('e').equals("eagle") &&
                map.get('k').equals("kangaroo");
        })
        .verifyComplete();
}
```

#### 逻辑操作

all():可以对 Flux 进行测试以确保所有消息在所有操作中都满足某些条件

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKU3ecM0lZq-Ndf%2F10.21.png?alt=media&token=49333867-ebdc-495d-8ca4-3ed28502309f)

any():可以对 Flux 进行测试以确保在任何操作中至少有一条消息满足某些条件

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3LJelfIJ5oGzRrccER%2F-M3LKBKVL2vlVgLslz1w%2F10.22.png?alt=media&token=45200978-a3cb-4eef-95c6-b26ef969b66f)

```
@Test
public void all() {
    Flux<String> animalFlux = Flux.just(
        "aardvark", "elephant", "koala", "eagle", "kangaroo");
    
    Mono<Boolean> hasAMono = animalFlux.all(a -> a.contains("a"));
    StepVerifier.create(hasAMono)
        .expectNext(true)
        .verifyComplete();
    
    Mono<Boolean> hasKMono = animalFlux.all(a -> a.contains("k"));
    StepVerifier.create(hasKMono)
        .expectNext(false)
        .verifyComplete();
}
```

### 使用 Spring WebFlux

基于 Servlet 的 web 框架，比如 Spring MVC，本质上是阻塞和多线程的，每个连接都会使用一个线程。在处理请求时，将从线程池中提取一个工作线程来处理该请求。同时，请求线程被阻塞，直到工作线程通知它已完成为止。

在请求量很大的情况下，阻塞 web 框架不能有效地扩展。缓慢的工作线程中的延迟使情况更糟，因为会导致工作线程池准备处理另一个请求所需的时间更长

异步 web 框架实现用较少的线程达到更高的可扩展性，只需要和CPU核心数量相同的线程，使用事件轮询，能够用一个线程处理很多请求，导致每次连接成本变低

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3dKE8hXPAta7WS7LwB%2F-M3dKe--iufCXJAEepO4%2F11.1.png?alt=media&token=7c1a7f51-c021-49f5-844c-f1bfc745f12a)

在一个 event loop 中，一切皆为事件，其中包括像是数据库和网络操作这种密集操作的请求与回调。当需要完成一个重要的操作时，event loop 并行地为那个操作注册一个回调，然后它继续去处理其他事件。

当操作完成后，它会被 event loop 视为一个 event，对于请求也是一样的操作。这样异步 web 框架就能够使用更少的线程应对繁重的请求，从而实现更好的扩展性，这样做的结果就是降低了线程管理的开销。

#### Spring WebFlux 

SpringMVC 位于 Java Servlet API 之上，它需要一个 Servlet 容器来执行

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3dKE8hXPAta7WS7LwB%2F-M3dKe-2i7WXv9mZJEit%2F11.2.png?alt=media&token=fe387175-5641-4431-86f5-9e9f8f4afa93)

Spring MVC 和 Spring WebFlux 之间最显著的区别就是添加到构建中的依赖项不同。在使用 Spring WebFlux 时，需要添加 Spring Boot WebFlux starter 依赖项，而不是标准的 web starter

也可以通过选中 initializer 中的 Reactive Web 复选框添加到项目中

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

WebFlux 的默认嵌入式服务器是 Netty 而不是 Tomcat。Netty 是少数几个异步的事件驱动的服务器之一，它自然适合像 Spring WebFlux 这样的响应式 web 框架。

Spring WebFlux controller 方法通常接受并返回响应式类型，比如 Mono 和 Flux，而不是域类型和集合。

不同之处在于如何使用这些类型。Spring WebFlux 是一个真正的响应式 web 框架，允许在 event loop 中处理请求，而 Spring MVC 是基于 Servlet 的，依赖多线程处理多个请求。

#### 响应式 Controller

如下是一个RestFUL风格的controller：

```
@RestController
@RequestMapping(path="/design", produces="application/json")
@CrossOrigin(origins="*")
public class DesignTacoController {
    ...
    @GetMapping("/recent")
    public Iterable<Taco> recentTacos() {
    //recentTacos() controller 处理 /design/recent 的 HTTP GET 请求，以返回最近创建的 tacos 的列表。
        PageRequest page = PageRequest.of(
            0, 12, Sort.by("createdAt").descending());
        
        return tacoRepo.findAll(page).getContent();
    }
    ...
}
```

如何改变recentTaco，让 recentTacos() 返回一个 Flux<Taco>。

```
@GetMapping("/recent")
public Flux<Taco> recentTacos() {
    return Flux.fromIterable(tacoRepo.findAll()).take(12);
}
```

如果 repository 提供了一个可以开始使用的 Flux，就不需要进行转换。

尽管从 repository 中获得了一个 Flux<Taco>，但你可以在不调用 subscribe() 的情况下返回它。实际上，框架将为你调用 subscribe()。这意味着当处理对 `/design/recent` 的请求时，recentTacos() 方法将被调用，并在从数据库中获取数据之前返回

```
@GetMapping("/recent")
public Flux<Taco> recentTacos() {
    return tacoRepo.findAll().take(12);
}
```

一个响应式 cotroller 将是一个端到端的响应式栈的顶端，包括 controller、repository、database 和任何可能位于两者之间的 serviec。

![img](https://gblobscdn.gitbook.com/assets%2F-LrmLE3NwQoVJk02Q_BX%2F-M3dKE8hXPAta7WS7LwB%2F-M3dKe-3tZre3sAwHVOI%2F11.3.png?alt=media&token=23cb587f-8545-4f31-b299-7997b07e32f0)

这样的端到端的栈要求 repository 被写入以返回一个 Flux，而不是一个Iterable。

```
public interface TacoRepository extends ReactiveCrudRepository<Taco, Long> {
}
```

##### 返回单个值

RestFUL风格的控制器DsignTacoController中的tacoById()方法：

```
@GetMapping("/{id}")
public Taco tacoById(@PathVariable("id") Long id) {
    Optional<Taco> optTaco = tacoRepo.findById(id);
    
    if (optTaco.isPresent()) {
        return optTaco.get();
    }
    
    return null;
}
```

假设 findById() 返回 Mono<Taco> 而不是 Optional<Taco>。则可以重写controller的tacoById():

```
@GetMapping("/{id}")
public Mono<Taco> tacoById(@PathVariable("id") Long id) {
    return tacoRepo.findById(id);
}
```

通过返回 Mono<Taco> 而不是 Taco，可以使 Spring WebFlux 以一种被动的方式处理响应。因此，API将更好地响应大的负载

##### 使用 RxJava 类型

在使用 Spring WebFlux 时,可以选择使用像 Observable 和 Single 这样的 RxJava 类型

假设 DesignTacoController 和后端 repository 之间有一个 service，它处理 RxJava 类型。在这种情况下，recentTacos() 方法的编写：

```
@GetMapping("/recent")
public Observable<Taco> recentTacos() {
    return tacoService.getRecentTacos();
}
```

 tacoById() 方法来处理 RxJava 的 Single 元素

```
@GetMapping("/{id}")
public Single<Taco> tacoById(@PathVariable("id") Long id) {
    return tacoService.lookupTaco(id);
}
```

Spring WebFlux controller 方法还可以返回 RxJava 的 Completable，这相当于 Reactor 中的 Mono<Void>。WebFlux 还可以返回一个 Flowable，作为 Observable 或 Reactor 的 Flux 的替代。

##### 输入的反应式

使用 Spring WebFlux，还可以接受 Mono 或 Flux 作为处理程序方法的输入。

RestFUL风格的DesignTacoController 中 postTaco() 

```
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Taco postTaco(@RequestBody Taco taco) {
//postTaco() 不仅返回一个 Taco 对象，而且还接受一个绑定到请求主体内容的 Taco 对象。
    return tacoRepo.save(taco);
}
```

这意味着在请求有效负载完全解析并用于实例化 Taco 对象之前，无法调用 postTaco()。也意味着，对repository的save()阻塞调用返回之前，postTaco不能返回，意味着，阻塞了两次：

* 进入postTaco()的时候
* 在postTaco()调用的过程

非阻塞的postTaco():

```
@PostMapping(consumes="application/json")
@ResponseStatus(HttpStatus.CREATED)
public Mono<Taco> postTaco(@RequestBody Mono<Taco> tacoMono) {
    return tacoRepo.saveAll(tacoMono).next();
    //saveAll() 方法返回一个 Flux<Taco>
}
```

接受 Mono<Taco> 作为输入，可以立即调用该方法，而无需等待 Taco 从请求体被解析。由于 repository 也是被动的，它将接受一个 Mono 并立即返回一个 Flux<Taco>，从中调用 next() 并返回 Mono<Taco>。所有这些都是在处理请求之前完成的

##### 函数式请求处理器

对于注解的讨论：

任何基于注解的编程都涉及到注解应该对**做什么**以及**如何做**上区分。注解本身定义了该做什么，具体如何做则在框架代码的其他地方定义。当涉及到任何类型的定制或扩展时，会使编程模型复杂化，因为这样的更改需要在注解之外的代码。此外，调试这样的代码是很棘手的，因为不能在注解上设置断点。

使用 Spring 的函数式编程模型编写 API 涉及四种主要类型：

- RequestPredicate —— 声明将会被处理的请求类型
- RouteFunction —— 声明一个匹配的请求应该如何被路由到处理代码中
- ServerRequest —— 表示 HTTP 请求，包括对头和正文信息的访问
- ServerResponse —— 表示 HTTP 响应，包括头和正文信息

示例：

```
@Configuration
public class RouterFunctionConfig {
    @Bean
    //仅处理单一类型请求的 RouterFunction
    public RouterFunction<?> helloRouterFunction() {
    //RouterFunction 声明一个或多个 RequestPredicate 对象与将处理匹配请求的函数之间的映射。
        return route(GET("/hello"),
                     request -> ok().body(just("Hello World!"), String.class));
                     // route() 方法接受两个参数：RequestPredicate 和处理请求匹配的函数。
                     //RequestPredicates 的 GET() 方法声明了一个 RequestPredicate，它与 /hello 路径的 HTTP GET 请求相匹配。
                     //lambda 接受一个 ServerRequest 作为参数，使用来自 ServerResponse 的 ok() 和来自 BodyBuilder 的 body() 返回一个 ServerResponse
                     //创建一个带有 HTTP 200（OK）状态代码和一个表示 Hello World 的 body 负载的响应
    }
}
```

如果需要处理不同类型的请求，不必编写另一个 @Bean 方法。只需要调用 andRoute() 来声明另一个 RequestPredicate 到函数的映射

```
@Bean
public RouterFunction<?> helloRouterFunction() {
    return route(GET("/hello"), request -> ok().body(just("Hello World!"), String.class))
        .andRoute(GET("/bye"), request -> ok().body(just("See ya!"), String.class));
}
```

函数式的DesignTacoController ：

```
@Configuration
public class RouterFunctionConfig {
    @Autowired
    private TacoRepository tacoRepo;
    
    @Bean
    public RouterFunction<?> routerFunction() {
        return route(GET("/design/taco"), this::recents)
            .andRoute(POST("/design"), this::postTaco);
            //处理 /design/taco 的 GET 请求和 /design 的 POST 请求
    }
    
    public Mono<ServerResponse> recents(ServerRequest request) {
        return ServerResponse.ok()
            .body(tacoRepo.findAll().take(12), Taco.class);
    }
    
    public Mono<ServerResponse> postTaco(ServerRequest request) {
        Mono<Taco> taco = request.bodyToMono(Taco.class);
        Mono<Taco> savedTaco = tacoRepo.save(taco);
        return ServerResponse
            .created(URI.create(
                "http://localhost:8080/design/taco/" +
                savedTaco.getId()))
            .body(savedTaco, Taco.class);
    }
}
```

#### 测试反应式控制器

##### GET请求

对于 recentTacos() 方法，我们想断言的一件事是，如果为 `/design/recent` 路径发出了 HTTP GET 请求，那么响应将包含一个不超过 12 个 tacos 的 JSON 数据。

```
public class DesignTacoControllerTest {
    @Test
    public void shouldReturnRecentTacos() {
        Taco[] tacos = {
            testTaco(1L), testTaco(2L), testTaco(3L), testTaco(4L),
            testTaco(5L), testTaco(6L), testTaco(7L), testTaco(8L),
            testTaco(9L), testTaco(10L), testTaco(11L), testTaco(12L),
            testTaco(13L), testTaco(14L), testTaco(15L), testTaco(16L)
        };
        //创建一个包含16个taco的列表
       
        Flux<Taco> tacoFlux = Flux.just(tacos);
        //把taco数据转为Flux流
        TacoRepository tacoRepo = Mockito.mock(TacoRepository.class);

        when(tacoRepo.findAll()).thenReturn(tacoFlux);
        //        这个 Flux 作为模拟 TacoRepository 的 findAll() 方法的返回值。
        WebTestClient testClient = WebTestClient.bindToController(
            new DesignTacoController(tacoRepo)).build();
            //实例化了一个 DesignTacoController，将模拟的 TacoRepository 注入构造函数。Controller 被赋予 WebTestClient.bindToController() 以创建 WebTestClient 的实例。
          //使用 WebTestClient 向 /design/recent 提交 GET 请求
        testClient.get().uri("/design/recent")
      //调用 get().uri(“/design/recent”) 描述要发出的请求
            .exchange().expectStatus().isOk().expectBody()
            //调用 exchange() 提交请求，调用 expectStatus()，可以断言响应具有 HTTP 200(OK) 状态代码。
            .jsonPath("$").isArray()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$[0].id").isEqualTo(tacos[0].getId().toString())
            .jsonPath("$[0].name").isEqualTo("Taco 1")
            .jsonPath("$[1].id").isEqualTo(tacos[1].getId().toString())
            .jsonPath("$[1].name").isEqualTo("Taco 2")
            .jsonPath("$[11].id").isEqualTo(tacos[11].getId().toString())
            ...
            .jsonPath("$[11].name").isEqualTo("Taco 12")
            .jsonPath("$[12]").doesNotExist()
          
    }
    
    ...
}
```

如何生成Taco对象？

```
private Taco testTaco(Long number) {
    Taco taco = new Taco();
    taco.setId(UUID.randomUUID());
    taco.setName("Taco " + number);
    List<IngredientUDT> ingredients = new ArrayList<>();
    ingredients.add(
        new IngredientUDT("INGA", "Ingredient A", Type.WRAP));
    ingredients.add(
        new IngredientUDT("INGB", "Ingredient B", Type.PROTEIN));
    taco.setIngredients(ingredients);
    return taco;
}
```

使用jsonPath()有时很不方便，WebTestClient 提供了 json()，它接受包含 json 的 String 参数来对响应进行响应。

假设在一个名为 recent-tacos.JSON 的文件中创建了完整的响应 JSON，并将其放在路径 `/tacos` 下的类路径中。然后重写 WebTestClient 断言：

```
ClassPathResource recentsResource = new ClassPathResource("/tacos/recent-tacos.json");
String recentsJson = StreamUtils.copyToString(
    recentsResource.getInputStream(), Charset.defaultCharset());
    //copyToString() 返回的 String 将包含响应请求时预期的整个 JSON
    //将类路径资源加载到 String 对象中
testClient.get().uri("/design/recent")
    .accept(MediaType.APPLICATION_JSON)
    .exchange().expectStatus().isOk().expectBody()
    .json(recentsJson);
```

expectBodyList() 方法接受指示列表中元素类型的类或参数化类型引用，并返回要针对其进行断言的 istBodySpec 对象。

```
testClient.get().uri("/design/recent")
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk()
    .expectBodyList(Taco.class)
    .contains(Arrays.copyOf(tacos, 12));
```

##### 测试 POST 请求

通过向 `/design `提交   `POST`请求来编写针对创建 taco 端点 API 的测试：

```
@Test
public void shouldSaveATaco() {
    TacoRepository tacoRepo = Mockito.mock(TacoRepository.class);
    Mono<Taco> unsavedTacoMono = Mono.just(testTaco(null));
    Taco savedTaco = testTaco(null);
    savedTaco.setId(1L);
    Mono<Taco> savedTacoMono = Mono.just(savedTaco);
    when(tacoRepo.save(any())).thenReturn(savedTacoMono);
    WebTestClient testClient = WebTestClient.bindToController(
        new DesignTacoController(tacoRepo)).build();
    
    testClient.post()
        .uri("/design")
        .contentType(MediaType.APPLICATION_JSON)
        .body(unsavedTacoMono, Taco.class)
        .exchange()
        .expectStatus().isCreated()
        .expectBody(Taco.class)
        .isEqualTo(savedTaco);
}
```

shouldSaveATaco() 首先设置一些测试数据，模拟 TacoRepository，然后构建一个 WebTestClient，并绑定到 Controller。然后，使用 WebTestClient 向 `/design` 提交 POST 请求，请求的 body 类型为 application/json，有效负载是未保存 Mono 中 Taco 的 json 序列化形式。在执行 exchange() 之后，测试断言响应具有 HTTP 201(CREATED) 状态，并且正文中的有效负载等于保存的 Taco 对象。

##### 使用实时服务器进行测试

如果需要在 Netty 或 Tomcat 等服务器的上下文中测试 WebFlux Controller，并且可能需要使用 repository 或其他依赖项，可能需要一个集成环境

要编写 WebTestClient 集成测试，首先使用 @RunWith 和 @SpringBootTest 对测试类进行注解，就像其他任何 Spring Boot 集成测试一样：

```
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=WebEnvironment.RANDOM_PORT)
//要求 Spring 启动正在运行的服务器来监听随机选择的端口。
public class DesignTacoControllerWebTest {
    @Autowired
    private WebTestClient testClient;
    //WebTestClient 还将自动连接到测试类中，不再需要在测试方法中创建一个 URL，而且在发出请求时也不需要指定完整的 URL。
    //因为 WebTestClient 将被装配成知道测试服务器在哪个端口上运行。
}
```

```
@Test
public void shouldReturnRecentTacos() throws IOException {
    testClient.get().uri("/design/recent")
        .accept(MediaType.APPLICATION_JSON).exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$[?(@.id == 'TACO1')].name").isEqualTo("Carnivore")
        .jsonPath("$[?(@.id == 'TACO2')].name").isEqualTo("Bovine Bounty")
        .jsonPath("$[?(@.id == 'TACO3')].name").isEqualTo("Veg-Out");
}
```

#### 反应式消费 REST API

Spring 5 提供了 WebClient 作为 RestTemplate 的反应式版本。当 WebClient 向外部 APIs 发出请求的同时，也可以发出或接收反应式类型。

RestTemplate 会使用不同的方法处理不同类型的请求，而 WebClient 拥有了一个流畅的构建者风格接口，可以让你描述并发送请求。

WebClient 的常用使用方法有以下几种：

- 创建一个 WebClient 实例（或者注入一个 WebClient Bean）
- 指定发送请求的 HTTP 方法
- 指定请求中所必要的 URI 和 header
- 提交请求
- 消费响应

##### GET 获取资源

假设你需要通过 Taco Cloud API 根据 ID 获取 Ingredient 对象。如果使用 RestTemplate，那么你可能会使用 `getForObject()` 方法。

但，借助 WebClient 的话，你可以构建请求、获取响应并抽取一个会发布 Ingredient 对象的 Mono：

```
Mono<Ingredient> ingredient = WebClient.create()   
  .get()   
  .uri("http://localhost:8080/ingredients/{id}", ingredientId)   
  //{id} 占位符将会被 ingredientId 的值所替换
  .retrieve()   
  //retrieve() 会执行请求
  .bodyToMono(Ingredient.class);  
  //将响应体的载荷抽取到 Mono<Ingredient> 中
ingredient.subscribe(i -> { ... })
//为了对 bodyToMono() 返回 Mono 进行额外的操作，在请求发送之前对其进行订阅
```

获取所有 Ingredient:

```
Flux<Ingredient> ingredients = WebClient.create()   
  .get()
  .uri("http://localhost:8080/ingredients") 
  .retrieve()   
  .bodyToFlux(Ingredient.class); 
ingredients.subscribe(i -> { ... })
//要订阅结果所形成的 Flux，否则请求将始终不会发送。
```

获取多个条目与获取单个条目是相同的。最大的差异在于我们不再是使用 `bodyToMono()` 将响应体抽取为 Mono，而是使用 `bodyToFlux()` 将其抽取为一个 Flux。

##### 使用基础 URI 发送请求

很多请求中都会使用一个通用的基础 URI，创建 WebClient bean 的时候设置一个基础 URI 并将其注入到所需的地方。

```
@Bean 
public WebClient webClient() {  
  return WebClient.create("http://localhost:8080"); 
}
```

将 WebClient bean 注入进来并使用：

```
@Autowired 
WebClient webClient; 
public Mono<Ingredient> getIngredientById(String ingredientId) {
  Mono<Ingredient> ingredient = webClient   
    .get() 
    .uri("/ingredients/{id}", ingredientId)  
    .retrieve()  
    .bodyToMono(Ingredient.class);  
	ingredient.subscribe(i -> { ... }) 
}
```

##### 对长时间运行的请求进行超时处理

使用 Flux 或 Mono 的 `timeout()` 方法，为等待数据发布的过程设置一个时长限制。

```
Flux<Ingredient> ingredients = WebClient.create() 
  .get()   
  .uri("http://localhost:8080/ingredients")  
  .retrieve()   
  .bodyToFlux(Ingredient.class);  
ingredients.timeout(Duration.ofSeconds(1))  
  .subscribe(   
  	i -> { ... },  
  	e -> {    
      // handle timeout error   
    })
```

如果请求能够在 1 秒之内返回，就不会有任何问题。但是，如果请求花费的时间超过 1s，则会超时，然后会调用 `subscribe()` 的第二个参数进行错误处理。

####  POST 发送资源

假设你拥有一个 Mono，并希望使用由 Mono 发布的 Ingredient 发送一个 POST 请求到具有 `/ingredients` 相对路径的 URI。

```
Mono<Ingredient> ingredientMono = ...;

Mono<Ingredient> result = webClient
    .post()
    .uri("/ingredients")
    .body(ingredientMono, Ingredient.class)
    //使用 Mono 调用 body() 来填充请求体：
    .retrieve()
    .bodyToMono(Ingredient.class);

result.subscribe(i -> { ... })
```

如果你没有要发送的 Mono 或 Flux，而是手头有原始域对象，你可以使用 syncBody()

```
Ingedient ingredient = ...;

Mono<Ingredient> result = webClient
    .post()
    .uri("/ingredients")
    .syncBody(ingredient)
    .retrieve()
    .bodyToMono(Ingredient.class);

result.subscribe(i -> { ... })
```

如果不是 POST 请求，而是要用 PUT 请求更新 Ingredient，则调用 put() 而不是 post() 并相应调整 URI 路径：

```
Mono<Void> result = webClient
    .put()
    .uri("/ingredients/{id}", ingredient.getId())
    .syncBody(ingredient)
    .retrieve()
    .bodyToMono(Void.class)
    //PUT 请求通常具有空的响应体，因此必须请求 bodyToMano() 返回类型为 Void 的 Mono。
    .subscribe();
```

##### 删除资源

删除配料

```
Mono<Void> result = webClient
    .delete()
    .uri("/ingredients/{id}", ingredientId)
    .retrieve()
    .bodyToMono(Void.class)
    .subscribe();
    //返回并订阅就会发送请求
```

##### 处理错误

如果出现400和500级别的状态码，webClient就会记录失败信息，如果要处理这类型错误，就可以调用onStatus() 来指定各种类型的HTTP状态码如何处理，

onStatues()接受两个参数：一个断言函数匹配HTTP状态，另一个函数得到ClientResponse对象，返回Mono<Throwable>

使用WebClient根据Id获取配料：、

```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("/ingredients/{id}", ingredientId)
    .retrieve()
    .bodyToMono(Ingredient.class);
ingredientMono.subscribe(
	ingredient -> {
		//recieve the ingredient data
	},
	error -> {
	//deal with error
	});
```

如果找到资源，返回第一个lambda表达式，并将匹配的Ingredient对象传回来；没有找到资源，返回第二个表达式，并会得到一个HTTP 404，并传递一个默认的WebClentResponseException

而传递的WebClentResponseException问题在于，他无法表达error出现的问题是什么，可以添加一个自定义的错误处理器

```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("/ingredients/{id}", ingredientId)
    .retrieve()
    .onStatus(HttpStatus::is4xxClientError,response -> Mono.just(new UnknownIngredientExcepition()))
    //if true then reponse
    .bodyToMono(Ingredient.class);
```

##### 交换请求

通常使用retrieve() 方法发送webClient请求，会返回一个ReponseSpec类型的对象，调用它的Status(), bodyToFlux(), bodyToMono()方法就能处理响应

也可以使用exchange()方法替换retrieve()方法，返回ClientResponse类型的Mono，对他进行反应式操作，可以探测并使用响应中的数据（载荷、头信息、cookie）

根据ID获取单个配料：

```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("/ingredients/{id}", ingredientId)
    .exchange()
    .flatMap(cr -> cr.bodyToMono(Ingredient.class));
    //不使用ResponseSpec对象的bodyToMono而获取Mono<Ingredient>，而得到的是Mono<ClientResponse>,用ClientReponse扁平化映射为Mono<Ingredient>
```

```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("/ingredients/{id}", ingredientId)
    .retrieve()
    .bodyToMono(ingredient.class)
```

假如需要验证web头信息：

```
Mono<Ingredient> ingredientMono = webClient
    .get()
    .uri("/ingredients/{id}", ingredientId)
    .exchange()
    .flatMap(cr -> {
    	if(cr.headers().header("X_UNAVAILABLE").contains("true")){
    	return Mono.empty();
    	//如果响应这个头信息为true，那么返回一个空的Mono
    	}
    	return Mono.just(cr);
    	//不然就返回原cr
    })
    .flatMap(cr -> cr.bodyToMono(Ingredient.class));
    //不管如何，第二层都会接受上一层信息，扁平化
```

##### 反应式Web API Security

Spring security基于servlet Filter，可以用在基于netty的Srping WebFlux吗？

答案是可以，在Spring5.0.0，Spring Security使用了Spring的WebFilter框架，所以不依赖Servlet API了

所以，这一个security依赖可以负责Spring MVC和Spring Web Flux，那么两者的代码配置差异也很小

```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
```

##### 配置反应式Web应用的安全

非反应式Spring MVC的Security Config配置：

```
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http)throws Exception{
		http
			.authrizeRequests()
				.antMatchers("/design","/orders").hasAuthority("USER")
				.antMatchers("/**").permitAll();
	}
}
```

相同的反应式配置如何用到Spring Web Flux？

```
@Configuration
@EnableWebFluxSecurity
//启动WebFlux Security注解
public class SecurityConfig{
//没有extends其他基类
	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http){
		return http
		//配置通过ServerHttpSecurity对象，而不是HttpSecurity，但他其实模拟了HttpSecurity
				.authorizeExchange()
				//声明请求级安全性
					.pathMatcher("/design","/orders").hasAuthority("USER")
					.anyExchange().permitAll()
					//anyExchange会映射所有路径
				.and()
					.build();
					//调用build方法将所有的安全规则聚合到一个要返回的SecurityWebFilterChain对象中
	}
}
```

##### 配置反应式用户详情服务

在Spring Security中对Spring MVC配置，要extend WebSecurityConfigurerAdapter，然后两次重写configure方法，这也需要定义一个UserDetails, 如下：

```
@Autowired
UserRepository userRepo;
@Override
protected void configure(AuthenticationManagerBuilder auth)throws Exception{
	auth.userDetailsService(new UserDetailsService(){
		@Override
		public UserDetails loadUserByUsername(String username)throws UsernameNotFoundException{
			User user = userRepo.findByUsername(username)
			if(user == null){
				throw new UsernameNotFoundException(username"+ not found")
			}
			return user.toUserDetails();
			//返回UserDetails对象
		}
	});

}
```

在反应式安全配置中，声明一个ReactiveUserDetailsService bean，他是userDetailsService的等价形式，只需要实现一个返回Mono<UserDetails>的findByUsername()方法，而不是UserDetails对象

```
@Service
public ReactiveUserDetailsService userDetailsService(UserRepository userRepo){
	return new ReactiveUserDetailsService(){
		@Override
		public Mono<UserDetails> findByUsername(String name){
			return userRepo.findByUsername(name)
			//返回的是一个User，所以在下方map把他映射为UserDetails对象
				.map(user -> {
					return user.toUserDetails();
				});
		}
	};
}
```

### 反应式持久化数据

如果想让整个数据流变为反应式和非阻塞的，那么就需要从控制器到数据库都如此。

但目前还不存在支持关系型数据库的反应式处理(Spring Data R2DBC)，而应该使用Cassandra，MongoDB，Couchbase或者Redis

#### 反应式持久化数据基础

##### 反应式的本质：

反应式repository的方法中，要接受和返回Mono和Flux，而不是领域实体或者集合

比如根据配料类型获取Ingredient对象的repository：

```
Flux<Ingredient> findByType(Ingredient.type type)
```

保存Taco，repository的saveAll() 方法：

```
<Taco> Flux<Taco> saveAll(Publisher<Taco> tacoPublisher)
//接受一个Taco类型的Publicsher，可能是Mono<Taco>或者Flux<Taco>,返回一个Flux<Taco>
```

##### 反应式和非反应式的类型转换

假如已经使用了关系型数据库，该如何使用反应式编程？

即便数据库不使用非阻塞的反应式查询，也依然可以用阻塞的方式获得数据，用于上游的反应式代码中

假设关系型数据库有一个查询语句：

```
List<Order> findByUser(User user);
```

可以在就受到非反应式的List<Order>时把它转化为Flux：

```
List<Order> findByUser(User user);
Flux<Order> orderFlux = Flux.fromIterable(orders);//fromIterable  fromArray fromStream
```

或者根据Id获得一个order

```
Order findOrderById(Long Id);
Mono<Order> orderMono = Mono.just(orders);
```

这样可以把非反应式的阻塞代码隔离在repository内

save：假设WebFlux控制器接受了一个Mono<Taco>，要使用Spring Data JPA repository的save() 方法，就要调用Mono的block()方法：

```
Taco taco = tacoMono.block();
//执行一个阻塞操作，抽取taco
tacoRepo.save(taco);
```

```
Iterable<Taco> tacos = tacoFlux.toIterable();
tacoRepo.saveAll(tacos);
```

本质上，block方法和toIterable方法都是阻塞的，会让使用它的代码段打破反应式模型

一种避免阻塞的办法：订阅Mono或者Flux，在每个元素发布的时候，执行所需的操作

```
tacoFlux.subscribe(taco -> {tacoRepo.save(taco);});
```

#### 如何使用反应式的Cassandra repository呢？

Cassandra 是一个分布式，高性能，始终可用，最终一致，分区行存储的NoSql数据库

Cassandra处理的是数据行，这些数据行在多点分区存储，任何行都会在多个节点保存数据副本，消除了单点故障

Spring Data Cassandra提供了自动化repository的支持

##### 启动Spring Data Cassandra

如果不为Cassandra编写反应式repository：

```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-cassandra</artifactId>
		</dependency>
```

支持反应式的Cassandra依赖，从而替代了Spring Data JPA，就可以移除JPA H2 JDBC等关系型数据库的依赖：

```
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-cassandra-reactive</artifactId>
		</dependency>
```

另外Cassandra需要配置键空间：（键空间是Cassandra节点中的一组表）可以手动创建，也可以自动创建，手动借助Cassandra CQL shell，可以使用creat keyspace命令为Taco cloud应用创建键空间：

```
cqlsh> create keyspace tacocloud
```

之后再yml文件中配置键空间属性：

```
spring： 
  data: 
    cassandra: 
      keyspace-name: tacocloud
      schema-action: recreat-drop-unused
```

schema-action: recreat-drop-unused保证每次应用重新启动，所有表和用户都会删除再重建

默认情况下，Spring Data Cassandra会假定Cassandra在本地运行并监听9092端口，也可以再yml配置：

```
spring: 
  data: 
    cassandra:
      keyspace-name: tacocloud
      contact-points:
      - casshost-1.tacocloud.com
      - casshost-2.tacocloud.com
      - casshost-3.tacocloud.com
      port: 9043
      username: tacocloud
      password: qwertyuiop
```

contact-points是识别Cassandra主机名的地方：每个contact-point代表运行Cassandra节点的主机

##### Cassandra的数据模型：

* Cassandra表有任意数量的列，但不是所有行用到这些列（数据稀疏）
* Cassandra被分为多个分区，每个行可能有多个分区管理（分布管理）
* Cassandra表有两种键，分区键和集群键，Cassandra对每一行的分区键进行hash操作，确定由哪个分区管理该行，集群键决定行在分区维护的数据
* 对读操作进行优化，最好让表高度非规范化，让数据跨多个表复制

##### 替换domain对象

Ingredient：

```
@Data
@RequiredArgConstructor
@NoArgConstructor(access=AccessLevel.PRIVATE,force=true)
@Table("ingredients")
//配料持久化到ingredients的表里
public class Ingredient{
	@PrimaryKey
	private final String id;
	private final String name;
	private final Type type;
	public static enum Type{
	WRAP,PROTEIN.....,SAUCE
	}
}
```

Taco：

```
@Data
@RestResource(rel="tacos",path="tacos")
@Table("tacos")
//持久化到tacos表里
public class Taco{
	@PrimaryKeyColumn(type=PrimaryKeyType.PARTITIONED)
	//定义分区键
	private UUID id = UUIDs.timeBased();
	//id为主键，使用了PrimaryKeyType.PARTITIONED，表明id属性要作为分区键，确定taco数据的每一行要写入哪个分区
	//属性是UUID，保存系统生成的属性值通常是UUID类型
	@NotNull
	@Size(...)
	private String name;
	@PrimaryKeyColumn(type=PrimaryKeyType.CLUSTERED,ordering=Ordering.DESCENDING)
	//定义集群键
	private Date createdAt = new Date();
	//作为集群键，确定在集群中的顺序
	@Size(...)
	@Column("ingredients")
	//将列表映射到ingredients列
	private List<IngredientUDT> ingredients;
	//包含数据集合的列，必须是原生类型的集合或者用户类型的集合
	//因为@Table已经将Ingredient映射成一个Cassandra持久化实体，所以必须定义一个新类，指示如何将配料信息存储在taco表的ingredients列
}
```

IngredientUDT指示如何将配料信息存储在taco表的ingredients列：

```
@Data
@RequiredArgsConstructor
@NoArgConstructor(access=AccessLevel.PRIVATE,force=true)
@UserDefinedType("ingredient")
//表明是用户定义类型
public class IngredientUTD{
	private final String name;
	private final Ingredient.Type type;
}
```

数据模型：