package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class InternshipStatsPanel extends JPanel {
    private JFrame parentFrame;
    private JPanel statsPanel;

    public InternshipStatsPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建标题面板
        JLabel titleLabel = new JLabel("实习统计");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        
        // 创建统计卡片面板
        statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(Color.WHITE);

        // 加载统计数据
        loadInternshipStats();

        // 设置布局
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(titleLabel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
    }

    private void loadInternshipStats() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 获取实习申请总数
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as total FROM internship_applications"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard("实习申请总数", String.valueOf(rs.getInt("total")));
                }
            }

            // 获取正在实习的学生数
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as total FROM internship_applications WHERE status = '已通过'"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard("正在实习学生数", String.valueOf(rs.getInt("total")));
                }
            }

            // 获取已完成实习的学生数
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as total FROM internship_applications WHERE status = '已结束'"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard("已完成实习学生数", String.valueOf(rs.getInt("total")));
                }
            }

            // 获取平均实习成绩
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT AVG(score) as avg_score FROM internship_scores"
            )) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double avgScore = rs.getDouble("avg_score");
                    addStatCard("平均实习成绩", String.format("%.2f", avgScore));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载实习统计数据失败：" + e.getMessage());
        }
    }

    private void addStatCard(String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(102, 102, 102));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        valueLabel.setForeground(new Color(51, 51, 51));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(valueLabel);

        statsPanel.add(card);
    }
} 