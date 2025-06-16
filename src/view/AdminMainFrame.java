package view;

import view.admin.*;
import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminMainFrame extends BaseMainFrame {
    private JPanel mainContentPanel;
    
    public AdminMainFrame(String username) {
        super("学生实习管理系统 - 管理员界面", username);
    }

    @Override
    protected void createMenuBar() {
        // 不使用菜单栏
    }

    @Override
    protected void createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 255)); // 浅蓝色背景
        
        // 创建顶部功能面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(new Color(25, 118, 210)); // 蓝色背景
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 添加系统标题到顶部面板
        JLabel systemTitle = new JLabel("管理员控制台");
        systemTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        systemTitle.setForeground(Color.WHITE);
        
        topPanel.add(systemTitle);
        
        // 创建功能按钮
        String[] functionNames = {"用户管理", "企业审核", "企业列表", "岗位管理", "实习统计", "成绩管理", "日志管理"};
        ActionListener[] listeners = {
            e -> showUserList(),
            e -> showUserApproval(),
            e -> showCompanyList(),
            e -> showPositionManagement(),
            e -> showInternshipStats(),
            e -> showGradeManagement(),
            e -> showLogManagement()
        };
        
        // 添加功能按钮到顶部面板
        for (int i = 0; i < functionNames.length; i++) {
            JButton button = createFunctionButton(functionNames[i], listeners[i]);
            topPanel.add(button);
        }
        
        // 创建主内容面板
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 显示欢迎面板
        showWelcomePanel();

        // 添加面板到主界面
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }
    
    private JButton createFunctionButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 118, 210)); // 蓝色背景
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(13, 71, 161)); // 深蓝色
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(25, 118, 210)); // 恢复原来的蓝色
            }
        });
        
        return button;
    }

    private void showWelcomePanel() {
        mainContentPanel.removeAll();
        
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("欢迎使用系统管理后台");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("请从左侧选择功能开始使用");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 添加统计卡片
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        statsPanel.setMaximumSize(new Dimension(800, 300));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 从数据库加载实际统计数据
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 获取注册用户数
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM users")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard(statsPanel, "注册用户数", String.valueOf(rs.getInt("count")));
                }
            }

            // 获取企业数量
            try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) as count FROM users WHERE user_type = '企业'")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard(statsPanel, "企业数量", String.valueOf(rs.getInt("count")));
                }
            }

            // 获取实习岗位数
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM internship_positions")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard(statsPanel, "实习岗位数", String.valueOf(rs.getInt("count")));
                }
            }

            // 获取实习申请数
            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as count FROM internship_applications")) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    addStatCard(statsPanel, "实习申请数", String.valueOf(rs.getInt("count")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // 如果查询失败，显示默认值
        addStatCard(statsPanel, "注册用户数", "0");
        addStatCard(statsPanel, "企业数量", "0");
        addStatCard(statsPanel, "实习岗位数", "0");
        addStatCard(statsPanel, "实习申请数", "0");
        }

        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(subtitleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        welcomePanel.add(statsPanel);

        mainContentPanel.add(welcomePanel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void addStatCard(JPanel panel, String title, String value) {
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

        panel.add(card);
    }

    // 用户管理功能
    private void showUserList() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new UserManagementPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 企业审核功能
    private void showUserApproval() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new CompanyApprovalPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 企业管理功能
    private void showCompanyList() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new CompanyManagementPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 岗位管理功能
    private void showPositionManagement() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new PositionManagementPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 实习统计功能
    private void showInternshipStats() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new InternshipStatsPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 成绩管理功能
    private void showGradeManagement() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new GradeManagementPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    // 日志管理功能
    private void showLogManagement() {
        mainContentPanel.removeAll();
        mainContentPanel.add(new LogManagementPanel(this));
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }
}