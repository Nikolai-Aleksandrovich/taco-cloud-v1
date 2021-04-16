# Spring MVC @PathVariable with a dot (.) gets truncated

* spring认为路径的最后一个.是文件扩展，会忽略最后一个点之后的值
* 对于单例，使用regex或者加/可以避免
* 全局设置需要进行自动配置
* 在spring5.2.4中，关闭spring该项配置的值的方法被抛弃，因为会引起RFD attack，而只在显式注册前缀中设置为true。

**when using a Spring \*@PathVariable\* with a \*@RequestMapping\* to map the end of a request URI that contains a dot, we'll end up with a partial value in our variable, truncated at the last dot.**

**Spring considers that anything behind the last dot is a file extension** such as *.json* or *.xml.*

As a result, it truncates the value to retrieve the parameter：

```
@RestController
public class CustomController {
    @GetMapping("/example/{firstValue}/{secondValue}")
    public void example(@PathVariable("firstValue") String firstValue,
      @PathVariable("secondValue") String secondValue) {
        // ...  
    }
}
```

With the above example, let's consider the next requests and evaluate our variables:

- the URL <u>*example/gallery/link*</u> results in evaluating 

  *firstValue =* “gallery” and *secondValue =* “link”

- when using the <u>*example/gallery.df/link.ar*</u> URL, we'll have 

  *firstValue* = “gallery.df” and *secondValue* = “link”

- with the <u>*example/gallery.df/link.com.ar*</u> URL, our variables will be:

   *firstValue* = “gallery.df” and *secondValue* = “link.com”

As we can see, the first variable isn't affected but **<u>the second is always truncated.</u>**

## **Solutions**

One way to solve this inconvenience is to **modify our \*@PathVariable\* definition by adding a regex mapping**. Thereby any dot, including the last one, will be considered as part of our parameter:

```
@GetMapping("/example/{firstValue}/{secondValue:.+}")   
public void example(
  @PathVariable("firstValue") String firstValue,
  @PathVariable("secondValue") String secondValue) {
    //...
}
```

Another way to avoid this issue is by **adding a slash at the end of our \*@PathVariable\***. This will enclose our second variable protecting it from Spring's default behavior:

```
@GetMapping("/example/{firstValue}/{secondValue}/")
```

The two solutions above apply to a single request mapping that we're modifying.

**If we want to change the behavior at a global MVC level, we need to provide a custom configuration**. For this purpose, we can extend the *WebMvcConfigurationSupport* and override its *getPathMatchConfigurer()* method to adjust a *PathMatchConfigurer*.

```
@Configuration
public class CustomWebMvcConfigurationSupport extends WebMvcConfigurationSupport {

    @Override
    protected PathMatchConfigurer getPathMatchConfigurer() {
        PathMatchConfigurer pathMatchConfigurer = super.getPathMatchConfigurer();
        pathMatchConfigurer.setUseSuffixPatternMatch(false);

        return pathMatchConfigurer;
    }
}
```

We have to remember that this approach affects all URLs.

With these three options, we' ll obtain the same result: when calling the *example/gallery.df/link.com.ar* URL, our *secondValue* variable will be evaluated to “link.com.ar”, which is what we want.

###  Deprecation Notice

As of Spring Framework [5.2.4](https://github.com/spring-projects/spring-framework/issues/24179), **the \*setUseSuffixPatternMatch(boolean)\* method is deprecated in order to discourage the use of path extensions for request routing and content negotiation.** Basically, the current implementation makes it hard to protect web applications against the [Reflected File Download (RFD)](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-requestmapping-rfd) attack.

Also, as of Spring Framework 5.3, the suffix pattern matching will only work for explicitly registered suffixes, to prevent arbitrary extensions.

The bottom line is, as of Spring 5.3, we won't need to use the *setUseSuffixPatternMatch(false)* since it's disabled by default.