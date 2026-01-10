package io.github.vishalmysore.service;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.detect.ActionCallback;
import com.t4a.processor.AIProcessor;
import io.github.vishalmysore.common.CallBackType;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.java.Log;
import io.github.vishalmysore.pojo.RestaurantPojo;
import org.springframework.stereotype.Service;

import java.util.*;

@Log
@Service
@Agent(groupName = "restaurantBooking", groupDescription = "Book restaurant reservations with menu selection")
public class RestaurantBookingService implements A2UIDisplay {

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */
    private static final ThreadLocal<ActionCallback> callbackThreadLocal = new ThreadLocal<>();

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */
    private AIProcessor processor;

    public RestaurantBookingService(){
        log.info("created RestaurantBookingService");
    }

    @Action(description = "Book a restaurant reservation - shows a form to collect reservation details")
    public Object bookRestaurantReservation(String restaurantName) {
        log.info("Booking restaurant reservation for: " + restaurantName);
        
        // Check if A2UI is requested
        if(isUICallback(callbackThreadLocal)) {
            return createReservationFormUI(restaurantName);
        } else {
            return "Please provide reservation details for " + restaurantName + 
                   ": date, time, number of people, and menu preference";
        }
    }

    @Action(description = "Confirm restaurant reservation with all details including menu type")
    public Object confirmReservation(String restaurantName, String date, String time, 
                                     int numberOfPeople, String menuType, String specialRequests) {
        log.info("Confirming reservation - Restaurant: " + restaurantName + 
                ", Date: " + date + ", Time: " + time + 
                ", People: " + numberOfPeople + ", Menu: " + menuType);
        
        // Generate confirmation number
        String confirmationNumber = "RES-" + System.currentTimeMillis() % 100000;
        
        // Check if A2UI is requested
        if(isUICallback(callbackThreadLocal)) {
            return createConfirmationUI(restaurantName, date, time, numberOfPeople, 
                                       menuType, specialRequests, confirmationNumber);
        } else {
            return "Reservation confirmed! Confirmation #" + confirmationNumber + 
                   " for " + restaurantName + " on " + date + " at " + time + 
                   " for " + numberOfPeople + " people. Menu: " + menuType;
        }
    }

    public String bookReservation(RestaurantPojo restaurantPojo){
        log.info(restaurantPojo.toString());
        return "This has been booked "+restaurantPojo.toString();
    }

    /**
     * Creates an A2UI form for restaurant reservation with menu options
     */
    private Map<String, Object> createReservationFormUI(String restaurantName) {
        String surfaceId = "restaurant_reservation";
        String rootId = "root";

        // Define child component IDs
        List<String> childIds = Arrays.asList(
            "title",
            "restaurant_name",
            "form_section_title",
            "date_input",
            "time_input",
            "people_input",
            "menu_section_title",
            "menu_appetizer",
            "menu_entree",
            "menu_dessert",
            "special_requests_input",
            "confirm_button",
            "confirm_button_text"
        );

        // Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Add root column
        components.add(createRootColumn(rootId, childIds));

        // Add title
        components.add(createTextComponent("title", "🍽️ Restaurant Reservation", "h2"));

        // Add restaurant name display
        components.add(createTextComponent("restaurant_name", "Restaurant: " + restaurantName, "h3"));

        // Add form section title
        components.add(createTextComponent("form_section_title", "Reservation Details:", "body"));

        // Add TextField components with data model bindings
        components.add(createTextFieldComponent("date_input", "Date (YYYY-MM-DD)", "/reservation/date"));
        components.add(createTextFieldComponent("time_input", "Time (HH:MM)", "/reservation/time"));
        components.add(createTextFieldComponent("people_input", "Number of People", "/reservation/numberOfPeople"));

        // Add menu section
        components.add(createTextComponent("menu_section_title", "📋 Menu Preferences:", "h3"));
        components.add(createTextFieldComponent("menu_appetizer", "Appetizer Choice", "/reservation/menu/appetizer"));
        components.add(createTextFieldComponent("menu_entree", "Entrée Choice", "/reservation/menu/entree"));
        components.add(createTextFieldComponent("menu_dessert", "Dessert Choice", "/reservation/menu/dessert"));

        // Add special requests field
        components.add(createTextFieldComponent("special_requests_input", "Special Requests", "/reservation/specialRequests"));

        // Add confirm button with context bindings
        Map<String, String> contextBindings = new HashMap<>();
        contextBindings.put("restaurantName", "/reservation/restaurantName");
        contextBindings.put("date", "/reservation/date");
        contextBindings.put("time", "/reservation/time");
        contextBindings.put("numberOfPeople", "/reservation/numberOfPeople");
        contextBindings.put("menuType", "/reservation/menu/entree"); // Use main entree as menu type
        contextBindings.put("specialRequests", "/reservation/specialRequests");
        components.add(createButtonComponent("confirm_button", "Confirm Reservation", "confirmReservation", contextBindings));
        
        // Add button text child
        components.add(createTextComponent("confirm_button_text", "Confirm Reservation"));

        // Initialize data model with empty values
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/reservation/restaurantName", "");
        dataModel.put("/reservation/date", "");
        dataModel.put("/reservation/time", "");
        dataModel.put("/reservation/numberOfPeople", "");
        dataModel.put("/reservation/menu/appetizer", "");
        dataModel.put("/reservation/menu/entree", "");
        dataModel.put("/reservation/menu/dessert", "");
        dataModel.put("/reservation/specialRequests", "");

        // Build complete A2UI message with data model
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }

