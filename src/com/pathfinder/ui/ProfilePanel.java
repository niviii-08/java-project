package com.pathfinder.ui;

import com.pathfinder.db.UserDAO;
import com.pathfinder.model.User;
import com.pathfinder.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ProfilePanel extends JPanel {

    private User user;
    private DashboardFrame dashboard;
    private JPanel dynamicArea;
    private JLabel statusLbl;

    // Student fields
    private DarkComboBox standardCombo;
    private DarkComboBox stateCombo;
    private DarkComboBox districtCombo;
    private DarkTextField institutionField;
    private DarkTextField interestsField;
    private JPanel followUpPanel;

    // Professional fields
    private DarkTextField domainField;
    private DarkComboBox experienceCombo;
    private DarkTextField skillsField;
    private DarkTextField proInterestsField;
    private DarkTextField currentRoleField;
    private DarkTextField companyField;

    // Dynamic follow-up fields for student
    private DarkComboBox streamCombo;
    private JPanel marksPanel;
    private DarkTextField coachingField;
    private DarkTextField targetCollegeField;
    private DarkTextField degField;

    public ProfilePanel(User user, DashboardFrame dashboard) {
        this.user = user;
        this.dashboard = dashboard;
        setBackground(Theme.BG);
        setLayout(new BorderLayout());
        build();
        loadProfile();
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
        JLabel titleLbl = new JLabel(user.getRole().equals("student") ? "🎓 Student Profile" : "💼 Professional Profile");
        titleLbl.setFont(Theme.display(28, Font.BOLD));
        titleLbl.setForeground(Theme.TXT);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Tell us about yourself to get personalised career guidance.");
        subLbl.setFont(Theme.body(14));
        subLbl.setForeground(Theme.TXT2);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        outer.add(titleLbl);
        outer.add(Box.createVerticalStrut(6));
        outer.add(subLbl);
        outer.add(Box.createVerticalStrut(32));

        // Dynamic form
        dynamicArea = new JPanel();
        dynamicArea.setLayout(new BoxLayout(dynamicArea, BoxLayout.Y_AXIS));
        dynamicArea.setOpaque(false);
        dynamicArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (user.getRole().equals("student")) buildStudentForm();
        else buildProfessionalForm();

        outer.add(dynamicArea);
        outer.add(Box.createVerticalStrut(24));

        // Status
        statusLbl = new JLabel(" ");
        statusLbl.setFont(Theme.body(12));
        statusLbl.setForeground(Theme.GREEN);
        statusLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        outer.add(statusLbl);
        outer.add(Box.createVerticalStrut(12));

        // Save + Continue
        GradientButton saveBtn = new GradientButton("Save Profile & Get Career Matches →");
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(400, 50));
        saveBtn.addActionListener(e -> saveProfile());
        outer.add(saveBtn);

        return outer;
    }

    // ── STUDENT FORM ──────────────────────────────────────────────
    private void buildStudentForm() {
        // ONE initial question: Standard or College
        addSectionHeader("📚 What are you studying?");

        String[] standards = {
            "Class 6", "Class 7", "Class 8",
            "Class 9", "Class 10",
            "Class 11", "Class 12",
            "1st Year College", "2nd Year College", "3rd Year College", "4th Year College",
            "Postgraduate (PG)"
        };
        standardCombo = new DarkComboBox(standards);
        standardCombo.setMaximumSize(new Dimension(400, 44));
        standardCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(standardCombo);
        dynamicArea.add(Box.createVerticalStrut(20));

        // Follow-up section (shown after standard selected)
        followUpPanel = new JPanel();
        followUpPanel.setLayout(new BoxLayout(followUpPanel, BoxLayout.Y_AXIS));
        followUpPanel.setOpaque(false);
        followUpPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(followUpPanel);

        // Location
        addSectionHeader("📍 Location");
        buildLocationFields();

        // Institution
        addSectionHeader("🏫 School / College Name");
        institutionField = new DarkTextField("e.g. Delhi Public School, IIT Bombay", 30);
        institutionField.setMaximumSize(new Dimension(500, 44));
        institutionField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(institutionField);
        dynamicArea.add(Box.createVerticalStrut(20));

        // Interests
        addSectionHeader("⭐ Interests & Hobbies");
        JLabel interestHint = new JLabel("Enter your interests separated by commas (we auto-correct spelling)");
        interestHint.setFont(Theme.body(12));
        interestHint.setForeground(Theme.TXT3);
        interestHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(interestHint);
        dynamicArea.add(Box.createVerticalStrut(6));
        interestsField = new DarkTextField("e.g. Mathematics, Drawing, Cricket, Coding", 30);
        interestsField.setMaximumSize(new Dimension(500, 44));
        interestsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(interestsField);
        dynamicArea.add(Box.createVerticalStrut(20));

        // Listen for standard changes → update follow-up
        standardCombo.addActionListener(e -> updateStudentFollowUp());
        updateStudentFollowUp(); // initial
    }

    private void updateStudentFollowUp() {
        followUpPanel.removeAll();
        String selected = (String) standardCombo.getSelectedItem();
        if (selected == null) return;

        if (selected.equals("Class 9") || selected.equals("Class 10")) {
            addFollowUpHeader("📝 Academic Details (Class 9–10)");

            JLabel streamLabel = new JLabel("Which subjects are you strong in?");
            streamLabel.setFont(Theme.body(13));
            streamLabel.setForeground(Theme.TXT2);
            streamLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(streamLabel);
            followUpPanel.add(Box.createVerticalStrut(8));

            String[] subjectPrefs = {"Science & Mathematics", "Languages & Social Studies", "Commerce related", "Arts & Crafts / Sports"};
            streamCombo = new DarkComboBox(subjectPrefs);
            streamCombo.setMaximumSize(new Dimension(400, 44));
            streamCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(streamCombo);
            followUpPanel.add(Box.createVerticalStrut(16));

            JLabel pctLabel = new JLabel("Approximate overall percentage:");
            pctLabel.setFont(Theme.body(13));
            pctLabel.setForeground(Theme.TXT2);
            pctLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(pctLabel);
            followUpPanel.add(Box.createVerticalStrut(6));

            String[] ranges = {"Below 60%", "60–70%", "70–80%", "80–90%", "Above 90%"};
            DarkComboBox pctCombo = new DarkComboBox(ranges);
            pctCombo.setMaximumSize(new Dimension(300, 44));
            pctCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(pctCombo);
            followUpPanel.add(Box.createVerticalStrut(20));

        } else if (selected.equals("Class 11") || selected.equals("Class 12")) {
            addFollowUpHeader("📝 Stream & Marks (" + selected + ")");

            JLabel streamLbl = new JLabel("Your stream:");
            streamLbl.setFont(Theme.body(13));
            streamLbl.setForeground(Theme.TXT2);
            streamLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(streamLbl);
            followUpPanel.add(Box.createVerticalStrut(6));

            String[] streams = {"Science (PCM)", "Science (PCB)", "Science (PCM + Biology)", "Commerce (With Maths)", "Commerce (Without Maths)", "Arts / Humanities"};
            streamCombo = new DarkComboBox(streams);
            streamCombo.setMaximumSize(new Dimension(400, 44));
            streamCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(streamCombo);
            followUpPanel.add(Box.createVerticalStrut(16));

            // Specialized questions for Class 12
            if (selected.equals("Class 12")) {
                JLabel coachingLbl = new JLabel("Are you attending any coaching? (e.g. FIITJEE, Aakash, Allen)");
                coachingLbl.setFont(Theme.body(13));
                coachingLbl.setForeground(Theme.TEAL);
                coachingLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                followUpPanel.add(coachingLbl);
                followUpPanel.add(Box.createVerticalStrut(6));

                coachingField = new DarkTextField("e.g. FIITJEE Bangalore", 30);
                coachingField.setMaximumSize(new Dimension(500, 44));
                coachingField.setAlignmentX(Component.LEFT_ALIGNMENT);
                followUpPanel.add(coachingField);
                followUpPanel.add(Box.createVerticalStrut(16));

                JLabel targetLbl = new JLabel("What is your dream college?");
                targetLbl.setFont(Theme.body(13));
                targetLbl.setForeground(Theme.TEAL);
                targetLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                followUpPanel.add(targetLbl);
                followUpPanel.add(Box.createVerticalStrut(6));

                targetCollegeField = new DarkTextField("e.g. IIT Bombay, AIIMS Delhi, SRCC", 30);
                targetCollegeField.setMaximumSize(new Dimension(500, 44));
                targetCollegeField.setAlignmentX(Component.LEFT_ALIGNMENT);
                followUpPanel.add(targetCollegeField);
                followUpPanel.add(Box.createVerticalStrut(16));
            }

            JLabel marksLbl = new JLabel("Enter marks / grades for your subjects (optional):");
            marksLbl.setFont(Theme.body(13));
            marksLbl.setForeground(Theme.TXT2);
            marksLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(marksLbl);
            followUpPanel.add(Box.createVerticalStrut(6));

            DarkTextField marksField = new DarkTextField("e.g. Physics:85, Chemistry:78, Maths:92", 30);
            marksField.setMaximumSize(new Dimension(500, 44));
            marksField.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(marksField);
            followUpPanel.add(Box.createVerticalStrut(16));

            // Exam prep
            JLabel examLbl = new JLabel("Which entrance exams are you targeting?");
            examLbl.setFont(Theme.body(13));
            examLbl.setForeground(Theme.TXT2);
            examLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(examLbl);
            followUpPanel.add(Box.createVerticalStrut(6));

            DarkTextField examField = new DarkTextField("e.g. JEE Main, NEET, CUET, CLAT...", 30);
            examField.setMaximumSize(new Dimension(500, 44));
            examField.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(examField);
            followUpPanel.add(Box.createVerticalStrut(20));

        } else if (selected.contains("College") || selected.contains("Postgraduate")) {
            addFollowUpHeader("🎓 College Details");
 
            JLabel degLabel = new JLabel("Your degree / programme:");
            degLabel.setFont(Theme.body(13));
            degLabel.setForeground(Theme.TXT2);
            degLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(degLabel);
            followUpPanel.add(Box.createVerticalStrut(6));
 
            degField = new DarkTextField("e.g. B.Tech CSE, BBA, BA Economics, MBBS", 30);
            degField.setMaximumSize(new Dimension(500, 44));
            degField.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(degField);
            followUpPanel.add(Box.createVerticalStrut(16));

            JLabel cgpaLabel = new JLabel("Current CGPA / Percentage:");
            cgpaLabel.setFont(Theme.body(13));
            cgpaLabel.setForeground(Theme.TXT2);
            cgpaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(cgpaLabel);
            followUpPanel.add(Box.createVerticalStrut(6));

            DarkTextField cgpaField = new DarkTextField("e.g. 8.5 / 10 or 78%", 20);
            cgpaField.setMaximumSize(new Dimension(300, 44));
            cgpaField.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(cgpaField);
            followUpPanel.add(Box.createVerticalStrut(16));

            JLabel futureLabel = new JLabel("Post-graduation plans:");
            futureLabel.setFont(Theme.body(13));
            futureLabel.setForeground(Theme.TXT2);
            futureLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(futureLabel);
            followUpPanel.add(Box.createVerticalStrut(6));

            String[] plans = {"Job (Placement / Off-campus)", "Higher Studies (M.Tech / MBA / MS)", "Government Job (UPSC / SSC / PSU)", "Entrepreneurship / Startup", "Not sure yet"};
            DarkComboBox plansCombo = new DarkComboBox(plans);
            plansCombo.setMaximumSize(new Dimension(500, 44));
            plansCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(plansCombo);
            followUpPanel.add(Box.createVerticalStrut(20));

        } else if (selected.contains("Class 6") || selected.contains("Class 7") || selected.contains("Class 8")) {
            addFollowUpHeader("🌟 Let's explore your interests! (Class 6–8)");

            JLabel msg = new JLabel("<html><body style='width:500px;color:#8A90A4;font-size:12px'>"
                + "At this stage, it's all about discovering what excites you. No pressure to pick a career yet! "
                + "Just tell us your favourite subjects and activities.</body></html>");
            msg.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(msg);
            followUpPanel.add(Box.createVerticalStrut(12));

            JLabel favLbl = new JLabel("Favourite subjects (choose the closest):");
            favLbl.setFont(Theme.body(13));
            favLbl.setForeground(Theme.TXT2);
            favLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(favLbl);
            followUpPanel.add(Box.createVerticalStrut(6));

            String[] favs = {"Science & Nature", "Mathematics", "English & Literature", "Social Studies & History", "Arts & Crafts", "Sports & Physical Education", "Computers / Technology", "Music & Dance"};
            DarkComboBox favCombo = new DarkComboBox(favs);
            favCombo.setMaximumSize(new Dimension(400, 44));
            favCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
            followUpPanel.add(favCombo);
            followUpPanel.add(Box.createVerticalStrut(20));
        }

        followUpPanel.revalidate();
        followUpPanel.repaint();
    }

    // ── PROFESSIONAL FORM ─────────────────────────────────────────
    private void buildProfessionalForm() {
        addSectionHeader("🏢 Professional Domain");
        JLabel domainHint = new JLabel("Enter your primary work domain/industry");
        domainHint.setFont(Theme.body(12));
        domainHint.setForeground(Theme.TXT3);
        domainHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(domainHint);
        dynamicArea.add(Box.createVerticalStrut(6));
        domainField = new DarkTextField("e.g. Software Engineering, Healthcare, Finance, Education", 30);
        domainField.setMaximumSize(new Dimension(500, 44));
        domainField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(domainField);
        dynamicArea.add(Box.createVerticalStrut(20));

        addSectionHeader("💼 Current Role & Company");
        JPanel roleRow = new JPanel(new GridLayout(1, 2, 16, 0));
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(600, 44));
        roleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentRoleField = new DarkTextField("Your job title", 15);
        companyField = new DarkTextField("Company name", 15);
        roleRow.add(currentRoleField);
        roleRow.add(companyField);
        dynamicArea.add(roleRow);
        dynamicArea.add(Box.createVerticalStrut(20));

        addSectionHeader("⏱ Years of Experience");
        String[] expOptions = {"< 1 year", "1–2 years", "3–5 years", "6–10 years", "10+ years"};
        experienceCombo = new DarkComboBox(expOptions);
        experienceCombo.setMaximumSize(new Dimension(300, 44));
        experienceCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(experienceCombo);
        dynamicArea.add(Box.createVerticalStrut(20));

        addSectionHeader("🛠 Skills");
        JLabel skillsHint = new JLabel("List your skills separated by commas (spelling auto-corrected)");
        skillsHint.setFont(Theme.body(12));
        skillsHint.setForeground(Theme.TXT3);
        skillsHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(skillsHint);
        dynamicArea.add(Box.createVerticalStrut(6));
        skillsField = new DarkTextField("e.g. Python, Machine Learning, Project Management, SQL", 30);
        skillsField.setMaximumSize(new Dimension(600, 44));
        skillsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(skillsField);
        dynamicArea.add(Box.createVerticalStrut(20));

        addSectionHeader("⭐ Career Interests & Goals");
        JLabel intHint = new JLabel("What are you looking to achieve? (spelling auto-corrected)");
        intHint.setFont(Theme.body(12));
        intHint.setForeground(Theme.TXT3);
        intHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(intHint);
        dynamicArea.add(Box.createVerticalStrut(6));
        proInterestsField = new DarkTextField("e.g. Leadership, AI/ML, Career Pivot, Entrepreneurship", 30);
        proInterestsField.setMaximumSize(new Dimension(600, 44));
        proInterestsField.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(proInterestsField);
        dynamicArea.add(Box.createVerticalStrut(20));

        addSectionHeader("📍 Location");
        buildLocationFields();
    }

    private void buildLocationFields() {
        JPanel locationRow = new JPanel(new GridLayout(1, 2, 16, 0));
        locationRow.setOpaque(false);
        locationRow.setMaximumSize(new Dimension(600, 44));
        locationRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] stateList = IndiaData.getStates();
        stateCombo = new DarkComboBox(stateList);
        districtCombo = new DarkComboBox(IndiaData.getDistricts(stateList[0]));

        stateCombo.addActionListener(e -> {
            String state = (String) stateCombo.getSelectedItem();
            String[] districts = IndiaData.getDistricts(state);
            districtCombo.removeAllItems();
            for (String d : districts) districtCombo.addItem(d);
        });

        locationRow.add(stateCombo);
        locationRow.add(districtCombo);
        dynamicArea.add(locationRow);
        dynamicArea.add(Box.createVerticalStrut(20));
    }

    private void addSectionHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.body(14, Font.BOLD));
        lbl.setForeground(Theme.TXT);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        dynamicArea.add(lbl);
        dynamicArea.add(Box.createVerticalStrut(8));
    }

    private void addFollowUpHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.body(14, Font.BOLD));
        lbl.setForeground(Theme.TEAL);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        followUpPanel.add(lbl);
        followUpPanel.add(Box.createVerticalStrut(10));
    }

    private String getJsonValue(String json, String key) {
        if (json == null || !json.contains("\"" + key + "\"")) return null;
        try {
            String search = "\"" + key + "\":\"";
            int start = json.indexOf(search);
            if (start == -1) return null;
            start += search.length();
            int end = json.indexOf("\"", start);
            if (end > start) return json.substring(start, end);
        } catch (Exception e) {}
        return null;
    }

    private void loadProfile() {
        try {
            Map<String, String> p = UserDAO.getUnifiedProfile(user.getEmail());
            
            if (user.getRole().equals("professional")) {
                Map<String, String> pro = UserDAO.getProfessionalProfileV2(user.getEmail());
                if (pro != null) {
                    domainField.setText(pro.get("domain"));
                    experienceCombo.setSelectedItem(pro.get("experience"));
                    skillsField.setText(pro.get("skills")); 
                    proInterestsField.setText(pro.get("interests"));
                    currentRoleField.setText(pro.get("current_role"));
                    companyField.setText(pro.get("company"));
                    stateCombo.setSelectedItem(pro.get("state"));
                    
                    // Trigger district update
                    String st = pro.get("state");
                    if (st != null) {
                        districtCombo.removeAllItems();
                        for (String d : IndiaData.getDistricts(st)) districtCombo.addItem(d);
                        districtCombo.setSelectedItem(pro.get("district"));
                    }
                    return; // Done for pro
                }
            }

            if (p == null) return;

            if (user.getRole().equals("student")) {
                standardCombo.setSelectedItem(p.get("class_level"));
                updateStudentFollowUp(); // Force UI update
                
                if (streamCombo != null) streamCombo.setSelectedItem(p.get("stream"));
                
                String standard = p.get("class_level");
                if (standard != null && (standard.contains("College") || standard.contains("Postgraduate"))) {
                    if (degField != null) degField.setText(p.get("stream"));
                }
                
                institutionField.setText(p.get("institution"));
                if ("Class 12".equals(standard)) {
                    String extra = p.get("extra_info_json");
                    if (coachingField != null) coachingField.setText(getJsonValue(extra, "coaching"));
                    if (targetCollegeField != null) targetCollegeField.setText(getJsonValue(extra, "target_college"));
                }
            } else {
                domainField.setText(p.get("stream")); // In unified, stream stores domain for pros
                String extra = p.get("extra_info_json");
                experienceCombo.setSelectedItem(getJsonValue(extra, "experience"));
                skillsField.setText(p.get("interests")); 
                proInterestsField.setText(getJsonValue(extra, "pro_interests"));
                currentRoleField.setText(getJsonValue(extra, "current_role"));
                companyField.setText(p.get("institution"));
            }

            if (stateCombo != null) {
                stateCombo.setSelectedItem(p.get("state"));
                // Trigger district update manually
                String state = p.get("state");
                String[] districts = IndiaData.getDistricts(state);
                districtCombo.removeAllItems();
                for (String d : districts) districtCombo.addItem(d);
                districtCombo.setSelectedItem(p.get("district"));
            }
        } catch (Exception e) {
            System.err.println("Load profile error: " + e.getMessage());
        }
    }

    private void saveProfile() {
        try {
            statusLbl.setText("Saving profile...");
            statusLbl.setForeground(Theme.TXT2);

            String state    = stateCombo != null ? (String) stateCombo.getSelectedItem() : "";
            String district = districtCombo != null ? (String) districtCombo.getSelectedItem() : "";

            if (user.getRole().equals("student")) {
                String standard    = (String) standardCombo.getSelectedItem();
                
                // If college, use degField as stream
                String stream;
                if (standard != null && (standard.contains("College") || standard.contains("Postgraduate"))) {
                    stream = degField != null ? degField.getText().trim() : "";
                } else {
                    stream = streamCombo != null ? (String) streamCombo.getSelectedItem() : "";
                }
                
                String institution = institutionField.getText().trim();
                String interests   = interestsField.getText().trim();

                // Build extra JSON for Class 12
                String extraJson = "{}";
                if (standard.equals("Class 12")) {
                    String coaching = coachingField != null ? coachingField.getText().trim() : "";
                    String target = targetCollegeField != null ? targetCollegeField.getText().trim() : "";
                    extraJson = "{\"coaching\":\"" + coaching + "\", \"target_college\":\"" + target + "\"}";
                }

                // Save to legacy table (optional, keeping for compatibility)
                UserDAO.saveStudentProfile(user.getId(), standard, stream, state, district, institution, interests, "");
                
                // Save to new unified v2 table (linked by email)
                UserDAO.saveUnifiedProfile(user.getEmail(), standard, stream, state, district, institution, interests, extraJson);

            } else {
                String domain      = domainField.getText().trim();
                String experience  = (String) experienceCombo.getSelectedItem();
                String skills      = skillsField.getText().trim();
                String interests   = proInterestsField.getText().trim();
                String currentRole = currentRoleField.getText().trim();
                String company     = companyField.getText().trim();

                UserDAO.saveProfessionalProfile(user.getId(), domain, experience, skills, interests, currentRole, company);
                
                // Save to dedicated pro v2 table
                UserDAO.saveProfessionalProfileV2(user.getEmail(), domain, currentRole, company, experience, skills, interests, state, district);
            }

            statusLbl.setText("✓ Profile saved successfully!");
            statusLbl.setForeground(Theme.GREEN);

            // Navigate to careers after 1 second
            javax.swing.Timer t = new javax.swing.Timer(1200, e -> dashboard.navigateTo("careers"));
            t.setRepeats(false);
            t.start();

        } catch (Exception ex) {
            statusLbl.setText("Error: " + ex.getMessage());
            statusLbl.setForeground(Theme.ROSE);
        }
    }
}
