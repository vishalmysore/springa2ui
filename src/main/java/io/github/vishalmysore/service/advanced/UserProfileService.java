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
@Agent(groupName = "userProfile",      groupDescription = "View and manage user profile with multiple sections", prompt = "you are a user profile management assistant. Provide users with detailed information about their profiles, including overview, activity, settings, and security sections based on the user ID they provide.")
@Slf4j
public class UserProfileService implements A2UIAware, ProcessorAware {
    
    private ThreadLocal<ActionCallback> callback = new ThreadLocal<>();
    
    @Action(description = "Display user profile with selected tab")
    public Object viewProfile(String userId, String tab) {
        log.info("Viewing profile for user {} on tab {}", userId, tab);
        
        if(isUICallback(getCallback())) {
            return createProfileUI(userId, tab);
        } else {
            return "Profile for user: " + userId + " (" + tab + " section)";
        }
    }
    
    private Map<String, Object> createProfileUI(String userId, String activeTab) {
        String surfaceId = "user_profile";
        String rootId = "root";
        
        // Tab configuration
        String[] tabs = {"Overview", "Activity", "Settings", "Security"};
        
        List<String> childIds = new ArrayList<>();
        childIds.add("header");
        childIds.add("user_id_display");
        childIds.add("divider1");
        childIds.add("tab_nav_title");
        
        // Add tab navigation buttons
        for (int i = 0; i < tabs.length; i++) {
            childIds.add("tab_" + i + "_button");
            childIds.add("tab_" + i + "_text");
        }
        
        childIds.add("divider2");
        childIds.add("content_title");
        
        // Add content for active tab
        if ("Overview".equalsIgnoreCase(activeTab)) {
            childIds.addAll(Arrays.asList("overview_name", "overview_email", 
                "overview_joined", "overview_role", "overview_status"));
        } else if ("Activity".equalsIgnoreCase(activeTab)) {
            childIds.addAll(Arrays.asList("activity_title", "activity_1", 
                "activity_2", "activity_3", "activity_4"));
        } else if ("Settings".equalsIgnoreCase(activeTab)) {
            childIds.addAll(Arrays.asList("settings_notifications", "settings_privacy", 
                "settings_language", "settings_timezone"));
        } else if ("Security".equalsIgnoreCase(activeTab)) {
            childIds.addAll(Arrays.asList("security_password", "security_2fa", 
                "security_sessions", "security_devices"));
        }
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "👤 User Profile", "h1"));
        components.add(createTextComponent("user_id_display", 
            "User ID: " + userId, "h3"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Tab Navigation
        components.add(createTextComponent("tab_nav_title", 
            "📑 Profile Sections:", "h2"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/profile/userId", userId);
        
        for (int i = 0; i < tabs.length; i++) {
            String tabName = tabs[i];
            String tabKey = "tab" + i;
            
            // Visual indicator for active tab
            String indicator = tabName.equalsIgnoreCase(activeTab) ? "▶ " : "  ";
            String emphasis = tabName.equalsIgnoreCase(activeTab) ? " [Active]" : "";
            
            Map<String, String> tabBindings = new HashMap<>();
            tabBindings.put("userId", "/profile/userId");
            tabBindings.put("tab", "/profile/" + tabKey);
            
            components.add(createButtonComponent("tab_" + i + "_button", 
                tabName, "viewProfile", tabBindings));
            components.add(createTextComponent("tab_" + i + "_text", 
                indicator + tabName + emphasis));
            
            dataModel.put("/profile/" + tabKey, tabName);
        }
        
        components.add(createTextComponent("divider2", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Tab Content
        components.add(createTextComponent("content_title", 
            "📄 " + activeTab + " Information:", "h2"));
        
        if ("Overview".equalsIgnoreCase(activeTab)) {
            components.add(createTextComponent("overview_name", 
                "📛 Name: John Anderson", "h3"));
            components.add(createTextComponent("overview_email", 
                "📧 Email: john.anderson@company.com", "body"));
            components.add(createTextComponent("overview_joined", 
                "📅 Member Since: January 2024", "body"));
            components.add(createTextComponent("overview_role", 
                "👔 Role: Senior Developer", "body"));
            components.add(createTextComponent("overview_status", 
                "✅ Account Status: Active & Verified", "body"));
            
        } else if ("Activity".equalsIgnoreCase(activeTab)) {
            components.add(createTextComponent("activity_title", 
                "Recent Activity:", "h3"));
            components.add(createTextComponent("activity_1", 
                "🔵 Jan 10, 10:30 AM - Logged in from Chrome on Windows", "body"));
            components.add(createTextComponent("activity_2", 
                "🟢 Jan 9, 3:15 PM - Updated profile settings", "body"));
            components.add(createTextComponent("activity_3", 
                "🟡 Jan 8, 11:00 AM - Changed password", "body"));
            components.add(createTextComponent("activity_4", 
                "🔵 Jan 7, 9:45 AM - Logged in from Safari on macOS", "body"));
            
        } else if ("Settings".equalsIgnoreCase(activeTab)) {
            components.add(createTextComponent("settings_notifications", 
                "🔔 Email Notifications: Enabled", "body"));
            components.add(createTextComponent("settings_privacy", 
                "🔒 Profile Visibility: Friends Only", "body"));
            components.add(createTextComponent("settings_language", 
                "🌐 Language: English (US)", "body"));
            components.add(createTextComponent("settings_timezone", 
                "🕐 Timezone: UTC-5 (Eastern Time)", "body"));
            
        } else if ("Security".equalsIgnoreCase(activeTab)) {
            components.add(createTextComponent("security_password", 
                "🔑 Password: Last changed 5 days ago", "body"));
            components.add(createTextComponent("security_2fa", 
                "🛡️ Two-Factor Auth: Enabled (Authenticator App)", "body"));
            components.add(createTextComponent("security_sessions", 
                "💻 Active Sessions: 2 devices", "body"));
            components.add(createTextComponent("security_devices", 
                "📱 Trusted Devices: 3 registered", "body"));
        }
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
