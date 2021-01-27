package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.User;

/**
 * @author Yuyuan Huang
 * @create 2021-01-27 16:06
 */
public interface UserRepository extends CrudRepository<User,Long> {
    User findByUsername(String username);
    //Spring Data JPA 会自动生成这个接口的实现
}
