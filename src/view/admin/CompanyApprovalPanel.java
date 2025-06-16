package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 企业审核模块面板
 */
public class CompanyApprovalPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    
    public CompanyApprovalPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("企业审核");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"ID", "企业名称", "联系人", "联系方式", "申请时间", "状态"};
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

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createStyledButton("查看详情", new Color(23, 162, 184));
        JButton approveButton = createStyledButton("通过", new Color(40, 167, 69));
        JButton rejectButton = createStyledButton("拒绝", new Color(220, 53, 69));
        
        buttonPanel.add(viewButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        // 添加按钮事件
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (Integer) model.getValueAt(selectedRow, 0);
                showCompanyDetail(userId);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的企业");
            }
        });
        
        approveButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (Integer) model.getValueAt(selectedRow, 0);
                updateUserStatus(userId, "已通过");
                loadPendingCompanies();
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要审核的企业");
            }
        });
        
        rejectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (Integer) model.getValueAt(selectedRow, 0);
                updateUserStatus(userId, "已拒绝");
                loadPendingCompanies();
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要审核的企业");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载待审核企业
        loadPendingCompanies();
    }
    
    private void loadPendingCompanies() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, u.id as user_id, u.name as contact_name, u.phone " +
                 "FROM company_info c " +
                 "JOIN users u ON c.user_id = u.id " +
                 "WHERE u.user_type = '企业' " +
                 "ORDER BY c.created_at DESC"
             )) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("user_id"),
                    rs.getString("company_name"),
                    rs.getString("contact_name"),
                    rs.getString("phone"),
                    rs.getTimestamp("created_at"),
                    "待审核"  // 使用固定状态，因为这里只显示待审核的企业
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载待审核企业失败：" + e.getMessage());
        }
    }
    
    private void updateUserStatus(int userId, String status) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE internship_applications SET status = ? WHERE student_id = ?"
             )) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(parentFrame, "企业状态更新成功！");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "企业状态更新失败，请重试。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "更新企业状态失败：" + e.getMessage());
        }
    }
    
    private void showCompanyDetail(int userId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT c.*, u.name as contact_name, u.email, u.phone " +
                 "FROM company_info c " +
                 "JOIN users u ON c.user_id = u.id " +
                 "WHERE u.id = ?")) {
            stmt.setInt(1, userId);
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
                addDetailItem(panel, "申请时间：", rs.getTimestamp("created_at").toString());

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