package view.company;

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
    private int companyId;
    private int currentUserId;
    
    public PositionManagementPanel(JFrame parentFrame, int companyId, int currentUserId) {
        this.parentFrame = parentFrame;
        this.companyId = companyId;
        this.currentUserId = currentUserId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("岗位管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton addBtn = createStyledButton("新增岗位", new Color(0, 123, 255));
        JButton editBtn = createStyledButton("编辑岗位", new Color(108, 117, 125));
        JButton delBtn = createStyledButton("删除岗位", new Color(220, 53, 69));
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // 表格
        String[] columnNames = {"岗位标题", "招聘人数", "发布时间", "状态"};
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
        
        // 加载数据
        loadPositionsData();
        
        // 新增
        addBtn.addActionListener(e -> showPositionEditDialog(null));
        
        // 编辑
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String title = (String) model.getValueAt(row, 0);
                showPositionEditDialog(title);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要编辑的岗位");
            }
        });
        
        // 删除
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String title = (String) model.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(
                    parentFrame, 
                    "确定要删除该岗位吗？", 
                    "确认删除", 
                    JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    deletePosition(title, row);
                    loadPositionsData();
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要删除的岗位");
            }
        });
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadPositionsData() {
        if (companyId <= 0) {
            return;
        }

        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT title, quota, created_at, status FROM internship_positions " +
                 "WHERE company_id = ? ORDER BY created_at DESC"
             )) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getInt("quota"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, 
                "加载岗位列表失败：" + e.getMessage(),
                "错误",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showPositionEditDialog(String title) {
        // 再次检查企业ID
        if (companyId <= 0) {
            JOptionPane.showMessageDialog(parentFrame,
                "企业信息未完善，无法进行岗位操作",
                "错误",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isEdit = (title != null);
        JDialog dialog = new JDialog(parentFrame, isEdit ? "编辑岗位" : "新增岗位", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("岗位标题:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(25);
        panel.add(titleField, gbc);
        
        // 描述
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("岗位描述:"), gbc);
        gbc.gridx = 1;
        JTextArea descArea = new JTextArea(4, 25);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        panel.add(descScroll, gbc);
        
        // 要求
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("岗位要求:"), gbc);
        gbc.gridx = 1;
        JTextArea reqArea = new JTextArea(4, 25);
        reqArea.setLineWrap(true);
        reqArea.setWrapStyleWord(true);
        JScrollPane reqScroll = new JScrollPane(reqArea);
        panel.add(reqScroll, gbc);
        
        // 招聘人数
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("招聘人数:"), gbc);
        gbc.gridx = 1;
        JSpinner quotaSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        panel.add(quotaSpinner, gbc);
        
        // 如果是编辑，加载原数据
        if (isEdit) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM internship_positions WHERE company_id = ? AND title = ?"
                 )) {
                stmt.setInt(1, companyId);
                stmt.setString(2, title);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    descArea.setText(rs.getString("description"));
                    reqArea.setText(rs.getString("requirements"));
                    quotaSpinner.setValue(rs.getInt("quota"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        // 按钮
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = createStyledButton("保存", new Color(0, 123, 255));
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String desc = descArea.getText().trim();
            String req = reqArea.getText().trim();
            int quota = (Integer) quotaSpinner.getValue();
            
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写岗位标题");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection()) {
                if (isEdit) {
                    // 更新时先验证岗位是否属于当前企业
                    PreparedStatement checkStmt = conn.prepareStatement(
                        "SELECT id FROM internship_positions WHERE company_id = ? AND title = ?"
                    );
                    checkStmt.setInt(1, companyId);
                    checkStmt.setString(2, title);
                    ResultSet rs = checkStmt.executeQuery();
                    
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(dialog, "无权修改此岗位信息");
                        return;
                    }

                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE internship_positions SET title=?, description=?, requirements=?, quota=? " +
                        "WHERE company_id=? AND title=?"
                    );
                    updateStmt.setString(1, newTitle);
                    updateStmt.setString(2, desc);
                    updateStmt.setString(3, req);
                    updateStmt.setInt(4, quota);
                    updateStmt.setInt(5, companyId);
                    updateStmt.setString(6, title);
                    updateStmt.executeUpdate();
                } else {
                    PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO internship_positions (company_id, title, description, requirements, quota, status) " +
                        "VALUES (?, ?, ?, ?, ?, 'active')"
                    );
                    insertStmt.setInt(1, companyId);
                    insertStmt.setString(2, newTitle);
                    insertStmt.setString(3, desc);
                    insertStmt.setString(4, req);
                    insertStmt.setInt(5, quota);
                    insertStmt.executeUpdate();
                }
                
                loadPositionsData();
                dialog.dispose();
                JOptionPane.showMessageDialog(parentFrame, 
                    isEdit ? "岗位更新成功！" : "岗位发布成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, 
                    (isEdit ? "更新" : "发布") + "失败：" + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(saveBtn, gbc);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private void deletePosition(String positionTitle, int selectedRow) {
        if (companyId <= 0) {
            return;
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM internship_positions WHERE company_id = ? AND title = ?"
             )) {
            stmt.setInt(1, companyId);
            stmt.setString(2, positionTitle);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                model.removeRow(selectedRow);
                JOptionPane.showMessageDialog(parentFrame, "岗位删除成功！");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "岗位删除失败，请重试。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "删除失败：" + e.getMessage());
        }
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