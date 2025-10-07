package org.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.example.model.Product;
import org.example.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product addProduct(Product p) {
        return repo.save(p);
    }

    public Optional<Product> getProductById(String id) {
        return repo.findById(id);
    }

    public Product updateProduct(Product p) {
        return repo.save(p);
    }

    public void deleteProduct(String id) {
        repo.deleteById(id);
    }
}
