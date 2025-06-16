package view.teacher;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 实习情况查看模块面板
 */
public class InternshipStatusPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    private String convertStatusToChinese(String status) {
        if (status == null) return "";
        
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("approved", "已通过");
        statusMap.put("rejected", "已拒绝");
        statusMap.put("pending", "待审核");
        statusMap.put("active", "有效");
        statusMap.put("inactive", "无效");
        statusMap.put("recruiting", "招聘中");
        statusMap.put("closed", "已结束");
        statusMap.put("submitted", "已提交");
        statusMap.put("reviewed", "已审阅");
        statusMap.put("待审核", "待审核");
        statusMap.put("已提交", "已提交");
        statusMap.put("已通过", "已通过");
        statusMap.put("已拒绝", "已拒绝");

        return statusMap.getOrDefault(status.toLowerCase(), status);
    }

    public InternshipStatusPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("实习情况");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"学生姓名", "实习企业", "实习岗位", "申请状态", "日志数量"};
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
        loadStatusData();

        statusPanel.add(headerPanel, BorderLayout.NORTH);
        statusPanel.add(scrollPane, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.CENTER);
    }

    private void loadStatusData() {
        tableModel.setRowCount(0);
        System.out.println("正在加载实习情况数据，教师ID: " + currentUserId);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, c.company_name, p.title as position_title, a.status, " +
                 "(SELECT COUNT(*) FROM internship_logs l WHERE l.student_id = a.student_id) as log_count " +
                 "FROM internship_applications a " +
                 "JOIN users u ON a.student_id = u.id " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "JOIN student_info s ON a.student_id = s.user_id " +
                 "WHERE s.teacher_id = ?")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("company_name"),
                    rs.getString("position_title"),
                    convertStatusToChinese(rs.getString("status")),
                    rs.getInt("log_count")
                });
            }
            System.out.println("加载到 " + count + " 条实习申请记录");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("加载实习情况数据失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "获取实习情况失败：" + e.getMessage());
        }
    }
} 