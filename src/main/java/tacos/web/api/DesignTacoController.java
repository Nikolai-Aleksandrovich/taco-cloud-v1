package tacos.web.api;
import org.hibernate.EntityMode;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tacos.domain.Taco;
import tacos.data.JPATacoRepository;

import javax.annotation.Resource;
import javax.annotation.Resources;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Yuyuan Huang
 * @create 2021-03-07 20:19
 */
@RestController
@RequestMapping(path="/design",produces = "application/json")
@CrossOrigin(origins = "*")
public class DesignTacoController {
    private JPATacoRepository jpaTacoRepository;

    public DesignTacoController(JPATacoRepository jpaTacoRepository){
        this.jpaTacoRepository=jpaTacoRepository;
    }
    @GetMapping("/recent")
    public CollectionModel<TacoResource> recentTacos(){
        PageRequest page = PageRequest.of(0,12, Sort.by("createdAt").descending());
        List<Taco> tacos = jpaTacoRepository.findAll(page).getContent();
        CollectionModel<TacoResource> tacoResources = new TacoResourceAssembler().toCollectionModel(tacos);
        CollectionModel<TacoResource> recentResources = CollectionModel.of(tacoResources);
        recentResources.add(
                linkTo(methodOn(DesignTacoController.class).recentTacos())
                .withRel("recents")
        );
        return recentResources;
    }
//    @GetMapping("/{id}")
//    public Taco tacoById(@PathVariable("id")Long id){
//        Optional<Taco> optionalTaco = jpaTacoRepository.findById(id);
//        if(optionalTaco.isPresent()){
//            return optionalTaco.get();
//        }
//        return null;
//    }
    @GetMapping("/{id}")
    public ResponseEntity<Taco> tacoById(@PathVariable("id")Long id){
        Optional<Taco> optionalTaco = jpaTacoRepository.findById(id);
        if(optionalTaco.isPresent()){
            return new ResponseEntity<>(optionalTaco.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
    }
}
