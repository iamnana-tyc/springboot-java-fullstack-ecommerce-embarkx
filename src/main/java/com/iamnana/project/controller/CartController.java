package com.iamnana.project.controller;

import com.iamnana.project.model.Cart;
import com.iamnana.project.payload.CartDTO;
import com.iamnana.project.respositories.CartRepository;
import com.iamnana.project.service.CartService;
import com.iamnana.project.util.AuthUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Cart", description = "APIs for managing the cart endpoints")
@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService  cartService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;


    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cart created successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content),
    })
    @PostMapping("/carts/product/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.CREATED);

    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> allCarts = cartService.getAllCarts();

        return new ResponseEntity<List<CartDTO>>(allCarts, HttpStatus.OK);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartById() {
        String emailId = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();

        CartDTO cartDTO = cartService.getCart(emailId, cartId);

        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content),
    })
    @PutMapping("/carts/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(
            @Parameter(description = "The product id")
            @PathVariable Long productId,
            @PathVariable String operation) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);

        return new ResponseEntity<>(cartDTO, HttpStatus.OK);

    }

    @DeleteMapping("carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(
            @Parameter(description = "The cart id")
            @PathVariable Long cartId,
            @Parameter(description = "The product id")
            @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId,  productId);

        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}
