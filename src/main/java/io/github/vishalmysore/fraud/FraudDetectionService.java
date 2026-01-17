package io.github.vishalmysore.fraud;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.processor.ProcessorAware;
import io.github.vishalmysore.a2ui.A2UIAware;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Agent(groupName = "FraudDetection", groupDescription = "Detects fraud and returns a knowledge graph visualization")
@Slf4j
public class FraudDetectionService implements A2UIAware, ProcessorAware {

    private final Map<String, String> userSurfaceMap = new ConcurrentHashMap<>();

    @Action(description = "Show transactions for a user in a knowledge graph")
    public Map<String, Object> showTransaction(String username) {
        log.info("Showing transactions for user: {}", username);

        String surfaceId = "fraud-detection-" + username + "-" + UUID.randomUUID().toString().substring(0, 8);
        userSurfaceMap.put(username, surfaceId);
        String rootId = "root";

        // 1. Build components list
        List<Map<String, Object>> components = new ArrayList<>();

        // Root Column
        Map<String, Object> rootColumn = new HashMap<>();
        rootColumn.put("id", rootId);
        Map<String, Object> columnComponent = new HashMap<>();
        Map<String, Object> columnDetails = new HashMap<>();
        Map<String, Object> children = new HashMap<>();
        children.put("explicitList", Collections.singletonList("fraud-graph"));
        columnDetails.put("children", children);
        columnComponent.put("Column", columnDetails);
        rootColumn.put("component", columnComponent);
        components.add(rootColumn);

        // KnowledgeGraph Component
        Map<String, Object> fraudGraph = new HashMap<>();
        fraudGraph.put("id", "fraud-graph");
        Map<String, Object> kgComponent = new HashMap<>();
        Map<String, Object> kgDetails = new HashMap<>();
        kgDetails.put("title", "Transaction Network for " + username);
        kgDetails.put("layout", "cose");
        Map<String, Object> kgData = new HashMap<>();
        kgData.put("path", "/fraudData");
        kgDetails.put("data", kgData);
        kgComponent.put("KnowledgeGraph", kgDetails);
        fraudGraph.put("component", kgComponent);
        components.add(fraudGraph);

        // 2. Build Personalized Data Model
        List<Map<String, Object>> fraudData = getPersonalizedData(username, false);

        return buildManualResponse(surfaceId, rootId, components, fraudData);
    }

    @Action(description = "Show suspected fraud alert for a user")
    public Map<String, Object> showSuspected(String username) {
        log.info("Showing suspected fraud for user: {}", username);

        String surfaceId = userSurfaceMap.get(username);
        if (surfaceId == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No active transaction surface found for user: " + username
                    + ". Please call showTransaction first.");
            return errorResponse;
        }

        String rootId = "root";

        // 1. Build components list (updating the same surface)
        List<Map<String, Object>> components = new ArrayList<>();

        // Root Column
        Map<String, Object> rootColumn = new HashMap<>();
        rootColumn.put("id", rootId);
        Map<String, Object> columnComponent = new HashMap<>();
        Map<String, Object> columnDetails = new HashMap<>();
        Map<String, Object> children = new HashMap<>();
        children.put("explicitList", Collections.singletonList("fraud-graph"));
        columnDetails.put("children", children);
        columnComponent.put("Column", columnDetails);
        rootColumn.put("component", columnComponent);
        components.add(rootColumn);

        // KnowledgeGraph Component (Updated Title)
        Map<String, Object> fraudGraph = new HashMap<>();
        fraudGraph.put("id", "fraud-graph");
        Map<String, Object> kgComponent = new HashMap<>();
        Map<String, Object> kgDetails = new HashMap<>();
        kgDetails.put("title", "FRAUD ALERT - Suspicious Network for " + username);
        kgDetails.put("layout", "cose");
        Map<String, Object> kgData = new HashMap<>();
        kgData.put("path", "/fraudData");
        kgDetails.put("data", kgData);
        kgComponent.put("KnowledgeGraph", kgDetails);
        fraudGraph.put("component", kgComponent);
        components.add(fraudGraph);

        // 2. Build Personalized Data Model (Expanded with suspicious nodes)
        List<Map<String, Object>> fraudData = getPersonalizedData(username, true);

