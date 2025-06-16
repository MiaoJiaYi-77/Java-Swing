package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class LogManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;

    public LogManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建标题和搜索面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("实习日志管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JTextField searchField = new JTextField(20);
        JComboBox<String> statusFilter = new JComboBox<>(new String[]{"全部", "待审核", "已通过", "已拒绝"});
        JButton searchButton = createStyledButton("搜索", new Color(0, 123, 255));
        searchPanel.add(new JLabel("学生姓名："));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("状态："));
        searchPanel.add(statusFilter);
        searchPanel.add(searchButton);
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        // 创建表格
        String[] columnNames = {"学生姓名", "日志内容", "提交日期", "状态"};
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

        // 加载日志数据
        loadLogData("", "全部");

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            String status = (String) statusFilter.getSelectedItem();
            loadLogData(keyword, status);
        });

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createStyledButton("查看详情", new Color(23, 162, 184));
        JButton approveButton = createStyledButton("通过", new Color(40, 167, 69));
        JButton rejectButton = createStyledButton("拒绝", new Color(220, 53, 69));
        
        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showLogDetail(selectedRow);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的日志");
            }
        });

        approveButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                updateLogStatus(selectedRow, "已通过");
                loadLogData("", "全部");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要审核的日志");
            }
        });

        rejectButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                updateLogStatus(selectedRow, "已拒绝");
                loadLogData("", "全部");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要审核的日志");
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(approveButton);
        buttonPanel.add(rejectButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadLogData(String keyword, String status) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT l.*, u.name as student_name " +
                        "FROM internship_logs l " +
                        "JOIN users u ON l.student_id = u.id " +
                        "WHERE 1=1";
            if (!keyword.isEmpty()) {
                sql += " AND u.name LIKE ?";
            }
            if (!"全部".equals(status)) {
                sql += " AND l.status = ? ";
            }
            sql += " ORDER BY l.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            if (!keyword.isEmpty()) {
                stmt.setString(paramIndex++, "%" + keyword + "%");
            }
            if (!"全部".equals(status)) {
                stmt.setString(paramIndex, status);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("student_name"),
                    rs.getString("content"),
                    rs.getDate("date"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载日志数据失败：" + e.getMessage());
        }
    }

    private void showLogDetail(int selectedRow) {
        String studentName = (String) model.getValueAt(selectedRow, 0);
        String content = (String) model.getValueAt(selectedRow, 1);
        String date = model.getValueAt(selectedRow, 2).toString();
        String status = (String) model.getValueAt(selectedRow, 3);

        JDialog dialog = new JDialog(parentFrame, "日志详情", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parentFrame);
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        // 添加日志信息
        addDetailItem(panel, "学生姓名：", studentName);
        addDetailItem(panel, "提交日期：", date);
        addDetailItem(panel, "状态：", status);
        addDetailItem(panel, "日志内容：", content);

        JButton closeButton = createStyledButton("关闭", new Color(108, 117, 125));
        closeButton.addActionListener(e -> dialog.dispose());
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalStrut(20));
        panel.add(closeButton);

        dialog.add(new JScrollPane(panel));
        dialog.setVisible(true);
    }

    private void updateLogStatus(int selectedRow, String status) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            String studentName = (String) model.getValueAt(selectedRow, 0);
            String date = model.getValueAt(selectedRow, 2).toString();
            
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE internship_logs SET status = ? " +
                "WHERE student_id = (SELECT id FROM users WHERE name = ?) " +
                "AND date = ?"
            );
            stmt.setString(1, status);
            stmt.setString(2, studentName);
            stmt.setString(3, date);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(parentFrame, "日志状态更新成功！");
            } else {
                JOptionPane.showMessageDialog(parentFrame, "日志状态更新失败，请重试。");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "更新日志状态失败：" + e.getMessage());
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