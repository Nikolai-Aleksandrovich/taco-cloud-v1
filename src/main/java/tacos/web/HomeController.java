package tacos.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Yuyuan Huang
 * @create 2021-01-13 16:44
 */
@Controller//
public class HomeController {
    @GetMapping("/")
    public String home(){
        return "home";
    }
}
