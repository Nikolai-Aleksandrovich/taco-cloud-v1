package tacos.data;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import tacos.domain.Taco;

import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-06 16:32
 */
public interface JPATacoRepository extends PagingAndSortingRepository<Taco,Long> {
//    Iterable<Taco> findAll(PageRequest pageRequest);
//    List<Taco> getContent(Iterable<Taco> tacos);
}
