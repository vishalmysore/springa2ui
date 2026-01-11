package io.github.vishalmysore.service.advanced;

import com.t4a.annotations.Action;
import com.t4a.annotations.Agent;
import com.t4a.annotations.Prompt;
import com.t4a.detect.ActionCallback;
import io.github.vishalmysore.util.A2UIDisplay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Agent(groupName = "loanCalculator",
       groupDescription = "Calculate loan payments and total interest")
@Slf4j
public class LoanCalculatorService implements A2UIDisplay {
    
    private ThreadLocal<ActionCallback> callback = new ThreadLocal<>();
    
    @Action(description = "Calculate monthly loan payment and total interest")
    public Object calculateLoan(double principal, @Prompt(describe = "Do not put any comments as it spoils json format, if you cannot decide put 0.5 as default value") double annualRate, int years) {
        log.info("Calculating loan: ${}, {}% APR, {} years", principal, annualRate, years);
        
        // Loan calculation
        double monthlyRate = annualRate / 100 / 12;
        int months = years * 12;
        double monthlyPayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, months)) 
                               / (Math.pow(1 + monthlyRate, months) - 1);
        double totalPayment = monthlyPayment * months;
        double totalInterest = totalPayment - principal;
        
        if(isUICallback(callback)) {
            return createLoanResultUI(principal, annualRate, years, 
                monthlyPayment, totalPayment, totalInterest);
        } else {
            return String.format("Monthly Payment: $%.2f, Total Interest: $%.2f", 
                monthlyPayment, totalInterest);
        }
    }
    
    private Map<String, Object> createLoanResultUI(double principal, double rate, 
                                                    int years, double monthly,
                                                    double total, double interest) {
        String surfaceId = "loan_calculator";
        String rootId = "root";
        
        List<String> childIds = Arrays.asList(
            "header", "calc_icon", "divider1",
            "input_section", "input_principal", "input_rate", "input_years",
            "divider2", "results_section",
            "result_monthly", "result_total", "result_interest", "result_breakdown",
            "divider3", "comparison_title", "comparison_info",
            "divider4", "recalc_section",
            "new_principal", "new_rate", "new_years",
            "calc_button", "calc_button_text"
        );
        
        List<Map<String, Object>> components = new ArrayList<>();
        components.add(createRootColumn(rootId, childIds));
        
        // Header
        components.add(createTextComponent("header", 
            "🏦 Loan Payment Calculator", "h1"));
        components.add(createTextComponent("calc_icon", 
            "💰 Calculate your monthly payments and total interest", "body"));
        components.add(createTextComponent("divider1", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Input Summary
        components.add(createTextComponent("input_section", 
            "📝 Loan Details:", "h2"));
        components.add(createTextComponent("input_principal", 
            "💵 Loan Amount: $" + String.format("%,.2f", principal), "h3"));
        components.add(createTextComponent("input_rate", 
            "📊 Annual Interest Rate: " + String.format("%.2f%%", rate), "h3"));
        components.add(createTextComponent("input_years", 
            "⏱️ Loan Term: " + years + " years (" + (years * 12) + " months)", "h3"));
        
        components.add(createTextComponent("divider2", 
            "─────────────────────────", "body"));
        
        // Results Section
        components.add(createTextComponent("results_section", 
            "📈 Calculation Results:", "h2"));
        components.add(createTextComponent("result_monthly", 
            "💳 Monthly Payment: $" + String.format("%,.2f", monthly), "h2"));
        components.add(createTextComponent("result_total", 
            "💰 Total Payment: $" + String.format("%,.2f", total), "h3"));
        components.add(createTextComponent("result_interest", 
            "📊 Total Interest: $" + String.format("%,.2f", interest), "h3"));
        
        // Interest percentage
        double interestPercent = (interest / principal) * 100;
        components.add(createTextComponent("result_breakdown", 
            "ℹ️  You will pay " + String.format("%.1f%%", interestPercent) + 
            " more than the original loan amount", "body"));
        
        components.add(createTextComponent("divider3", 
            "─────────────────────────", "body"));
        
        // Comparison Info
        components.add(createTextComponent("comparison_title", 
            "💡 Payment Breakdown:", "h3"));
        String breakdown = String.format(
            "Principal per month: $%.2f | Interest per month: $%.2f",
            principal / (years * 12), interest / (years * 12)
        );
        components.add(createTextComponent("comparison_info", breakdown, "body"));
        
        components.add(createTextComponent("divider4", 
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "body"));
        
        // Recalculate Section
        components.add(createTextComponent("recalc_section", 
            "🔄 Calculate Another Loan:", "h3"));
        components.add(createTextFieldComponent("new_principal", 
            "Loan Amount ($)", "/calculator/principal"));
        components.add(createTextFieldComponent("new_rate", 
            "Annual Interest Rate (%)", "/calculator/rate"));
        components.add(createTextFieldComponent("new_years", 
            "Loan Term (years)", "/calculator/years"));
        
        Map<String, String> calcBindings = new HashMap<>();
        calcBindings.put("principal", "/calculator/principal");
        calcBindings.put("annualRate", "/calculator/rate");
        calcBindings.put("years", "/calculator/years");
        components.add(createButtonComponent("calc_button", 
            "Calculate", "calculateLoan", calcBindings));
        components.add(createTextComponent("calc_button_text", "🧮 Calculate Payment"));
        
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("/calculator/principal", String.valueOf(principal));
        dataModel.put("/calculator/rate", String.valueOf(rate));
        dataModel.put("/calculator/years", String.valueOf(years));
        
        return buildA2UIMessageWithData(surfaceId, rootId, components, dataModel);
    }
}
