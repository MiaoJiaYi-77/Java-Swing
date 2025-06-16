package view.company;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 讨论区模块面板
 */
public class DiscussionPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    private int companyId;
    private int currentUserId;
    
    public DiscussionPanel(JFrame parentFrame, int companyId, int currentUserId) {
        this.parentFrame = parentFrame;
        this.companyId = companyId;
        this.currentUserId = currentUserId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("讨论区");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton newButton = createStyledButton("发布新讨论", new Color(0, 123, 255));
        actionPanel.add(newButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // 创建表格模型
        String[] columnNames = {"标题", "发布者", "发布时间", "回复数"};
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
        loadDiscussionData();

        // 添加按钮事件
        newButton.addActionListener(e -> createNewDiscussion());

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton viewButton = createStyledButton("查看详情", new Color(0, 123, 255));
        JButton replyButton = createStyledButton("回复", new Color(0, 123, 255));

        viewButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String postTitle = (String)table.getValueAt(selectedRow, 0);
                viewDiscussionDetail(postTitle);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择一个讨论");
            }
        });

        replyButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String postTitle = (String)table.getValueAt(selectedRow, 0);
                addReply(postTitle);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择一个讨论");
            }
        });

        buttonPanel.add(viewButton);
        buttonPanel.add(replyButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadDiscussionData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT dp.title, u.name, dp.created_at, " +
                 "(SELECT COUNT(*) FROM discussion_replies dr WHERE dr.post_id = dp.id) as reply_count " +
                 "FROM discussion_posts dp " +
                 "JOIN users u ON dp.user_id = u.id " +
                 "ORDER BY dp.created_at DESC"
             )) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("name"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("reply_count")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载讨论列表失败：" + e.getMessage());
        }
    }
    
    private void viewDiscussionDetail(String postTitle) {
        JDialog dialog = new JDialog(parentFrame, "讨论详情", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(parentFrame);

        // 创建主贴面板
        JPanel postPanel = new JPanel(new BorderLayout());
        postPanel.setBorder(BorderFactory.createTitledBorder("主贴"));

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT dp.*, u.name FROM discussion_posts dp " +
                 "JOIN users u ON dp.user_id = u.id " +
                 "WHERE dp.title = ?"
             )) {
            stmt.setString(1, postTitle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JTextArea contentArea = new JTextArea(rs.getString("content"));
                contentArea.setEditable(false);
                contentArea.setLineWrap(true);
                contentArea.setWrapStyleWord(true);

                JLabel infoLabel = new JLabel(String.format(
                    "发布者: %s | 发布时间: %s",
                    rs.getString("name"),
                    rs.getTimestamp("created_at")
                ));

                postPanel.add(infoLabel, BorderLayout.NORTH);
                postPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "获取讨论详情失败：" + e.getMessage());
        }

        // 创建回复列表面板
        JPanel repliesPanel = new JPanel(new BorderLayout());
        repliesPanel.setBorder(BorderFactory.createTitledBorder("回复列表"));

        DefaultTableModel replyModel = new DefaultTableModel(
            new Object[]{"回复者", "内容", "回复时间"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable replyTable = new JTable(replyModel);
        JScrollPane replyScroll = new JScrollPane(replyTable);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT dr.*, u.name FROM discussion_replies dr " +
                 "JOIN users u ON dr.user_id = u.id " +
                 "WHERE dr.post_id = ? " +
                 "ORDER BY dr.created_at ASC"
             )) {
            stmt.setString(1, postTitle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                replyModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "获取回复列表失败：" + e.getMessage());
        }

        repliesPanel.add(replyScroll, BorderLayout.CENTER);

        // 创建分割面板
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            postPanel,
            repliesPanel
        );
        splitPane.setDividerLocation(200);

        dialog.add(splitPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void addReply(String postTitle) {
        JDialog dialog = new JDialog(parentFrame, "添加回复", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea contentArea = new JTextArea(5, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("提交");
        JButton cancelButton = new JButton("取消");

        submitButton.addActionListener(e -> {
            String content = contentArea.getText().trim();
            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入回复内容");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO discussion_replies (post_id, user_id, content) VALUES (?, ?, ?)"
                 )) {
                stmt.setString(1, postTitle);
                stmt.setInt(2, currentUserId);
                stmt.setString(3, content);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(dialog, "回复成功！");
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "回复失败，请重试。");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "提交回复失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void createNewDiscussion() {
        JDialog dialog = new JDialog(parentFrame, "发布新讨论", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 添加标题输入
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("标题:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(30);
        formPanel.add(titleField, gbc);

        // 添加内容输入
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(10, 30);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        formPanel.add(new JScrollPane(contentArea), gbc);

        // 添加按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("发布");
        JButton cancelButton = new JButton("取消");

        submitButton.addActionListener(e -> {
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty() || content.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "标题和内容不能为空");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO discussion_posts (user_id, title, content) VALUES (?, ?, ?)"
                 )) {
                stmt.setInt(1, currentUserId);
                stmt.setString(2, title);
                stmt.setString(3, content);

                int result = stmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(dialog, "发布成功！");
                    loadDiscussionData();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "发布失败，请重试。");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "发布失败：" + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
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