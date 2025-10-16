package org.example.client.models;

public class Product {
    private String id;
    private String name;
    private double price;
    private int stock;
    private String imageUrl;
    private String brandId;

    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
    public String getBrandId() { return brandId; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setBrandId(String brandId) { this.brandId = brandId; }
}
