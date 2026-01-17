package io.github.vishalmysore.service.advanced;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.detect.ActionCallback;
import com.t4a.processor.ProcessorAware;
import io.github.vishalmysore.a2ui.A2UIAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Agent(groupName = "productCatalog",     groupDescription = "Browse product catalog and view details", prompt = "You are a product catalog assistant. Help users explore products, filter by category, and view detailed information about each product.")
@Slf4j
public class ProductCatalogService implements A2UIAware, ProcessorAware {
    
    private ThreadLocal<ActionCallback> callback = new ThreadLocal<>();
    
    // Product data structure
    private static class Product {
        String id, name, category, rating;
        double price;
        int stock;
        
        Product(String id, String name, String category, double price, String rating, int stock) {
            this.id = id; this.name = name; this.category = category;
            this.price = price; this.rating = rating; this.stock = stock;
        }
    }
    
    @Action(description = "Display product catalog with filtering")
    public Object showCatalog(String category) {
        log.info("Displaying catalog for category: {}", category);
        
        if(isUICallback(getCallback())) {
            return createCatalogGridUI(category);
        } else {
            return "Showing products in category: " + category;
        }
    }
    
    @Action(description = "View detailed information about a specific product")
    public Object viewProductDetails(String productId) {
        log.info("Viewing product details for ID: {}", productId);
        
        // Fetch product details (simulated)
        Product product = getProductById(productId);
        
        if(isUICallback(getCallback())) {
            return createProductDetailUI(product);
        } else {
            return String.format("%s - $%.2f (Stock: %d)", 
                product.name, product.price, product.stock);
        }
    }
    
    private Product getProductById(String id) {
        // Simulate database lookup
        Map<String, Product> db = new HashMap<>();
        db.put("P001", new Product("P001", "Wireless Headphones Pro", "Electronics", 299.99, "⭐⭐⭐⭐⭐", 45));
        db.put("P002", new Product("P002", "Smart Fitness Watch", "Wearables", 199.99, "⭐⭐⭐⭐", 120));
        db.put("P003", new Product("P003", "Portable Bluetooth Speaker", "Audio", 89.99, "⭐⭐⭐⭐⭐", 78));
        return db.getOrDefault(id, new Product(id, "Unknown Product", "Other", 0, "⭐", 0));
    }
    
