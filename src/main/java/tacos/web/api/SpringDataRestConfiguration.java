package tacos.web.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import tacos.domain.Taco;

/**
 * @author Yuyuan Huang
 * @create 2021-03-10 16:05
 */
@Configuration
public class SpringDataRestConfiguration {
    @Bean
    public RepresentationModelProcessor<PagedModel<EntityModel<Taco>>> tacoProcessor(EntityLinks links){
        return model -> {
            model.add(
                    links.linkFor(Taco.class)
                            .slash("recent")
                            .withRel("recents")
            );
            return model;
        };
    }
}
