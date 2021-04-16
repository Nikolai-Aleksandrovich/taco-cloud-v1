## service和Dao层有什么关系?

Dao(Repository)层：主要是做数据持久层的工作，负责与数据库进行联络的一些任务都封装在此，DAO层的设计首先是设计DAO的接口，然后就可在模块中调用此接口来进行数据业务的处理，而不用关心此接口的具体实现类是哪个类，显得结构非常清晰，DAO层的数据源配置。

dao层代码示例：

```
public interface UserDao{
	void add(User user);
	User find(String id);
	User find(String username,String password);
}
```

**service层：主要负责业务模块的逻辑应用设计**, Service层的业务实现，具体要调用到已定义的DAO层的接口，封装Service层的业务逻辑有利于通用的业务逻辑的独立性和重复利用性，程序显得非常简洁。 

service层代码示例：

```
public class userServiceImpl implements UserService{
	private UserDao userDao;
	@Override
	public void add(User user){
		this.userDao.add(user);
	}
}
```

**dao层和service层关系：**service层经常要调用dao层的方法对数据进行增删改查的操作，现实开发中，对业务的操作会涉及到数据的操作，而对数据操作常常要用到数据库，所以service层会经常调用dao层的方法。

**扩展资料**

Service层是建立在DAO层之上的，建立了DAO层后才可以建立Service层，而Service层又是在Controller层之下的，因而Service层应该既调用DAO层的接口，它刚好处于一个中间层的位置。每个模型都有一个Service接口，每个接口分别封装各自的业务处理方法。

在实际开发中DAO层大多是对某张表进行增删改查，都是一些固定的语句，除非涉及到更复杂的service层业务逻辑，才可能要添加更复杂的DAO层方法。

 **Controller层一般都是写接口**提供给前端或者后端其他服务使用的，**一般后端的接口都是写在 Controller层，\**Controller层的接口里面不写业务逻辑，主要是调用Service层的业务逻辑方法，\*\*\*\*service层主要是写业务逻辑方法，\*\*\*\*service层经常要调用dao层的方法对数据进行增删改查的操作。\****