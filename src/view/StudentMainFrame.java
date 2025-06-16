package view;

import view.student.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import config.DatabaseConfig;

public class StudentMainFrame extends BaseMainFrame implements ActionListener {
    private String currentUsername;
    private int currentUserId;
    private JPanel mainContentPanel;

    public StudentMainFrame(String username) {
        super("学生实习管理系统 - 学生界面", username);
        this.currentUsername = username;
        initializeUserId();
    }

    private void initializeUserId() {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, currentUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                currentUserId = rs.getInt("id");
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
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(new Color(240, 245, 255)); // 浅蓝色背景

        // 创建顶部功能面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        topPanel.setBackground(new Color(25, 118, 210)); // 蓝色背景
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 添加系统标题到顶部面板
        JLabel systemTitle = new JLabel("学生实习管理系统");
        systemTitle.setFont(new Font("微软雅黑", Font.BOLD, 18));
        systemTitle.setForeground(Color.WHITE);
        topPanel.add(systemTitle);

        // 创建功能按钮
        JButton resumeManageBtn = createStyledButton("个人简历管理", new Color(25, 118, 210));
        JButton positionBtn = createStyledButton("浏览岗位", new Color(25, 118, 210));
        JButton applicationBtn = createStyledButton("我的申请", new Color(25, 118, 210));
        JButton logManageBtn = createStyledButton("日志管理", new Color(25, 118, 210));
        JButton replyManageBtn = createStyledButton("评论管理", new Color(25, 118, 210));

        // 添加按钮事件
        resumeManageBtn.addActionListener(e -> showPanel(new ResumeManagementPanel(currentUserId)));
        positionBtn.addActionListener(e -> showPanel(new InternshipPositionPanel(currentUserId)));
        applicationBtn.addActionListener(e -> showPanel(new ApplicationManagementPanel(currentUserId)));
        logManageBtn.addActionListener(e -> showPanel(new LogManagementPanel(currentUserId)));
        replyManageBtn.addActionListener(e -> showPanel(new ReplyManagementPanel(currentUserId)));

        // 添加按钮到顶部面板
        topPanel.add(resumeManageBtn);
        topPanel.add(positionBtn);
        topPanel.add(applicationBtn);
        topPanel.add(logManageBtn);
        topPanel.add(replyManageBtn);

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

    private void showWelcomePanel() {
        mainContentPanel.removeAll();
        
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("欢迎使用学生实习管理系统");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("请从上方选择功能开始使用");
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(102, 102, 102));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(titleLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(subtitleLabel);

        mainContentPanel.add(welcomePanel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private void showPanel(JPanel panel) {
        mainContentPanel.removeAll();
        mainContentPanel.add(panel, BorderLayout.CENTER);
        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });

        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 不需要实现
    }
}