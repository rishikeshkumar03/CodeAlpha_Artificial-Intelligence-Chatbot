package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.model.ChatSession;
import com.codealpha.chatbot.service.FAQService;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Collapsible side panel hosting Suggested Questions, Conversation Stats, and CodeAlpha Project Info.
 */
public class SidebarPanel extends JPanel {

    private final FAQService faqService;
    private final Consumer<String> onQuestionSelected;

    private JLabel lblTotalMessages;
    private JLabel lblUserQueries;
    private JLabel lblBotResponses;
    private JLabel lblFaqCount;
    private JLabel lblAvgConfidence;

    private static final String[] SUGGESTIONS = {
            "What is artificial intelligence?",
            "What is Java?",
            "What are the four pillars of OOP?",
            "What is machine learning?",
            "Difference between JDK, JRE, and JVM?",
            "What is an algorithm?",
            "Abstract class vs interface in Java",
            "What is Big O notation?",
            "How does multithreading work in Java?",
            "What is natural language processing?"
    };

    public SidebarPanel(FAQService faqService, Consumer<String> onQuestionSelected) {
        this.faqService = faqService;
        this.onQuestionSelected = onQuestionSelected;

        setPreferredSize(new Dimension(300, 0));
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_CARD);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, ModernTheme.BORDER_SUBTLE));

        buildUI();
    }

    private void buildUI() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 14, 15, 14));

        // 1. Suggested Questions Section
        contentPanel.add(createSectionHeader("💡 Suggested Questions"));
        contentPanel.add(Box.createVerticalStrut(8));

        JPanel suggestionsBox = new JPanel();
        suggestionsBox.setLayout(new BoxLayout(suggestionsBox, BoxLayout.Y_AXIS));
        suggestionsBox.setOpaque(false);

        for (String q : SUGGESTIONS) {
            JButton chip = createQuestionChip(q);
            suggestionsBox.add(chip);
            suggestionsBox.add(Box.createVerticalStrut(5));
        }

        contentPanel.add(suggestionsBox);
        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Statistics Section
        contentPanel.add(createSectionHeader("📊 Session Statistics"));
        contentPanel.add(Box.createVerticalStrut(8));

        JPanel statsCard = createStatsCard();
        contentPanel.add(statsCard);
        contentPanel.add(Box.createVerticalStrut(18));

        // 3. Project Information Section
        contentPanel.add(createSectionHeader("ℹ️ Project Information"));
        contentPanel.add(Box.createVerticalStrut(8));

        JPanel aboutCard = createAboutCard();
        contentPanel.add(aboutCard);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUI(new ModernTheme.ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String title) {
        JLabel header = new JLabel(title);
        header.setFont(ModernTheme.FONT_TITLE);
        header.setForeground(ModernTheme.TEXT_PRIMARY);
        return header;
    }

    private JButton createQuestionChip(String question) {
        JButton btn = new JButton("• " + question) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);
                if (getModel().isRollover()) {
                    g2.setColor(ModernTheme.BG_HOVER);
                } else {
                    g2.setColor(ModernTheme.BG_INPUT);
                }
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.setColor(ModernTheme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(ModernTheme.FONT_SMALL);
        btn.setForeground(ModernTheme.TEXT_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(5, 8, 5, 8));

        btn.addActionListener(e -> {
            if (onQuestionSelected != null) {
                onQuestionSelected.accept(question);
            }
        });

        return btn;
    }

    private JPanel createStatsCard() {
        JPanel card = new JPanel(new GridLayout(5, 2, 6, 6));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 10, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblTotalMessages = createStatValue("0");
        lblUserQueries = createStatValue("0");
        lblBotResponses = createStatValue("0");
        lblFaqCount = createStatValue(String.valueOf(faqService.getAllFAQs().size()));
        lblAvgConfidence = createStatValue("0%");

        card.add(createStatLabel("Total Messages:"));
        card.add(lblTotalMessages);
        card.add(createStatLabel("User Inquiries:"));
        card.add(lblUserQueries);
        card.add(createStatLabel("AI Responses:"));
        card.add(lblBotResponses);
        card.add(createStatLabel("FAQs in DB:"));
        card.add(lblFaqCount);
        card.add(createStatLabel("Avg Confidence:"));
        card.add(lblAvgConfidence);

        return card;
    }

    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ModernTheme.FONT_SMALL);
        label.setForeground(ModernTheme.TEXT_MUTED);
        return label;
    }

    private JLabel createStatValue(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(ModernTheme.FONT_SMALL_BOLD);
        label.setForeground(ModernTheme.ACCENT_CYAN);
        return label;
    }

    private JPanel createAboutCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 10, 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel title = new JLabel("CodeAlpha AI Chatbot");
        title.setFont(ModernTheme.FONT_BODY_BOLD);
        title.setForeground(ModernTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Internship Project");
        subtitle.setFont(ModernTheme.FONT_SMALL);
        subtitle.setForeground(ModernTheme.ACCENT_PRIMARY);

        JTextArea desc = new JTextArea(
                "Java-based Artificial Intelligence Chatbot\n" +
                "Developed as a CodeAlpha Internship Project\n\n" +
                "• 100% Java & Java Swing\n" +
                "• Local Explainable NLP\n" +
                "• Trainable Knowledge Base\n" +
                "• Persistent RFC 4180 CSV"
        );
        desc.setFont(ModernTheme.FONT_SMALL);
        desc.setForeground(ModernTheme.TEXT_SECONDARY);
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(null);

        card.add(title);
        card.add(Box.createVerticalStrut(2));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);

        return card;
    }

    /**
     * Updates displayed statistics based on the active session.
     */
    public void updateStats(ChatSession session) {
        if (session == null) return;
        lblTotalMessages.setText(String.valueOf(session.getTotalMessageCount()));
        lblUserQueries.setText(String.valueOf(session.getUserMessageCount()));
        lblBotResponses.setText(String.valueOf(session.getBotMessageCount()));
        lblFaqCount.setText(String.valueOf(faqService.getAllFAQs().size()));

        double avgConf = session.getAverageConfidence();
        if (avgConf > 0) {
            lblAvgConfidence.setText((int) Math.round(avgConf * 100) + "%");
        } else {
            lblAvgConfidence.setText("N/A");
        }
    }
}
