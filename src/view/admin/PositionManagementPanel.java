package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 岗位管理模块面板
 */
public class PositionManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    
    public PositionManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和搜索面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("岗位管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        JTextField searchField = new JTextField(20);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"全部", "招聘中", "已结束"});
        JButton searchButton = createStyledButton("搜索");
        
        searchPanel.add(new JLabel("岗位名称："));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("状态："));
        searchPanel.add(statusFilter);
        searchPanel.add(searchButton);
        
        headerPanel.add(searchPanel, BorderLayout.CENTER);
        
        // 创建表格
        String[] columnNames = {"企业名称", "岗位名称", "招聘人数", "申请人数", "状态", "发布时间"};
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
        
        // 加载岗位数据
        loadPositionData("", "全部");
        
        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            String status = (String) statusFilter.getSelectedItem();
            loadPositionData(keyword, status);
        });
        
        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createStyledButton("查看详情", new Color(23, 162, 184));
        JButton editButton = createStyledButton("编辑", new Color(255, 193, 7));
        JButton deleteButton = createStyledButton("删除", new Color(220, 53, 69));
        
        buttonPanel.add(viewButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        // 添加按钮事件
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                String positionName = (String) model.getValueAt(selectedRow, 1);
                // 通过企业名称和岗位名称查找岗位ID
                int positionId = getPositionId(companyName, positionName);
                if (positionId != -1) {
                    showPositionDetail(positionId);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取岗位ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的岗位");
            }
        });
        
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                String positionName = (String) model.getValueAt(selectedRow, 1);
                // 通过企业名称和岗位名称查找岗位ID
                int positionId = getPositionId(companyName, positionName);
                if (positionId != -1) {
                    showEditPosition(positionId);
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取岗位ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要编辑的岗位");
            }
        });
        
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String companyName = (String) model.getValueAt(selectedRow, 0);
                String positionName = (String) model.getValueAt(selectedRow, 1);
                // 通过企业名称和岗位名称查找岗位ID
                int positionId = getPositionId(companyName, positionName);
                if (positionId != -1) {
                    deletePosition(positionId);
                    loadPositionData("", "全部");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "无法获取岗位ID");
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要删除的岗位");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadPositionData(String keyword, String status) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT p.*, c.company_name, " +
                        "(SELECT COUNT(*) FROM internship_applications a WHERE a.position_id = p.id) as application_count " +
                        "FROM internship_positions p " +
                        "JOIN company_info c ON p.company_id = c.id ";
            if (!keyword.isEmpty() || !"全部".equals(status)) {
                sql += "WHERE 1=1 ";
                if (!keyword.isEmpty()) {
                    sql += "AND (p.title LIKE ? OR c.company_name LIKE ?) ";
                }
                if (!"全部".equals(status)) {
                    sql += "AND p.status = ? ";
                }
            }
            sql += "ORDER BY p.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            if (!keyword.isEmpty()) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            if (!"全部".equals(status)) {
                stmt.setString(paramIndex, status);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("company_name"),
                    rs.getString("title"),
                    rs.getInt("quota"),
                    rs.getInt("application_count"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载岗位数据失败：" + e.getMessage());
        }
    }
    
    private int getPositionId(String companyName, String positionName) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.id FROM internship_positions p " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "WHERE c.company_name = ? AND p.title = ? LIMIT 1"
             )) {
            stmt.setString(1, companyName);
            stmt.setString(2, positionName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private void showPositionDetail(int positionId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.*, c.company_name " +
                 "FROM internship_positions p " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "WHERE p.id = ?")) {
            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "岗位详情", true);
                dialog.setSize(600, 500);
                dialog.setLocationRelativeTo(parentFrame);
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                panel.setBackground(Color.WHITE);

                // 添加岗位信息
                addDetailItem(panel, "企业名称：", rs.getString("company_name"));
                addDetailItem(panel, "岗位名称：", rs.getString("title"));
                addDetailItem(panel, "招聘人数：", String.valueOf(rs.getInt("quota")));
                addDetailItem(panel, "岗位要求：", rs.getString("requirements"));
                addDetailItem(panel, "岗位描述：", rs.getString("description"));
                addDetailItem(panel, "状态：", rs.getString("status"));
                addDetailItem(panel, "发布时间：", rs.getTimestamp("created_at").toString());

                // 添加申请人列表
                JPanel applicantsPanel = new JPanel(new BorderLayout());
                applicantsPanel.setBackground(Color.WHITE);
                applicantsPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    "申请人列表",
                    javax.swing.border.TitledBorder.LEFT,
                    javax.swing.border.TitledBorder.TOP,
                    new Font("微软雅黑", Font.BOLD, 14)
                ));

                String[] columnNames = {"学生姓名", "申请时间", "状态"};
                DefaultTableModel applicantsModel = new DefaultTableModel(columnNames, 0);
                JTable applicantsTable = new JTable(applicantsModel);
                applicantsTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
                applicantsTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
                applicantsTable.setRowHeight(25);

                // 加载申请人数据
                loadApplicantsData(applicantsModel, positionId);

                applicantsPanel.add(new JScrollPane(applicantsTable));
                panel.add(applicantsPanel);

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
            JOptionPane.showMessageDialog(parentFrame, "获取岗位详情失败：" + e.getMessage());
        }
    }
    
    private void loadApplicantsData(DefaultTableModel model, int positionId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, a.created_at, a.status " +
                 "FROM internship_applications a " +
                 "JOIN users u ON a.student_id = u.id " +
                 "WHERE a.position_id = ? " +
                 "ORDER BY a.created_at DESC")) {
            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载申请人数据失败：" + e.getMessage());
        }
    }
    
    private void showEditPosition(int positionId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.*, c.company_name " +
                 "FROM internship_positions p " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "WHERE p.id = ?")) {
            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "编辑岗位", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(parentFrame);
                
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridwidth = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.insets = new Insets(5, 5, 5, 5);

                // 创建表单字段
                JTextField titleField = new JTextField(rs.getString("title"), 30);
                JSpinner quotaSpinner = new JSpinner(new SpinnerNumberModel(
                    rs.getInt("quota"), 1, 100, 1
                ));
                JTextArea requirementsArea = new JTextArea(rs.getString("requirements"), 5, 30);
                requirementsArea.setLineWrap(true);
                requirementsArea.setWrapStyleWord(true);
                JTextArea descriptionArea = new JTextArea(rs.getString("description"), 5, 30);
                descriptionArea.setLineWrap(true);
                descriptionArea.setWrapStyleWord(true);
                JComboBox<String> statusCombo = new JComboBox<>(new String[]{"招聘中", "已结束"});
                statusCombo.setSelectedItem(rs.getString("status"));

                // 添加表单字段
                addFormField(panel, gbc, "岗位名称：", titleField, 0);
                addFormField(panel, gbc, "招聘人数：", quotaSpinner, 1);
                addFormField(panel, gbc, "岗位要求：", new JScrollPane(requirementsArea), 2);
                addFormField(panel, gbc, "岗位描述：", new JScrollPane(descriptionArea), 3);
                addFormField(panel, gbc, "状态：", statusCombo, 4);

                // 添加按钮面板
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton submitButton = createStyledButton("保存");
                JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

                submitButton.addActionListener(e -> {
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE internship_positions SET title = ?, quota = ?, requirements = ?, description = ?, status = ? WHERE id = ?"
                    )) {
                        updateStmt.setString(1, titleField.getText().trim());
                        updateStmt.setInt(2, (Integer) quotaSpinner.getValue());
                        updateStmt.setString(3, requirementsArea.getText().trim());
                        updateStmt.setString(4, descriptionArea.getText().trim());
                        updateStmt.setString(5, (String) statusCombo.getSelectedItem());
                        updateStmt.setInt(6, positionId);
                        
                        updateStmt.executeUpdate();
                        JOptionPane.showMessageDialog(dialog, "岗位信息更新成功！");
                        dialog.dispose();
                        loadPositionData("", "全部"); // 刷新岗位列表
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(dialog, "更新岗位信息失败：" + ex.getMessage());
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
            JOptionPane.showMessageDialog(parentFrame, "获取岗位信息失败：" + e.getMessage());
        }
    }
    
    private void deletePosition(int positionId) {
        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "确定要删除这个岗位吗？此操作不可恢复。",
            "确认删除",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM internship_positions WHERE id = ?")) {
                stmt.setInt(1, positionId);
                int result = stmt.executeUpdate();
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(parentFrame, "岗位删除成功！");
                } else {
                    JOptionPane.showMessageDialog(parentFrame, "岗位删除失败，请重试。");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parentFrame, "删除岗位失败：" + e.getMessage());
            }
        }
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