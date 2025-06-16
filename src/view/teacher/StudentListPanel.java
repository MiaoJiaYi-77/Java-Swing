package view.teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import config.DatabaseConfig;

/**
 * 学生列表管理模块面板
 */
public class StudentListPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public StudentListPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel studentPanel = new JPanel(new BorderLayout());
        studentPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("学生列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"姓名", "院系", "专业", "班级", "GPA"};
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
        loadStudentData();

        studentPanel.add(headerPanel, BorderLayout.NORTH);
        studentPanel.add(scrollPane, BorderLayout.CENTER);

        add(studentPanel, BorderLayout.CENTER);
    }

    private void loadStudentData() {
        tableModel.setRowCount(0);
        System.out.println("正在加载学生列表数据，教师ID: " + currentUserId);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, s.department, s.major, s.class_name, s.gpa " +
                 "FROM student_info s " +
                 "JOIN users u ON s.user_id = u.id " +
                 "WHERE s.teacher_id = ?")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("major"),
                    rs.getString("class_name"),
                    rs.getDouble("gpa")
                });
            }
            System.out.println("加载到 " + count + " 条学生记录");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("加载学生数据失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "获取学生列表失败：" + e.getMessage());
        }
    }
} 