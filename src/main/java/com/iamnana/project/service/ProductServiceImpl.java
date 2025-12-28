package com.iamnana.project.service;

import com.iamnana.project.exceptions.APIException;
import com.iamnana.project.exceptions.ResourceNotFoundException;
import com.iamnana.project.model.Cart;
import com.iamnana.project.model.Category;
import com.iamnana.project.model.Product;
import com.iamnana.project.payload.CartDTO;
import com.iamnana.project.payload.ProductDTO;
import com.iamnana.project.payload.ProductResponse;
import com.iamnana.project.respositories.CartRepository;
import com.iamnana.project.respositories.CategoryRepository;
import com.iamnana.project.respositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartService cartService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO saveProduct(ProductDTO productDTO, Long categoryId) {
        // First we need the category id
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        //  check if product already exist or not
        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();
        for (Product product : products){
            if (product.getProductName().equals(productDTO.getProductName())){
                isProductNotPresent = false;
                break;
            }
        }

        if (isProductNotPresent){
            Product product = modelMapper.map(productDTO, Product.class);
            product.setImage("default.jpg");
            product.setCategory(category);

            // The special price is price after reducing the discount price. calculated as: price - (discount * 100 * price)
            // so if price is 100, and discount is 15 then special price will be: [100 - %15 * 100] = 100 - (0.15 * 100) = 85
            double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
            product.setSpecialPrice(specialPrice);
            Product savedProduct =  productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }else {
            throw new APIException("Product already exist.");
        }

    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageable);
        List<Product> products = productPage.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageDetail = productRepository.findByCategoryOrderByPriceAsc(category, pageable);
        List<Product> products = pageDetail.getContent();

        if (products.isEmpty()){
            throw new APIException(category.getCategoryName() + " category does not have any products");
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetail.getNumber());
        productResponse.setPageSize(pageDetail.getSize());
        productResponse.setTotalElements(pageDetail.getTotalElements());
        productResponse.setTotalPages(pageDetail.getTotalPages());
        productResponse.setLastPage(pageDetail.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> pageDetail = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageable);
        List<Product> products = pageDetail.getContent();


        List<ProductDTO> productDTOS = products.stream()
                .map(product-> modelMapper.map(product, ProductDTO.class))
                .toList();

        if (products.isEmpty()){
            throw new APIException("Product not found with the keyword: " + keyword);
        }


        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(pageDetail.getNumber());
        productResponse.setPageSize(pageDetail.getSize());
        productResponse.setTotalElements(pageDetail.getTotalElements());
        productResponse.setTotalPages(pageDetail.getTotalPages());
        productResponse.setLastPage(pageDetail.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());

        double specialPrice = product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice());
        existingProduct.setSpecialPrice(specialPrice);

        // save the product back to database
        Product savedProduct = productRepository.save(existingProduct);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
       List<CartDTO> cartDTOs = carts.stream().map(cart -> {
           CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
           List<ProductDTO> products = cart.getCartItems().stream()
                   .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class))
                   .toList();
           cartDTO.setProducts(products);
           return cartDTO;
       }).toList();

       cartDTOs.forEach(cart -> cartService.updateProductsInCart(cart.getCartId(), productId));

        return modelMapper.map(savedProduct,ProductDTO.class);
    }

    @Override
    public void deleteProduct(Long productId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

        productRepository.deleteById(productId);
    }

    @Override
    public ProductDTO updateProductImage(Long productId, MultipartFile file) throws IOException {
        // we need to get product from database
        Product productFromDB = productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        // we upload or save image to server
        // Get the filename of the uploaded image
        String filePath = path;
        String fileName = fileService.uploadImage(filePath, file);

        // Update the new filename to product
        productFromDB.setImage(fileName);

        // save the update to the database
        Product savedImage = productRepository.save(productFromDB);

        return modelMapper.map(savedImage, ProductDTO.class);
    }
}
