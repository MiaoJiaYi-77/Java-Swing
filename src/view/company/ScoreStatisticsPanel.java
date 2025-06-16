package view.company;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 成绩统计模块面板
 */
public class ScoreStatisticsPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    private int companyId;
    
    public ScoreStatisticsPanel(JFrame parentFrame, int companyId) {
        this.parentFrame = parentFrame;
        this.companyId = companyId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("成绩统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建成绩分布表格
        String[] columnNames = {"学生姓名", "岗位名称", "评分", "评语"};
        model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载详细数据
        loadScoreStatistics();

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadScoreStatistics() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, ip.title, is2.score, is2.comments " +
                 "FROM internship_scores is2 " +
                 "JOIN internship_applications ia ON is2.application_id = ia.id " +
                 "JOIN internship_positions ip ON ia.position_id = ip.id " +
                 "JOIN users u ON ia.student_id = u.id " +
                 "WHERE ip.company_id = ? " +
                 "ORDER BY is2.score DESC"
             )) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getDouble("score"),
                    rs.getString("comments")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载成绩数据失败：" + e.getMessage());
        }
    }
} 