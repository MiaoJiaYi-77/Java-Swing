package view.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import javax.swing.SpinnerDateModel;
import config.DatabaseConfig;

/**
 * 日志管理模块面板
 */
public class LogManagementPanel extends JPanel {
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public LogManagementPanel(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("日志管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton addBtn = createStyledButton("新增日志", new Color(25, 118, 210));
        JButton editBtn = createStyledButton("编辑日志", new Color(108, 117, 125));
        JButton delBtn = createStyledButton("删除日志", new Color(220, 53, 69));
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 表格
        tableModel = new DefaultTableModel(
            new Object[]{"日期", "内容", "状态", "创建时间"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(28);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(table);

        // 加载数据
        loadLogData();

        // 按钮事件
        addBtn.addActionListener(e -> showLogEditDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Object dateObj = tableModel.getValueAt(row, 0);
                java.sql.Date dateValue;
                if (dateObj instanceof java.sql.Date) {
                    dateValue = (java.sql.Date) dateObj;
                } else if (dateObj instanceof java.util.Date) {
                    dateValue = new java.sql.Date(((java.util.Date) dateObj).getTime());
                } else {
                    dateValue = java.sql.Date.valueOf(dateObj.toString());
                }
                showLogEditDialog(dateValue);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的日志");
            }
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Object dateObj = tableModel.getValueAt(row, 0);
                java.sql.Date dateValue;
                if (dateObj instanceof java.sql.Date) {
                    dateValue = (java.sql.Date) dateObj;
                } else if (dateObj instanceof java.util.Date) {
                    dateValue = new java.sql.Date(((java.util.Date) dateObj).getTime());
                } else {
                    dateValue = java.sql.Date.valueOf(dateObj.toString());
                }
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除该日志吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteLogByDate(dateValue);
                    loadLogData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要删除的日志");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadLogData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT date, content, status, created_at FROM internship_logs WHERE student_id = ? ORDER BY date DESC"
             )) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getDate("date"),
                    rs.getString("content"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载日志失败：" + e.getMessage());
        }
    }

    private void showLogEditDialog(java.sql.Date date) {
        boolean isEdit = (date != null);
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, isEdit ? "编辑日志" : "新增日志", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 日期
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("日期:"), gbc);
        gbc.gridx = 1;
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        // 内容
        JTextArea contentArea = new JTextArea(6, 25);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);

        // 如果是编辑，加载原数据
        if (isEdit && date != null) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT date, content FROM internship_logs WHERE student_id = ? AND date = ?"
                 )) {
                stmt.setInt(1, currentUserId);
                stmt.setDate(2, date);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    java.sql.Date dbDate = rs.getDate("date");
                    if (dbDate != null) {
                        dateSpinner.setValue(new java.util.Date(dbDate.getTime()));
                    }
                    contentArea.setText(rs.getString("content"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        panel.add(dateSpinner, gbc);

        // 内容
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("内容:"), gbc);
        gbc.gridx = 1;
        panel.add(contentScroll, gbc);

        // 保存按钮
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = createStyledButton("保存", new Color(25, 118, 210));
        saveBtn.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            String content = contentArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写内容");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection()) {
                if (isEdit) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE internship_logs SET date=?, content=? WHERE student_id=? AND date=?");
                    stmt.setDate(1, new java.sql.Date(selectedDate.getTime()));
                    stmt.setString(2, content);
                    stmt.setInt(3, currentUserId);
                    stmt.setDate(4, date);
                    stmt.executeUpdate();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO internship_logs (student_id, date, content, status) VALUES (?, ?, ?, '已提交')");
                    stmt.setInt(1, currentUserId);
                    stmt.setDate(2, new java.sql.Date(selectedDate.getTime()));
                    stmt.setString(3, content);
                    stmt.executeUpdate();
                }
                loadLogData();
                dialog.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "保存失败：" + ex.getMessage());
            }
        });
        panel.add(saveBtn, gbc);
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void deleteLogByDate(java.sql.Date date) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM internship_logs WHERE student_id = ? AND date = ?"
             )) {
            stmt.setInt(1, currentUserId);
            stmt.setDate(2, date);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除日志失败：" + e.getMessage());
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