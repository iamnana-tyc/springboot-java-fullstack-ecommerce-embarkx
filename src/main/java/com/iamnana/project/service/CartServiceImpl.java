package com.iamnana.project.service;

import com.iamnana.project.exceptions.APIException;
import com.iamnana.project.exceptions.ResourceNotFoundException;
import com.iamnana.project.model.Cart;
import com.iamnana.project.model.CartItem;
import com.iamnana.project.model.Product;
import com.iamnana.project.payload.CartDTO;
import com.iamnana.project.payload.ProductDTO;
import com.iamnana.project.respositories.CartItemRepository;
import com.iamnana.project.respositories.CartRepository;
import com.iamnana.project.respositories.ProductRepository;
import com.iamnana.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;


@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtil authUtil;


    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();

        // Retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId",  productId));

        // Perform validations
        // 1. we check if product exist in the cartItem connected to user.
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );

        // 2. we check if the product name already exist in cart
        if (cartItem != null){
            throw new APIException("Product " + product.getProductName() + " already exists in cart");
        }

        // 3. check if that product is available - as in there are stocks.
        if (product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        // 4. check if quantity requested by user is enough
        if (product.getQuantity() < quantity){
            throw new APIException("Please, place an order of " + product.getProductName()
            + " less than or equal to " + product.getQuantity() + ".");
        }

        // Create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setCart(cart);
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        // save the cart item
        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity()); // set the quantity of product in stocks

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        // Return cartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream()
                .map(item -> {
                    ProductDTO mapT = modelMapper.map(item.getProduct(), ProductDTO.class);
                    mapT.setQuantity(item.getQuantity()); // this updates the quantity in cart item
                    return mapT;
                });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;

    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        if (carts.isEmpty()){
            throw new APIException("No carts found.");
        }

        // if there are carts we need to convert into CartDTO and return
        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
                    CartDTO cartDto = modelMapper.map(cart, CartDTO.class);

                    List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                        ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                        productDTO.setQuantity(cartItem.getQuantity());
                        return productDTO;
                    }).toList();

                    cartDto.setProducts(products);
                    return cartDto;
                }).toList();

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(product -> product.getProduct().setQuantity(product.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                .toList();
        cartDTO.setProducts(products);

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0){
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity){
            throw new APIException("Please, place an order of " + product.getProductName()
                    + " less than or equal to " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if (cartItem == null){
            throw new APIException("Product " + product.getProductName() + " not available in cart");
        }

        // calculate the new quantity
        int newQuantity = cartItem.getQuantity() + quantity;

        // Validation to prevent negatives and if 0 we delete the cart
        if (newQuantity < 0){
            throw new APIException("The quantity cannot be negative.");
        }
        if (newQuantity == 0 ){
            deleteProductFromCart(productId, cartId);
        }else {
            // Update the cart item
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cartItem.setProductPrice(product.getSpecialPrice());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

            cartRepository.save(cart);
        }

        CartItem updatedCartItem =  cartItemRepository.save(cartItem);

        // after updating the cartItem is possible there will nothing in the cartItem and we need to handle such case
        if (updatedCartItem.getQuantity() == 0){
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO mapProduct = modelMapper.map(item.getProduct(), ProductDTO.class);
                    mapProduct.setQuantity(item.getQuantity());
                    return mapProduct;
                });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if (cartItem == null){
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product" + cartItem.getProduct().getProductName() + " is removed from cart";
    }

    @Override
    public void updateProductsInCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null){
            throw new APIException("Product " + product.getProductName() + " not available in cart");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }

    private Cart createCart(){
        // Find existing or create cart associated with the user.
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());

        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
