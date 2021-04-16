build a *Customer* entity:

```java
@Entity
public class customer{
	@Id
	@GeneratedValue(strategy = GenerationType.Auto)
	public long id;
	public String phone;
	public String name;
}
```

CRUD repository:

```java
@Repository
public interface CustomerRepository extends CrudRepository{
	Customer findById(long id);
}
```

Customer Service:

```java
@Service
public class CustomerService{
	@Autowired
	CustomerRepository repo;
	public void addCustomer(String name){
		Customer c = new Customer();
		c.name = name;
		repo.save(c);
	}
}
```

**loading our entities from the database and then updating only the fields we need.**

This basic technique is efficient when the number of fields to update is relatively small, and our entities are rather simple.

```java
//in CustomerService 
public void updateCustomer(long id,String phone){
	Customer myCustomer = repo.findById(id);
	myCustomer.phone = phone;
	repo.save(mycustomer);
}
```

##  DTO pattern

When our objects have a large number of fields with [different access levels](https://www.baeldung.com/java-access-modifiers), it's quite common to implement the [DTO pattern](https://www.baeldung.com/entity-to-and-from-dto-for-a-java-spring-application).

create a *CustomerDto*:

```java
public class CustomerDto{
	private long id;
	public String name;
	public String phone;
	...
	public String phone99;
}
```

Customer Mapper:

*MapStruct* has a *@BeanMapping* method decorator, that lets us define a rule to skip *null* values during the mapping process. Let's add it to our *updateCustomerFromDto* method interface:

```java
@Mapper(componentModel = "spring")
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CustomerMapper{
	void updateCustomerFromCustomerDto(Customer Dto,@MappingTarget Customer entity);
}
//@MappingTarget annotation lets us update an existing object
```

**With this, we can load stored entities and merge them with a DTO before calling JPA \*save\* method: in fact, we'll update only the modified values.**

So, let's add a method to our service, which will call our mapper:

```java
public void updateCustomer(CustomerDto dto) {
    Customer myCustomer = repo.findById(dto.id);
    mapper.updateCustomerFromDto(dto, myCustomer);
    repo.save(myCustomer);
}
```

The drawback of this approach is that we can't pass *null* values to the database during an update.

## Simpler Entities

**It's essential to define our entities to be as small as possible.**

What if we structure it a little bit, and extract all the *phone* fields to *ContactPhone* entities and be under a [one-to-many](https://www.baeldung.com/hibernate-one-to-many) relationship?

```java
@Entity public class CustomerStructured {
    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    public String name;
    @OneToMany(fetch = FetchType.EAGER, targetEntity=ContactPhone.class, mappedBy="customerId")    
    private List<ContactPhone> contactPhones;
}
```

The code is clean and, more importantly, we achieved something. Now, we can update our entities without having to retrieve and fill all the *phone* data.

**Handling small and bounded entities allows us to update only the necessary fields.**

## Custom Query

In fact, JPA defines two annotations, *[@Modifying](https://www.baeldung.com/spring-data-jpa-modifying-annotation)* and [*@Query*](https://www.baeldung.com/spring-data-jpa-query), which allow us to write our update statement explicitly.

add our custom update method in the repository:

```java
@Modifying
@Query("update Customer u set u.phone = :phone where u.id = :id")
void updatePhone(@Param(value = "id") long id, @Param(value = "phone") String phone);
```

Now, we can rewrite our update method:

```java
public void updateCustomerContacts(long id, String phone) {
    repo.updatePhone(id, phone);
}
```

