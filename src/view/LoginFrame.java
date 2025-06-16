package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    
    // 定义配色方案
    private static final Color PRIMARY_BLUE = new Color(25, 118, 210); // 主要蓝色
    private static final Color LIGHT_BLUE = new Color(66, 139, 202); // 浅蓝色
    private static final Color BACKGROUND_WHITE = Color.WHITE; // 背景白色
    private static final Color TEXT_DARK = new Color(33, 33, 33); // 文字深色

    public LoginFrame() {
        setTitle("学生实习信息管理系统 - 登录");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // 创建主面板
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(BACKGROUND_WHITE);

        // 创建顶部面板（包含标题和图标）
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setBackground(BACKGROUND_WHITE);
        
        // 创建标题标签
        JLabel titleLabel = new JLabel("学生实习管理系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_BLUE);
        
        JLabel subtitleLabel = new JLabel("用户登录", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_DARK);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setBackground(BACKGROUND_WHITE);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BACKGROUND_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 设置标签和输入框的首选尺寸
        Dimension labelSize = new Dimension(80, 30);
        Dimension fieldSize = new Dimension(200, 30);
        
        // 用户名输入
        JLabel usernameLabel = new JLabel("用户名：");
        usernameLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameLabel.setForeground(TEXT_DARK);
        usernameLabel.setPreferredSize(labelSize);
        
        usernameField = new JTextField();
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        usernameField.setPreferredSize(fieldSize);
        usernameField.setMinimumSize(fieldSize);
        usernameField.setMaximumSize(fieldSize);
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // 密码输入
        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordLabel.setForeground(TEXT_DARK);
        passwordLabel.setPreferredSize(labelSize);
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passwordField.setPreferredSize(fieldSize);
        passwordField.setMinimumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        
        // 用户类型选择
        JLabel userTypeLabel = new JLabel("用户类型：");
        userTypeLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userTypeLabel.setForeground(TEXT_DARK);
        userTypeLabel.setPreferredSize(labelSize);
        
        String[] userTypes = {"学生", "企业", "教师", "管理员"};
        userTypeCombo = new JComboBox<>(userTypes);
        userTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userTypeCombo.setBackground(BACKGROUND_WHITE);
        userTypeCombo.setPreferredSize(fieldSize);
        userTypeCombo.setMinimumSize(fieldSize);
        userTypeCombo.setMaximumSize(fieldSize);
        userTypeCombo.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE));
        // 让ComboBox的编辑器高度和文本框一致
        userTypeCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = super.createArrowButton();
                button.setPreferredSize(new Dimension(30, 30));
                button.setMinimumSize(new Dimension(30, 30));
                button.setMaximumSize(new Dimension(30, 30));
                return button;
            }
        });
        if (userTypeCombo.getEditor().getEditorComponent() instanceof JTextField) {
            JTextField comboEditor = (JTextField) userTypeCombo.getEditor().getEditorComponent();
            comboEditor.setPreferredSize(fieldSize);
            comboEditor.setMinimumSize(fieldSize);
            comboEditor.setMaximumSize(fieldSize);
        }

        // 添加组件到输入面板
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(usernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(passwordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(userTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(userTypeCombo, gbc);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_WHITE);
        
        JButton loginButton = createStyledButton("登录", PRIMARY_BLUE);
        JButton registerButton = createStyledButton("注册", LIGHT_BLUE);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> showRegisterDialog());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        
        // 添加一些内边距
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // 设置窗口背景
        getContentPane().setBackground(BACKGROUND_WHITE);
        add(mainPanel);
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setForeground(BACKGROUND_WHITE);
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

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeCombo.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 兼容中英文 user_type
        String userTypeEn = null;
        switch (userType) {
            case "学生": userTypeEn = "student"; break;
            case "企业": userTypeEn = "company"; break;
            case "教师": userTypeEn = "teacher"; break;
            case "管理员": userTypeEn = "admin"; break;
            default: userTypeEn = userType;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND (user_type = ? OR user_type = ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, userType);
            pstmt.setString(4, userTypeEn);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "登录成功！");
                openMainFrame(userType, username);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "登录失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRegisterDialog() {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }

    private void openMainFrame(String userType, String username) {
        SwingUtilities.invokeLater(() -> {
            JFrame mainFrame = null;
            switch (userType) {
                case "学生":
                    mainFrame = new StudentMainFrame(username);
                    break;
                case "企业":
                    mainFrame = new CompanyMainFrame(username);
                    break;
                case "教师":
                    mainFrame = new TeacherMainFrame(username);
                    break;
                case "管理员":
                    mainFrame = new AdminMainFrame(username);
                    break;
            }
            if (mainFrame != null) {
                mainFrame.setVisible(true);
            }
        });
    }

    private int getUserId(String username) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // 如果未找到用户ID，返回-1
    }

    public static void main(String[] args) {
        try {
            // 设置本地系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}