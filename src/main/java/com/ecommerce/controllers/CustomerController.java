package com.ecommerce.controllers;

import com.ecommerce.Repository.UserRepository;
import com.ecommerce.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.Repository.OrderRepository;
import com.ecommerce.entities.Order;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
public class CustomerController {

    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);


    @GetMapping("/myOrders")
    public String myOrders(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.loadUserByUserName(principal.getName());  // Adjust this method to match your UserRepository.
            if (user != null) {
                List<Order> orders = orderRepo.findByOrderBy(user.getId());  // Ensure this method exists and works.
                model.addAttribute("orders", orders);
                return "MyOrders";  // Ensure this view exists in 'src/main/resources/templates/'
            }
        }
        return "redirect:/login";
    }
    @PostMapping("/cancelOrder")
    public String cancelOrder(@RequestParam("orderId") Integer orderId, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderRepo.findById(orderId).orElseThrow(() -> new Exception("Order not found"));
            if ("Pending".equals(order.getOrderStatus())) {
                order.setOrderStatus("Cancelled");
                orderRepo.save(order);
                redirectAttributes.addFlashAttribute("successMessage", "Order has been successfully cancelled.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Order cannot be cancelled as it is not in a pending state.");
            }
        } catch (Exception e) {
            log.error("Error cancelling order: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error occurred: " + e.getMessage());
        }
        return "redirect:/myOrders";
    }

}
