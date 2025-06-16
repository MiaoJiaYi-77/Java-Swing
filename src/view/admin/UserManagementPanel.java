package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 用户管理模块面板
 */
public class UserManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    
    public UserManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("用户管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建搜索和筛选面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(20);
        JComboBox<String> typeFilter = new JComboBox<>(new String[]{"全部用户", "学生", "教师", "企业", "管理员"});
        JButton searchButton = createStyledButton("搜索");
        
        searchPanel.add(new JLabel("搜索："));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("用户类型："));
        searchPanel.add(typeFilter);
        searchPanel.add(searchButton);
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // 创建操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addButton = createStyledButton("添加用户", new Color(40, 167, 69));
        JButton editButton = createStyledButton("编辑", new Color(255, 193, 7));
        JButton deleteButton = createStyledButton("删除", new Color(220, 53, 69));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 创建表格（去除ID列）
        String[] columnNames = {"用户名", "姓名", "邮箱", "电话号码", "角色", "注册时间"};
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
        
        // 加载用户数据
        loadUserData("", "全部用户");
        
        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            String type = (String) typeFilter.getSelectedItem();
            loadUserData(keyword, type);
        });
        
        // 添加按钮事件
        addButton.addActionListener(e -> showAddUser());
        
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String username = (String) model.getValueAt(selectedRow, 0);
                int userId = getUserIdByUsername(username);
                if (userId != -1) {
                    showEditUser(userId);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取用户ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要编辑的用户");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String username = (String) model.getValueAt(selectedRow, 0);
                int userId = getUserIdByUsername(username);
                if (userId != -1) {
                    deleteUser(userId);
                    loadUserData("", "全部用户");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取用户ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要删除的用户");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void showAddUser() {
        JDialog dialog = new JDialog(parentFrame, "添加用户", true);
        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 创建表单字段
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"学生", "教师", "企业", "管理员"});

        // 添加表单字段
        addFormField(panel, gbc, "用户名：", usernameField, 0);
        addFormField(panel, gbc, "密码：", passwordField, 1);
        addFormField(panel, gbc, "姓名：", nameField, 2);
        addFormField(panel, gbc, "邮箱：", emailField, 3);
        addFormField(panel, gbc, "电话号码：", phoneField, 4);
        addFormField(panel, gbc, "角色：", userTypeCombo, 5);

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = createStyledButton("保存");
        JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

        submitButton.addActionListener(e -> {
            // 验证输入
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String userType = (String) userTypeCombo.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "用户名和密码不能为空！");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, name, email, phone, user_type) VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS
                 )) {
                stmt.setString(1, username);
                stmt.setString(2, password); // 注意：实际应用中应该对密码进行加密
                stmt.setString(3, name);
                stmt.setString(4, email);
                stmt.setString(5, phone);
                stmt.setString(6, userType);
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // 如果是学生，需要创建学生信息记录
                    if ("学生".equals(userType)) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            try (PreparedStatement studentStmt = conn.prepareStatement(
                                "INSERT INTO student_info (user_id) VALUES (?)"
                            )) {
                                studentStmt.setInt(1, userId);
                                studentStmt.executeUpdate();
                            }
                        }
                    }
                    // 如果是企业，需要创建企业信息记录
                    else if ("企业".equals(userType)) {
                        ResultSet rs = stmt.getGeneratedKeys();
                        if (rs.next()) {
                            int userId = rs.getInt(1);
                            try (PreparedStatement companyStmt = conn.prepareStatement(
                                "INSERT INTO company_info (user_id, company_name) VALUES (?, ?)"
                            )) {
                                companyStmt.setInt(1, userId);
                                companyStmt.setString(2, name); // 暂时使用name作为公司名称
                                companyStmt.executeUpdate();
                            }
                        }
                    }
                    
                    JOptionPane.showMessageDialog(dialog, "用户添加成功！");
                    dialog.dispose();
                    loadUserData("", "全部用户"); // 刷新用户列表
                } else {
                    JOptionPane.showMessageDialog(dialog, "用户添加失败，请重试。");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "添加用户失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void loadUserData(String keyword, String type) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT * FROM users WHERE 1=1";
            if (!keyword.isEmpty()) {
                sql += " AND (username LIKE ? OR name LIKE ? OR email LIKE ?)";
            }
            if (!"全部用户".equals(type)) {
                sql += " AND user_type = ?";
            }
            sql += " ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            if (!keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            if (!"全部用户".equals(type)) {
                stmt.setString(paramIndex, type);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("username"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("user_type"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载用户数据失败：" + e.getMessage());
        }
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(label, gbc);

        gbc.gridx = 1;
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(field, gbc);
    }

    private void showEditUser(int userId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "编辑用户", true);
                dialog.setSize(400, 500);
                dialog.setLocationRelativeTo(parentFrame);
                
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 5, 5, 5);

                // 创建表单字段并填充现有数据
                JTextField usernameField = new JTextField(rs.getString("username"), 20);
                JTextField nameField = new JTextField(rs.getString("name"), 20);
                JTextField emailField = new JTextField(rs.getString("email"), 20);
                JTextField phoneField = new JTextField(rs.getString("phone"), 20);
                JComboBox<String> userTypeCombo = new JComboBox<>(new String[]{"学生", "教师", "企业", "管理员"});
                userTypeCombo.setSelectedItem(rs.getString("user_type"));

                // 添加表单字段
                addFormField(panel, gbc, "用户名：", usernameField, 0);
                addFormField(panel, gbc, "姓名：", nameField, 1);
                addFormField(panel, gbc, "邮箱：", emailField, 2);
                addFormField(panel, gbc, "电话号码：", phoneField, 3);
                addFormField(panel, gbc, "角色：", userTypeCombo, 4);

                // 添加按钮面板
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton submitButton = createStyledButton("保存");
                JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

                submitButton.addActionListener(e -> {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE users SET name = ?, email = ?, phone = ?, user_type = ? WHERE id = ?"
                    )) {
                        updateStmt.setString(1, nameField.getText().trim());
                        updateStmt.setString(2, emailField.getText().trim());
                        updateStmt.setString(3, phoneField.getText().trim());
                        updateStmt.setString(4, (String) userTypeCombo.getSelectedItem());
                        updateStmt.setInt(5, userId);
                        
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(dialog, "用户信息更新成功！");
                        dialog.dispose();
                        loadUserData("", "全部用户"); // 刷新用户列表
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "更新用户信息失败：" + ex.getMessage());
                    }
                });

                cancelButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(submitButton);
                buttonPanel.add(cancelButton);

                gbc.gridy = 5;
                gbc.gridwidth = 2;
                panel.add(buttonPanel, gbc);

                dialog.add(new JScrollPane(panel));
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取用户信息失败：" + e.getMessage());
        }
    }

    private void deleteUser(int userId) {
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "确定要删除这个用户吗？此操作不可恢复。",
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                stmt.setInt(1, userId);
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(parentFrame, "用户删除成功！");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "用户删除失败，请重试。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "删除用户失败：" + e.getMessage());
            }
        }
    }
    
    // 通过用户名查找用户ID
    private int getUserIdByUsername(String username) {
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
        return -1;
    }

    private JButton createStyledButton(String text) {
        return createStyledButton(text, new Color(0, 123, 255));
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