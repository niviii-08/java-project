package com.pathfinder.ui;

import com.pathfinder.model.User;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;

public class HomePanel extends JPanel {

    private User user;
    private DashboardFrame dashboard;

    public HomePanel(User user, DashboardFrame dashboard) {
        this.user = user;
        this.dashboard = dashboard;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        JScrollPane scroll = new JScrollPane(buildContent());
        scroll.setBorder(null);
        scroll.setBackground(Theme.BG);
        scroll.getViewport().setBackground(Theme.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildContent() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BG);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        // Greeting
        String greeting = getGreeting();
        JLabel greetLbl = new JLabel(greeting + ", " + user.getName() + "! 👋");
        greetLbl.setFont(Theme.display(32, Font.BOLD));
        greetLbl.setForeground(Theme.TXT);
        greetLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subGreet = new JLabel(getSubGreeting());
        subGreet.setFont(Theme.body(16));
        subGreet.setForeground(Theme.TXT2);
        subGreet.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(greetLbl);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subGreet);
        panel.add(Box.createVerticalStrut(40));

        // Quick start card
        JPanel quickCard = buildQuickStart();
        quickCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(quickCard);
        panel.add(Box.createVerticalStrut(32));

        // Feature cards row
        JLabel featLabel = new JLabel("Explore Features");
        featLabel.setFont(Theme.display(20, Font.BOLD));
        featLabel.setForeground(Theme.TXT);
        featLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(featLabel);
        panel.add(Box.createVerticalStrut(16));

        JPanel featureGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        featureGrid.setOpaque(false);
        featureGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        featureGrid.setMaximumSize(new Dimension(900, 300));

        boolean isPro = user.getRole().equals("professional");
        featureGrid.add(featureCard("👤", "Build Profile",
            isPro ? "Add your domain, skills & experience" : "Share your academic background",
            Theme.BLUE, 1));
        featureGrid.add(featureCard("🎯", "Career Matches",
            "Get AI-powered career suggestions tailored to you",
            Theme.TEAL, 2));
        featureGrid.add(featureCard("🗺", "Roadmap",
            "Step-by-step plan to reach your career goal",
            Theme.PURPLE, 3));
        featureGrid.add(featureCard("💬", "AI Chat",
            "Ask anything about careers, exams, and growth",
            Theme.AMBER, 4));

        panel.add(featureGrid);
        panel.add(Box.createVerticalStrut(32));

        // Tip of the day
        JPanel tipCard = buildTipCard();
        tipCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tipCard);

        return panel;
    }

    private JPanel buildQuickStart() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(0,212,170,25),
                    getWidth(), getHeight(), new Color(155,109,255,25));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                g2.setColor(new Color(0,212,170,60));
                g2.setStroke(new java.awt.BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(20, 0));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel title = new JLabel("🚀 Ready to find your path?");
        title.setFont(Theme.display(18, Font.BOLD));
        title.setForeground(Theme.TXT);
        JLabel sub = new JLabel("Complete your profile to get personalised career guidance from AI.");
        sub.setFont(Theme.body(13));
        sub.setForeground(Theme.TXT2);
        left.add(title);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        GradientButton startBtn = new GradientButton("Start →");
        startBtn.setPreferredSize(new Dimension(140, 44));
        startBtn.addActionListener(e -> dashboard.navigateTo("profile"));

        card.add(left, BorderLayout.CENTER);
        card.add(startBtn, BorderLayout.EAST);
        return card;
    }

    private JPanel featureCard(String icon, String title, String desc, Color accent, int navIdx) {
        JPanel card = new JPanel() {
            boolean hover = false;
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { dashboard.switchPanel(navIdx); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? Theme.CARD2 : Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                if (hover) {
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 15));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                }
                g2.setColor(hover ? accent : Theme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(hover ? 1.5f : 1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel iconLbl = new JLabel(icon);
        iconLbl.setFont(Theme.body(28));
        iconLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.display(15, Font.BOLD));
        titleLbl.setForeground(Theme.TXT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><body style='width:150px'>" + desc + "</body></html>");
        descLbl.setFont(Theme.body(12));
        descLbl.setForeground(Theme.TXT2);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(iconLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(6));
        card.add(descLbl);
        return card;
    }

    private JPanel buildTipCard() {
        String[] tips = {
            "💡 Tip: Update your skills regularly — the job market evolves fast!",
            "📚 Tip: Students in Class 10 should explore all three streams before choosing.",
            "🎯 Tip: GATE, CAT, UPSC — start preparation at least 1 year in advance.",
            "🌐 Tip: Build an online portfolio on GitHub or Behance to stand out.",
            "🤝 Tip: Networking on LinkedIn can open doors that applications can't.",
        };
        String tip = tips[(int)(Math.random() * tips.length)];

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x4F, 0x8E, 0xF7, 15));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R12, Theme.R12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel tipLbl = new JLabel(tip);
        tipLbl.setFont(Theme.body(13));
        tipLbl.setForeground(Theme.TXT2);
        card.add(tipLbl);
        return card;
    }

    private String getGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Good morning";
        if (hour < 17) return "Good afternoon";
        return "Good evening";
    }

    private String getSubGreeting() {
        return user.getRole().equals("student")
            ? "Let's explore the right career path for your academic journey."
            : "Discover opportunities and roadmaps to advance your career.";
    }
}
