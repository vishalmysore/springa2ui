package io.github.vishalmysore.service;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;

import com.t4a.api.JavaMethodAction;

import com.t4a.detect.ActionCallback;
import com.t4a.detect.ActionState;
import com.t4a.processor.AIProcessor;
import io.github.vishalmysore.common.CallBackType;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log
@Agent(groupName ="raiseTicket", groupDescription = "Create a ticket for customer")
public class RaiseCustomerTicket implements A2UIDisplay {
    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     * Use ThreadLocal to store the ActionCallback for the current thread if you are concerned
     * about concurrency issues.
     */
    private static final ThreadLocal<ActionCallback> callbackThreadLocal = new ThreadLocal<>();

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */
    private AIProcessor processor;
    
    /**
     * ActionCallback for A2UI support
     */
    private ActionCallback callback;
    
    @Action(description = "Raise a ticket for customer")
    public Object raiseTicket(String customerName, String reason) {
        log.info("Raising ticket for customer: " + customerName + ", reason: " + reason);
        
        // Handle ThreadLocal callback for status updates
        if(callbackThreadLocal.get() != null) {
            ActionCallback localCallback = callbackThreadLocal.get();
            log.info("callback is set "+localCallback);
            if(localCallback!=null) {
                //the spelling mistake in the message is intentional to test the callback and the ai
                localCallback.sendtStatus("raiseed ticket for will be resoved soon , t"+customerName+  " reason "+reason, ActionState.COMPLETED);
            }
        } else {
            log.warning("ThreadLocal callback is not set");
        }
        
        // Generate ticket number
        String ticketId = "TKT-" + System.currentTimeMillis() % 100000;
        String result = "Ticket " + ticketId + " raised for " + customerName;
        
        // Check if A2UI is requested
        if(isUICallback(callbackThreadLocal)) {
            return createTicketUI(customerName, reason, ticketId);
        } else {
            return result;
        }
    }
    
    /**
     * Creates an A2UI display for ticket creation with form to raise another ticket
     */
    private Map<String, Object> createTicketUI(String customerName, String reason, String ticketId) {
        String surfaceId = "customer_ticket";
        String rootId = "root";

        // Define child component IDs
        List<String> childIds = Arrays.asList(
            "title", 
            "success_message", 
            "ticket_id", 
            "customer_name", 
            "reason_text", 
            "divider", 
            "form_title", 
            "new_customer_input", 
            "new_reason_input", 
            "submit_button",
            "submit_button_text"
        );

        // Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Add root column
        components.add(createRootColumn(rootId, childIds));

        // Add title
        components.add(createTextComponent("title", "🎫 Customer Support Ticket", "h2"));

        // Add success message
        components.add(createTextComponent("success_message", "✅ Ticket created successfully!", "body"));

        // Add ticket details
        components.add(createTextComponent("ticket_id", "Ticket ID: " + ticketId, "h3"));
        components.add(createTextComponent("customer_name", "Customer: " + customerName, "body"));
        components.add(createTextComponent("reason_text", "Reason: " + reason, "body"));

        // Add divider
        components.add(createTextComponent("divider", "───────────────────────", "body"));

        // Add form title
        components.add(createTextComponent("form_title", "Raise a new ticket:", "h3"));

        // Add TextField components with data model bindings
        components.add(createTextFieldComponent("new_customer_input", "Customer Name", "/form/customerName"));
        components.add(createTextFieldComponent("new_reason_input", "Reason", "/form/reason"));

        // Add submit button with context bindings
        Map<String, String> contextBindings = new HashMap<>();
        contextBindings.put("customerName", "/form/customerName");
        contextBindings.put("reason", "/form/reason");
        components.add(createButtonComponent("submit_button", "Submit Ticket", "raiseTicket", contextBindings));
        
        // Add button text child
        components.add(createTextComponent("submit_button_text", "Submit Ticket"));

        // Initialize data model with empty values
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/form/customerName", "");
        dataModel.put("/form/reason", "");

        // Build complete A2UI message with data model
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
