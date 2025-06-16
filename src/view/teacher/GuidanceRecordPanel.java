package view.teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import config.DatabaseConfig;

/**
 * 指导记录模块面板
 */
public class GuidanceRecordPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public GuidanceRecordPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel recordsPanel = new JPanel(new BorderLayout());
        recordsPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("指导记录");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"学生姓名", "最近日志日期", "日志状态", "总日志数"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadGuidanceData();

        recordsPanel.add(headerPanel, BorderLayout.NORTH);
        recordsPanel.add(scrollPane, BorderLayout.CENTER);

        add(recordsPanel, BorderLayout.CENTER);
    }

    private void loadGuidanceData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, l.date, l.status, " +
                 "(SELECT COUNT(*) FROM internship_logs WHERE student_id = l.student_id) as total_logs " +
                 "FROM internship_logs l " +
                 "JOIN users u ON l.student_id = u.id " +
                 "JOIN student_info s ON l.student_id = s.user_id " +
                 "WHERE s.teacher_id = ? " +
                 "GROUP BY l.student_id, l.date, l.status " +
                 "ORDER BY l.date DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getDate("date"),
                    rs.getString("status"),
                    rs.getInt("total_logs")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取指导记录失败：" + e.getMessage());
        }
    }
} 