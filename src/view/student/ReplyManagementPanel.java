package view.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import config.DatabaseConfig;

public class ReplyManagementPanel extends JPanel {
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public ReplyManagementPanel(int userId) {
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
        
        JLabel titleLabel = new JLabel("评论管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton addBtn = createStyledButton("新增评论", new Color(25, 118, 210));
        JButton editBtn = createStyledButton("编辑评论", new Color(108, 117, 125));
        JButton delBtn = createStyledButton("删除评论", new Color(220, 53, 69));
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 表格
        tableModel = new DefaultTableModel(
            new Object[]{"评论内容", "所属帖子", "回复时间"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(table);

        // 加载数据
        loadReplyData();

        // 按钮事件
        addBtn.addActionListener(e -> showReplyEditDialog(null, null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String content = (String) tableModel.getValueAt(row, 0);
                String postTitle = (String) tableModel.getValueAt(row, 1);
                showReplyEditDialog(content, postTitle);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的评论");
            }
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String content = (String) tableModel.getValueAt(row, 0);
                String postTitle = (String) tableModel.getValueAt(row, 1);
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除该评论吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteReplyByContentAndPost(content, postTitle);
                    loadReplyData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要删除的评论");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadReplyData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT dr.content, dp.title as post_title, dr.created_at " +
                 "FROM discussion_replies dr " +
                 "JOIN discussion_posts dp ON dr.post_id = dp.id " +
                 "WHERE dr.user_id = ? ORDER BY dr.created_at DESC"
             )) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("content"),
                    rs.getString("post_title"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载评论失败：" + e.getMessage());
        }
    }

    private void showReplyEditDialog(String content, String postTitle) {
        boolean isEdit = (content != null && postTitle != null);
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, isEdit ? "编辑评论" : "新增评论", true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 所属帖子
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("所属帖子:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> postCombo = new JComboBox<>();
        Map<String, Integer> postMap = new HashMap<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, title FROM discussion_posts WHERE user_id = ? OR 1=1 ORDER BY created_at DESC")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String title = rs.getString("title");
                int id = rs.getInt("id");
                postCombo.addItem(title);
                postMap.put(title, id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (isEdit && postTitle != null) {
            postCombo.setSelectedItem(postTitle);
        }
        panel.add(postCombo, gbc);

        // 评论内容
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("评论内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(4, 25);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        panel.add(contentScroll, gbc);
        if (isEdit) {
            contentArea.setText(content);
        }

        // 保存按钮
        gbc.gridx = 1; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = createStyledButton("保存", new Color(25, 118, 210));
        saveBtn.addActionListener(e -> {
            String newContent = contentArea.getText().trim();
            String selectedPost = (String) postCombo.getSelectedItem();
            if (newContent.isEmpty() || selectedPost == null) {
                JOptionPane.showMessageDialog(dialog, "请填写评论内容并选择帖子");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection()) {
                if (isEdit) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE discussion_replies SET content=? WHERE user_id=? AND content=? AND post_id=(SELECT id FROM discussion_posts WHERE title=?)");
                    stmt.setString(1, newContent);
                    stmt.setInt(2, currentUserId);
                    stmt.setString(3, content);
                    stmt.setString(4, postTitle);
                    stmt.executeUpdate();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO discussion_replies (user_id, post_id, content) VALUES (?, ?, ?)");
                    stmt.setInt(1, currentUserId);
                    stmt.setInt(2, postMap.get(selectedPost));
                    stmt.setString(3, newContent);
                    stmt.executeUpdate();
                }
                loadReplyData();
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

    private void deleteReplyByContentAndPost(String content, String postTitle) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM discussion_replies WHERE user_id = ? AND content = ? AND post_id = (SELECT id FROM discussion_posts WHERE title = ?)")
        ) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, content);
            stmt.setString(3, postTitle);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除评论失败：" + e.getMessage());
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