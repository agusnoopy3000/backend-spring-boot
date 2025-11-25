package cl.huertohogar.huertohogar_api.service;

import cl.huertohogar.huertohogar_api.dto.ProductRequest;
import cl.huertohogar.huertohogar_api.dto.ProductResponse;
import cl.huertohogar.huertohogar_api.exception.BadRequestException;
import cl.huertohogar.huertohogar_api.exception.ResourceNotFoundException;
import cl.huertohogar.huertohogar_api.model.Product;
import cl.huertohogar.huertohogar_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new BadRequestException("Product code already exists");
        }

        Product product = new Product();
        product.setCodigo(request.getCodigo());
        product.setNombre(request.getNombre());
        product.setDescripcion(request.getDescripcion());
        product.setPrecio(request.getPrecio());
        product.setStock(request.getStock());
        product.setImagen(request.getImagen());
        product.setCategoria(request.getCategoria());

        product = productRepository.save(product);
        return new ProductResponse(product);
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return new ProductResponse(product);
    }

    public ProductResponse getProductByCodigo(String codigo) {
        Product product = productRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with code: " + codigo));
        return new ProductResponse(product);
    }

    public List<ProductResponse> getAllProducts(String q) {
        List<Product> products = (q != null && !q.isEmpty()) ? productRepository.findByQuery(q) : productRepository.findAll();
        return products.stream().map(ProductResponse::new).toList();
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setNombre(request.getNombre());
        product.setDescripcion(request.getDescripcion());
        product.setPrecio(request.getPrecio());
        product.setStock(request.getStock());
        product.setImagen(request.getImagen());
        product.setCategoria(request.getCategoria());

        product = productRepository.save(product);
        return new ProductResponse(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
