package view;

import view.teacher.*;
import config.DatabaseConfig;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class TeacherMainFrame extends BaseMainFrame {
    private String currentUsername;
    private int currentUserId;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    public TeacherMainFrame(String username) {
        super("学生实习管理系统 - 教师界面", username);
        this.currentUsername = username;
        initializeUserId();
        createContentPanel();
    }

    private void initializeUserId() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
                System.out.println("当前登录教师ID: " + currentUserId);
            } else {
                System.out.println("未找到用户: " + currentUsername);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    protected void createMenuBar() {
        // 不使用菜单栏
    }

    @Override
    protected void createContentPanel() {
        if (currentUserId == 0) {
            System.out.println("警告：教师ID未正确初始化");
            return;
        }
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 255));
        
        // 顶部横向功能按钮区
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(new Color(25, 118, 210));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 系统标题
        JLabel systemTitle = new JLabel("教师实习管理系统 - " + currentUsername + " (ID: " + currentUserId + ")");
        systemTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        systemTitle.setForeground(Color.WHITE);
        topPanel.add(systemTitle);

        // 创建内容面板（使用CardLayout）
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(Color.WHITE);
        
        // 添加各个功能模块
        mainContentPanel.add(createWelcomePanel(), "welcome");
        mainContentPanel.add(new StudentListPanel(this, currentUserId), "students");
        mainContentPanel.add(new InternshipStatusPanel(this, currentUserId), "status");
        mainContentPanel.add(new GradeManagementPanel(this, currentUserId), "grades");
        mainContentPanel.add(new ScoreStatisticsPanel(this, currentUserId), "statistics");
        mainContentPanel.add(new LogReviewPanel(this, currentUserId), "logs");
        mainContentPanel.add(new GuidanceRecordPanel(this, currentUserId), "guidance");
        mainContentPanel.add(new DiscussionReplyPanel(this, currentUserId), "reply");
        
        // 添加功能按钮
        addNavButton(topPanel, "学生列表", "students");
        addNavButton(topPanel, "实习情况", "status");
        addNavButton(topPanel, "成绩管理", "grades");
        addNavButton(topPanel, "成绩统计", "statistics");
        addNavButton(topPanel, "日志批阅", "logs");
        addNavButton(topPanel, "指导记录", "guidance");
        addNavButton(topPanel, "回复讨论", "reply");
        
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

        JLabel titleLabel = new JLabel("欢迎使用教师实习管理系统");
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