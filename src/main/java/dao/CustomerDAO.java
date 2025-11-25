package dao;

import exception.CustomerCreateException;
import model.Customer;
import model.CustomerType;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS customer (" +
                "id IDENTITY PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "type VARCHAR(50) NOT NULL" +
                ")";
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating customer table", e);
        }
    }

    public int insert(Customer customer) throws CustomerCreateException {
        String sql = "INSERT INTO customer(name, type) VALUES(?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getType().name());
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new CustomerCreateException("Creating customer failed, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    customer.setId(id);
                    return id;
                } else {
                    throw new CustomerCreateException("Creating customer failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new CustomerCreateException("Database error while creating customer", e);
        }
    }

    public List<Customer> search(String namePattern) {
        List<Customer> result = new ArrayList<>();
        String sql = "SELECT id, name, type FROM customer WHERE name LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + namePattern + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer c = new Customer();
                    c.setId(rs.getInt("id"));
                    c.setName(rs.getString("name"));
                    String t = rs.getString("type");
                    c.setType(CustomerType.valueOf(t));
                    result.add(c);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error searching customers", e);
        }
        return result;
    }
}
