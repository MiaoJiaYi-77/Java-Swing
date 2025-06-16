package view.company;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 申请管理模块面板
 */
public class ApplicationManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    private int companyId;
    
    public ApplicationManagementPanel(JFrame parentFrame, int companyId) {
        this.parentFrame = parentFrame;
        this.companyId = companyId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("申请列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"学生姓名", "岗位名称", "简历标题", "申请状态", "申请时间"};
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
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadApplicationData();

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton approveButton = createStyledButton("通过", new Color(40, 167, 69));
        JButton rejectButton = createStyledButton("拒绝", new Color(220, 53, 69));
        JButton viewButton = createStyledButton("查看简历", new Color(108, 117, 125));
        
        approveButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String positionTitle = (String)table.getValueAt(selectedRow, 1);
                updateApplicationStatus(positionTitle, "approved");
                model.setValueAt("approved", selectedRow, 3);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要通过的申请");
            }
        });
        
        rejectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String positionTitle = (String)table.getValueAt(selectedRow, 1);
                updateApplicationStatus(positionTitle, "rejected");
                model.setValueAt("rejected", selectedRow, 3);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要拒绝的申请");
            }
        });
        
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                String positionTitle = (String)table.getValueAt(selectedRow, 1);
                viewResumeDetails(positionTitle);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的申请");
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadApplicationData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, p.title, r.title as resume_title, a.status, a.created_at " +
                 "FROM internship_applications a " +
                 "JOIN users u ON a.student_id = u.id " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN resumes r ON a.resume_id = r.id " +
                 "WHERE p.company_id = ? " +
                 "ORDER BY a.created_at DESC")) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getString("resume_title"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取申请列表失败：" + e.getMessage());
        }
    }
    
    private void updateApplicationStatus(String positionTitle, String status) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false); // 开启事务
            try {
                // 首先获取position_id
                PreparedStatement posStmt = conn.prepareStatement(
                    "SELECT id FROM internship_positions WHERE title = ? AND company_id = ?"
                );
                posStmt.setString(1, positionTitle);
                posStmt.setInt(2, companyId);
                ResultSet posRs = posStmt.executeQuery();
                
                if (posRs.next()) {
                    int positionId = posRs.getInt("id");
                    
                    // 更新申请状态
                    PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE internship_applications SET status = ? WHERE position_id = ?"
                    );
                    updateStmt.setString(1, status);
                    updateStmt.setInt(2, positionId);
                    int result = updateStmt.executeUpdate();
                    
                    if (result > 0) {
                        conn.commit(); // 提交事务
                        JOptionPane.showMessageDialog(parentFrame, "申请状态更新成功！");
                    } else {
                        conn.rollback(); // 回滚事务
                        JOptionPane.showMessageDialog(parentFrame, "没有找到相关申请记录");
                    }
                } else {
                    conn.rollback(); // 回滚事务
                    JOptionPane.showMessageDialog(parentFrame, "未找到对应的岗位");
                }
            } catch (SQLException e) {
                conn.rollback(); // 出错时回滚
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "更新申请状态失败：" + e.getMessage());
        }
    }
    
    private void viewResumeDetails(String positionTitle) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT r.*, u.name as student_name " +
                 "FROM resumes r " +
                 "JOIN internship_applications ia ON r.id = ia.resume_id " +
                 "JOIN internship_positions ip ON ia.position_id = ip.id " +
                 "JOIN users u ON r.student_id = u.id " +
                 "WHERE ip.title = ? AND ip.company_id = ?")) {
            stmt.setString(1, positionTitle);
            stmt.setInt(2, companyId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog(parentFrame, "简历详情", true);
                dialog.setLayout(new BorderLayout());
                dialog.setSize(600, 400);
                dialog.setLocationRelativeTo(parentFrame);

                JPanel contentPanel = new JPanel();
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBackground(Color.WHITE);
                contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                // 添加简历信息
                JLabel nameLabel = new JLabel("学生姓名：" + rs.getString("student_name"));
                JLabel titleLabel = new JLabel("简历标题：" + rs.getString("title"));
                JTextArea contentArea = new JTextArea(rs.getString("content"));
                contentArea.setEditable(false);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);

                contentPanel.add(nameLabel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                contentPanel.add(titleLabel);
                contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                contentPanel.add(new JScrollPane(contentArea));

                JButton closeButton = createStyledButton("关闭", new Color(0, 123, 255));
                closeButton.addActionListener(e -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(Color.WHITE);
                buttonPanel.add(closeButton);

                dialog.add(contentPanel, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取简历详情失败：" + e.getMessage());
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