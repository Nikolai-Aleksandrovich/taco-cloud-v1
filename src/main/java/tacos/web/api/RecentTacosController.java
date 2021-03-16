package tacos.web.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import tacos.data.JPATacoRepository;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import tacos.domain.Taco;

import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-10 14:38
 */
@RepositoryRestController//确保这个路径添加Spring Data REST基础路径为前缀，那么recentTaco方法会处理"/api/tacos/recent"的Get请求
//但是这个注解不能起到@RestController的作用，所有要么加@RestController，要么返回体为ResponseEntity
public class RecentTacosController {
    private JPATacoRepository jpaTacoRepository;
    public RecentTacosController(JPATacoRepository jpaTacoRepository){
        //注入SPRING DATA JPA CRUD
        this.jpaTacoRepository=jpaTacoRepository;
    }
    @GetMapping(path = "/tacos/recent",produces = "application/hal+json")
    public ResponseEntity<CollectionModel<TacoResource>> recentTaco(){
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        List<Taco> tacos = jpaTacoRepository.findAll(page).getContent();
        CollectionModel<TacoResource> tacoResources = new TacoResourceAssembler().toCollectionModel(tacos);
        CollectionModel<TacoResource> recentResources= CollectionModel.of(tacoResources);
        recentResources.add(
                linkTo(methodOn(RecentTacosController.class).recentTaco()).withRel("recents")
        );
        return  new ResponseEntity<>(recentResources, HttpStatus.OK);
    }
//    import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
//
//    @GetMapping("/employees")
//    public Mono<CollectionModel<EntityModel<Employee>>> all() {
//
//        var controller = methodOn(WebFluxEmployeeController.class);
//
//        return Flux.fromIterable(EMPLOYEES.keySet())
//                .flatMap(id -> findOne(id))
//                .collectList()
//                .flatMap(resources -> linkTo(controller.all()).withSelfRel() (1)
//                .andAffordance(controller.newEmployee(null)) (2)
//                .andAffordance(controller.search(null, null))
//                .toMono() (3)
//                .map(selfLink -> new CollectionModel<>(resources, selfLink)));
//    }

}
