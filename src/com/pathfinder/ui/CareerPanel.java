package com.pathfinder.ui;

import com.pathfinder.api.OpenRouterClient;
import com.pathfinder.db.UserDAO;
import com.pathfinder.model.User;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class CareerPanel extends JPanel {

    private User user;
    private JPanel resultsArea;
    private JLabel statusLbl;
    private GradientButton analyzeBtn;

    public CareerPanel(User user) {
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

        // Header
        JLabel title = new JLabel("🎯 Career Suggestions");
        title.setFont(Theme.display(28, Font.BOLD));
        title.setForeground(Theme.TXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("AI-powered career matches based on your profile and interests.");
        sub.setFont(Theme.body(14));
        sub.setForeground(Theme.TXT2);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        outer.add(title);
        outer.add(Box.createVerticalStrut(6));
        outer.add(sub);
        outer.add(Box.createVerticalStrut(28));

        // Analyze button
        analyzeBtn = new GradientButton("✨ Analyse My Profile with AI");
        analyzeBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        analyzeBtn.setMaximumSize(new Dimension(340, 50));
        analyzeBtn.addActionListener(e -> runAnalysis());
        outer.add(analyzeBtn);
        outer.add(Box.createVerticalStrut(16));

        statusLbl = new JLabel(" ");
        statusLbl.setFont(Theme.body(13));
        statusLbl.setForeground(Theme.TXT2);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(statusLbl);
        outer.add(Box.createVerticalStrut(24));

        // Results area
        resultsArea = new JPanel();
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        resultsArea.setOpaque(false);
        resultsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPlaceholder();
        outer.add(resultsArea);

        return outer;
    }

    private void showPlaceholder() {
        resultsArea.removeAll();
        JPanel placeholder = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        placeholder.setOpaque(false);
        placeholder.setLayout(new BoxLayout(placeholder, BoxLayout.Y_AXIS));
        placeholder.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        placeholder.setMaximumSize(new Dimension(800, 250));
        placeholder.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel icon = new JLabel("🎯");
        icon.setFont(Theme.body(48));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel("Complete your profile and click 'Analyse' to see career matches");
        msg.setFont(Theme.body(15));
        msg.setForeground(Theme.TXT2);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel hint = new JLabel("Our AI will suggest careers tailored to your academic level and interests");
        hint.setFont(Theme.body(12));
        hint.setForeground(Theme.TXT3);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);

        placeholder.add(icon);
        placeholder.add(Box.createVerticalStrut(12));
        placeholder.add(msg);
        placeholder.add(Box.createVerticalStrut(6));
        placeholder.add(hint);

        resultsArea.add(placeholder);
        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private void runAnalysis() {
        analyzeBtn.setEnabled(false);
        statusLbl.setText("⏳ Analysing your profile with AI... this may take 10–20 seconds.");
        statusLbl.setForeground(Theme.AMBER);

        resultsArea.removeAll();
        JPanel loading = buildLoadingPanel();
        resultsArea.add(loading);
        resultsArea.revalidate();

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                String profile = buildProfileString();
                return OpenRouterClient.getCareerSuggestions(profile);
            }

            @Override
            protected void done() {
                analyzeBtn.setEnabled(true);
                try {
                    String response = get();
                    displayResults(response);
                    statusLbl.setText("✓ Analysis complete!");
                    statusLbl.setForeground(Theme.GREEN);

                    // Save to DB
                    try { UserDAO.saveResult(user.getId(), "career_suggestions", response); }
                    catch (Exception ignored) {}

                } catch (Exception ex) {
                    statusLbl.setText("Error: " + ex.getMessage());
                    statusLbl.setForeground(Theme.ROSE);
                    showPlaceholder();
                }
            }
        };
        worker.execute();
    }

    private String buildProfileString() {
        StringBuilder sb = new StringBuilder();
        sb.append("User Name: ").append(user.getName()).append("\n");
        sb.append("User Role: ").append(user.getRole()).append("\n");
        sb.append("Email: ").append(user.getEmail()).append("\n");

        try {
            if (user.getRole().equals("professional")) {
                Map<String, String> pro = UserDAO.getProfessionalProfileV2(user.getEmail());
                if (pro != null) {
                    sb.append("Domain: ").append(pro.get("domain")).append("\n");
                    sb.append("Current Role: ").append(pro.get("current_role")).append("\n");
                    sb.append("Company: ").append(pro.get("company")).append("\n");
                    sb.append("Experience: ").append(pro.get("experience")).append("\n");
                    sb.append("Skills: ").append(pro.get("skills")).append("\n");
                    sb.append("Interests: ").append(pro.get("interests")).append("\n");
                    sb.append("Location: ").append(pro.get("state")).append(", ").append(pro.get("district")).append("\n");
                }
            } else {
                Map<String, String> p = UserDAO.getUnifiedProfile(user.getEmail());
                if (p != null) {
                    sb.append("Level: ").append(p.get("class_level")).append("\n");
                    sb.append("Stream: ").append(p.get("stream")).append("\n");
                    sb.append("Interests: ").append(p.get("interests")).append("\n");
                    sb.append("Institution: ").append(p.get("institution")).append("\n");
                    sb.append("Location: ").append(p.get("state")).append(", ").append(p.get("district")).append("\n");
                    sb.append("Additional Context: ").append(p.get("extra_info_json")).append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("Error fetching profile: ").append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }

    private void displayResults(String json) {
        resultsArea.removeAll();

        // Parse simple JSON to extract careers
        try {
            String[] careers = extractCareers(json);
            String summary   = extractField(json, "summary");

            if (summary != null && !summary.isEmpty()) {
                JPanel summaryCard = buildSummaryCard(summary);
                summaryCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsArea.add(summaryCard);
                resultsArea.add(Box.createVerticalStrut(20));
            }

            if (careers != null) {
                JLabel matchTitle = new JLabel("Top Career Matches");
                matchTitle.setFont(Theme.display(18, Font.BOLD));
                matchTitle.setForeground(Theme.TXT);
                matchTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsArea.add(matchTitle);
                resultsArea.add(Box.createVerticalStrut(12));

                for (int i = 0; i < careers.length; i++) {
                    JPanel card = buildCareerCard(careers[i], i);
                    card.setAlignmentX(Component.LEFT_ALIGNMENT);
                    resultsArea.add(card);
                    resultsArea.add(Box.createVerticalStrut(12));
                }
            }

            // If parsing fails or results are minimal, show raw text nicely
            if (careers == null || careers.length == 0) {
                showRawResponse(json);
            }

        } catch (Exception e) {
            showRawResponse(json);
        }

        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private String[] extractCareers(String json) {
        // Find "careers" array
        int start = json.indexOf("\"careers\"");
        if (start == -1) return null;
        int arrStart = json.indexOf("[", start);
        int arrEnd   = json.lastIndexOf("]");
        if (arrStart == -1 || arrEnd == -1) return null;

        String arr = json.substring(arrStart + 1, arrEnd);
        // Split by career objects
        String[] raw = arr.split("\\{");
        java.util.List<String> careers = new java.util.ArrayList<>();
        for (String r : raw) {
            if (r.trim().startsWith("\"title\"") || r.trim().contains("\"title\"")) {
                careers.add("{" + r.replaceAll("\\}.*", "}"));
            }
        }
        return careers.toArray(new String[0]);
    }

    private String extractField(String json, String field) {
        String search = "\"" + field + "\"";
        int idx = json.indexOf(search);
        if (idx == -1) return null;
        
        int colon = json.indexOf(":", idx + search.length());
        if (colon == -1) return null;
        
        // Check if value is a string (starts with ")
        int quoteStart = json.indexOf("\"", colon + 1);
        int nextBrace = json.indexOf("}", colon + 1);
        int nextComma = json.indexOf(",", colon + 1);
        
        // If quote is found before any comma or brace, assume it's a string
        int valEnd = (nextComma != -1 && nextBrace != -1) ? Math.min(nextComma, nextBrace) : Math.max(nextComma, nextBrace);

        if (quoteStart != -1 && (valEnd == -1 || quoteStart < valEnd)) {
            int quoteEnd = json.indexOf("\"", quoteStart + 1);
            if (quoteEnd != -1) return json.substring(quoteStart + 1, quoteEnd);
        } else {
            // It's likely a number (unquoted)
            if (valEnd != -1) {
                return json.substring(colon + 1, valEnd).trim();
            }
        }
        return null;
    }

    private JPanel buildSummaryCard(String summary) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0,212,170,12));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.setColor(new Color(0,212,170,40));
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R12, Theme.R12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(800, 120));

        JLabel title = new JLabel("🧠 AI Summary");
        title.setFont(Theme.body(12, Font.BOLD));
        title.setForeground(Theme.TEAL);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel summaryLbl = new JLabel("<html><body style='width:700px'>" + summary + "</body></html>");
        summaryLbl.setFont(Theme.body(13));
        summaryLbl.setForeground(Theme.TXT2);
        summaryLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(summaryLbl);
        return card;
    }

    private JPanel buildCareerCard(String careerJson, int rank) {
        String title     = extractField(careerJson, "title");
        String desc      = extractField(careerJson, "description");
        String salary    = extractField(careerJson, "avgSalary");
        String timeframe = extractField(careerJson, "timeframe");
        String growth    = extractField(careerJson, "growth");
        String matchStr  = extractField(careerJson, "match");
        int match = 0;
        try { match = Integer.parseInt(matchStr); } catch (Exception ignored) {}

        Color accent = rank == 0 ? Theme.TEAL : rank == 1 ? Theme.BLUE : Theme.PURPLE;

        final int finalMatch = match;
        final String finalTitle = title != null ? title : "Career " + (rank+1);

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R16, Theme.R16);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R16, Theme.R16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setMaximumSize(new Dimension(800, 160));

        // Rank badge
        JLabel rankLbl = new JLabel("#" + (rank+1));
        rankLbl.setFont(Theme.display(22, Font.BOLD));
        rankLbl.setForeground(accent);
        rankLbl.setVerticalAlignment(SwingConstants.TOP);
        rankLbl.setPreferredSize(new Dimension(45, 60));

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel nameLbl = new JLabel(finalTitle);
        nameLbl.setFont(Theme.display(17, Font.BOLD));
        nameLbl.setForeground(Theme.TXT);
        nameLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        String descText = desc != null ? desc : "AI-recommended career path.";
        JLabel descLbl = new JLabel("<html><body style='width:500px'>" + descText + "</body></html>");
        descLbl.setFont(Theme.body(12));
        descLbl.setForeground(Theme.TXT2);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tags row
        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tags.setOpaque(false);
        tags.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (salary != null)    tags.add(tag("💰 " + salary, Theme.GREEN));
        if (timeframe != null) tags.add(tag("⏱ " + timeframe, Theme.BLUE));
        if (growth != null)    tags.add(tag("📈 " + growth + " Growth", Theme.AMBER));

        content.add(nameLbl);
        content.add(Box.createVerticalStrut(4));
        content.add(descLbl);
        content.add(Box.createVerticalStrut(8));
        content.add(tags);

        // Match bar
        JPanel matchPanel = new JPanel();
        matchPanel.setLayout(new BoxLayout(matchPanel, BoxLayout.Y_AXIS));
        matchPanel.setOpaque(false);
        matchPanel.setPreferredSize(new Dimension(100, 60));

        JLabel matchLbl = new JLabel(finalMatch + "%");
        matchLbl.setFont(Theme.display(20, Font.BOLD));
        matchLbl.setForeground(accent);
        matchLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel matchSub = new JLabel("match");
        matchSub.setFont(Theme.body(10));
        matchSub.setForeground(Theme.TXT3);
        matchSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        JPanel barBg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BORDER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                int fw = (int)(getWidth() * finalMatch / 100.0);
                GradientPaint gp = new GradientPaint(0, 0, accent, fw, 0, Theme.PURPLE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, fw, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        barBg.setPreferredSize(new Dimension(90, 6));
        barBg.setMaximumSize(new Dimension(90, 6));
        barBg.setOpaque(false);

        matchPanel.add(matchLbl);
        matchPanel.add(matchSub);
        matchPanel.add(Box.createVerticalStrut(4));
        matchPanel.add(barBg);

        card.add(rankLbl, BorderLayout.WEST);
        card.add(content, BorderLayout.CENTER);
        card.add(matchPanel, BorderLayout.EAST);

        return card;
    }

    private JLabel tag(String text, Color color) {
        JLabel lbl = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 20));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R8, Theme.R8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setOpaque(false);
        lbl.setFont(Theme.body(11));
        lbl.setForeground(color);
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return lbl;
    }

    private void showRawResponse(String text) {
        JTextArea area = new JTextArea(text);
        area.setBackground(Theme.CARD);
        area.setForeground(Theme.TXT2);
        area.setFont(Theme.body(12));
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        area.setMaximumSize(new Dimension(800, 400));
        resultsArea.add(area);
        resultsArea.revalidate();
        resultsArea.repaint();
    }

    private JPanel buildLoadingPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel("⚙️ AI is analysing your profile...");
        lbl.setFont(Theme.body(14));
        lbl.setForeground(Theme.TXT2);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBackground(Theme.BORDER);
        bar.setForeground(Theme.TEAL);
        bar.setMaximumSize(new Dimension(400, 6));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(lbl);
        p.add(Box.createVerticalStrut(12));
        p.add(bar);
        return p;
    }
}
