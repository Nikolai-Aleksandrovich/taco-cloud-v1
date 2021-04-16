**the \*@PathVariable\* annotation can be used to handle template variables in the request URI mapping**, and use them as method parameters.

* 简单映射，改变不符合名称的字段名，多个变量，或者在多个变量的情况下，使用集合类
* 当需要同时处理有id的输入和无id的输入，可以设置required=false，也可以用Optional来判断id是否存在，或者使用默认的id值



## **A Simple Mapping**

we use *@PathVariable* annotation to extract the templated part of the URI represented by the variable *{id}*.

```
@GetMapping("/api/employees/{id}")
@ResponseBody
public String getEmployeesById(@PathVariable String id) {
    return "ID: " + id;
}
```

## **Specifying the Path Variable Name**

if the path variable name is different, we can specify it in the argument of the *@PathVariable* annotation:

```
@GetMapping("/api/employeeswithvariable/{id}")
@ResponseBody
public String getEmployeesByIdWithVariableName(@PathVariable("id") String employeeId) {
    return "ID: " + employeeId;
}
```

## Multiple Path Variables in a Single Request

**we can have more than one path variable in our request URI for a controller method, which also has multiple method parameters**:

```
@GetMapping("/api/employees/{id}/{name}")
@ResponseBody
public String getEmployeesByIdAndName(@PathVariable String id, @PathVariable String name) {
    return "ID: " + id + ", name: " + name;
}
```

**We can also handle more than one \*@PathVariable\* parameters using a method parameter of type \*java.util.Map<String, String>:\***

```
@GetMapping("/api/employeeswithmapvariable/{id}/{name}")
@ResponseBody
public String getEmployeesByIdAndNameWithMapVariable(@PathVariable Map<String, String> pathVarsMap) {
    String id = pathVarsMap.get("id");
    String name = pathVarsMap.get("name");
    if (id != null && name != null) {
        return "ID: " + id + ", name: " + name;
    } else {
        return "Missing Parameters";
    }
}
```

## **Optional Path Variables**

**In Spring, method parameters annotated with \*@PathVariable\* are required by default:**

```
@GetMapping(value = { "/api/employeeswithrequired", "/api/employeeswithrequired/{id}" })
@ResponseBody
public String getEmployeesByIdWithRequired(@PathVariable String id) {
    return "ID: " + id;
}
```

By how it looks, the above controller should handle both */api/employeeswithrequired* and */api/employeeswithrequired/1* request paths. But, since method parameters annotated by *@PathVariables* are mandatory by default, it doesn't handle the requests sent to */api/employeeswithrequired* path

We can handle this in two ways.

### Setting *@PathVariable* as Not Required

**We can set the \*required\* property of \*@PathVariable\* to \*false\* to make it optional.** Hence, modifying our previous example, we can now handle the URI versions with and without the path variable:

```
@GetMapping(value = { "/api/employeeswithrequiredfalse", "/api/employeeswithrequiredfalse/{id}" })
@ResponseBody
public String getEmployeesByIdWithRequiredFalse(@PathVariable(required = false) String id) {
    if (id != null) {
        return "ID: " + id;
    } else {
        return "ID missing";
    }
}
```

### Using *java.util.Optional*

Since Spring 4.1, we can also use [*java.util.Optional*](https://www.baeldung.com/java-optional) (available in Java 8+) to handle a non-mandatory path variable:

```
@GetMapping(value = { "/api/employeeswithoptional", "/api/employeeswithoptional/{id}" })
@ResponseBody
public String getEmployeesByIdWithOptional(@PathVariable Optional<String> id) {
    if (id.isPresent()) {
        return "ID: " + id.get();
    } else {
        return "ID missing";
    }
}
```

### Using a Method Parameter of Type *Map<String, String>*

As shown earlier, we can use a single method parameter of type *java.util.Map* to handle all the path variables in the request URI. **We can also use this strategy to handle the optional path variables case:**

```
@GetMapping(value = { "/api/employeeswithmap/{id}", "/api/employeeswithmap" })
@ResponseBody
public String getEmployeesByIdWithMap(@PathVariable Map<String, String> pathVarsMap) {
    String id = pathVarsMap.get("id");
    if (id != null) {
        return "ID: " + id;
    } else {
        return "ID missing";
    }
}
```

## Default Value for *@PathVariable*

Out of the box, there isn't a provision to define a default value for method parameters annotated with *@PathVariable*. However, we can use the same strategies discussed above to satisfy the default value case for *@PathVariable*. We just need to check for *null* on the path variable.

For instance, using *java.util.Optional<String, String>*, we can identify if the path variable is *null* or not. If it is *null* then we can just respond to the request with a default value:

```
@GetMapping(value = { "/api/defaultemployeeswithoptional", "/api/defaultemployeeswithoptional/{id}" })
@ResponseBody
public String getDefaultEmployeesByIdWithOptional(@PathVariable Optional<String> id) {
    if (id.isPresent()) {
        return "ID: " + id.get();
    } else {
        return "ID: Default Employee";
    }
}
```

