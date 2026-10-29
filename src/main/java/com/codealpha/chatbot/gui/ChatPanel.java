package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.model.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main conversation viewport displaying the message history and animated typing indicator.
 */
public class ChatPanel extends JPanel {

    private final JPanel messagesContainer;
    private final JScrollPane scrollPane;
    private final JPanel typingIndicatorPanel;
    private final JLabel typingLabel;
    private final Timer typingTimer;
    private int typingDotCount = 0;
    private final List<ChatMessage> messageHistory;

    public ChatPanel() {
        this.messageHistory = new ArrayList<>();
        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(ModernTheme.BG_DARK);
        messagesContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        scrollPane = new JScrollPane(messagesContainer);
        scrollPane.setBorder(null);
        scrollPane.setBackground(ModernTheme.BG_DARK);
        scrollPane.getViewport().setBackground(ModernTheme.BG_DARK);
        scrollPane.getVerticalScrollBar().setUI(new ModernTheme.ModernScrollBarUI());
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        // Typing indicator
        typingIndicatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        typingIndicatorPanel.setBackground(ModernTheme.BG_DARK);
        typingIndicatorPanel.setVisible(false);

        JLabel typingAvatar = new JLabel("AI", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);
                g2.setColor(ModernTheme.ACCENT_PRIMARY);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        typingAvatar.setPreferredSize(new Dimension(30, 30));
        typingAvatar.setFont(ModernTheme.FONT_SMALL_BOLD);
        typingAvatar.setForeground(Color.WHITE);

        typingLabel = new JLabel("CodeAlpha AI is thinking");
        typingLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        typingLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        typingLabel.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 12, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        typingIndicatorPanel.add(typingAvatar);
        typingIndicatorPanel.add(typingLabel);
        add(typingIndicatorPanel, BorderLayout.SOUTH);

        // Typing animation timer
        typingTimer = new Timer(350, e -> {
            typingDotCount = (typingDotCount + 1) % 4;
            String dots = ".".repeat(typingDotCount);
            typingLabel.setText("CodeAlpha AI is thinking" + dots);
        });

        // Initial welcome card
        showWelcomeMessage();
    }

    public void showWelcomeMessage() {
        messagesContainer.removeAll();
        messageHistory.clear();

        JPanel welcomeCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);
                g2.setColor(ModernTheme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(ModernTheme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        welcomeCard.setLayout(new BoxLayout(welcomeCard, BoxLayout.Y_AXIS));
        welcomeCard.setOpaque(false);
        welcomeCard.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        welcomeCard.setMaximumSize(new Dimension(680, 240));

        JLabel title = new JLabel("Welcome to CodeAlpha AI Chatbot");
        title.setFont(ModernTheme.FONT_HEADER_TITLE);
        title.setForeground(ModernTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Local NLP & Intelligent Knowledge-Base Assistant");
        subtitle.setFont(ModernTheme.FONT_BODY);
        subtitle.setForeground(ModernTheme.ACCENT_CYAN);

        JTextArea desc = new JTextArea(
                "Ask questions in natural English about Java Programming, Artificial Intelligence, " +
                "OOP Concepts, Data Structures, or Technology.\n" +
                "• 100% Offline with zero external API calls.\n" +
                "• Powered by Explainable Java NLP matching (Jaccard, Levenshtein, Keywords).\n" +
                "• Train new questions in real-time via the FAQ Manager."
        );
        desc.setFont(ModernTheme.FONT_SMALL);
        desc.setForeground(ModernTheme.TEXT_SECONDARY);
        desc.setOpaque(false);
        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setBorder(null);

        welcomeCard.add(title);
        welcomeCard.add(Box.createVerticalStrut(4));
        welcomeCard.add(subtitle);
        welcomeCard.add(Box.createVerticalStrut(10));
        welcomeCard.add(desc);

        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(welcomeCard);

        messagesContainer.add(wrapper);
        messagesContainer.add(Box.createVerticalStrut(15));
        messagesContainer.revalidate();
        messagesContainer.repaint();
    }

    public void addMessage(ChatMessage message) {
        if (message == null) return;
        messageHistory.add(message);

        MessageBubble bubble = new MessageBubble(message);
        messagesContainer.add(bubble);
        messagesContainer.add(Box.createVerticalStrut(8));

        messagesContainer.revalidate();
        messagesContainer.repaint();
        scrollToBottom();
    }

    public void showTypingIndicator() {
        typingIndicatorPanel.setVisible(true);
        typingTimer.start();
        scrollToBottom();
    }

    public void hideTypingIndicator() {
        typingTimer.stop();
        typingIndicatorPanel.setVisible(false);
        typingDotCount = 0;
        typingLabel.setText("CodeAlpha AI is thinking");
    }

    public void clearChat() {
        showWelcomeMessage();
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public List<ChatMessage> getMessageHistory() {
        return new ArrayList<>(messageHistory);
    }
}
