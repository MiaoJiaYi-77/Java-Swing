package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class BaseMainFrame extends JFrame {
    protected String username;
    protected JPanel mainPanel;
    protected JMenuBar menuBar;

    public BaseMainFrame(String title, String username) {
        this.username = username;
        setTitle(title);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建菜单栏
        createMenuBar();
        
        // 创建主面板
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建欢迎面板
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel welcomeLabel = new JLabel("欢迎, " + username + "!");
        JButton logoutButton = new JButton("退出登录");
        logoutButton.addActionListener(e -> logout());
        
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(logoutButton);
        mainPanel.add(welcomePanel, BorderLayout.NORTH);
        
        // 添加主要内容面板
        createContentPanel();
        
        add(mainPanel);
    }

    protected void createMenuBar() {
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
    }

    protected abstract void createContentPanel();

    protected void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要退出登录吗？",
            "退出确认",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginFrame().setVisible(true);
        }
    }

    protected void addMenu(String menuName, String[] menuItems, ActionListener[] listeners) {
        JMenu menu = new JMenu(menuName);
        for (int i = 0; i < menuItems.length; i++) {
            JMenuItem menuItem = new JMenuItem(menuItems[i]);
            if (listeners != null && i < listeners.length) {
                menuItem.addActionListener(listeners[i]);
            }
            menu.add(menuItem);
        }
        menuBar.add(menu);
    }
} 