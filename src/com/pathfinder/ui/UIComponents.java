package com.pathfinder.ui;

import com.pathfinder.util.Theme;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.plaf.basic.*;

// ── Rounded Panel ─────────────────────────────────────────────────
class RoundedPanel extends JPanel {
    private int radius;
    private Color bgColor;

    public RoundedPanel(int radius, Color bgColor) {
        this.radius = radius;
        this.bgColor = bgColor;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(bgColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        g2.dispose();
    }
}

// ── Gradient Button ───────────────────────────────────────────────
class GradientButton extends JButton {
    private Color c1, c2;
    private boolean isOutline = false;

    public GradientButton(String text, Color c1, Color c2) {
        super(text);
        this.c1 = c1; this.c2 = c2;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(Theme.TXT);
        setFont(Theme.body(14, Font.BOLD));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
    }

    public GradientButton(String text) {
        this(text, Theme.TEAL, Theme.PURPLE);
    }

    public void setOutline(boolean outline) {
        this.isOutline = outline;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        if (isOutline) {
            g2.setColor(new Color(255,255,255,10));
            g2.fillRoundRect(0, 0, w, h, Theme.R12, Theme.R12);
            g2.setColor(Theme.BORDER);
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(0, 0, w-1, h-1, Theme.R12, Theme.R12);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, c1, w, h, c2);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w, h, Theme.R12, Theme.R12);
            // Hover effect
            if (getModel().isRollover()) {
                g2.setColor(new Color(255,255,255,20));
                g2.fillRoundRect(0, 0, w, h, Theme.R12, Theme.R12);
            }
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

// ── Dark Text Field ───────────────────────────────────────────────
class DarkTextField extends JTextField {
    private String placeholder;

    public DarkTextField(String placeholder, int cols) {
        super(cols);
        this.placeholder = placeholder;
        setBackground(Theme.BG3);
        setForeground(Theme.TXT);
        setCaretColor(Theme.TEAL);
        setFont(Theme.body(14));
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true) {
                @Override
                public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.BORDER);
                    g2.drawRoundRect(x, y, w-1, h-1, Theme.R8, Theme.R8);
                    g2.dispose();
                }
            },
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R8, Theme.R8);
        g2.dispose();
        super.paintComponent(g);
        if (getText().isEmpty() && placeholder != null) {
            g2 = (Graphics2D) g.create();
            g2.setColor(Theme.TXT3);
            g2.setFont(getFont());
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, ins.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}

// ── Dark Password Field ───────────────────────────────────────────
class DarkPasswordField extends JPasswordField {
    private String placeholder;

    public DarkPasswordField(String placeholder, int cols) {
        super(cols);
        this.placeholder = placeholder;
        setBackground(Theme.BG3);
        setForeground(Theme.TXT);
        setCaretColor(Theme.TEAL);
        setFont(Theme.body(14));
        setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R8, Theme.R8);
        g2.dispose();
        super.paintComponent(g);
        if (getPassword().length == 0 && placeholder != null) {
            g2 = (Graphics2D) g.create();
            g2.setColor(Theme.TXT3);
            g2.setFont(getFont());
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(placeholder, ins.left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}

// ── Dark Combo Box ────────────────────────────────────────────────
class DarkComboBox extends JComboBox<String> {
    public DarkComboBox(String[] items) {
        super(items);
        setBackground(Theme.BG3);
        setForeground(Theme.TXT);
        setFont(Theme.body(13));
        setOpaque(true);
        setFocusable(true);
        setBorder(new CompoundBorder(
            new LineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        setRenderer(new DarkComboRenderer());
        
        // CUSTOM UI to override Windows/System native defaults
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("▾");
                btn.setFont(Theme.body(14));
                btn.setForeground(Theme.TXT2);
                btn.setBackground(Theme.BG3);
                btn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                btn.setContentAreaFilled(false);
                btn.setFocusPainted(false);
                return btn;
            }
            @Override
            public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG3);
                g2.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
                g2.dispose();
            }
        });

        // Ensure the editor (if editable) also follows theme
        Component editor = getEditor().getEditorComponent();
        if (editor instanceof JComponent jc) {
            jc.setBackground(Theme.BG3);
            jc.setForeground(Theme.TXT);
            jc.setBorder(null);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Theme.BG3);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.R8, Theme.R8);
        g2.dispose();
        super.paintComponent(g);
    }
}

class DarkComboRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        setBackground(isSelected ? Theme.CARD2 : Theme.BG3);
        setForeground(isSelected ? Theme.TXT : Theme.TXT2);
        setFont(Theme.body(13));
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return this;
    }
}

// ── Section Label ─────────────────────────────────────────────────
class SectionLabel extends JLabel {
    public SectionLabel(String text) {
        super(text);
        setFont(Theme.body(11, Font.BOLD));
        setForeground(Theme.TXT3);
    }
}

// ── Card Panel ────────────────────────────────────────────────────
class CardPanel extends RoundedPanel {
    public CardPanel() {
        super(Theme.R16, Theme.CARD);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
    }
}

// Export all as package-accessible
public class UIComponents {
    static {
        // Fix for Windows L&F combo box and text field colors
        UIManager.put("ComboBox.background", Theme.BG3);
        UIManager.put("ComboBox.foreground", Theme.TXT);
        UIManager.put("ComboBox.selectionBackground", Theme.CARD2);
        UIManager.put("ComboBox.selectionForeground", Theme.TXT);
        UIManager.put("ComboBox.buttonBackground", Theme.BG3);
        UIManager.put("ComboBox.buttonDarkShadow", Theme.BG3);
        UIManager.put("ComboBox.buttonShadow", Theme.BG3);
        UIManager.put("ComboBox.buttonHighlight", Theme.BG3);
        UIManager.put("ComboBox.font", Theme.body(13));
        
        UIManager.put("TextField.background", Theme.BG3);
        UIManager.put("TextField.foreground", Theme.TXT);
        UIManager.put("TextField.caretForeground", Theme.TEAL);
        UIManager.put("TextField.selectionBackground", Theme.TEAL);
        
        UIManager.put("List.background", Theme.BG3);
        UIManager.put("List.foreground", Theme.TXT);
        UIManager.put("List.selectionBackground", Theme.CARD2);
        UIManager.put("List.selectionForeground", Theme.TXT);
    }

    public static RoundedPanel roundedPanel(int r, Color c) { return new RoundedPanel(r, c); }
    public static GradientButton gradientButton(String t) { return new GradientButton(t); }
    public static GradientButton gradientButton(String t, Color c1, Color c2) { return new GradientButton(t, c1, c2); }
    public static DarkTextField textField(String ph, int cols) { return new DarkTextField(ph, cols); }
    public static DarkPasswordField passwordField(String ph, int cols) { return new DarkPasswordField(ph, cols); }
    public static DarkComboBox comboBox(String[] items) { return new DarkComboBox(items); }
    public static SectionLabel sectionLabel(String t) { return new SectionLabel(t); }
    public static CardPanel card() { return new CardPanel(); }

    public static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body(size, style));
        l.setForeground(color);
        return l;
    }
}
