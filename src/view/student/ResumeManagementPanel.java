package view.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import config.DatabaseConfig;

public class ResumeManagementPanel extends JPanel {
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public ResumeManagementPanel(int userId) {
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
        
        JLabel titleLabel = new JLabel("个人简历管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton addBtn = createStyledButton("新增简历", new Color(25, 118, 210));
        JButton editBtn = createStyledButton("编辑简历", new Color(108, 117, 125));
        JButton delBtn = createStyledButton("删除简历", new Color(220, 53, 69));
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(delBtn);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 表格
        tableModel = new DefaultTableModel(
            new Object[]{"标题", "文件路径", "内容", "状态", "创建时间"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        JScrollPane scrollPane = new JScrollPane(table);

        // 加载数据
        loadResumeData();

        // 按钮事件
        addBtn.addActionListener(e -> showResumeEditDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String title = (String) tableModel.getValueAt(row, 0);
                showResumeEditDialog(title);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的简历");
            }
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                String title = (String) tableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "确定要删除该简历吗？", "确认删除", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteResumeByTitle(title);
                    loadResumeData();
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要删除的简历");
            }
        });

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadResumeData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT title, file_path, content, status, created_at FROM resumes WHERE student_id = ? ORDER BY created_at DESC"
             )) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("title"),
                    rs.getString("file_path"),
                    rs.getString("content"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载简历失败：" + e.getMessage());
        }
    }

    private void showResumeEditDialog(String title) {
        boolean isEdit = (title != null);
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, isEdit ? "编辑简历" : "新增简历", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("简历标题:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(25);
        panel.add(titleField, gbc);

        // 文件路径
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("文件路径:"), gbc);
        gbc.gridx = 1;
        JTextField fileField = new JTextField(25);
        fileField.setEditable(false);
        JButton chooseBtn = createStyledButton("选择文件", new Color(25, 118, 210));
        chooseBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                fileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.setBackground(Color.WHITE);
        filePanel.add(fileField);
        filePanel.add(chooseBtn);
        panel.add(filePanel, gbc);

        // 内容
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("简历内容:"), gbc);
        gbc.gridx = 1;
        JTextArea contentArea = new JTextArea(6, 25);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        panel.add(contentScroll, gbc);

        // 如果是编辑，加载原数据
        if (isEdit) {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "SELECT title, file_path, content FROM resumes WHERE student_id = ? AND title = ?"
                 )) {
                stmt.setInt(1, currentUserId);
                stmt.setString(2, title);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    titleField.setText(rs.getString("title"));
                    fileField.setText(rs.getString("file_path"));
                    contentArea.setText(rs.getString("content"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 保存按钮
        gbc.gridx = 1; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveBtn = createStyledButton("保存", new Color(25, 118, 210));
        saveBtn.addActionListener(e -> {
            String newTitle = titleField.getText().trim();
            String filePath = fileField.getText().trim();
            String content = contentArea.getText().trim();
            
            if (newTitle.isEmpty() || filePath.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请填写标题并选择文件");
                return;
            }

            try (Connection conn = DatabaseConfig.getConnection()) {
                if (isEdit) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE resumes SET title=?, file_path=?, content=? WHERE student_id=? AND title=?");
                    stmt.setString(1, newTitle);
                    stmt.setString(2, filePath);
                    stmt.setString(3, content);
                    stmt.setInt(4, currentUserId);
                    stmt.setString(5, title);
                    stmt.executeUpdate();
                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO resumes (student_id, title, file_path, content, status) VALUES (?, ?, ?, ?, 'active')");
                    stmt.setInt(1, currentUserId);
                    stmt.setString(2, newTitle);
                    stmt.setString(3, filePath);
                    stmt.setString(4, content);
                    stmt.executeUpdate();
                }
                loadResumeData();
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

    private void deleteResumeByTitle(String title) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM resumes WHERE student_id = ? AND title = ?"
             )) {
            stmt.setInt(1, currentUserId);
            stmt.setString(2, title);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "删除简历失败：" + e.getMessage());
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