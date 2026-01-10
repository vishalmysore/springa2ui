package io.github.vishalmysore.service;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;

import com.t4a.api.JavaMethodAction;

import com.t4a.detect.ActionCallback;
import com.t4a.processor.AIProcessor;
import io.github.vishalmysore.common.CallBackType;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Agent(groupName ="whatThisPersonFavFood", groupDescription = "Provide persons name and then find out what does that person like")
@Slf4j
public class SimpleService implements A2UIDisplay {

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */
    private ActionCallback callback;

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */
    private AIProcessor processor;
    public SimpleService(){
      log.info(" Created Simple Service");
    }

    @Action(description = "Get the favourite food of a person")
    public Object whatThisPersonFavFood(String name) {
        log.info("Getting favorite food for: {}", name);
        
        String favFood;
        if("vishal".equalsIgnoreCase(name))
            favFood = "Paneer Butter Masala";
        else if ("vinod".equalsIgnoreCase(name)) {
            favFood = "aloo kofta";
        }else
            favFood = "something yummy";
        
        // Check if A2UI is requested
        if(callback != null && callback.getType().equals(CallBackType.A2UI.name())) {
            return createFavoriteFoodUI(name, favFood);
        } else {
            return favFood;
        }
    }

    /**
     * Creates an A2UI display for favorite food with form to query another person
     */
    private Map<String, Object> createFavoriteFoodUI(String name, String favFood) {
        String surfaceId = "favorite_food";
        String rootId = "root";

        // Define child component IDs
        List<String> childIds = Arrays.asList("title", "result", "divider", "form_title", "name_input", "submit_button", "submit_button_text");

        // Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Add root column
        components.add(createRootColumn(rootId, childIds));

        // Add title
        components.add(createTextComponent("title", "🍽️ Favorite Food Finder", "h2"));

        // Add result
        String resultText = name + "'s favorite food is: " + favFood + " 😋";
        components.add(createTextComponent("result", resultText, "body"));

        // Add divider text
        components.add(createTextComponent("divider", "───────────────────────", "body"));

        // Add form title
        components.add(createTextComponent("form_title", "Find another person's favorite food:", "h3"));

        // Add TextField with data model binding
        components.add(createTextFieldComponent("name_input", "Person's Name", "/form/name"));

        // Add submit button with context binding
        Map<String, String> contextBindings = new HashMap<>();
        contextBindings.put("name", "/form/name");
        components.add(createButtonComponent("submit_button", "Find Favorite Food", "whatThisPersonFavFood", contextBindings));
        
        // Add button text child
        components.add(createTextComponent("submit_button_text", "Find Favorite Food"));

        // Initialize data model with empty value
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/form/name", "");

        // Build complete A2UI message with data model
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }

}