    private Map<String, Object> createCatalogGridUI(String category) {
        String surfaceId = "product_catalog_grid";
        String rootId = "root";
        
        List<String> childIds = new ArrayList<>();
        childIds.add("header");
        childIds.add("category_badge");
        childIds.add("total_products");
        childIds.add("divider1");
        childIds.add("grid_title");
        
        // Product cards (3 products)
        Product[] products = {
            new Product("P001", "Wireless Headphones Pro", "Electronics", 299.99, "⭐⭐⭐⭐⭐", 45),
            new Product("P002", "Smart Fitness Watch", "Wearables", 199.99, "⭐⭐⭐⭐", 120),
            new Product("P003", "Portable Bluetooth Speaker", "Audio", 89.99, "⭐⭐⭐⭐⭐", 78)
        };
        
        for (int i = 0; i < products.length; i++) {
            String idx = String.valueOf(i + 1);
            childIds.addAll(Arrays.asList(
                "card" + idx + "_title",
                "card" + idx + "_price",
                "card" + idx + "_rating",
                "card" + idx + "_stock",
                "card" + idx + "_button",
                "card" + idx + "_button_text",
                "card" + idx + "_divider"
            ));
        }
        
        childIds.addAll(Arrays.asList("divider2", "filter_section", 
            "category_input", "filter_button", "filter_button_text"));
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "🛍️ Product Catalog", "h1"));
        components.add(createTextComponent("category_badge", 
            "🏷️ Category: " + category, "h2"));
        components.add(createTextComponent("total_products", 
            "📦 " + products.length + " products available", "body"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        components.add(createTextComponent("grid_title", 
            "Featured Products:", "h2"));
        
        // Product Cards Grid
        Map<String, Object> dataModel = new HashMap<>();
        
        for (int i = 0; i < products.length; i++) {
            Product p = products[i];
            String idx = String.valueOf(i + 1);
            
            components.add(createTextComponent("card" + idx + "_title", 
                "📦 " + p.name, "h3"));
            components.add(createTextComponent("card" + idx + "_price", 
                "💵 Price: $" + String.format("%.2f", p.price), "body"));
            components.add(createTextComponent("card" + idx + "_rating", 
                "Rating: " + p.rating, "body"));
            
            String stockStatus = p.stock > 50 ? " ✅ In Stock" : 
                                p.stock > 10 ? " ⚠️ Low Stock" : " ❌ Limited";
            components.add(createTextComponent("card" + idx + "_stock", 
                "📊 Stock: " + p.stock + stockStatus, "body"));
            
            // View Details button
            Map<String, String> detailBindings = new HashMap<>();
            detailBindings.put("productId", "/catalog/product" + idx + "/id");
            components.add(createButtonComponent("card" + idx + "_button", 
                "View Details", "viewProductDetails", detailBindings));
            components.add(createTextComponent("card" + idx + "_button_text", "🔍 View Details"));
            
            components.add(createTextComponent("card" + idx + "_divider", 
                "─────────────────────────", "body"));
            
            // Store product ID in data model
            dataModel.put("/catalog/product" + idx + "/id", p.id);
        }
        
        components.add(createTextComponent("divider2", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Filter Section
        components.add(createTextComponent("filter_section", 
            "🔎 Filter Products:", "h3"));
        components.add(createTextFieldComponent("category_input", 
            "Category (Electronics/Wearables/Audio)", "/catalog/filter/category"));
        
        Map<String, String> filterBindings = new HashMap<>();
        filterBindings.put("category", "/catalog/filter/category");
        components.add(createButtonComponent("filter_button", 
            "Apply Filter", "showCatalog", filterBindings));
        components.add(createTextComponent("filter_button_text", "🎯 Apply Filter"));
        
        dataModel.put("/catalog/filter/category", category);
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
    
    private Map<String, Object> createProductDetailUI(Product product) {
        String surfaceId = "product_details";
        String rootId = "root";
        
        List<String> childIds = Arrays.asList(
            "header", "product_name", "divider1",
            "details_section", "sku", "price", "rating", "stock_status",
            "divider2", "description_title", "desc1", "desc2", "desc3",
            "divider3", "features_title", "feature1", "feature2", "feature3", "feature4",
            "divider4", "actions_title", "qty_label", "qty_input",
            "add_cart_button", "add_cart_text", "back_button", "back_text"
        );
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "🎁 Product Details", "h1"));
        components.add(createTextComponent("product_name", 
            product.name, "h2"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Product Details
        components.add(createTextComponent("details_section", 
            "📋 Product Information:", "h3"));
        components.add(createTextComponent("sku", 
            "SKU: " + product.id, "body"));
        components.add(createTextComponent("price", 
            "💰 Price: $" + String.format("%.2f", product.price), "h3"));
        components.add(createTextComponent("rating", 
            "⭐ Customer Rating: " + product.rating, "body"));
        
        String stockEmoji = product.stock > 50 ? "✅" : product.stock > 10 ? "⚠️" : "🚨";
        String stockText = product.stock > 50 ? "In Stock" : 
                          product.stock > 10 ? "Limited Stock" : "Low Stock - Order Soon!";
        components.add(createTextComponent("stock_status", 
            stockEmoji + " Availability: " + stockText + " (" + product.stock + " units)", "body"));
        
        components.add(createTextComponent("divider2", 
            "─────────────────────────", "body"));
        
        // Description
        components.add(createTextComponent("description_title", 
            "📝 Description:", "h3"));
        components.add(createTextComponent("desc1", 
            "Premium quality " + product.name + " designed for excellence.", "body"));
        components.add(createTextComponent("desc2", 
            "Perfect for everyday use with industry-leading performance.", "body"));
        components.add(createTextComponent("desc3", 
            "Backed by our satisfaction guarantee and 2-year warranty.", "body"));
        
        components.add(createTextComponent("divider3", 
            "─────────────────────────", "body"));
        
        // Features
        components.add(createTextComponent("features_title", 
            "✨ Key Features:", "h3"));
        components.add(createTextComponent("feature1", 
            "✓ Premium Build Quality", "body"));
        components.add(createTextComponent("feature2", 
            "✓ Advanced Technology", "body"));
        components.add(createTextComponent("feature3", 
            "✓ User-Friendly Design", "body"));
        components.add(createTextComponent("feature4", 
            "✓ Free Shipping & Returns", "body"));
        
        components.add(createTextComponent("divider4", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Purchase Actions
        components.add(createTextComponent("actions_title", 
            "🛒 Purchase Options:", "h3"));
        components.add(createTextComponent("qty_label", 
            "Select Quantity:", "body"));
        components.add(createTextFieldComponent("qty_input", 
            "Quantity (1-" + Math.min(10, product.stock) + ")", "/product/quantity"));
        
        Map<String, String> cartBindings = new HashMap<>();
        cartBindings.put("category", "/product/category");
        components.add(createButtonComponent("add_cart_button", 
            "Add to Cart", "showCatalog", cartBindings));
        components.add(createTextComponent("add_cart_text", "🛒 Add to Cart"));
        
        Map<String, String> backBindings = new HashMap<>();
        backBindings.put("category", "/product/category");
        components.add(createButtonComponent("back_button", 
            "Back to Catalog", "showCatalog", backBindings));
        components.add(createTextComponent("back_text", "◀️ Back to Catalog"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/product/quantity", "1");
        dataModel.put("/product/category", product.category);
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
