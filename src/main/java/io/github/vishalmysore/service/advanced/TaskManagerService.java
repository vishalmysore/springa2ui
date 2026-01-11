package io.github.vishalmysore.service.advanced;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.detect.ActionCallback;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Agent(groupName = "taskManager",
       groupDescription = "Manage tasks with priority and completion tracking")
@Slf4j
public class TaskManagerService implements A2UIDisplay {
    
    private ThreadLocal<ActionCallback> callback = new ThreadLocal<>();
    
    private static class Task {
        String id, title, priority, dueDate;
        boolean completed;
        
        Task(String id, String title, String priority, String dueDate, boolean completed) {
            this.id = id; this.title = title; this.priority = priority;
            this.dueDate = dueDate; this.completed = completed;
        }
    }
    
    @Action(description = "Show task list with completion status")
    public Object showTasks(String filter) {
        log.info("Displaying tasks with filter: {}", filter);
        
        if(isUICallback(callback)) {
            return createTaskListUI(filter);
        } else {
            return "Task list for filter: " + filter;
        }
    }
    
    @Action(description = "Mark selected tasks as complete")
    public Object completeTasks(String taskIds) {
        log.info("Completing tasks: {}", taskIds);
        
        String[] ids = taskIds.split(",");
        String message = "Marked " + ids.length + " task(s) as complete!";
        
        if(isUICallback(callback)) {
            return createCompletionConfirmationUI(ids, message);
        } else {
            return message;
        }
    }
    
    private List<Task> getTasks(String filter) {
        List<Task> allTasks = Arrays.asList(
            new Task("T001", "Review Q4 financial reports", "High", "Jan 12", false),
            new Task("T002", "Update product documentation", "Medium", "Jan 15", false),
            new Task("T003", "Schedule team meeting", "Low", "Jan 11", true),
            new Task("T004", "Fix critical bug in payment module", "High", "Jan 10", false),
            new Task("T005", "Design new landing page", "Medium", "Jan 18", false),
            new Task("T006", "Send client proposals", "High", "Jan 13", false)
        );
        
        if ("all".equalsIgnoreCase(filter)) {
            return allTasks;
        } else if ("pending".equalsIgnoreCase(filter)) {
            List<Task> pending = new ArrayList<>();
            for (Task t : allTasks) if (!t.completed) pending.add(t);
            return pending;
        } else if ("completed".equalsIgnoreCase(filter)) {
            List<Task> completed = new ArrayList<>();
            for (Task t : allTasks) if (t.completed) completed.add(t);
            return completed;
        }
        return allTasks;
    }
    
