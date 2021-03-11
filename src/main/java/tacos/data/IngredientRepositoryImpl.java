package tacos.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tacos.domain.Ingredient;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Yuyuan Huang
 * @create 2021-01-21 22:54
 */
@Repository//添加该注解，spring组件扫描会发现这个类，并把它初始化为bean并加入到容器中，类似于@Controller@Component
public class IngredientRepositoryImpl implements IngredientRepository{
    private JdbcTemplate jdbc;
    @Autowired
    public void JdbcIngredientRepository(JdbcTemplate jdbcTemplate){
        this.jdbc=jdbcTemplate;
    }

    @Override
    public Iterable<Ingredient> findAll() {
        return jdbc.query("select id,name,type from Ingredient",this::mapRowToIngredient);
    }
    @Override
    public Ingredient findOne(String id){
        return jdbc.queryForObject("select id,name,type from Ingredient where id = ?",this::mapRowToIngredient,id);
    }
    @Override
    public Ingredient save(Ingredient ingredient){
        jdbc.update("insert into Ingredient (id,name,type) values(?,?,?)",
        ingredient.getId(),
        ingredient.getName(),
        ingredient.getType().toString());
        return ingredient;
    }
    private Ingredient mapRowToIngredient(ResultSet rs,int rowNum)throws SQLException{
        return new Ingredient(rs.getString("id"),rs.getString("name"),Ingredient.Type.valueOf(rs.getString("type")));
    }

//    @Override
//    public Ingredient findById(String id) {
//        return jdbc.queryForObject("select id,name,type from Ingredient where id=?",this::mapRowToIngredient,id);
//    }
//
//    @Override
//    public Ingredient save(Ingredient ingredient) {
//        jdbc.update("insert into Ingredient(id,name,type) values(?,?,?)",ingredient.getId(),ingredient.getName(),ingredient.getType());
//        return ingredient;
//    }

//    private Ingredient mapRowToIngredient(ResultSet rs,int rowNum) throws SQLException {
//        return new Ingredient(
//                rs.getString("id"),
//                rs.getString("name"),
//                Ingredient.Type.valueOf(rs.getString("Type")));
//    }
    //编写完毕需要注入到DesignTacoController
}
