package view.company;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * 评分管理模块面板
 */
public class ScoreManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;
    private int companyId;
    private int currentUserId;
    
    public ScoreManagementPanel(JFrame parentFrame, int companyId, int currentUserId) {
        this.parentFrame = parentFrame;
        this.companyId = companyId;
        this.currentUserId = currentUserId;
        
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 创建标题和按钮面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("实习评分管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton addButton = createStyledButton("添加评分", new Color(0, 123, 255));
        actionPanel.add(addButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // 创建表格模型
        String[] columnNames = {"学生姓名", "岗位名称", "开始时间", "评分", "评语"};
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
        loadScoreData();

        // 添加按钮事件
        addButton.addActionListener(e -> addScore());

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton editButton = createStyledButton("修改评分", new Color(0, 123, 255));
        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow >= 0) {
                String positionTitle = (String)table.getValueAt(selectedRow, 1);
                editScore(positionTitle, selectedRow);
            } else {
                JOptionPane.showMessageDialog(parentFrame, "请先选择一条记录");
            }
        });
        buttonPanel.add(editButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadScoreData() {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, p.title, ia.created_at, is2.score, is2.comments " +
                 "FROM internship_scores is2 " +
                 "JOIN internship_applications ia ON is2.application_id = ia.id " +
                 "JOIN internship_positions p ON ia.position_id = p.id " +
                 "JOIN users u ON ia.student_id = u.id " +
                 "WHERE p.company_id = ? " +
                 "ORDER BY ia.created_at DESC"
             )) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("title"),
                    rs.getTimestamp("created_at"),
                    rs.getObject("score") != null ? rs.getDouble("score") : "未评分",
                    rs.getString("comments")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载评分数据失败：" + e.getMessage());
        }
    }
    
    private void addScore() {
        // 创建选择实习生对话框
        JDialog dialog = new JDialog(parentFrame, "添加评分", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 添加实习生选择下拉框
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("选择实习生:"), gbc);
        gbc.gridx = 1;
        JComboBox<ApplicationItem> applicationCombo = new JComboBox<>();
        loadApplications(applicationCombo);
        formPanel.add(applicationCombo, gbc);

        // 添加评分输入
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("评分(0-100):"), gbc);
        gbc.gridx = 1;
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(80.0, 0.0, 100.0, 0.5));
        formPanel.add(scoreSpinner, gbc);

        // 添加评语输入
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("评语:"), gbc);
        gbc.gridx = 1;
        JTextArea commentsArea = new JTextArea(5, 20);
        commentsArea.setLineWrap(true);
        formPanel.add(new JScrollPane(commentsArea), gbc);

        // 添加按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton submitButton = new JButton("提交");
        JButton cancelButton = new JButton("取消");

        submitButton.addActionListener(e -> {
            ApplicationItem selectedItem = (ApplicationItem)applicationCombo.getSelectedItem();
            if (selectedItem != null) {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    conn.setAutoCommit(false); // 开启事务
                    try {
                        // 检查是否已经有评分
                        PreparedStatement checkStmt = conn.prepareStatement(
                            "SELECT COUNT(*) FROM internship_scores WHERE application_id = ?"
                        );
                        checkStmt.setInt(1, selectedItem.getId());
                        ResultSet checkRs = checkStmt.executeQuery();
                        checkRs.next();
                        if (checkRs.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(dialog, "该学生已有评分记录，请使用修改功能");
                            return;
                        }
                        
                        // 添加评分
                        PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO internship_scores (application_id, scorer_id, score, comments) VALUES (?, ?, ?, ?)"
                        );
                        stmt.setInt(1, selectedItem.getId());
                        stmt.setInt(2, currentUserId);
                        stmt.setDouble(3, (Double)scoreSpinner.getValue());
                        stmt.setString(4, commentsArea.getText().trim());

                        int result = stmt.executeUpdate();
                        if (result > 0) {
                            conn.commit(); // 提交事务
                            JOptionPane.showMessageDialog(dialog, "评分添加成功！");
                            loadScoreData();
                            dialog.dispose();
                        } else {
                            conn.rollback(); // 回滚事务
                            JOptionPane.showMessageDialog(dialog, "评分添加失败，请重试。");
                        }
                    } catch (SQLException ex) {
                        conn.rollback(); // 出错时回滚
                        throw ex;
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "添加评分失败：" + ex.getMessage());
                }
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void loadApplications(JComboBox<ApplicationItem> combo) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT ia.id, u.name, ip.title " +
                 "FROM internship_applications ia " +
                 "JOIN internship_positions ip ON ia.position_id = ip.id " +
                 "JOIN resumes r ON ia.resume_id = r.id " +
                 "JOIN users u ON r.student_id = u.id " +
                 "WHERE ip.company_id = ? AND ia.status = 'accepted' " +
                 "AND NOT EXISTS (SELECT 1 FROM internship_scores is2 WHERE is2.application_id = ia.id)"
             )) {
            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                combo.addItem(new ApplicationItem(
                    rs.getInt("id"),
                    rs.getString("name") + " - " + rs.getString("title")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载实习申请数据失败：" + e.getMessage());
        }
    }
    
    private void editScore(String positionTitle, int selectedRow) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // 首先获取application_id
            PreparedStatement findStmt = conn.prepareStatement(
                "SELECT ia.id FROM internship_applications ia " +
                "JOIN internship_positions ip ON ia.position_id = ip.id " +
                "JOIN users u ON ia.student_id = u.id " +
                "WHERE ip.title = ? AND u.name = ?"
            );
            findStmt.setString(1, positionTitle);
            findStmt.setString(2, (String)model.getValueAt(selectedRow, 0)); // 学生姓名
            ResultSet findRs = findStmt.executeQuery();
            
            if (findRs.next()) {
                int applicationId = findRs.getInt("id");
                
                // 然后获取评分信息
                PreparedStatement scoreStmt = conn.prepareStatement(
                    "SELECT * FROM internship_scores WHERE application_id = ?"
                );
                scoreStmt.setInt(1, applicationId);
                ResultSet rs = scoreStmt.executeQuery();

                if (rs.next()) {
                    JDialog dialog = new JDialog(parentFrame, "修改评分", true);
                    dialog.setLayout(new BorderLayout());
                    dialog.setSize(500, 400);
                    dialog.setLocationRelativeTo(parentFrame);

                    JPanel formPanel = new JPanel(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(5, 5, 5, 5);
                    gbc.fill = GridBagConstraints.HORIZONTAL;

                    // 添加评分输入
                    gbc.gridx = 0; gbc.gridy = 0;
                    formPanel.add(new JLabel("评分(0-100):"), gbc);
                    gbc.gridx = 1;
                    JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(
                        rs.getDouble("score"), 0.0, 100.0, 0.5
                    ));
                    formPanel.add(scoreSpinner, gbc);

                    // 添加评语输入
                    gbc.gridx = 0; gbc.gridy = 1;
                    formPanel.add(new JLabel("评语:"), gbc);
                    gbc.gridx = 1;
                    JTextArea commentsArea = new JTextArea(rs.getString("comments"), 5, 20);
                    commentsArea.setLineWrap(true);
                    formPanel.add(new JScrollPane(commentsArea), gbc);

                    // 添加按钮
                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    JButton submitButton = new JButton("保存");
                    JButton cancelButton = new JButton("取消");

                    submitButton.addActionListener(e -> {
                        try (Connection conn2 = DatabaseConfig.getConnection()) {
                            conn2.setAutoCommit(false); // 开启事务
                            try {
                                PreparedStatement updateStmt = conn2.prepareStatement(
                                    "UPDATE internship_scores SET score = ?, comments = ? WHERE application_id = ?"
                                );
                                updateStmt.setDouble(1, (Double)scoreSpinner.getValue());
                                updateStmt.setString(2, commentsArea.getText().trim());
                                updateStmt.setInt(3, applicationId);

                                int result = updateStmt.executeUpdate();
                                if (result > 0) {
                                    conn2.commit(); // 提交事务
                                    JOptionPane.showMessageDialog(dialog, "评分更新成功！");
                                    loadScoreData();
                                    dialog.dispose();
                                } else {
                                    conn2.rollback(); // 回滚事务
                                    JOptionPane.showMessageDialog(dialog, "评分更新失败，请重试。");
                                }
                            } catch (SQLException ex) {
                                conn2.rollback(); // 出错时回滚
                                throw ex;
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(dialog, "更新评分失败：" + ex.getMessage());
                        }
                    });

                    cancelButton.addActionListener(e -> dialog.dispose());

                    buttonPanel.add(submitButton);
                    buttonPanel.add(cancelButton);

                    dialog.add(formPanel, BorderLayout.CENTER);
                    dialog.add(buttonPanel, BorderLayout.SOUTH);
                    dialog.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(parentFrame, "未找到对应的申请记录");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "获取评分信息失败：" + e.getMessage());
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
    
    // 用于实习申请下拉框的辅助类
    private static class ApplicationItem {
        private final int id;
        private final String display;

        public ApplicationItem(int id, String display) {
            this.id = id;
            this.display = display;
        }

        public int getId() {
            return id;
        }

        @Override
        public String toString() {
            return display;
        }
    }
} 