    private Map<String, Object> createTaskListUI(String filter) {
        String surfaceId = "task_manager";
        String rootId = "root";
        
        List<Task> tasks = getTasks(filter);
        
        List<String> childIds = new ArrayList<>();
        childIds.add("header");
        childIds.add("filter_badge");
        childIds.add("stats");
        childIds.add("divider1");
        childIds.add("instructions");
        
        // Add task components
        for (int i = 0; i < tasks.size(); i++) {
            String idx = String.valueOf(i + 1);
            childIds.add("task" + idx + "_checkbox");
            childIds.add("task" + idx + "_title");
            childIds.add("task" + idx + "_details");
            childIds.add("task" + idx + "_divider");
        }
        
        childIds.addAll(Arrays.asList("divider2", "actions_title",
            "task_ids_input", "complete_button", "complete_button_text",
            "filter_section", "filter_input", "filter_button", "filter_button_text"));
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "✅ Task Manager", "h1"));
        components.add(createTextComponent("filter_badge", 
            "🔍 Filter: " + filter.toUpperCase(), "h2"));
        
        // Statistics
        int total = tasks.size();
        int completed = 0;
        for (Task t : tasks) if (t.completed) completed++;
        int pending = total - completed;
        
        components.add(createTextComponent("stats", 
            String.format("📊 Total: %d | ✅ Completed: %d | ⏳ Pending: %d", 
                total, completed, pending), "h3"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        components.add(createTextComponent("instructions", 
            "💡 Enter task IDs (comma-separated) to mark as complete", "body"));
        
        // Task List
        Map<String, Object> dataModel = new HashMap<>();
        
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            String idx = String.valueOf(i + 1);
            
            String checkbox = task.completed ? "[✅]" : "[  ]";
            String priorityEmoji = task.priority.equals("High") ? "🔴" : 
                                  task.priority.equals("Medium") ? "🟡" : "🟢";
            
            components.add(createTextComponent("task" + idx + "_checkbox", 
                checkbox + " Task #" + task.id, "h3"));
            
            String titleText = task.completed ? 
                "~~" + task.title + "~~" : task.title;
            components.add(createTextComponent("task" + idx + "_title", 
                titleText, "body"));
            
            components.add(createTextComponent("task" + idx + "_details", 
                "   " + priorityEmoji + " Priority: " + task.priority + 
                " | 📅 Due: " + task.dueDate, "body"));
            
            components.add(createTextComponent("task" + idx + "_divider", 
                "─────────────────────────", "body"));
        }
        
        components.add(createTextComponent("divider2", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Actions Section
        components.add(createTextComponent("actions_title", 
            "⚡ Quick Actions:", "h3"));
        components.add(createTextFieldComponent("task_ids_input", 
            "Task IDs to complete (e.g., T001,T004)", "/tasks/selected"));
        
        Map<String, String> completeBindings = new HashMap<>();
        completeBindings.put("taskIds", "/tasks/selected");
        components.add(createButtonComponent("complete_button", 
            "Mark as Complete", "completeTasks", completeBindings));
        components.add(createTextComponent("complete_button_text", "✅ Mark Complete"));
        
        // Filter Section
        components.add(createTextComponent("filter_section", 
            "🔎 Change Filter:", "h3"));
        components.add(createTextFieldComponent("filter_input", 
            "Filter (all/pending/completed)", "/tasks/filter"));
        
        Map<String, String> filterBindings = new HashMap<>();
        filterBindings.put("filter", "/tasks/filter");
        components.add(createButtonComponent("filter_button", 
            "Apply Filter", "showTasks", filterBindings));
        components.add(createTextComponent("filter_button_text", "🎯 Apply Filter"));
        
        dataModel.put("/tasks/selected", "");
        dataModel.put("/tasks/filter", filter);
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
    
    private Map<String, Object> createCompletionConfirmationUI(String[] taskIds, String message) {
        String surfaceId = "task_completion";
        String rootId = "root";
        
        List<String> childIds = Arrays.asList(
            "header", "success_message", "completed_count", "divider",
            "tasks_title", "task_list", "divider2",
            "back_button", "back_button_text"
        );
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        components.add(createTextComponent("header", 
            "✅ Tasks Completed!", "h1"));
        components.add(createTextComponent("success_message", 
            message, "h2"));
        components.add(createTextComponent("completed_count", 
            "🎉 Great job! You completed " + taskIds.length + " task(s)", "h3"));
        components.add(createTextComponent("divider", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        components.add(createTextComponent("tasks_title", 
            "Completed Tasks:", "h3"));
        
        StringBuilder taskList = new StringBuilder();
        for (String id : taskIds) {
            taskList.append("✅ ").append(id.trim()).append("\n");
        }
        components.add(createTextComponent("task_list", taskList.toString(), "body"));
        
        components.add(createTextComponent("divider2", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        Map<String, String> backBindings = new HashMap<>();
        backBindings.put("filter", "/completion/filter");
        components.add(createButtonComponent("back_button", 
            "Back to Tasks", "showTasks", backBindings));
        components.add(createTextComponent("back_button_text", "◀️ Back to Task List"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/completion/filter", "all");
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
