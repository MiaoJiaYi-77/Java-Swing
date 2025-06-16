package view.teacher;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 讨论回复模块面板
 */
public class DiscussionReplyPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    private int currentUserId;
    
    public DiscussionReplyPanel(JFrame parentFrame, int currentUserId) {
        this.parentFrame = parentFrame;
        this.currentUserId = currentUserId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("学生讨论");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建表格
        String[] columnNames = {"ID", "学生姓名", "标题", "内容", "发布时间", "回复数"};
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
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);
        table.getColumnModel().getColumn(3).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadDiscussionData();

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton replyButton = createStyledButton("回复", new Color(25, 118, 210));
        JButton viewRepliesButton = createStyledButton("查看回复", new Color(108, 117, 125));
        
        replyButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showReplyDialog(selectedRow);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要回复的讨论");
            }
        });
        
        viewRepliesButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                showRepliesDialog(selectedRow);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择要查看的讨论");
            }
        });

        buttonPanel.add(replyButton);
        buttonPanel.add(viewRepliesButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // 加载讨论数据
    private void loadDiscussionData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.id, u.name as author, p.title, p.content, p.created_at, " +
                 "(SELECT COUNT(*) FROM discussion_replies r WHERE r.post_id = p.id) as reply_count " +
                 "FROM discussion_posts p " +
                 "JOIN users u ON p.user_id = u.id " +
                 "LEFT JOIN student_info s ON p.user_id = s.user_id " +
                 "WHERE s.teacher_id = ? OR p.user_id = ? " +
                 "ORDER BY p.created_at DESC")) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("author"),
                    rs.getString("title"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("reply_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取讨论列表失败：" + e.getMessage());
        }
    }
    
    // 显示回复对话框
    private void showReplyDialog(int selectedRow) {
        JDialog dialog = new JDialog(parentFrame, "回复讨论", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea contentArea = new JTextArea(5, 40);
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton submitButton = createStyledButton("提交", new Color(25, 118, 210));
        JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

        submitButton.addActionListener(e -> {
            String content = contentArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入回复内容");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO discussion_replies (post_id, user_id, content) VALUES (?, ?, ?)")) {
                stmt.setInt(1, (Integer) model.getValueAt(selectedRow, 0));
                stmt.setInt(2, currentUserId);
                stmt.setString(3, content);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "回复成功！");
                dialog.dispose();
                loadDiscussionData(); // 刷新讨论列表
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "回复失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        formPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(formPanel);
        dialog.setVisible(true);
    }
    
    // 显示回复列表对话框
    private void showRepliesDialog(int selectedRow) {
        JDialog dialog = new JDialog(parentFrame, "查看回复", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(parentFrame);

        // 创建表格
        String[] columnNames = {"回复者", "回复内容", "回复时间"};
        DefaultTableModel replyModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable replyTable = new JTable(replyModel);
        replyTable.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        replyTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        replyTable.setRowHeight(30);
        replyTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        JScrollPane scrollPane = new JScrollPane(replyTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name as author, r.content, r.created_at " +
                 "FROM discussion_replies r " +
                 "JOIN users u ON r.user_id = u.id " +
                 "WHERE r.post_id = ? " +
                 "ORDER BY r.created_at")) {
            stmt.setInt(1, (Integer) model.getValueAt(selectedRow, 0));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                replyModel.addRow(new Object[]{
                    rs.getString("author"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "获取回复列表失败：" + e.getMessage());
        }

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton closeButton = createStyledButton("关闭", new Color(108, 117, 125));
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
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