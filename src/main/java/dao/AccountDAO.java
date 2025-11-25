package dao;

import exception.AccountCreateException;
import model.Account;
import model.AccountType;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS account (" +
                "id IDENTITY PRIMARY KEY, " +
                "customer_id BIGINT NOT NULL, " +
                "type VARCHAR(50) NOT NULL, " +
                "balance DOUBLE, " +
                "FOREIGN KEY (customer_id) REFERENCES customer(id)" +
                ")";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating account table", e);
        }
    }

    public int insert(Account account) throws AccountCreateException {
        String sql = "INSERT INTO account(customer_id, type, balance) VALUES(?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getCustomerId());
            ps.setString(2, account.getType().name());
            ps.setDouble(3, account.getBalance());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new AccountCreateException("Creating account failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    account.setId(id);
                    return id;
                } else {
                    throw new AccountCreateException("Creating account failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new AccountCreateException("Database error while creating account", e);
        }
    }

    public List<Account> searchByCustomerId(int customerId) {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT id, customer_id, type, balance FROM account WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Account a = new Account();
                    a.setId(rs.getInt("id"));
                    a.setCustomerId(rs.getInt("customer_id"));
                    a.setType(AccountType.valueOf(rs.getString("type")));
                    a.setBalance(rs.getDouble("balance"));
                    result.add(a);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching accounts", e);
        }
        return result;
    }
}
