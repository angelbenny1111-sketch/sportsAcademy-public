
package com.Inferno.sportsacademy.ui;

import com.Inferno.sportsacademy.model.User;
import com.Inferno.sportsacademy.service.Athleteservice;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class Myperformancepanel extends JPanel {

    public Myperformancepanel(User user) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Performance");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        String[] columns = {
            "Sport", "Performance Date", "Score", "Remarks"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadPerformance(user, model);
    }

    private void loadPerformance(User user, DefaultTableModel model) {
        try {
            var athlete = new Athleteservice()
                .getAthleteByUsername(user.getUsername());

            if (athlete == null) {
                JOptionPane.showMessageDialog(this, "Athlete not found.");
                return;
            }

            String sql =
                "SELECT s.sport_name, p.performance_date, " +
                "p.score, p.remarks " +
                "FROM Performance p " +
                "JOIN Training t ON p.training_id = t.training_id " +
                "JOIN Sport s ON t.sport_id = s.sport_id " +
                "WHERE p.athlete_id = ? " +
                "ORDER BY p.performance_date DESC";

            try (Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/sports_academy",
                    "root", System.getenv("DB_PASSWORD"));
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, athlete.getAthleteId());

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                            rs.getString("sport_name"),
                            rs.getDate("performance_date"),
                            rs.getString("score"),
                            rs.getString("remarks")
                        });
                    }
                }
            }

            if (model.getRowCount() == 0) {
                add(new JLabel("no performance records yet."),
                    BorderLayout.SOUTH);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "could not load performance: " + e.getMessage(),
                "database error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

