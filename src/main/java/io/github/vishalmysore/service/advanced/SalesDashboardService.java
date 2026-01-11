package io.github.vishalmysore.service.advanced;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.detect.ActionCallback;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Agent(groupName = "salesDashboard",
       groupDescription = "View sales performance metrics and KPIs")
@Slf4j
public class SalesDashboardService implements A2UIDisplay {
    
    private ThreadLocal<ActionCallback> callback = new ThreadLocal<>();
    
    @Action(description = "Show sales dashboard with metrics and KPIs")
    public Object showSalesDashboard(String period) {
        log.info("Generating sales dashboard for period: {}", period);
        
        // Simulate fetching metrics from database
        Map<String, Object> metrics = calculateMetrics(period);
        
        if(isUICallback(callback)) {
            return createDashboardUI(period, metrics);
        } else {
            return String.format("Sales Dashboard - Period: %s, Total: $%,d, Growth: %.1f%%",
                period, metrics.get("totalSales"), metrics.get("growthRate"));
        }
    }
    
    private Map<String, Object> calculateMetrics(String period) {
        // Simulate business logic
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSales", 245780);
        metrics.put("newCustomers", 342);
        metrics.put("avgOrderValue", 718.48);
        metrics.put("growthRate", 18.5);
        metrics.put("conversionRate", 3.2);
        metrics.put("activeUsers", 1247);
        return metrics;
    }
    
    private Map<String, Object> createDashboardUI(String period, Map<String, Object> metrics) {
        String surfaceId = "sales_dashboard";
        String rootId = "root";
        
        List<String> childIds = Arrays.asList(
            "header", "period_badge", "divider1",
            "kpi_section_title", 
            "metric_sales", "metric_customers", "metric_avg_order",
            "metric_growth", "metric_conversion", "metric_active_users",
            "divider2", "chart_section", "chart_placeholder",
            "divider3", "controls_title", 
            "period_input", "export_button", "export_text",
            "refresh_button", "refresh_text"
        );
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header Section
        components.add(createTextComponent("header", 
            "📊 Sales Performance Dashboard", "h1"));
        components.add(createTextComponent("period_badge", 
            "📅 Reporting Period: " + period.toUpperCase(), "h3"));
        components.add(createTextComponent("divider1", 
            "═══════════════════════════════════════════", "body"));
        
        // KPI Cards Section
        components.add(createTextComponent("kpi_section_title", 
            "🎯 Key Performance Indicators", "h2"));
        
        components.add(createTextComponent("metric_sales", 
            "💰 Total Revenue: $" + String.format("%,d", metrics.get("totalSales")), "h3"));
        components.add(createTextComponent("metric_customers", 
            "👥 New Customers: " + metrics.get("newCustomers"), "h3"));
        components.add(createTextComponent("metric_avg_order", 
            "🛒 Avg Order Value: $" + String.format("%.2f", metrics.get("avgOrderValue")), "h3"));
        
        double growth = (double) metrics.get("growthRate");
        String growthEmoji = growth > 0 ? "📈" : "📉";
        String growthColor = growth > 15 ? " (Excellent!)" : growth > 5 ? " (Good)" : "";
        components.add(createTextComponent("metric_growth", 
            growthEmoji + " Growth Rate: " + String.format("%.1f%%", growth) + growthColor, "h3"));
        
        components.add(createTextComponent("metric_conversion", 
            "🎯 Conversion Rate: " + String.format("%.1f%%", metrics.get("conversionRate")), "h3"));
        components.add(createTextComponent("metric_active_users", 
            "🌟 Active Users: " + String.format("%,d", metrics.get("activeUsers")), "h3"));
        
        components.add(createTextComponent("divider2", 
            "═══════════════════════════════════════════", "body"));
        
        // Chart Section (placeholder)
        components.add(createTextComponent("chart_section", 
            "📈 Trend Analysis", "h2"));
        components.add(createTextComponent("chart_placeholder", 
            "▂▃▅▆▇█▇▆▅▃▂ Sales Trend: Upward trajectory detected", "body"));
        
        components.add(createTextComponent("divider3", 
            "═══════════════════════════════════════════", "body"));
        
        // Controls Section
        components.add(createTextComponent("controls_title", 
            "⚙️ Dashboard Controls", "h3"));
        
        components.add(createTextFieldComponent("period_input", 
            "Change Period (daily/weekly/monthly/quarterly)", "/dashboard/period"));
        
        Map<String, String> exportBindings = new HashMap<>();
        exportBindings.put("period", "/dashboard/period");
        components.add(createButtonComponent("export_button", 
            "Export Data", "showSalesDashboard", exportBindings));
        components.add(createTextComponent("export_text", "📥 Export Data"));
        
        Map<String, String> refreshBindings = new HashMap<>();
        refreshBindings.put("period", "/dashboard/period");
        components.add(createButtonComponent("refresh_button", 
            "Refresh Dashboard", "showSalesDashboard", refreshBindings));
        components.add(createTextComponent("refresh_text", "🔄 Refresh Dashboard"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/dashboard/period", period);
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
