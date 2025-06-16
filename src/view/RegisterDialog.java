package view;

import javax.swing.*;
import java.awt.*;
import config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JComboBox<String> userTypeCombo;
    
    // 定义配色方案
    private static final Color PRIMARY_BLUE = new Color(25, 118, 210); // 主要蓝色
    private static final Color LIGHT_BLUE = new Color(66, 139, 202); // 浅蓝色
    private static final Color BACKGROUND_WHITE = Color.WHITE; // 背景白色
    private static final Color TEXT_DARK = new Color(33, 33, 33); // 文字深色
    private static final Dimension LABEL_SIZE = new Dimension(100, 30);
    private static final Dimension FIELD_SIZE = new Dimension(200, 30);

    public RegisterDialog(Frame owner) {
        super(owner, "用户注册", true);
        initComponents();
    }

    private void initComponents() {
        setSize(450, 550);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(BACKGROUND_WHITE);

        // 创建标题标签
        JLabel titleLabel = new JLabel("学生实习管理系统", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 26));
        titleLabel.setForeground(PRIMARY_BLUE);
        
        JLabel subtitleLabel = new JLabel("用户注册", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        subtitleLabel.setForeground(TEXT_DARK);
        
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setBackground(BACKGROUND_WHITE);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(BACKGROUND_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 用户名
        JLabel usernameLabel = createFormLabel("用户名：");
        usernameField = createFormField();
        addFormRow(inputPanel, gbc, 0, usernameLabel, usernameField);

        // 密码
        JLabel passwordLabel = createFormLabel("密码：");
        passwordField = new JPasswordField();
        styleField(passwordField);
        addFormRow(inputPanel, gbc, 1, passwordLabel, passwordField);

        // 确认密码
        JLabel confirmPasswordLabel = createFormLabel("确认密码：");
        confirmPasswordField = new JPasswordField();
        styleField(confirmPasswordField);
        addFormRow(inputPanel, gbc, 2, confirmPasswordLabel, confirmPasswordField);

        // 姓名
        JLabel nameLabel = createFormLabel("姓名：");
        nameField = createFormField();
        addFormRow(inputPanel, gbc, 3, nameLabel, nameField);

        // 邮箱
        JLabel emailLabel = createFormLabel("邮箱：");
        emailField = createFormField();
        addFormRow(inputPanel, gbc, 4, emailLabel, emailField);

        // 电话
        JLabel phoneLabel = createFormLabel("电话：");
        phoneField = createFormField();
        addFormRow(inputPanel, gbc, 5, phoneLabel, phoneField);

        // 用户类型
        JLabel userTypeLabel = createFormLabel("用户类型：");
        String[] userTypes = {"学生", "企业", "教师"};
        userTypeCombo = new JComboBox<>(userTypes);
        userTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userTypeCombo.setBackground(BACKGROUND_WHITE);
        userTypeCombo.setPreferredSize(FIELD_SIZE);
        userTypeCombo.setMinimumSize(FIELD_SIZE);
        userTypeCombo.setMaximumSize(FIELD_SIZE);
        userTypeCombo.setBorder(BorderFactory.createLineBorder(LIGHT_BLUE));
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
        addFormRow(inputPanel, gbc, 6, userTypeLabel, userTypeCombo);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(BACKGROUND_WHITE);

        JButton registerButton = createStyledButton("注册", PRIMARY_BLUE);
        JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

        registerButton.addActionListener(e -> register());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(BACKGROUND_WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        // 设置窗口背景
        getContentPane().setBackground(BACKGROUND_WHITE);
        add(mainPanel);
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_DARK);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        label.setPreferredSize(LABEL_SIZE);
        return label;
    }

    private JTextField createFormField() {
        JTextField field = new JTextField();
        styleField(field);
        return field;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        field.setPreferredSize(FIELD_SIZE);
        field.setMinimumSize(FIELD_SIZE);
        field.setMaximumSize(FIELD_SIZE);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(LIGHT_BLUE),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, JLabel label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(field, gbc);
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

    private void register() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String userType = (String) userTypeCombo.getSelectedItem();

        // 验证输入
        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写必要信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "INSERT INTO users (username, password, name, email, phone, user_type) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, name);
            pstmt.setString(4, email);
            pstmt.setString(5, phone);
            pstmt.setString(6, userType);

            int result = pstmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "注册成功！");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "注册失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "注册失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}