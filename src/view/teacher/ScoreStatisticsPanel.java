package view.teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import config.DatabaseConfig;

/**
 * 成绩统计模块面板
 */
public class ScoreStatisticsPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public ScoreStatisticsPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("成绩统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"学生姓名", "实习企业", "实习岗位", "成绩", "评语", "评分时间"};
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
        loadStatisticsData();

        statsPanel.add(headerPanel, BorderLayout.NORTH);
        statsPanel.add(scrollPane, BorderLayout.CENTER);

        add(statsPanel, BorderLayout.CENTER);
    }

    private void loadStatisticsData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, c.company_name, p.title as position_title, " +
                 "s.score, s.comments, s.created_at " +
                 "FROM internship_scores s " +
                 "JOIN internship_applications a ON s.application_id = a.id " +
                 "JOIN users u ON a.student_id = u.id " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "JOIN student_info si ON a.student_id = si.user_id " +
                 "WHERE si.teacher_id = ? " +
                 "ORDER BY s.score DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("company_name"),
                    rs.getString("position_title"),
                    rs.getDouble("score"),
                    rs.getString("comments"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取成绩统计失败：" + e.getMessage());
        }
    }
} 