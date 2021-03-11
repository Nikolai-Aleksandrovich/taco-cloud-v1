package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.domain.User;

/**
 * @author Yuyuan Huang
 * @create 2021-01-27 16:06
 */
public interface UserRepository extends CrudRepository<User,Long>{
    User findByUsername(String userName);
}