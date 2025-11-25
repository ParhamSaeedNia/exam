package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.AccountDAO;
import dao.CustomerDAO;
import exception.AccountCreateException;
import exception.CustomerCreateException;
import model.Account;
import model.Customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankService {
    private final CustomerDAO customerDAO;
    private final AccountDAO accountDAO;
    private final Gson gson;

    public BankService() {
        this.customerDAO = new CustomerDAO();
        this.accountDAO = new AccountDAO();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void initDatabase() {
        customerDAO.createTable();
        accountDAO.createTable();
        Map<String, String> info = new HashMap<>();
        info.put("status", "tables_created");
        System.out.println(gson.toJson(info));
    }

    public void clearDatabase() {
        try (java.sql.Connection conn = util.DBConnection.getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.execute("DELETE FROM account");
            st.execute("DELETE FROM customer");
            Map<String, String> info = new HashMap<>();
            info.put("status", "database_cleared");
            System.out.println(gson.toJson(info));
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            System.out.println(gson.toJson(err));
        }
    }

    public Thread createCustomerAndAccount(Customer customer, Account account) {
        Thread t = new Thread(() -> {
            Map<String, Object> out = new HashMap<>();
            try {
                int custId = customerDAO.insert(customer);
                account.setCustomerId(custId);
                int accId = accountDAO.insert(account);
                out.put("status", "success");
                out.put("customer", customer);
                out.put("account", account);
                System.out.println(gson.toJson(out));
            } catch (CustomerCreateException e) {
                Map<String, String> err = new HashMap<>();
                err.put("status", "error");
                err.put("type", "CustomerCreateException");
                err.put("message", e.getMessage());
                System.out.println(gson.toJson(err));
            } catch (AccountCreateException e) {
                Map<String, String> err = new HashMap<>();
                err.put("status", "error");
                err.put("type", "AccountCreateException");
                err.put("message", e.getMessage());
                System.out.println(gson.toJson(err));
            } catch (Exception e) {
                Map<String, String> err = new HashMap<>();
                err.put("status", "error");
                err.put("message", e.getMessage());
                System.out.println(gson.toJson(err));
            }
        });
        t.start();
        return t;
    }

    public String searchCustomers(String namePattern) {
        try {
            List<Customer> customers = customerDAO.search(namePattern);
            return gson.toJson(customers);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return gson.toJson(err);
        }
    }

    public String searchAccounts(int customerId) {
        try {
            List<Account> accounts = accountDAO.searchByCustomerId(customerId);
            return gson.toJson(accounts);
        } catch (Exception e) {
            Map<String, String> err = new HashMap<>();
            err.put("status", "error");
            err.put("message", e.getMessage());
            return gson.toJson(err);
        }
    }
}
