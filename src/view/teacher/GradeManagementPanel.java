package view.teacher;

import config.DatabaseConfig;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 成绩管理模块面板
 */
public class GradeManagementPanel extends JPanel {
    private final JFrame parentFrame;
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public GradeManagementPanel(JFrame parent, int userId) {
        this.parentFrame = parent;
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel gradesPanel = new JPanel(new BorderLayout());
        gradesPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("成绩管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        JButton addButton = createStyledButton("添加评分", new Color(25, 118, 210));
        actionPanel.add(addButton);
        headerPanel.add(actionPanel, BorderLayout.EAST);

        // 创建表格
        String[] columnNames = {"学生姓名", "实习企业", "实习岗位", "成绩", "评语"};
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
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadGradeData();

        // 添加按钮事件
        addButton.addActionListener(e -> showAddScoreDialog());

        gradesPanel.add(headerPanel, BorderLayout.NORTH);
        gradesPanel.add(scrollPane, BorderLayout.CENTER);

        add(gradesPanel, BorderLayout.CENTER);
    }

    private void loadGradeData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT u.name, c.company_name, p.title as position_title, " +
                 "s.score, s.comments " +
                 "FROM internship_scores s " +
                 "JOIN internship_applications a ON s.application_id = a.id " +
                 "JOIN users u ON a.student_id = u.id " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "JOIN student_info si ON a.student_id = si.user_id " +
                 "WHERE si.teacher_id = ? AND s.scorer_id = ?")) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("company_name"),
                    rs.getString("position_title"),
                    rs.getDouble("score"),
                    rs.getString("comments")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取成绩信息失败：" + e.getMessage());
        }
    }

    private void showAddScoreDialog() {
        JDialog dialog = new JDialog(parentFrame, "添加评分", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 加载未评分的实习申请
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        Map<String, Integer> applicationIds = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT a.id, u.name, c.company_name, p.title as position_title " +
                 "FROM internship_applications a " +
                 "JOIN users u ON a.student_id = u.id " +
                 "JOIN internship_positions p ON a.position_id = p.id " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "JOIN student_info s ON a.student_id = s.user_id " +
                 "WHERE s.teacher_id = ? AND a.status = 'approved' " +
                 "AND NOT EXISTS (SELECT 1 FROM internship_scores WHERE application_id = a.id AND scorer_id = ?)")) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                String display = rs.getString("name") + " - " + 
                               rs.getString("company_name") + " - " + 
                               rs.getString("position_title");
                model.addElement(display);
                applicationIds.put(display, rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "获取实习申请失败：" + e.getMessage());
            dialog.dispose();
            return;
        }

        if (model.getSize() == 0) {
            JOptionPane.showMessageDialog(this, "没有需要评分的实习申请");
            dialog.dispose();
            return;
        }

        // 创建表单组件
        JComboBox<String> applicationList = new JComboBox<>(model);
        applicationList.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JSpinner scoreSpinner = new JSpinner(new SpinnerNumberModel(80.0, 0.0, 100.0, 0.5));
        scoreSpinner.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        JTextArea commentsArea = new JTextArea(5, 30);
        commentsArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        commentsArea.setLineWrap(true);
        commentsArea.setWrapStyleWord(true);

        // 添加表单组件
        JLabel selectLabel = new JLabel("选择实习申请：");
        selectLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(selectLabel, gbc);
        formPanel.add(applicationList, gbc);

        JLabel scoreLabel = new JLabel("评分（0-100）：");
        scoreLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(scoreLabel, gbc);
        formPanel.add(scoreSpinner, gbc);

        JLabel commentsLabel = new JLabel("评语：");
        commentsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        formPanel.add(commentsLabel, gbc);
        formPanel.add(new JScrollPane(commentsArea), gbc);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton submitButton = createStyledButton("提交", new Color(25, 118, 210));
        JButton cancelButton = createStyledButton("取消", new Color(108, 117, 125));

        submitButton.addActionListener(e -> {
            String selected = (String) applicationList.getSelectedItem();
            if (selected == null) {
                JOptionPane.showMessageDialog(dialog, "请选择实习申请");
                return;
            }
            int applicationId = applicationIds.get(selected);
            double score = (Double) scoreSpinner.getValue();
            String comments = commentsArea.getText();

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO internship_scores (application_id, scorer_id, score, comments) VALUES (?, ?, ?, ?)")) {
                stmt.setInt(1, applicationId);
                stmt.setInt(2, currentUserId);
                stmt.setDouble(3, score);
                stmt.setString(4, comments);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "评分添加成功！");
                dialog.dispose();
                loadGradeData(); // 刷新成绩列表
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "添加评分失败：" + ex.getMessage());
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