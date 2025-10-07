package org.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.example.model.Product;

public interface ProductRepository extends MongoRepository<Product, String> {
    // You can define custom query methods here if needed
}
