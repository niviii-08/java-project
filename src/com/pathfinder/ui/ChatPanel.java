package com.pathfinder.ui;

import com.pathfinder.api.OpenRouterClient;
import com.pathfinder.model.User;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends JPanel {

    private User user;
    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private DarkTextField inputField;
    private GradientButton sendBtn;

    public ChatPanel(User user) {
        this.user = user;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
    }

    private void build() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.BG2);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(20, 32, 20, 32)
        ));

        JLabel titleLbl = new JLabel("💬 AI Career Chat");
        titleLbl.setFont(Theme.display(22, Font.BOLD));
        titleLbl.setForeground(Theme.TXT);

        JLabel subLbl = new JLabel("Ask anything about careers, exams, skills, or your future");
        subLbl.setFont(Theme.body(13));
        subLbl.setForeground(Theme.TXT2);

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);
        headerText.add(titleLbl);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(subLbl);

        header.add(headerText, BorderLayout.WEST);

        // Messages area
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(Theme.BG);
        messagesPanel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // Welcome message
        addBotMessage("Hello " + user.getName() + "! 👋 I'm your AI career guide.\n\n"
            + "I can help you with:\n"
            + "• Career suggestions based on your background\n"
            + "• Information about entrance exams (JEE, NEET, GATE, UPSC, CAT...)\n"
            + "• Study tips and learning resources\n"
            + "• Salary insights and job market trends\n"
            + "• Career transitions and upskilling\n\n"
            + "What would you like to know today?");

        // Quick prompts
        addQuickPrompts();

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(Theme.BG);
        scrollPane.getViewport().setBackground(Theme.BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Input area
        JPanel inputArea = buildInputArea();

        add(header,     BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(inputArea,  BorderLayout.SOUTH);
    }

    private JPanel buildInputArea() {
        JPanel area = new JPanel(new BorderLayout(12, 0));
        area.setBackground(Theme.BG2);
        area.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            BorderFactory.createEmptyBorder(16, 32, 20, 32)
        ));

        inputField = new DarkTextField("Ask about careers, exams, skills...", 30);
        inputField.setPreferredSize(new Dimension(0, 46));
        inputField.addActionListener(e -> sendMessage());

        sendBtn = new GradientButton("Send →");
        sendBtn.setPreferredSize(new Dimension(120, 46));
        sendBtn.addActionListener(e -> sendMessage());

        area.add(inputField, BorderLayout.CENTER);
        area.add(sendBtn,    BorderLayout.EAST);
        return area;
    }

    private void addQuickPrompts() {
        JPanel promptsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        promptsRow.setOpaque(false);
        promptsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        promptsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        String[] prompts = user.getRole().equals("student")
            ? new String[]{"Best careers for PCM stream?", "How to prepare for JEE?", "What is NEET exam?", "Careers in Arts?"}
            : new String[]{"How to switch to tech?", "Best certifications for data science?", "How to get promoted?", "Startup vs job?"};

        for (String p : prompts) {
            JButton btn = new JButton(p) {
                boolean hover = false;
                {
                    addMouseListener(new MouseAdapter() {
                        public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                        public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    });
                }
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(hover ? Theme.CARD2 : Theme.CARD);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R8, Theme.R8);
                    g2.setColor(hover ? Theme.TEAL : Theme.BORDER);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R8, Theme.R8);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            btn.setFont(Theme.body(12));
            btn.setForeground(Theme.TXT2);
            btn.setOpaque(false);
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
            btn.addActionListener(e -> {
                inputField.setText(p);
                sendMessage();
            });
            promptsRow.add(btn);
        }

        messagesPanel.add(Box.createVerticalStrut(12));
        messagesPanel.add(promptsRow);
        messagesPanel.add(Box.createVerticalStrut(20));
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        inputField.setText("");
        addUserMessage(text);
        sendBtn.setEnabled(false);
        inputField.setEnabled(false);

        // Typing indicator
        JPanel typingPanel = buildTypingIndicator();
        typingPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typingPanel.setName("typing");
        messagesPanel.add(typingPanel);
        messagesPanel.add(Box.createVerticalStrut(8));
        scrollToBottom();

        // Context for AI
        String context = "User profile: Name=" + user.getName()
            + ", Role=" + user.getRole()
            + ". The user is asking: " + text
            + "\nPlease respond in a helpful, conversational way. "
            + "Be specific to India and the Indian education/job market. "
            + "Keep response concise but informative (under 300 words unless a detailed roadmap is requested).";

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return OpenRouterClient.chat(text, context);
            }

            @Override
            protected void done() {
                // Remove typing indicator
                Component[] comps = messagesPanel.getComponents();
                for (int i = comps.length - 1; i >= 0; i--) {
                    if ("typing".equals(comps[i].getName())) {
                        messagesPanel.remove(comps[i]);
                        if (i + 1 < comps.length) messagesPanel.remove(comps[i + 1]);
                        break;
                    }
                }

                sendBtn.setEnabled(true);
                inputField.setEnabled(true);
                inputField.requestFocus();

                try {
                    String response = get();
                    addBotMessage(response);
                } catch (Exception ex) {
                    addBotMessage("Sorry, I couldn't connect to the AI. Please check your API key and internet connection.\n\nError: " + ex.getMessage());
                }
                scrollToBottom();
            }
        };
        worker.execute();
    }

    private void addUserMessage(String text) {
        JPanel bubble = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bubble.setOpaque(false);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel lbl = new JLabel("<html><body style='width:380px;padding:2px'>" + escapeHtml(text) + "</body></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.TEAL2, getWidth(), 0, Theme.BLUE);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(Theme.body(13));
        lbl.setForeground(Theme.TXT);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        bubble.add(lbl);
        messagesPanel.add(bubble);
        messagesPanel.add(Box.createVerticalStrut(8));
        messagesPanel.revalidate();
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        JPanel bubble = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bubble.setOpaque(false);
        bubble.setAlignmentX(Component.LEFT_ALIGNMENT);
        bubble.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Avatar
        JLabel avatar = new JLabel("◈") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();
                int size = Math.min(w, h);
                int x = (w - size) / 2;
                int y = (h - size) / 2;
                
                GradientPaint gp = new GradientPaint(0, 0, Theme.TEAL, w, h, Theme.PURPLE);
                g2.setPaint(gp);
                g2.fillOval(x, y, size, size);
                g2.setColor(Theme.TXT);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), x + (size - fm.stringWidth(getText())) / 2, 
                             y + (size + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(32, 32));
        avatar.setFont(Theme.body(12));

        // Filter out technical artifacts like raw JSON blocks or separators
        String cleanText = text;
        if (cleanText.contains("---")) cleanText = cleanText.split("---")[0];
        if (cleanText.contains("```json")) cleanText = cleanText.split("```json")[0];
        if (cleanText.contains("{ \"careers\":")) cleanText = cleanText.split("\\{ \"careers\":")[0];
        if (cleanText.contains("JSON Response:")) cleanText = cleanText.split("JSON Response:")[0];

        // Format text: replace \n with <br>, handle bullet points, strip markdown
        String formatted = escapeHtml(cleanText.trim())
            .replace("**", "")
            .replace("###", "")
            .replace("\n•", "<br>&bull;")
            .replace("\n-", "<br>&ndash;")
            .replace("\n", "<br>");

        JLabel lbl = new JLabel("<html><body style='width:480px;padding:4px;line-height:1.4'>" + formatted + "</body></html>") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R12, Theme.R12);
                g2.setColor(Theme.BORDER);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.R12, Theme.R12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(Theme.body(13));
        lbl.setForeground(Theme.TXT);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        bubble.add(avatar);
        bubble.add(Box.createHorizontalStrut(8));
        bubble.add(lbl);
        messagesPanel.add(bubble);
        messagesPanel.add(Box.createVerticalStrut(8));
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private JPanel buildTypingIndicator() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);

        JLabel dots = new JLabel("AI is thinking") {
            int dotCount = 0;
            Timer t = new Timer(500, e -> {
                dotCount = (dotCount + 1) % 4;
                setText("AI is thinking" + ".".repeat(dotCount));
            });
            { t.start(); }
        };
        dots.setFont(Theme.body(12));
        dots.setForeground(Theme.TXT3);
        p.add(dots);
        return p;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private String escapeHtml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }
}
