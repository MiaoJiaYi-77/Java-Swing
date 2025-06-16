package view.teacher;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 日志批阅模块面板
 */
public class LogReviewPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public LogReviewPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel logsPanel = new JPanel(new BorderLayout());
        logsPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("日志批阅");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"ID", "学生姓名", "日志内容", "日期", "状态"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadLogData();

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton approveButton = createStyledButton("批准", new Color(40, 167, 69));
        JButton rejectButton = createStyledButton("退回", new Color(220, 53, 69));
        
        approveButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                updateLogStatus(selectedRow, "已通过");
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要批准的日志");
            }
        });
        
        rejectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                updateLogStatus(selectedRow, "已拒绝");
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要退回的日志");
            }
        });

        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        logsPanel.add(headerPanel, BorderLayout.NORTH);
        logsPanel.add(scrollPane, BorderLayout.CENTER);
        logsPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(logsPanel, BorderLayout.CENTER);
    }

    private void loadLogData() {
        tableModel.setRowCount(0);
        System.out.println("正在加载日志数据，教师ID: " + currentUserId);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT l.id, u.name, l.content, l.created_at, l.status " +
                 "FROM internship_logs l " +
                 "JOIN users u ON l.student_id = u.id " +
                 "JOIN student_info s ON l.student_id = s.user_id " +
                 "WHERE s.teacher_id = ? " +
                 "ORDER BY l.created_at DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at"),
                    rs.getString("status")
                });
            }
            System.out.println("加载到 " + count + " 条日志记录");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("加载日志数据失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "获取日志失败：" + e.getMessage());
        }
    }

    private void updateLogStatus(int selectedRow, String status) {
        int logId = (Integer) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE internship_logs SET status = ? WHERE id = ?")) {
            stmt.setString(1, status);
            stmt.setInt(2, logId);
            stmt.executeUpdate();
            tableModel.setValueAt(status, selectedRow, 4);
            JOptionPane.showMessageDialog(this, "日志状态更新成功！");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "更新日志状态失败：" + e.getMessage());
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