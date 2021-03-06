package tacos.data;

import org.springframework.data.repository.CrudRepository;
import tacos.Taco;

/**
 * @author Yuyuan Huang
 * @create 2021-03-06 16:32
 */
public interface JPATacoRepository extends CrudRepository<Taco,Long> {
}
