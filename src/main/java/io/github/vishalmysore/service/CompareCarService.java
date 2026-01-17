package io.github.vishalmysore.service;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.detect.ActionCallback;
import com.t4a.processor.ProcessorAware;
import io.github.vishalmysore.a2ui.A2UIAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Agent(groupName = "compareCar", groupDescription = "compare 2 cars", prompt = "you are a car comparison assistant. Provide users with detailed comparisons between two car models based on their features, performance, and specifications.")
@Slf4j
public class CompareCarService implements A2UIAware, ProcessorAware {

    /**
     * Each action has access to AIProcessor and ActionCallback which are autowired by tools4ai
     */


    public CompareCarService() {
        log.info("created car service");
    }

    @Action(description = "compare 2 cars")
    public Object compareCar(String car1, String car2) {
        log.info("Comparing cars: {} vs {}", car1, car2);
        
        // Simple comparison logic
        String betterCar;
        if (car1.toLowerCase().contains("toyota")) {
            betterCar = car1;
        } else if (car2.toLowerCase().contains("toyota")) {
            betterCar = car2;
        } else {
            betterCar = car1;
        }
        if(isUICallback(getCallback()))
        {
            String result = betterCar + " is better than " + (betterCar.equals(car1) ? car2 : car1);
            return createComparisonUI(car1, car2, betterCar, result);
        } else {
            return betterCar + " is better than " + (betterCar.equals(car1) ? car2 : car1);
        }
    }



    private Map<String, Object> createComparisonUI(String car1, String car2, String winner, String result) {
        String surfaceId = "car_comparison";
        String rootId = "root";

        // Define child component IDs
        List<String> childIds = Arrays.asList("title", "car1_display", "car2_display", "result");

        // Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Add root column
        components.add(createRootColumn(rootId, childIds));

        // Add title
        components.add(createTextComponent("title", "Car Comparison", "h2"));

        // Add car displays with winner trophy
        String car1Text = "Car 1: " + car1 + (car1.equals(winner) ? " 🏆" : "");
        components.add(createTextComponent("car1_display", car1Text));

        String car2Text = "Car 2: " + car2 + (car2.equals(winner) ? " 🏆" : "");
        components.add(createTextComponent("car2_display", car2Text));

        // Add result
        components.add(createTextComponent("result", result, "body"));

        // Build complete A2UI message
        return buildA2UIMessage(surfaceId, rootId, components);
    }
}
