package com.pathfinder.ui;

import com.pathfinder.db.UserDAO;
import com.pathfinder.model.*;
import com.pathfinder.util.Theme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class AuthFrame extends JFrame {

    private String mode;       // "login" or "register"
    private String preselectedRole;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    // Form fields
    private DarkTextField nameField, emailField;
    private DarkPasswordField passField, confirmField;
    private DarkComboBox roleCombo;
    private JLabel statusLabel;

    public AuthFrame(String mode, String preselectedRole) {
        this.mode = mode;
        this.preselectedRole = preselectedRole;

        setTitle("Pathfinder — " + (mode.equals("login") ? "Sign In" : "Create Account"));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, mode.equals("login") ? 500 : 620);
        setLocationRelativeTo(null);
        setUndecorated(true);
        try { setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20)); } catch (Exception ignored) {}
        setResizable(false);

        setContentPane(buildUI());
    }

    private JPanel buildUI() {
        JPanel root = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG2);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Subtle top glow
                RadialGradientPaint rgp = new RadialGradientPaint(
                    new Point2D.Float(getWidth()/2f, -30), 300,
                    new float[]{0f, 1f},
                    new Color[]{new Color(0,212,170,15), new Color(0,0,0,0)}
                );
                g2.setPaint(rgp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setLayout(new BorderLayout());
        root.setOpaque(false);

        // ── Top bar ───────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 0, 16));

        JLabel logoLbl = new JLabel("◈ PATHFINDER");
        logoLbl.setFont(Theme.display(16, Font.BOLD));
        logoLbl.setForeground(Theme.TEAL);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(Theme.body(13));
        closeBtn.setForeground(Theme.TXT3);
        closeBtn.setBackground(null);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            new SplashScreen().setVisible(true);
            dispose();
        });

        topBar.add(logoLbl, BorderLayout.WEST);
        topBar.add(closeBtn, BorderLayout.EAST);

        // ── Form area ─────────────────────────────────────────
        JPanel formWrap = new JPanel();
        formWrap.setOpaque(false);
        formWrap.setLayout(new BoxLayout(formWrap, BoxLayout.Y_AXIS));
        formWrap.setBorder(BorderFactory.createEmptyBorder(24, 44, 64, 44));

        // Title
        JLabel titleLbl = new JLabel(mode.equals("login") ? "Welcome back" : "Join Pathfinder");
        titleLbl.setFont(Theme.display(28, Font.BOLD));
        titleLbl.setForeground(Theme.TXT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(mode.equals("login")
            ? "Sign in to continue your journey"
            : "Create your free account today");
        subLbl.setFont(Theme.body(14));
        subLbl.setForeground(Theme.TXT2);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        formWrap.add(titleLbl);
        formWrap.add(Box.createVerticalStrut(6));
        formWrap.add(subLbl);
        formWrap.add(Box.createVerticalStrut(28));

        if (mode.equals("register")) {
            // Name
            formWrap.add(fieldLabel("Full Name"));
            formWrap.add(Box.createVerticalStrut(6));
            nameField = new DarkTextField("Your full name", 20);
            nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
            formWrap.add(nameField);
            formWrap.add(Box.createVerticalStrut(12));

            // Role
            formWrap.add(fieldLabel("I am a"));
            formWrap.add(Box.createVerticalStrut(6));
            String[] roles = {"Student", "Professional"};
            roleCombo = new DarkComboBox(roles);
            if ("professional".equals(preselectedRole)) roleCombo.setSelectedIndex(1);
            else if ("student".equals(preselectedRole)) roleCombo.setSelectedIndex(0);
            roleCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            roleCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            formWrap.add(roleCombo);
            formWrap.add(Box.createVerticalStrut(12));
        }

        // Email
        formWrap.add(fieldLabel("Email Address"));
        formWrap.add(Box.createVerticalStrut(6));
        emailField = new DarkTextField("you@example.com", 20);
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrap.add(emailField);
        formWrap.add(Box.createVerticalStrut(12));

        // Password
        formWrap.add(fieldLabel("Password"));
        formWrap.add(Box.createVerticalStrut(6));
        passField = new DarkPasswordField("Enter password", 20);
        passField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        passField.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrap.add(passField);
        formWrap.add(Box.createVerticalStrut(12));

        if (mode.equals("register")) {
            formWrap.add(fieldLabel("Confirm Password"));
            formWrap.add(Box.createVerticalStrut(6));
            confirmField = new DarkPasswordField("Repeat password", 20);
            confirmField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            confirmField.setAlignmentX(Component.LEFT_ALIGNMENT);
            formWrap.add(confirmField);
            formWrap.add(Box.createVerticalStrut(12));
        }

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.body(12));
        statusLabel.setForeground(Theme.ROSE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        formWrap.add(statusLabel);
        formWrap.add(Box.createVerticalStrut(20));

        // Submit button
        String btnText = mode.equals("login") ? "Sign In →" : "Create Account →";
        GradientButton submitBtn = new GradientButton(btnText);
        submitBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        submitBtn.addActionListener(e -> handleSubmit());
        formWrap.add(submitBtn);
        formWrap.add(Box.createVerticalStrut(20));

        // Switch mode
        String switchTxt = mode.equals("login") ? "Don't have an account? " : "Already have an account? ";
        String switchLink = mode.equals("login") ? "Register" : "Sign In";
        JPanel switchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        switchRow.setOpaque(false);
        switchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel switchLbl = new JLabel(switchTxt);
        switchLbl.setFont(Theme.body(13));
        switchLbl.setForeground(Theme.TXT2);
        JLabel switchLinkLbl = new JLabel(switchLink);
        switchLinkLbl.setFont(Theme.body(13, Font.BOLD));
        switchLinkLbl.setForeground(Theme.TEAL);
        switchLinkLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        switchLinkLbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                String newMode = mode.equals("login") ? "register" : "login";
                new AuthFrame(newMode, preselectedRole).setVisible(true);
                dispose();
            }
        });
        switchRow.add(switchLbl);
        switchRow.add(switchLinkLbl);
        formWrap.add(switchRow);

        root.add(topBar, BorderLayout.NORTH);
        root.add(formWrap, BorderLayout.CENTER);

        // Enter key
        getRootPane().setDefaultButton(submitBtn);
        return root;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.body(12, Font.BOLD));
        l.setForeground(Theme.TXT3);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void handleSubmit() {
        String email = emailField.getText().trim();
        String pass  = new String(passField.getPassword()).trim();

        if (email.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        if (mode.equals("login")) {
            try {
                User user = UserDAO.login(email, pass);
                if (user == null) {
                    statusLabel.setText("Invalid email or password.");
                } else {
                    Session.getInstance().setCurrentUser(user);
                    openDashboard(user);
                }
            } catch (Exception ex) {
                statusLabel.setText("DB Error: " + ex.getMessage());
            }
        } else {
            String name = nameField.getText().trim();
            String conf = new String(confirmField.getPassword()).trim();
            String role = roleCombo.getSelectedIndex() == 0 ? "student" : "professional";

            if (name.isEmpty()) { statusLabel.setText("Please enter your name."); return; }
            if (!pass.equals(conf)) { statusLabel.setText("Passwords do not match."); return; }
            if (pass.length() < 6) { statusLabel.setText("Password must be at least 6 characters."); return; }

            try {
                if (UserDAO.emailExists(email)) {
                    statusLabel.setText("Email already registered. Please sign in.");
                    return;
                }
                int id = UserDAO.register(name, email, pass, role);
                if (id > 0) {
                    User user = new User();
                    user.setId(id);
                    user.setName(name);
                    user.setEmail(email);
                    user.setRole(role);
                    Session.getInstance().setCurrentUser(user);
                    openDashboard(user);
                } else {
                    statusLabel.setText("Registration failed. Try again.");
                }
            } catch (Exception ex) {
                statusLabel.setText("DB Error: " + ex.getMessage());
            }
        }
    }

    private void openDashboard(User user) {
        DashboardFrame dash = new DashboardFrame(user);
        dash.setVisible(true);
        dispose();
    }
}
