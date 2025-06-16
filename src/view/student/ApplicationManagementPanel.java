package view.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import config.DatabaseConfig;

public class ApplicationManagementPanel extends JPanel {
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public ApplicationManagementPanel(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建申请列表面板
        JPanel applicationPanel = new JPanel(new BorderLayout());
        applicationPanel.setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("我的申请");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton refreshButton = createStyledButton("刷新", new Color(25, 118, 210));
        refreshButton.addActionListener(e -> loadApplicationData());
        buttonPanel.add(refreshButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 创建表格
        tableModel = new DefaultTableModel(
            new Object[]{"申请ID", "岗位名称", "公司名称", "使用简历", "状态", "申请时间"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        JScrollPane scrollPane = new JScrollPane(table);
        
        // 加载数据
        loadApplicationData();

        applicationPanel.add(headerPanel, BorderLayout.NORTH);
        applicationPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(applicationPanel);
    }

    private void loadApplicationData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT a.id, p.title as position_title, c.company_name, r.title as resume_title, " +
                 "a.status, a.created_at " +
                 "FROM internship_applications a " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "JOIN resumes r ON a.resume_id = r.id " +
                 "WHERE a.student_id = ? ORDER BY a.created_at DESC"
             )) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("position_title"),
                    rs.getString("company_name"),
                    rs.getString("resume_title"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载申请记录失败：" + e.getMessage());
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