    /**
     * Creates an A2UI confirmation display with option to make another reservation
     */
    private Map<String, Object> createConfirmationUI(String restaurantName, String date, String time,
                                                     int numberOfPeople, String menuType, 
                                                     String specialRequests, String confirmationNumber) {
        String surfaceId = "reservation_confirmation";
        String rootId = "root";

        // Define child component IDs
        List<String> childIds = Arrays.asList(
            "title",
            "success_message",
            "confirmation_number",
            "details_title",
            "restaurant_detail",
            "date_detail",
            "time_detail",
            "people_detail",
            "menu_detail",
            "special_requests_detail",
            "divider",
            "new_booking_title",
            "new_restaurant_input",
            "book_button",
            "book_button_text"
        );

        // Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Add root column
        components.add(createRootColumn(rootId, childIds));

        // Add title
        components.add(createTextComponent("title", "✅ Reservation Confirmed!", "h2"));

        // Add success message
        components.add(createTextComponent("success_message", "Your table has been reserved successfully!", "body"));

        // Add confirmation number
        components.add(createTextComponent("confirmation_number", "Confirmation #: " + confirmationNumber, "h3"));

        // Add details section
        components.add(createTextComponent("details_title", "Reservation Details:", "h3"));
        components.add(createTextComponent("restaurant_detail", "🏨 Restaurant: " + restaurantName, "body"));
        components.add(createTextComponent("date_detail", "📅 Date: " + date, "body"));
        components.add(createTextComponent("time_detail", "🕐 Time: " + time, "body"));
        components.add(createTextComponent("people_detail", "👥 Party Size: " + numberOfPeople + " people", "body"));
        components.add(createTextComponent("menu_detail", "🍽️ Menu: " + menuType, "body"));
        
        if (specialRequests != null && !specialRequests.isEmpty()) {
            components.add(createTextComponent("special_requests_detail", "📝 Special Requests: " + specialRequests, "body"));
        } else {
            components.add(createTextComponent("special_requests_detail", "📝 No special requests", "body"));
        }

        // Add divider
        components.add(createTextComponent("divider", "───────────────────────", "body"));

        // Add new booking section
        components.add(createTextComponent("new_booking_title", "Book another restaurant:", "h3"));
        components.add(createTextFieldComponent("new_restaurant_input", "Restaurant Name", "/form/restaurantName"));

        // Add book button with context binding
        Map<String, String> contextBindings = new HashMap<>();
        contextBindings.put("restaurantName", "/form/restaurantName");
        components.add(createButtonComponent("book_button", "Start New Reservation", "bookRestaurantReservation", contextBindings));
        
        // Add button text child
        components.add(createTextComponent("book_button_text", "Start New Reservation"));

        // Initialize data model with empty value
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/form/restaurantName", "");

        // Build complete A2UI message with data model
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
