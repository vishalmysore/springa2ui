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
@Agent(groupName = "orderTracking",     groupDescription = "Track order fulfillment with progress visualization", prompt = "You are an order tracking assistant. Provide users with detailed status updates and visual progress of their orders based on the order ID they provide.")
@Slf4j
public class OrderTrackingService implements A2UIAware, ProcessorAware {
    

    
    private static class OrderStatus {
        String orderId, customerName, estimatedDelivery;
        int currentStep;
        String[] timeline;
        
        OrderStatus(String orderId, String customerName, int currentStep, 
                   String estimatedDelivery, String[] timeline) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.currentStep = currentStep;
            this.estimatedDelivery = estimatedDelivery;
            this.timeline = timeline;
        }
    }
    
    @Action(description = "Track order status with visual progress")
    public Object trackOrder(String orderId) {
        log.info("Tracking order: {}", orderId);
        
        // Simulate fetching order details
        OrderStatus status = fetchOrderStatus(orderId);
        
        if(isUICallback(getCallback())) {
            return createOrderTrackingUI(status);
        } else {
            return String.format("Order %s is at step %d of %d", 
                orderId, status.currentStep + 1, status.timeline.length);
        }
    }
    
    private OrderStatus fetchOrderStatus(String orderId) {
        // Simulate database lookup
        String[] timeline = {
            "Jan 8, 10:30 AM", "Jan 8, 2:45 PM", "Jan 9, 8:00 AM",
            "Jan 10, 6:30 AM", "", ""
        };
        return new OrderStatus(orderId, "John Doe", 3, "Jan 12, 2026", timeline);
    }
    
    private Map<String, Object> createOrderTrackingUI(OrderStatus status) {
        String surfaceId = "order_tracker";
        String rootId = "root";
        
        String[] stepNames = {
            "Order Placed", "Payment Confirmed", "Processing", 
            "Shipped", "Out for Delivery", "Delivered"
        };
        
        String[] stepDescriptions = {
            "Your order has been received",
            "Payment processed successfully",
            "Order is being prepared",
            "Package is on the way",
            "Delivery in progress",
            "Order completed"
        };
        
        List<String> childIds = new ArrayList<>();
        childIds.add("header");
        childIds.add("order_id");
        childIds.add("customer_name");
        childIds.add("estimated_delivery");
        childIds.add("divider1");
        childIds.add("progress_title");
        childIds.add("progress_bar");
        
        // Add stepper components
        for (int i = 0; i < stepNames.length; i++) {
            childIds.add("step_" + i + "_status");
            childIds.add("step_" + i + "_name");
            childIds.add("step_" + i + "_desc");
            childIds.add("step_" + i + "_time");
            if (i < stepNames.length - 1) {
                childIds.add("connector_" + i);
            }
        }
        
        childIds.addAll(Arrays.asList("divider2", "track_another_title",
            "order_input", "track_button", "track_button_text"));
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "📦 Order Tracking", "h1"));
        components.add(createTextComponent("order_id", 
            "Order ID: " + status.orderId, "h2"));
        components.add(createTextComponent("customer_name", 
            "👤 Customer: " + status.customerName, "body"));
        components.add(createTextComponent("estimated_delivery", 
            "📅 Estimated Delivery: " + status.estimatedDelivery, "h3"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Progress Section
        components.add(createTextComponent("progress_title", 
            "🔄 Order Progress:", "h2"));
        
        // Visual progress bar
        double progressPercent = ((status.currentStep + 1) * 100.0) / stepNames.length;
        String progressBar = createProgressBar(progressPercent);
        components.add(createTextComponent("progress_bar", 
            progressBar + String.format(" %.0f%% Complete", progressPercent), "h3"));
        
        // Stepper Components
        for (int i = 0; i < stepNames.length; i++) {
            String emoji, statusText, timeText;
            
            if (i < status.currentStep) {
                emoji = "✅";
                statusText = "Completed";
                timeText = "⏰ " + status.timeline[i];
            } else if (i == status.currentStep) {
                emoji = "🔄";
                statusText = "In Progress";
                timeText = status.timeline[i].isEmpty() ? "⏳ Processing..." : "⏰ " + status.timeline[i];
            } else {
                emoji = "⭕";
                statusText = "Pending";
                timeText = "⏳ Awaiting";
            }
            
            components.add(createTextComponent("step_" + i + "_status", 
                emoji + " [" + statusText + "]", "h3"));
            components.add(createTextComponent("step_" + i + "_name", 
                "Step " + (i + 1) + ": " + stepNames[i], "h3"));
            components.add(createTextComponent("step_" + i + "_desc", 
                "   " + stepDescriptions[i], "body"));
            components.add(createTextComponent("step_" + i + "_time", 
                "   " + timeText, "body"));
            
            // Connector between steps
            if (i < stepNames.length - 1) {
                String connector = i < status.currentStep ? "   ┃" : "   ┆";
                components.add(createTextComponent("connector_" + i, connector, "body"));
            }
        }
        
        components.add(createTextComponent("divider2", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Track Another Order
        components.add(createTextComponent("track_another_title", 
            "🔍 Track Another Order:", "h3"));
        components.add(createTextFieldComponent("order_input", 
            "Enter Order ID", "/tracking/orderId"));
        
        Map<String, String> trackBindings = new HashMap<>();
        trackBindings.put("orderId", "/tracking/orderId");
        components.add(createButtonComponent("track_button", 
            "Track Order", "trackOrder", trackBindings));
        components.add(createTextComponent("track_button_text", "📍 Track Order"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/tracking/orderId", "");
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
    
    private String createProgressBar(double percent) {
        int filled = (int) (percent / 10);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }
}
