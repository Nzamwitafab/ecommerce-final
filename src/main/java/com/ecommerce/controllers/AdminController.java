package com.ecommerce.controllers;

import java.io.IOException;
import java.security.Principal;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.ecommerce.config.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.Repository.CategoryRepository;
import com.ecommerce.Repository.ProductRepository;
import com.ecommerce.Repository.UserRepository;
import com.ecommerce.entities.Category;
import com.ecommerce.entities.Product;
import com.ecommerce.entities.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private static final Integer DEFAULT_SELLER_ID = 1;  // Set this ID based on your application needs

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProductRepository productRepo;
	@Autowired
	private ProductService productService;
	@Autowired
	private SellerController sellerController;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CategoryRepository categoryRepo;

	@GetMapping(value = { "/", "/home" })
	public String home(Model m, Principal principal) {
		User user = this.userRepo.loadUserByUserName(principal.getName());

		List<User> users = this.userRepo.getUsers();
		List<Category> categories = this.categoryRepo.findAll();
		List<Product> products = this.productRepo.findAll();

		m.addAttribute("title", "Admin | E-Commerce");
		m.addAttribute("user", user);
		m.addAttribute("users", users);
		m.addAttribute("products", products);
		m.addAttribute("product", new Product());  // Ensure 'product' is instantiated
		m.addAttribute("categories", categories);
		m.addAttribute("category", new Category());
		m.addAttribute("product", new Product());

		return "admin/index";
	}

	@GetMapping("/action")
	public String action(@RequestParam(value = "user_id", required = false) Integer id,
			@RequestParam(name = "user", required = false) String type,
			@RequestParam(name = "category", required = false) String categoryAction,
			@RequestParam(name = "title", required = false) String categoryTitle, Principal principal,
			HttpSession httpSession) throws IOException {

		/* Suspending or deleting user */

		if (type != null) {

			if (type.equals("suspend")) {

				User user = this.userRepo.getById(id);
				user.setEnable(false);
				this.userRepo.save(user);

				httpSession.setAttribute("status", "suspended");
				return "redirect:/admin/home?userSuspendById=" + id;

			}

			else if (type.equals("unsuspend")) {
				com.ecommerce.entities.User user = this.userRepo.getById(id);
				user.setEnable(true);
				this.userRepo.save(user);

				httpSession.setAttribute("status", "unsuspended");
				return "redirect:/admin/home?userUnsuspendById=" + id;
			}

			if (type.equals("delete")) {

				com.ecommerce.entities.User user = this.userRepo.getById(id);
				this.userRepo.delete(user);

				httpSession.setAttribute("status", "user-deleted");
				return "redirect:/admin/home?userDeleteById=" + id;

			}

		}

		/* =============================== */

		/* Adding categories or deleting categories */

		try {
			if (categoryAction.equals("addNew")) {

				if (categoryTitle.equals("") || categoryTitle == null || categoryTitle.isBlank()) {
					httpSession.setAttribute("status", "title-null");
					return "redirect:/admin/home";
				}

				Category category = new Category();
				category.setTitle(categoryTitle);

				this.categoryRepo.save(category);
				httpSession.setAttribute("status", "category-added");
				return "redirect:/admin/home";

			}

		} catch (DataIntegrityViolationException e) {
			httpSession.setAttribute("status", "category-already-exist");
			return "redirect:/admin/home";
		}

		/* ================================ */

		httpSession.setAttribute("status", "went-wrong");
		return "redirect:/admin/home";

	}
	@GetMapping("/productsModal")
	public String listProductsForModal(Model model) {
		model.addAttribute("products", productRepo.findAll());
		return "fragments/productModal"; // This should be a Thymeleaf fragment
	}

	// Save or update product from modal
	@PostMapping("/saveProduct")
	public String saveProductFromModal(@ModelAttribute Product product, RedirectAttributes redirectAttrs) {
		productRepo.save(product);
		redirectAttrs.addFlashAttribute("message", "Product saved successfully!");
		return "redirect:/admin/home";
	}

	// Delete a product from modal
	@PostMapping("/deleteProduct")
	public String deleteProduct(@RequestParam("productId") Integer productId, RedirectAttributes redirectAttributes) {
		try {
			productService.deleteProductById(productId);
			redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete the product. It may be linked to other records.");
		}
		return "redirect:/admin/dashboard";
	}
// AdminController

	@PostMapping("/addProductAsSeller")
	public String addProductAsSeller(@ModelAttribute("product") Product product,
									 BindingResult result,
									 @RequestParam("product_images") List<MultipartFile> images,
									 @RequestParam(value = "product_category", required = false) Integer selectedProductCategory,
									 Model model, HttpSession session,
									 RedirectAttributes redirectAttributes) {

		// Fetch the default seller details or let the admin select a seller ID
		User seller = userRepository.findById(DEFAULT_SELLER_ID)
				.orElseThrow(() -> new RuntimeException("Default seller not found"));

		// Create a Principal object with the seller's details
		Principal principal = () -> seller.getEmail();

		// Call the addProduct method from SellerController
		return sellerController.addProduct(product, result, images, selectedProductCategory, model, session, principal, redirectAttributes);
	}

}
