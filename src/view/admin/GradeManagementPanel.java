package view.admin;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GradeManagementPanel extends JPanel {
    private JFrame parentFrame;
    private DefaultTableModel model;
    private JTable table;

    public GradeManagementPanel(JFrame parentFrame) {
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
        
        JLabel titleLabel = new JLabel("成绩管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JTextField searchField = new JTextField(20);
        JButton searchButton = createStyledButton("搜索", new Color(0, 123, 255));
        searchPanel.add(new JLabel("学生姓名："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        headerPanel.add(searchPanel, BorderLayout.CENTER);

        // 创建表格
        String[] columnNames = {"学生姓名", "企业名称", "实习岗位", "成绩", "评语", "评分时间"};
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

        // 加载成绩数据
        loadGradeData("");

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            loadGradeData(keyword);
        });

        // 添加按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        JButton editButton = createStyledButton("编辑成绩", new Color(255, 193, 7));
        buttonPanel.add(editButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadGradeData(String keyword) {
        model.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection()) {
            String sql = "SELECT s.id, u.name as student_name, c.company_name, " +
                        "p.title as position_title, s.score, s.comments, s.created_at " +
                        "FROM internship_scores s " +
                        "JOIN internship_applications a ON s.application_id = a.id " +
                        "JOIN users u ON a.student_id = u.id " +
                        "JOIN internship_positions p ON a.position_id = p.id " +
                        "JOIN company_info c ON p.company_id = c.id " +
                        "WHERE 1=1";
            if (!keyword.isEmpty()) {
                sql += " AND u.name LIKE ?";
            }
            sql += " ORDER BY s.created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (!keyword.isEmpty()) {
                stmt.setString(1, "%" + keyword + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("student_name"),
                    rs.getString("company_name"),
                    rs.getString("position_title"),
                    rs.getDouble("score"),
                    rs.getString("comments"),
                    rs.getTimestamp("created_at")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame, "加载成绩数据失败：" + e.getMessage());
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