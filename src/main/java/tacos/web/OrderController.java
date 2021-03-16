package tacos.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import tacos.domain.Order;
import tacos.domain.User;
import tacos.data.JPAOrderRepository;
import tacos.data.UserRepository;

import javax.validation.Valid;
import java.awt.print.Pageable;

/**
 * @author Yuyuan Huang
 * @create 2021-01-14 15:01
 */
@Slf4j
@Controller
@RequestMapping("/orders")
@SessionAttributes("order")
//@ConfigurationProperties(prefix = "taco.orders")
public class OrderController {
//    private int pageSize=20;
    private OrderProps props;
    private JPAOrderRepository orderRepo;
    private UserRepository userRepository;
//    public void setPageSize(int pageSize){
//        this.pageSize=pageSize;
//    }

    public OrderController(JPAOrderRepository orderRepo,UserRepository userRepository,OrderProps props) {
        this.orderRepo = orderRepo;
        this.userRepository=userRepository;
        this.props=props;

    }
    @GetMapping
    public String orderForUser(@AuthenticationPrincipal User user, Model model){
        Pageable pageable = (Pageable) PageRequest.of(0, props.getPageSize());//只显示降序创建时间最近20条
        model.addAttribute("order",orderRepo.findByUserOrderByPlaceAtDesc(user,pageable));
        return "orderList";

    }

    @GetMapping("/current")
    public String orderForm( ){
        return "orderForm";
    }
    @PostMapping
    public String precessOrder(@Valid Order order, Errors errors,
                               SessionStatus sessionStatus, @AuthenticationPrincipal User user){
        if(errors.hasErrors()){
            return "orderForm";
        }

        order.setUser(user);
        orderRepo.save(order);
        sessionStatus.setComplete();//重置session
        return "redirect:/";
    }
    @PatchMapping(path = "/{orderId}",consumes = "application/json")
    public Order patchOrder(@PathVariable("orderId")Long orderId,@RequestBody Order patch){
        Order order = orderRepo.findById(orderId).get();
        if(order.getName()!=null){
            order.setName(patch.getName());
        }
        if(order.getStreet()!=null){
            order.setStreet(patch.getStreet());
        }
        return orderRepo.save(order);
    }
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //返回204No_CONTENT
    public void deleteOrder(@PathVariable("orderId")Long id){
        try{
            orderRepo.deleteById(id);
        }catch (EmptyResultDataAccessException e){
            //什么都不做，因为成功删除和没找到的结果，都是没有这个数据
        }
    }

}
