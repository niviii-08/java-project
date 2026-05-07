package com.pathfinder.ui;

import com.pathfinder.api.OpenRouterClient;
import com.pathfinder.db.UserDAO;
import com.pathfinder.model.User;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class RoadmapPanel extends JPanel {

    private User user;
    private JPanel resultsArea;
    private JLabel statusLbl;
    private DarkTextField careerInputField;

    public RoadmapPanel(User user) {
        this.user = user;
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
        JPanel outer = new JPanel();
        outer.setBackground(Theme.BG);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        JLabel title = new JLabel("🗺 Career Roadmap");
        title.setFont(Theme.display(28, Font.BOLD));
        title.setForeground(Theme.TXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Get a step-by-step action plan for your chosen career.");
        sub.setFont(Theme.body(14));
        sub.setForeground(Theme.TXT2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        outer.add(title);
        outer.add(Box.createVerticalStrut(6));
        outer.add(sub);
        outer.add(Box.createVerticalStrut(32));

        // Input row
        JLabel inputLbl = new JLabel("Enter your target career:");
        inputLbl.setFont(Theme.body(14, Font.BOLD));
        inputLbl.setForeground(Theme.TXT);
        inputLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(inputLbl);
        outer.add(Box.createVerticalStrut(8));

        JPanel inputRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        inputRow.setOpaque(false);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        careerInputField = new DarkTextField("e.g. Software Engineer, Doctor, CA, IAS Officer", 30);
        careerInputField.setPreferredSize(new Dimension(400, 44));
        inputRow.add(careerInputField);

        GradientButton genBtn = new GradientButton("Generate Roadmap →");
        genBtn.setPreferredSize(new Dimension(200, 44));
        genBtn.addActionListener(e -> generateRoadmap());
        inputRow.add(genBtn);

        outer.add(inputRow);
        outer.add(Box.createVerticalStrut(16));

        statusLbl = new JLabel(" ");
        statusLbl.setFont(Theme.body(13));
        statusLbl.setForeground(Theme.TXT2);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(statusLbl);
        outer.add(Box.createVerticalStrut(24));

        // Results
        resultsArea = new JPanel();
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        resultsArea.setOpaque(false);
        resultsArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(resultsArea);

        return outer;
    }

    private void generateRoadmap() {
        String career = careerInputField.getText().trim();
        if (career.isEmpty()) {
            statusLbl.setText("Please enter a career name.");
            statusLbl.setForeground(Theme.ROSE);
            return;
        }

        statusLbl.setText("⏳ Generating roadmap for '" + career + "'...");
        statusLbl.setForeground(Theme.AMBER);

        resultsArea.removeAll();
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(Theme.BORDER);
        bar.setForeground(Theme.PURPLE);
        bar.setMaximumSize(new Dimension(400, 6));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsArea.add(bar);
        resultsArea.revalidate();

        String profile = buildProfileString();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return OpenRouterClient.getRoadmap(career, profile);
            }

            @Override
            protected void done() {
                try {
                    String response = get();
                    displayRoadmap(response, career);
                    statusLbl.setText("✓ Roadmap generated!");
                    statusLbl.setForeground(Theme.GREEN);
                    try { UserDAO.saveResult(user.getId(), "roadmap", response); } catch (Exception ignored) {}
                } catch (Exception ex) {
                    statusLbl.setText("Error: " + ex.getMessage());
                    statusLbl.setForeground(Theme.ROSE);
                    resultsArea.removeAll();
                    resultsArea.revalidate();
                }
            }
        };
        worker.execute();
    }

    private void displayRoadmap(String json, String career) {
        resultsArea.removeAll();

        // Career title header
        JPanel headerCard = buildHeaderCard(career);
        headerCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsArea.add(headerCard);
        resultsArea.add(Box.createVerticalStrut(24));

        // Try to parse phases
        try {
            int phaseStart = json.indexOf("\"phases\"");
            int arrStart = phaseStart >= 0 ? json.indexOf("[", phaseStart) : -1;

            if (arrStart >= 0) {
                JLabel phasesTitle = new JLabel("Your Learning Journey");
                phasesTitle.setFont(Theme.display(20, Font.BOLD));
                phasesTitle.setForeground(Theme.TXT);
                phasesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsArea.add(phasesTitle);
                resultsArea.add(Box.createVerticalStrut(16));

                // Extract individual phase objects
                int depth = 0, objStart = -1, phaseIdx = 0;
                for (int i = arrStart; i < json.length(); i++) {
                    char c = json.charAt(i);
                    if (c == '{') {
                        depth++;
                        if (depth == 1) objStart = i;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0 && objStart >= 0) {
                            String phaseJson = json.substring(objStart, i + 1);
                            JPanel phaseCard = buildPhaseCard(phaseJson, phaseIdx);
                            phaseCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                            resultsArea.add(phaseCard);
                            resultsArea.add(Box.createVerticalStrut(16));
                            phaseIdx++;
                        }
                    }
                }
            }

            // Tips section
            String tips = extractArrayText(json, "tips");
            if (tips != null && !tips.isEmpty()) {
                JPanel tipsCard = buildTipsCard(tips);
                tipsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsArea.add(tipsCard);
            }

        } catch (Exception e) {
            // Fallback: show as text
            JTextArea area = new JTextArea(json);
            area.setBackground(Theme.CARD);
            area.setForeground(Theme.TXT2);
            area.setFont(Theme.body(12));
            area.setEditable(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
            resultsArea.add(area);
        }

        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private JPanel buildHeaderCard(String career) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(155,109,255,20), getWidth(), 0, new Color(0,212,170,20));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                g2.setColor(Theme.BORDER2);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setMaximumSize(new Dimension(800, 100));

        JLabel icon = new JLabel("🗺 Roadmap for: " + career);
        icon.setFont(Theme.display(20, Font.BOLD));
        icon.setForeground(Theme.TXT);
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Follow each phase to build skills and reach your goal step-by-step.");
        sub.setFont(Theme.body(13));
        sub.setForeground(Theme.TXT2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(icon);
        card.add(Box.createVerticalStrut(6));
        card.add(sub);
        return card;
    }

    private Color[] phaseColors = {Theme.TEAL, Theme.BLUE, Theme.PURPLE, Theme.AMBER, Theme.GREEN, Theme.ROSE};

    private JPanel buildPhaseCard(String phaseJson, int idx) {
        String phase    = extractFieldSimple(phaseJson, "phase");
        String duration = extractFieldSimple(phaseJson, "duration");
        String goals    = extractArrayText(phaseJson, "goals");
        String resources = extractArrayText(phaseJson, "resources");
        Color accent = phaseColors[idx % phaseColors.length];

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(0, 12, 0, getHeight()-12);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(1, 0, getWidth()-2, getHeight()-1, Theme.R12, Theme.R12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 16));
        card.setMaximumSize(new Dimension(800, 300));

        // Phase header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel phaseLbl = new JLabel("Phase " + (idx+1) + ": " + (phase != null ? phase : ""));
        phaseLbl.setFont(Theme.display(15, Font.BOLD));
        phaseLbl.setForeground(Theme.TXT);

        JLabel durLbl = new JLabel(duration != null ? "⏱ " + duration : "");
        durLbl.setFont(Theme.body(12));
        durLbl.setForeground(accent);

        header.add(phaseLbl, BorderLayout.WEST);
        header.add(durLbl,   BorderLayout.EAST);

        card.add(header);
        card.add(Box.createVerticalStrut(10));

        // Goals
        if (goals != null && !goals.isEmpty()) {
            JLabel goalsTitle = new JLabel("📌 Goals:");
            goalsTitle.setFont(Theme.body(12, Font.BOLD));
            goalsTitle.setForeground(Theme.TXT2);
            goalsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(goalsTitle);

            for (String goal : goals.split("\n")) {
                if (!goal.trim().isEmpty()) {
                    JLabel g = new JLabel("  • " + goal.trim());
                    g.setFont(Theme.body(12));
                    g.setForeground(Theme.TXT2);
                    g.setAlignmentX(Component.LEFT_ALIGNMENT);
                    card.add(g);
                }
            }
            card.add(Box.createVerticalStrut(8));
        }

        // Resources
        if (resources != null && !resources.isEmpty()) {
            JLabel resTitle = new JLabel("📚 Resources:");
            resTitle.setFont(Theme.body(12, Font.BOLD));
            resTitle.setForeground(Theme.TXT2);
            resTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(resTitle);

            for (String res : resources.split("\n")) {
                if (!res.trim().isEmpty()) {
                    JLabel r = new JLabel("  → " + res.trim());
                    r.setFont(Theme.body(12));
                    r.setForeground(accent);
                    r.setAlignmentX(Component.LEFT_ALIGNMENT);
                    card.add(r);
                }
            }
        }

        return card;
    }

    private JPanel buildTipsCard(String tips) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0xFF,0xB3,0x47,12));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.setColor(new Color(0xFF,0xB3,0x47,40));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R12, Theme.R12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        card.setMaximumSize(new Dimension(800, 200));

        JLabel title = new JLabel("💡 Pro Tips");
        title.setFont(Theme.body(13, Font.BOLD));
        title.setForeground(Theme.AMBER);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        for (String tip : tips.split("\n")) {
            if (!tip.trim().isEmpty()) {
                JLabel t = new JLabel("✦ " + tip.trim());
                t.setFont(Theme.body(12));
                t.setForeground(Theme.TXT2);
                t.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(t);
                card.add(Box.createVerticalStrut(4));
            }
        }
        return card;
    }

    private String extractFieldSimple(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(":", idx);
        if (colon < 0) return null;
        int q1 = json.indexOf("\"", colon+1);
        if (q1 < 0) return null;
        int q2 = json.indexOf("\"", q1+1);
        if (q2 < 0) return null;
        return json.substring(q1+1, q2);
    }

    private String extractArrayText(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx < 0) return null;
        int start = json.indexOf("[", idx);
        int end   = json.indexOf("]", start);
        if (start < 0 || end < 0) return null;
        String arr = json.substring(start+1, end);
        // Extract strings
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (pos < arr.length()) {
            int q1 = arr.indexOf("\"", pos);
            if (q1 < 0) break;
            int q2 = arr.indexOf("\"", q1+1);
            if (q2 < 0) break;
            sb.append(arr, q1+1, q2).append("\n");
            pos = q2+1;
        }
        return sb.toString().trim();
    }

    private String buildProfileString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User: ").append(user.getName()).append("\n");
        sb.append("Role: ").append(user.getRole()).append("\n");
        try (Connection c = com.pathfinder.db.DBConnection.getConnection()) {
            if (user.getRole().equals("student")) {
                try (var ps = c.prepareStatement("SELECT * FROM student_profiles WHERE user_id=?")) {
                    ps.setInt(1, user.getId());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sb.append("Standard: ").append(rs.getString("standard_level")).append("\n");
                            sb.append("Stream: ").append(rs.getString("stream")).append("\n");
                            sb.append("Interests: ").append(rs.getString("interests")).append("\n");
                        }
                    }
                }
            } else {
                try (var ps = c.prepareStatement("SELECT * FROM professional_profiles WHERE user_id=?")) {
                    ps.setInt(1, user.getId());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            sb.append("Domain: ").append(rs.getString("domain")).append("\n");
                            sb.append("Experience: ").append(rs.getString("years_experience")).append("\n");
                            sb.append("Skills: ").append(rs.getString("skills")).append("\n");
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return sb.toString();
    }
}
