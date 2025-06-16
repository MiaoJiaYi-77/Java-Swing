package view;

import view.company.*;
import config.DatabaseConfig;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CompanyMainFrame extends BaseMainFrame {
    private int companyId;
    private String currentUsername;
    private int currentUserId;
    private String companyName;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    public CompanyMainFrame(String username) {
        super("学生实习管理系统 - 企业界面", username);
        this.currentUsername = username;
        initializeUserAndCompany();
        createContentPanel();
    }

    private void initializeUserAndCompany() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, u.name FROM company_info c " +
                 "JOIN users u ON c.user_id = u.id " +
                 "WHERE u.username = ?")) {
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("user_id");
                companyId = rs.getInt("id");
                companyName = rs.getString("company_name");
                System.out.println("当前登录企业ID: " + companyId);
            } else {
                System.out.println("未找到企业信息: " + currentUsername);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取企业信息失败：" + e.getMessage());
        }
    }

    @Override
    protected void createMenuBar() {
        // 不使用菜单栏
    }

    @Override
    protected void createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 255));
        
        // 顶部横向功能按钮区
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(new Color(25, 118, 210));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 系统标题
        JLabel systemTitle = new JLabel("企业实习管理系统 - " + companyName + " (ID: " + companyId + ")");
        systemTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        systemTitle.setForeground(Color.WHITE);
        topPanel.add(systemTitle);

        // 创建内容面板（使用CardLayout）
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);
        
        // 添加各个功能模块
        mainContentPanel.add(createWelcomePanel(), "welcome");
        mainContentPanel.add(new PositionManagementPanel(this, companyId, currentUserId), "positions");
        mainContentPanel.add(new ApplicationManagementPanel(this, companyId), "applications");
        mainContentPanel.add(new ScoreManagementPanel(this, companyId, currentUserId), "scores");
        mainContentPanel.add(new ScoreStatisticsPanel(this, companyId), "statistics");
        mainContentPanel.add(new DiscussionPanel(this, companyId, currentUserId), "discussion");
        
        // 添加功能按钮
        addNavButton(topPanel, "岗位管理", "positions");
        addNavButton(topPanel, "申请管理", "applications");
        addNavButton(topPanel, "评分管理", "scores");
        addNavButton(topPanel, "成绩统计", "statistics");
        addNavButton(topPanel, "讨论区", "discussion");
        
        contentPanel.add(topPanel, BorderLayout.NORTH);
        contentPanel.add(mainContentPanel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // 默认显示欢迎面板
        cardLayout.show(mainContentPanel, "welcome");
    }
    
    // 添加导航按钮
    private void addNavButton(JPanel panel, String text, String cardName) {
        JButton button = createStyledButton(text, new Color(25, 118, 210));
        button.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));
        panel.add(button);
    }

    // 创建欢迎面板
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("欢迎使用企业实习管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("请从顶部选择功能开始使用");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(subtitleLabel);
        
        return welcomePanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(100, 35));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
} 