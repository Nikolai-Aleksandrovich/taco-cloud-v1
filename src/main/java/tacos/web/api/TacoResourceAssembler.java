package tacos.web.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import tacos.domain.Taco;

import java.util.List;

/**
 * @author Yuyuan Huang
 * @create 2021-03-08 11:37
 */
public class TacoResourceAssembler extends RepresentationModelAssemblerSupport<Taco,TacoResource> {
    public TacoResourceAssembler(){
        super(DesignTacoController.class,TacoResource.class);
    }
    @Override
    protected TacoResource instantiateModel(Taco taco){
        return new TacoResource(taco);
    }
    @Override
    public TacoResource toModel(Taco taco){
        return createModelWithId(taco.getId(),taco);
    }



}
