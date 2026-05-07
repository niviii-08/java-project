package com.pathfinder.ui;

import com.pathfinder.util.Theme;
import com.pathfinder.db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class SplashScreen extends JFrame {

    private float alpha = 0f;
    private Timer fadeTimer;
    private Timer particleTimer;
    private float[] particleX = new float[40];
    private float[] particleY = new float[40];
    private float[] particleSpeed = new float[40];
    private float[] particleAlpha = new float[40];

    public SplashScreen() {
        setTitle("Pathfinder — Career Guidance");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setUndecorated(true);
        // Remove rounded window for Linux compatibility; keep for Mac/Win
        try { setShape(new RoundRectangle2D.Double(0, 0, 1100, 680, 24, 24)); } catch (Exception ignored) {}

        initParticles();
        setContentPane(buildContent());
        startFadeIn();
    }

    private void initParticles() {
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] = (float)(Math.random() * 1100);
            particleY[i] = (float)(Math.random() * 680);
            particleSpeed[i] = (float)(0.2 + Math.random() * 0.5);
            particleAlpha[i] = (float)(0.1 + Math.random() * 0.3);
        }
    }

    private JPanel buildContent() {
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Background
                g2.setColor(Theme.BG);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Glow 1 - teal top-left
                RadialGradientPaint rgp1 = new RadialGradientPaint(
                    new Point2D.Float(-50, -50), 600,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0,212,170,30), new Color(0,0,0,0)}
                );
                g2.setPaint(rgp1);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Glow 2 - purple bottom-right
                RadialGradientPaint rgp2 = new RadialGradientPaint(
                    new Point2D.Float(getWidth()+50, getHeight()+50), 500,
                    new float[]{0f, 1f},
                    new Color[]{new Color(155,109,255,25), new Color(0,0,0,0)}
                );
                g2.setPaint(rgp2);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Particles
                for (int i = 0; i < particleX.length; i++) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, particleAlpha[i] * alpha));
                    g2.setColor(i % 2 == 0 ? Theme.TEAL : Theme.PURPLE);
                    g2.fillOval((int)particleX[i], (int)particleY[i], 2, 2);
                }

                // Grid lines
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.03f * alpha));
                g2.setColor(Theme.TXT);
                g2.setStroke(new BasicStroke(0.5f));
                for (int x = 0; x < getWidth(); x += 60) g2.drawLine(x, 0, x, getHeight());
                for (int y = 0; y < getHeight(); y += 60) g2.drawLine(0, y, getWidth(), y);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout());
        root.setOpaque(false);

        // ── Center content ─────────────────────────────────────
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(60, 80, 100, 80));

        // Logo icon
        JLabel logoIcon = new JLabel("◈") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.TEAL, getWidth(), getHeight(), Theme.PURPLE);
                g2.setPaint(gp);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 0, fm.getAscent());
                g2.dispose();
            }
        };
        logoIcon.setFont(Theme.display(48, Font.BOLD));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        // App name
        JLabel appName = new JLabel("PATHFINDER");
        appName.setFont(Theme.display(52, Font.BOLD));
        appName.setForeground(Theme.TXT);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tagline
        JLabel tagline = new JLabel("AI-Powered Career Guidance for India");
        tagline.setFont(Theme.body(17));
        tagline.setForeground(Theme.TXT2);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Badge
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,212,170,20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0,212,170,50));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setMaximumSize(new Dimension(320, 38));
        JLabel dot = new JLabel("●");
        dot.setForeground(Theme.TEAL);
        dot.setFont(Theme.body(8));
        JLabel badgeTxt = new JLabel("Powered by AI — OpenRouter + Mistral 7B");
        badgeTxt.setFont(Theme.body(11, Font.BOLD));
        badgeTxt.setForeground(Theme.TEAL);
        badge.add(dot);
        badge.add(badgeTxt);
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role selection label
        JLabel pickLabel = new JLabel("I am a...");
        pickLabel.setFont(Theme.display(22, Font.BOLD));
        pickLabel.setForeground(Theme.TXT);
        pickLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Role cards row
        JPanel roleRow = new JPanel(new GridLayout(1, 2, 20, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(700, 160));
        roleRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleRow.add(buildRoleCard("🎓", "Student", "Class 6–12 & College", Theme.TEAL, "student"));
        roleRow.add(buildRoleCard("💼", "Professional", "Working & Transitioning", Theme.AMBER, "professional"));

        // Login/Register row
        JPanel authRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        authRow.setOpaque(false);
        authRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        GradientButton loginBtn = new GradientButton("Sign In", Theme.TEAL2, Theme.BLUE);
        loginBtn.setPreferredSize(new Dimension(160, 44));
        loginBtn.addActionListener(e -> openAuth("login", null));

        GradientButton regBtn = new GradientButton("Create Account");
        regBtn.setPreferredSize(new Dimension(160, 44));
        regBtn.addActionListener(e -> openAuth("register", null));

        authRow.add(loginBtn);
        authRow.add(regBtn);

        // Close button top-right
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(Theme.body(14));
        closeBtn.setForeground(Theme.TXT3);
        closeBtn.setBackground(null);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        topBar.setOpaque(false);
        topBar.add(closeBtn);

        center.add(logoIcon);
        center.add(Box.createVerticalStrut(8));
        center.add(appName);
        center.add(Box.createVerticalStrut(8));
        center.add(Box.createVerticalStrut(16));
        center.add(badge);
        center.add(Box.createVerticalStrut(32));
        center.add(pickLabel);
        center.add(Box.createVerticalStrut(16));
        center.add(roleRow);
        center.add(Box.createVerticalStrut(24));
        center.add(authRow);

        root.add(topBar, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);

        // Animate particles
        particleTimer = new Timer(33, e -> {
            for (int i = 0; i < particleY.length; i++) {
                particleY[i] -= particleSpeed[i];
                if (particleY[i] < -5) {
                    particleY[i] = getHeight() + 5;
                    particleX[i] = (float)(Math.random() * getWidth());
                }
            }
            root.repaint();
        });
        particleTimer.start();

        return root;
    }

    private JPanel buildRoleCard(String icon, String title, String sub, Color accent, String role) {
        JPanel card = new JPanel() {
            boolean hover = false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { openAuth("register", role); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = hover ? Theme.CARD2 : Theme.CARD;
                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                if (hover) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 20));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                }
                g2.setColor(hover ? accent : Theme.BORDER);
                g2.setStroke(new BasicStroke(hover ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(Theme.body(32));
        iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.display(20, Font.BOLD));
        titleLbl.setForeground(Theme.TXT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(Theme.body(13));
        subLbl.setForeground(Theme.TXT2);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel arrow = new JLabel("→  Get Started");
        arrow.setFont(Theme.body(12, Font.BOLD));
        arrow.setForeground(accent);
        arrow.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(12));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(subLbl);
        card.add(Box.createVerticalStrut(12));
        card.add(arrow);
        return card;
    }

    private void openAuth(String mode, String preselectedRole) {
        if (particleTimer != null) particleTimer.stop();
        if (fadeTimer != null) fadeTimer.stop();
        AuthFrame auth = new AuthFrame(mode, preselectedRole);
        auth.setVisible(true);
        dispose();
    }

    private void startFadeIn() {
        alpha = 0f;
        fadeTimer = new Timer(20, e -> {
            alpha = Math.min(1f, alpha + 0.04f);
            repaint();
            if (alpha >= 1f) ((Timer) e.getSource()).stop();
        });
        fadeTimer.start();
    }
}
