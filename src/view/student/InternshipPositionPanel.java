package view.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import config.DatabaseConfig;

public class InternshipPositionPanel extends JPanel {
    private final int currentUserId;
    private DefaultTableModel tableModel;
    private JTable table;

    public InternshipPositionPanel(int userId) {
        this.currentUserId = userId;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        initializeUI();
    }

    private void initializeUI() {
        // 创建主面板
        JPanel positionPanel = new JPanel(new BorderLayout());
        positionPanel.setBackground(Color.WHITE);
        
        // 创建标题面板
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("实习岗位列表");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // 创建刷新按钮
        JButton refreshButton = createStyledButton("刷新", new Color(25, 118, 210));
        headerPanel.add(refreshButton, BorderLayout.EAST);

        // 创建表格
        String[] columnNames = {"岗位ID", "岗位名称", "公司名称", "要求", "名额", "状态"};
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
        table.getColumnModel().getColumn(3).setPreferredWidth(300); // 要求列宽一些
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // 加载数据
        loadPositionData();

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton applyButton = createStyledButton("申请岗位", new Color(25, 118, 210));
        JButton viewDetailButton = createStyledButton("查看详情", new Color(108, 117, 125));
        
        // 刷新按钮事件
        refreshButton.addActionListener(e -> loadPositionData());

        // 申请按钮事件
        applyButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int positionId = (Integer) tableModel.getValueAt(selectedRow, 0);
                applyPosition(positionId);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要申请的岗位");
            }
        });

        // 查看详情按钮事件
        viewDetailButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int positionId = (Integer) tableModel.getValueAt(selectedRow, 0);
                viewPositionDetail(positionId);
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要查看的岗位");
            }
        });

        buttonPanel.add(viewDetailButton);
        buttonPanel.add(applyButton);

        positionPanel.add(headerPanel, BorderLayout.NORTH);
        positionPanel.add(scrollPane, BorderLayout.CENTER);
        positionPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(positionPanel);
    }

    private void loadPositionData() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.id, p.title, c.company_name, p.requirements, p.quota, p.status " +
                 "FROM internship_positions p " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "WHERE p.status = '招聘中' " +
                 "ORDER BY p.created_at DESC")) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("company_name"),
                    rs.getString("requirements"),
                    rs.getInt("quota"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取岗位列表失败：" + e.getMessage());
        }
    }

    private void viewPositionDetail(int positionId) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT p.*, c.company_name, c.description as company_description " +
                 "FROM internship_positions p " +
                 "JOIN company_info c ON p.company_id = c.id " +
                 "WHERE p.id = ?")) {
            stmt.setInt(1, positionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JDialog dialog = new JDialog((JFrame)SwingUtilities.getWindowAncestor(this), "岗位详情", true);
                dialog.setLayout(new BorderLayout());
                dialog.setSize(600, 500);
                dialog.setLocationRelativeTo(this);

                JPanel detailPanel = new JPanel();
                detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
                detailPanel.setBackground(Color.WHITE);
                detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                // 添加详细信息
                addDetailField(detailPanel, "岗位名称:", rs.getString("title"));
                addDetailField(detailPanel, "公司名称:", rs.getString("company_name"));
                addDetailField(detailPanel, "公司简介:", rs.getString("company_description"));
                addDetailField(detailPanel, "岗位描述:", rs.getString("description"));
                addDetailField(detailPanel, "岗位要求:", rs.getString("requirements"));
                addDetailField(detailPanel, "招聘名额:", String.valueOf(rs.getInt("quota")));
                addDetailField(detailPanel, "岗位状态:", rs.getString("status"));

                JScrollPane scrollPane = new JScrollPane(detailPanel);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                // 添加关闭按钮
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.setBackground(Color.WHITE);
                JButton closeButton = createStyledButton("关闭", new Color(108, 117, 125));
                closeButton.addActionListener(e -> dialog.dispose());
                buttonPanel.add(closeButton);

                dialog.add(scrollPane, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                dialog.setVisible(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "获取岗位详情失败：" + e.getMessage());
        }
    }

    private void addDetailField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("微软雅黑", Font.BOLD, 14));
        fieldPanel.add(labelComponent, BorderLayout.NORTH);

        JTextArea valueArea = new JTextArea(value);
        valueArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueArea.setLineWrap(true);
        valueArea.setWrapStyleWord(true);
        valueArea.setEditable(false);
        valueArea.setBackground(Color.WHITE);
        valueArea.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        fieldPanel.add(valueArea, BorderLayout.CENTER);

        panel.add(fieldPanel);
    }

    private void applyPosition(int positionId) {
        // 检查是否已经申请过该岗位
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id FROM internship_applications " +
                 "WHERE student_id = ? AND position_id = ?")) {
            stmt.setInt(1, currentUserId);
            stmt.setInt(2, positionId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "您已经申请过该岗位了");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "检查申请记录失败：" + e.getMessage());
            return;
        }

        // 获取学生的简历列表
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id, title FROM resumes WHERE student_id = ? AND status = '待投递'")) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            
            Map<String, Integer> resumeMap = new HashMap<>();
            while (rs.next()) {
                resumeMap.put(rs.getString("title"), rs.getInt("id"));
            }

            if (resumeMap.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先创建简历后再申请岗位");
                return;
            }

            String[] resumeTitles = resumeMap.keySet().toArray(new String[0]);
            String selectedTitle = (String) JOptionPane.showInputDialog(
                this,
                "请选择要投递的简历：",
                "选择简历",
                JOptionPane.QUESTION_MESSAGE,
                null,
                resumeTitles,
                resumeTitles[0]
            );

            if (selectedTitle != null) {
                int resumeId = resumeMap.get(selectedTitle);
                
                // 提交申请
                try (PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO internship_applications (student_id, position_id, resume_id, status) " +
                    "VALUES (?, ?, ?, '待审核')")) {
                    insertStmt.setInt(1, currentUserId);
                    insertStmt.setInt(2, positionId);
                    insertStmt.setInt(3, resumeId);
                    insertStmt.executeUpdate();
                    
                    // 更新简历状态
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE resumes SET status = '已投递' WHERE id = ?")) {
                        updateStmt.setInt(1, resumeId);
                        updateStmt.executeUpdate();
                    }
                    
                    JOptionPane.showMessageDialog(this, "申请提交成功！");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "申请岗位失败：" + e.getMessage());
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