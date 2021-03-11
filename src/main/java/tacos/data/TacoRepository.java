package tacos.data;

import tacos.domain.Taco;

/**
 * @author Yuyuan Huang
 * @create 2021-01-22 16:36
 */
//这是使用JDBC模板
public interface TacoRepository {
    Taco save(Taco design);
}
//这是使用CrudRepository接口
//public interface TacoRepository extends CrudRepository<Taco,Long> {
//
//}