        return buildManualResponse(surfaceId, rootId, components, fraudData);
    }

    private List<Map<String, Object>> getPersonalizedData(String username, boolean isSuspected) {
        List<Map<String, Object>> fraudData = new ArrayList<>();

        if ("bob".equalsIgnoreCase(username)) {
            // Bob's specific network
            fraudData.add(createNode("0", "bob_acc", "Bob's Account" + (isSuspected ? "\n(HIGH RISK)" : "")));
            fraudData.add(createNode("1", "vpn_node", "VPN Service\n(Encrypted)"));
            fraudData.add(createNode("2", "crypto_ex", "Crypto Exchange\n(High Risk)"));

            fraudData.add(createEdge("3", "bob_acc", "vpn_node", "connects"));
            fraudData.add(createEdge("4", "vpn_node", "crypto_ex", "transfers $5,000"));

            if (isSuspected) {
                fraudData.add(createNode("5", "mule_acc", "Mule Account\n(Flagged)"));
                fraudData.add(createEdge("6", "crypto_ex", "mule_acc", "withdraws"));
            }
        } else if ("vishal".equalsIgnoreCase(username)) {
            // Vishal's network (existing example)
            fraudData.add(createNode("0", "acc1", "Vishal's Account" + (isSuspected ? "\n(HIGH RISK)" : "")));
            fraudData.add(createNode("5", "device1", "Device XYZ\n(Shared)"));
            fraudData.add(createNode("6", "merchant", "Electronics Store\n(High Volume)"));

            fraudData.add(createEdge("7", "acc1", "device1", "uses"));
            fraudData.add(createEdge("12", "acc1", "merchant", "$2,500"));

            if (isSuspected) {
                fraudData.add(createNode("1", "acc2", "Account B\n(HIGH RISK)"));
                fraudData.add(createNode("2", "acc3", "Account C\n(Suspicious)"));
                fraudData.add(createEdge("8", "acc2", "device1", "uses"));
                fraudData.add(createEdge("9", "acc3", "device1", "uses"));
                fraudData.add(createEdge("13", "acc2", "merchant", "$2,300"));
            }
        } else {
            // Generic network for others
            fraudData.add(createNode("0", "user_acc", username + "'s Account" + (isSuspected ? "\n(Suspicious)" : "")));
            fraudData.add(createNode("1", "atm_loc", "ATM - Downtown"));
            fraudData.add(createEdge("2", "user_acc", "atm_loc", "withdrawal $200"));

            if (isSuspected) {
                fraudData.add(createNode("3", "unknown_loc", "Unknown Location\n(Foreign IP)"));
                fraudData.add(createEdge("4", "user_acc", "unknown_loc", "login attempt"));
            }
        }

        return fraudData;
    }

    private Map<String, Object> buildManualResponse(String surfaceId, String rootId,
            List<Map<String, Object>> components, List<Map<String, Object>> fraudData) {
        Map<String, Object> response = new HashMap<>();

        // surfaceUpdate
        Map<String, Object> surfaceUpdate = new HashMap<>();
        surfaceUpdate.put("surfaceId", surfaceId);
        surfaceUpdate.put("components", components);
        response.put("surfaceUpdate", surfaceUpdate);

        // dataModelUpdate
        Map<String, Object> dataModelUpdate = new HashMap<>();
        dataModelUpdate.put("surfaceId", surfaceId);
        List<Map<String, Object>> contents = new ArrayList<>();
        Map<String, Object> contentItem = new HashMap<>();
        contentItem.put("key", "fraudData");
        contentItem.put("valueArray", fraudData);
        contents.add(contentItem);
        dataModelUpdate.put("contents", contents);
        response.put("dataModelUpdate", dataModelUpdate);

        // beginRendering
        Map<String, Object> beginRendering = new HashMap<>();
        beginRendering.put("root", rootId);
        beginRendering.put("surfaceId", surfaceId);
        beginRendering.put("viewportWidthPx", 800);
        beginRendering.put("viewportHeightPx", 600);
        response.put("beginRendering", beginRendering);

        return response;
    }

    private Map<String, Object> createNode(String key, String id, String label) {
        Map<String, Object> node = new HashMap<>();
        node.put("key", key);
        List<Map<String, String>> valueMap = new ArrayList<>();
        valueMap.add(createKeyValue("id", id));
        valueMap.add(createKeyValue("label", label));
        node.put("valueMap", valueMap);
        return node;
    }

    private Map<String, Object> createEdge(String key, String source, String target, String label) {
        Map<String, Object> edge = new HashMap<>();
        edge.put("key", key);
        List<Map<String, String>> valueMap = new ArrayList<>();
        valueMap.add(createKeyValue("source", source));
        valueMap.add(createKeyValue("target", target));
        valueMap.add(createKeyValue("label", label));
        edge.put("valueMap", valueMap);
        return edge;
    }

    private Map<String, String> createKeyValue(String key, String value) {
        Map<String, String> kv = new HashMap<>();
        kv.put("key", key);
        kv.put("valueString", value);
        return kv;
    }
}
