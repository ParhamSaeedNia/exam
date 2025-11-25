import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import model.Customer;
import model.CustomerType;
import model.Account;
import model.AccountType;
import service.BankService;
import util.AppConfig;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Gson gson = new Gson();
    private static final BankService service = new BankService();
    private static boolean running = true;

    public static void main(String[] args) throws Exception {
        service.initDatabase();

        System.out.println("\n========================================");
        System.out.println("  " + AppConfig.getAppName());
        System.out.println("  v" + AppConfig.getAppVersion());
        System.out.println("========================================\n");
        showMainMenu();

        while (running) {
            showOperationMenu();
        }

        scanner.close();
        System.out.println("\nThank you for using " + AppConfig.getAppName() + "!");
    }

    private static void showMainMenu() {
        System.out.println("Would you like to clear the database before starting? (y/n)");
        System.out.print("> ");
        String choice = scanner.nextLine().trim().toLowerCase();
        if (choice.equals("y") || choice.equals("yes")) {
            service.clearDatabase();
        }
    }

    private static void showOperationMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Create new customer and account");
        System.out.println("2. Search customers");
        System.out.println("3. Search accounts by customer ID");
        System.out.println("4. Exit");
        System.out.print("> ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                createNewCustomerAndAccount();
                break;
            case "2":
                searchCustomersMenu();
                break;
            case "3":
                searchAccountsMenu();
                break;
            case "4":
                running = false;
                break;
            default:
                System.out.println(AppConfig.getMessage("invalid_choice"));
        }
    }

    private static void createNewCustomerAndAccount() {
        System.out.println("\n--- Create New Customer and Account ---");
        Customer customer = getCustomerFromConsole();
        Account account = getAccountFromConsole();
        
        try {
            Thread t = service.createCustomerAndAccount(customer, account);
            t.join();
            
            System.out.println("\n--- Search Results ---");
            String searchName = customer.getName().split(" ")[0];
            String customersJson = service.searchCustomers(searchName);
            System.out.println("Customers found:");
            System.out.println(customersJson);
            
            Type listType = new TypeToken<List<Customer>>() {}.getType();
            List<Customer> customers = gson.fromJson(customersJson, listType);
            if (!customers.isEmpty()) {
                int customerId = customers.get(0).getId();
                String accountsJson = service.searchAccounts(customerId);
                System.out.println("\nAccounts for customer " + customerId + ":");
                System.out.println(accountsJson);
            }
        } catch (InterruptedException e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", AppConfig.getMessage("operation_interrupted"));
            System.out.println(gson.toJson(err));
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            System.out.println(gson.toJson(err));
        }
    }

    private static void searchCustomersMenu() {
        System.out.println("\n--- Search Customers ---");
        System.out.print("Enter customer name (or part of name): ");
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println(AppConfig.getMessage("empty_name"));
            return;
        }

        String customersJson = service.searchCustomers(name);
        System.out.println("\nSearch Results:");
        System.out.println(customersJson);
    }

    private static void searchAccountsMenu() {
        System.out.println("\n--- Search Accounts by Customer ID ---");
        System.out.print("Enter customer ID: ");
        try {
            int customerId = Integer.parseInt(scanner.nextLine().trim());
            String accountsJson = service.searchAccounts(customerId);
            System.out.println("\nAccounts for customer " + customerId + ":");
            System.out.println(accountsJson);
        } catch (NumberFormatException e) {
            System.out.println(AppConfig.getMessage("invalid_customer_id"));
        }
    }

    private static Customer getCustomerFromConsole() {
        System.out.println("\n--- Enter Customer Information ---");

        System.out.print("Enter customer name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Unknown Customer";
        }

        System.out.println("Customer types: REGULAR, VIP, BUSINESS");
        System.out.print("Enter customer type (default: REGULAR): ");
        String typeInput = scanner.nextLine().trim().toUpperCase();
        CustomerType type = CustomerType.REGULAR;
        try {
            if (!typeInput.isEmpty()) {
                type = CustomerType.valueOf(typeInput);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(AppConfig.getMessage("invalid_type"));
        }

        return new Customer(0, name, type);
    }

    private static Account getAccountFromConsole() {
        System.out.println("\n--- Enter Account Information ---");

        System.out.println("Account types: SAVINGS, CHECKING, LOAN");
        System.out.print("Enter account type (default: SAVINGS): ");
        String typeInput = scanner.nextLine().trim().toUpperCase();
        AccountType type = AccountType.SAVINGS;
        try {
            if (!typeInput.isEmpty()) {
                type = AccountType.valueOf(typeInput);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(AppConfig.getMessage("invalid_type"));
        }

        System.out.print("Enter initial balance (default: 0.0): ");
        double balance = 0.0;
        try {
            String balanceInput = scanner.nextLine().trim();
            if (!balanceInput.isEmpty()) {
                balance = Double.parseDouble(balanceInput);
            }
        } catch (NumberFormatException e) {
            System.out.println(AppConfig.getMessage("invalid_balance"));
        }

        return new Account(0, 0, type, balance);
    }
}
