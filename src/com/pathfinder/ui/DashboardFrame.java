package com.pathfinder.ui;

import com.pathfinder.model.*;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class DashboardFrame extends JFrame {

    private User user;
    private JPanel mainContent;
    private CardLayout cardLayout;
    private JButton[] navBtns;
    private String[] navItems = {"🏠  Home", "👤  Profile", "🎯  Careers", "🗺  Roadmap", "💬  AI Chat"};
    private String[] panelKeys = {"home", "profile", "careers", "roadmap", "chat"};

    // Panel instances
    private HomePanel homePanel;
    private ProfilePanel profilePanel;
    private CareerPanel careerPanel;
    private RoadmapPanel roadmapPanel;
    private ChatPanel chatPanel;

    public DashboardFrame(User user) {
        this.user = user;

        setTitle("Pathfinder — " + user.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 700));
        setBackground(Theme.BG);

        initPanels();
        setContentPane(buildLayout());
    }

    private void initPanels() {
        homePanel    = new HomePanel(user, this);
        profilePanel = new ProfilePanel(user, this);
        careerPanel  = new CareerPanel(user);
        roadmapPanel = new RoadmapPanel(user);
        chatPanel    = new ChatPanel(user);
    }

    private JPanel buildLayout() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(Theme.BG);

        // ── Sidebar ───────────────────────────────────────────
        JPanel sidebar = buildSidebar();

        // ── Main content area ─────────────────────────────────
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(Theme.BG);
        mainContent.add(homePanel,    panelKeys[0]);
        mainContent.add(profilePanel, panelKeys[1]);
        mainContent.add(careerPanel,  panelKeys[2]);
        mainContent.add(roadmapPanel, panelKeys[3]);
        mainContent.add(chatPanel,    panelKeys[4]);

        root.add(sidebar,     BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG2);
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Right border
                g2.setColor(Theme.BORDER);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoPanel.setOpaque(false);
        JLabel logoLbl = new JLabel("◈ Pathfinder");
        logoLbl.setFont(Theme.display(18, Font.BOLD));
        logoLbl.setForeground(Theme.TEAL);
        logoPanel.add(logoLbl);
        sidebar.add(logoPanel);

        // Divider
        sidebar.add(makeDivider());
        sidebar.add(Box.createRigidArea(new Dimension(0, 60))); // Increased to align Home tightly with the Good Morning message

        // Nav items
        navBtns = new JButton[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            final int idx = i;
            navBtns[i] = buildNavBtn(navItems[i], i == 0);
            navBtns[i].addActionListener(e -> switchPanel(idx));
            sidebar.add(navBtns[i]);
            if (i < navItems.length - 1) {
                sidebar.add(Box.createRigidArea(new Dimension(0, 8))); // Equal distances between them
            }
        }
        
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(makeDivider());

        // User info at bottom
        JPanel userBox = buildUserBox();
        sidebar.add(userBox);

        return sidebar;
    }

    private JButton buildNavBtn(String fullText, boolean active) {
        String[] parts = fullText.split("  ");
        String emoji = parts[0];
        String text = parts[1];

        JButton btn = new JButton() {
            boolean isActive = active;
            boolean isHover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { isHover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { isHover = false; repaint(); }
                });
            }
            @Override
            public void setBackground(Color c) {
                isActive = (c == Theme.TEAL);
                repaint();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                if (isActive) {
                    g2.setColor(new Color(0,212,170,15));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, Theme.R8, Theme.R8);
                    GradientPaint gp = new GradientPaint(0, 0, Theme.TEAL, 0, getHeight(), Theme.BLUE);
                    g2.setPaint(gp);
                    g2.fillRoundRect(8, 6, 3, getHeight()-12, 2, 2);
                } else if (isHover) {
                    g2.setColor(new Color(255,255,255,5));
                    g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, Theme.R8, Theme.R8);
                }

                g2.setFont(Theme.body(14, Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();

                g2.setColor(isActive ? Theme.TXT : Theme.TXT2);
                g2.drawString(emoji, 40, textY);
                g2.drawString(text, 74, textY);

                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setMinimumSize(new Dimension(240, 48));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel buildUserBox() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(16, 16, 20, 16));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Group avatar and info closely
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Avatar (Green Round)
        JLabel avatar = new JLabel(String.valueOf(user.getName().charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h);
                int x = (w - size) / 2;
                int y = (h - size) / 2;
                g2.setColor(Theme.GREEN);
                g2.fillOval(x, y, size, size);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);
        avatar.setFont(Theme.display(18, Font.BOLD));
        avatar.setForeground(Theme.BG);
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        JLabel nameLbl = new JLabel(user.getName());
        nameLbl.setFont(Theme.body(13, Font.BOLD));
        nameLbl.setForeground(Theme.TXT);
        JLabel roleLbl = new JLabel(capitalize(user.getRole()));
        roleLbl.setFont(Theme.body(11));
        roleLbl.setForeground(user.getRole().equals("professional") ? Theme.AMBER : Theme.TEAL);
        info.add(nameLbl);
        info.add(roleLbl);

        userInfoPanel.add(avatar);
        userInfoPanel.add(info);

        JButton logoutBtn = new JButton("Logout ↩") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69)); // Bright Red
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoutBtn.setFont(Theme.body(12, Font.BOLD));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        logoutBtn.setOpaque(false);
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        logoutBtn.addActionListener(e -> {
            Session.getInstance().logout();
            new SplashScreen().setVisible(true);
            dispose();
        });

        box.add(userInfoPanel);
        box.add(Box.createRigidArea(new Dimension(0, 16)));
        box.add(logoutBtn);

        return box;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER);
        sep.setBackground(Theme.BORDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    public void switchPanel(int idx) {
        for (int i = 0; i < navBtns.length; i++) {
            boolean active = (i == idx);
            navBtns[i].setForeground(active ? Theme.TXT : Theme.TXT2);
            navBtns[i].setFont(Theme.body(14, Font.BOLD));
            navBtns[i].setBackground(active ? Theme.TEAL : null);
        }
        cardLayout.show(mainContent, panelKeys[idx]);
    }

    public void navigateTo(String panel) {
        for (int i = 0; i < panelKeys.length; i++) {
            if (panelKeys[i].equals(panel)) { switchPanel(i); break; }
        }
    }

    public CareerPanel getCareerPanel() { return careerPanel; }
    public RoadmapPanel getRoadmapPanel() { return roadmapPanel; }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
