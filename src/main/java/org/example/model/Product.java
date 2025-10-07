package org.example.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {

    @Id
    private String id;

    private String name;
    private double price;
    private int stock;
    private String brandId;    // Store the brand reference

    public Product() {} // default constructor

    public Product(String name, double price, int stock, String brandId, String imagePath) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.brandId = brandId;
    }

    // Getters & Setters
    public String getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
}
