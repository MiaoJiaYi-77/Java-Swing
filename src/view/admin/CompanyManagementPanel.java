package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 企业管理模块面板
 */
public class CompanyManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    
    public CompanyManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("企业列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(20);
        JButton searchButton = createStyledButton("搜索");
        
        searchPanel.add(new JLabel("企业名称："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // 创建操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createStyledButton("查看详情", new Color(23, 162, 184));
        JButton editButton = createStyledButton("编辑", new Color(255, 193, 7));
        JButton deleteButton = createStyledButton("删除", new Color(220, 53, 69));
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 创建表格
        String[] columnNames = {"企业名称", "地址", "联系人", "联系电话", "创建时间"};
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
        
        // 加载企业数据
        loadCompanyData("");
        
        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadCompanyData(keyword);
        });
        
        // 添加按钮事件
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                int companyId = getCompanyIdByName(companyName);
                if (companyId != -1) {
                    showCompanyDetail(companyId);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取企业ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的企业");
            }
        });
        
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                int companyId = getCompanyIdByName(companyName);
                if (companyId != -1) {
                    showEditCompany(companyId);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取企业ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要编辑的企业");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                int companyId = getCompanyIdByName(companyName);
                if (companyId != -1) {
                    deleteCompany(companyId);
                    loadCompanyData("");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取企业ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要删除的企业");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadCompanyData(String keyword) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT c.*, u.name as contact_name, u.phone " +
                        "FROM company_info c " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.user_type = '企业'";
            if (!keyword.isEmpty()) {
                sql += " AND (c.company_name LIKE ? OR c.address LIKE ?)";
            }
            sql += " ORDER BY c.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (!keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("company_name"),
                    rs.getString("address"),
                    rs.getString("contact_name"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载企业数据失败：" + e.getMessage());
        }
    }
    
    private void showCompanyDetail(int companyId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, u.name as contact_name, u.email, u.phone " +
                 "FROM company_info c " +
                 "JOIN users u ON c.user_id = u.id " +
                 "WHERE c.id = ?")) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "企业详情", true);
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(parentFrame);
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground(Color.WHITE);

                // 添加企业信息
                addDetailItem(panel, "企业名称：", rs.getString("company_name"));
                addDetailItem(panel, "企业地址：", rs.getString("address"));
                addDetailItem(panel, "企业简介：", rs.getString("description"));
                addDetailItem(panel, "联系人：", rs.getString("contact_name"));
                addDetailItem(panel, "联系电话：", rs.getString("phone"));
                addDetailItem(panel, "联系邮箱：", rs.getString("email"));
                addDetailItem(panel, "创建时间：", rs.getTimestamp("created_at").toString());

                JButton closeButton = createStyledButton("关闭", new Color(108, 117, 125));
                closeButton.addActionListener(e -> dialog.dispose());
                closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(Box.createVerticalStrut(20));
                panel.add(closeButton);

                dialog.add(new JScrollPane(panel));
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取企业详情失败：" + e.getMessage());
        }
    }
    
    private void showEditCompany(int companyId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, u.name as contact_name, u.email, u.phone " +
                 "FROM company_info c " +
                 "JOIN users u ON c.user_id = u.id " +
                 "WHERE c.id = ?")) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "编辑企业信息", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(parentFrame);
                
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 5, 5, 5);

                // 创建表单字段
                JTextField nameField = new JTextField(rs.getString("company_name"), 30);
                JTextField addressField = new JTextField(rs.getString("address"), 30);
                JTextArea descArea = new JTextArea(rs.getString("description"), 5, 30);
                descArea.setLineWrap(true);
                descArea.setWrapStyleWord(true);

                // 添加表单字段
                addFormField(panel, gbc, "企业名称：", nameField, 0);
                addFormField(panel, gbc, "企业地址：", addressField, 1);
                addFormField(panel, gbc, "企业简介：", new JScrollPane(descArea), 2);

                // 添加按钮面板
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton submitButton = createStyledButton("保存");
                JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

                submitButton.addActionListener(e -> {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE company_info SET company_name = ?, address = ?, description = ? WHERE id = ?"
                    )) {
                        updateStmt.setString(1, nameField.getText().trim());
                        updateStmt.setString(2, addressField.getText().trim());
                        updateStmt.setString(3, descArea.getText().trim());
                        updateStmt.setInt(4, companyId);
                        
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(dialog, "企业信息更新成功！");
                        dialog.dispose();
                        loadCompanyData(""); // 刷新企业列表
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "更新企业信息失败：" + ex.getMessage());
                    }
                });

                cancelButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(submitButton);
                buttonPanel.add(cancelButton);

                gbc.gridy = 3;
                gbc.gridwidth = 2;
                panel.add(buttonPanel, gbc);

                dialog.add(new JScrollPane(panel));
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取企业信息失败：" + e.getMessage());
        }
    }
    
    private void deleteCompany(int companyId) {
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "确定要删除这个企业吗？此操作不可恢复。",
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM company_info WHERE id = ?")) {
                stmt.setInt(1, companyId);
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(parentFrame, "企业删除成功！");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "企业删除失败，请重试。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "删除企业失败：" + e.getMessage());
            }
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
    
    private void addDetailItem(JPanel panel, String label, String value) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("微软雅黑", Font.BOLD, 14));
        
        JLabel valueComp = new JLabel(value != null ? value : "");
        valueComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        itemPanel.add(labelComp, BorderLayout.WEST);
        itemPanel.add(valueComp, BorderLayout.CENTER);
        panel.add(itemPanel);
    }
    
    // 通过企业名称查找企业ID
    private int getCompanyIdByName(String companyName) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM company_info WHERE company_name = ?")) {
            stmt.setString(1, companyName);
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