package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.model.ChatMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Custom painted chat message bubble with sender avatars, timestamps, and confidence badges.
 */
public class MessageBubble extends JPanel {

    private final ChatMessage message;
    private static final int MAX_BUBBLE_WIDTH = 580;
    private static final int CORNER_RADIUS = 16;

    public MessageBubble(ChatMessage message) {
        this.message = message;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        if (message.isSystem()) {
            buildSystemBubble();
            return;
        }

        JPanel rowPanel = new JPanel(new FlowLayout(message.isUser() ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 6));
        rowPanel.setOpaque(false);

        // Bubble container
        JPanel bubbleContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);

                int w = getWidth();
                int h = getHeight();

                if (message.isUser()) {
                    // Vibrant indigo gradient
                    GradientPaint gradient = new GradientPaint(
                            0, 0, ModernTheme.ACCENT_GRADIENT_START,
                            w, h, ModernTheme.ACCENT_GRADIENT_END
                    );
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);
                } else {
                    // Dark slate bot bubble with subtle border
                    g2.setColor(ModernTheme.BUBBLE_BOT);
                    g2.fillRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);

                    g2.setColor(ModernTheme.BUBBLE_BOT_BORDER);
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(0, 0, w - 1, h - 1, CORNER_RADIUS, CORNER_RADIUS);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        bubbleContent.setLayout(new BoxLayout(bubbleContent, BoxLayout.Y_AXIS));
        bubbleContent.setOpaque(false);
        bubbleContent.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Header (Sender title + Category/Confidence tag for Bot)
        JPanel metaHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        metaHeader.setOpaque(false);

        JLabel senderLabel = new JLabel(message.isUser() ? "You" : "CodeAlpha AI");
        senderLabel.setFont(ModernTheme.FONT_SMALL_BOLD);
        senderLabel.setForeground(message.isUser() ? ModernTheme.TEXT_HIGHLIGHT : ModernTheme.ACCENT_CYAN);
        metaHeader.add(senderLabel);

        if (message.isBot() && message.getConfidenceScore() > 0) {
            String badgeText = String.format("%d%% %s",
                    (int) Math.round(message.getConfidenceScore() * 100),
                    message.getConfidenceLevel());
            JLabel badge = new JLabel(" • " + badgeText);
            badge.setFont(ModernTheme.FONT_SMALL);
            badge.setForeground(getConfidenceColor(message.getConfidenceLevel()));
            metaHeader.add(badge);
        }

        if (message.getCategory() != null && !message.getCategory().isEmpty()) {
            JLabel categoryLabel = new JLabel("[" + message.getCategory() + "]");
            categoryLabel.setFont(ModernTheme.FONT_SMALL);
            categoryLabel.setForeground(ModernTheme.TEXT_MUTED);
            metaHeader.add(categoryLabel);
        }

        bubbleContent.add(metaHeader);
        bubbleContent.add(Box.createVerticalStrut(6));

        // Message text area (auto-wrapped)
        JTextArea textArea = new JTextArea(message.getContent());
        textArea.setFont(ModernTheme.FONT_BODY);
        textArea.setForeground(ModernTheme.TEXT_PRIMARY);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFocusable(false);
        textArea.setBorder(null);

        // Constrain width for proper word wrap calculation
        int preferredWidth = Math.min(MAX_BUBBLE_WIDTH, Math.max(220, calculateTextWidth(message.getContent()) + 40));
        textArea.setSize(new Dimension(preferredWidth, Short.MAX_VALUE));
        bubbleContent.add(textArea);

        bubbleContent.add(Box.createVerticalStrut(6));

        // Footer with timestamp & copy button
        JPanel footerPanel = new JPanel(new BorderLayout(5, 0));
        footerPanel.setOpaque(false);

        JLabel timeLabel = new JLabel(message.getFormattedTime());
        timeLabel.setFont(ModernTheme.FONT_SMALL);
        timeLabel.setForeground(message.isUser() ? new Color(224, 231, 255) : ModernTheme.TEXT_MUTED);
        footerPanel.add(timeLabel, BorderLayout.WEST);

        if (message.isBot()) {
            JButton copyBtn = new JButton("Copy") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    ModernTheme.applyQualityRendering(g2);
                    if (getModel().isRollover()) {
                        g2.setColor(new Color(255, 255, 255, 30));
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            copyBtn.setFont(ModernTheme.FONT_SMALL);
            copyBtn.setForeground(ModernTheme.TEXT_MUTED);
            copyBtn.setOpaque(false);
            copyBtn.setContentAreaFilled(false);
            copyBtn.setBorderPainted(false);
            copyBtn.setFocusPainted(false);
            copyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            copyBtn.setMargin(new Insets(1, 4, 1, 4));
            copyBtn.setToolTipText("Copy answer to clipboard");
            copyBtn.addActionListener(e -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(message.getContent()), null);
                copyBtn.setText("Copied!");
                Timer t = new Timer(1500, evt -> copyBtn.setText("Copy"));
                t.setRepeats(false);
                t.start();
            });
            footerPanel.add(copyBtn, BorderLayout.EAST);
        }

        bubbleContent.add(footerPanel);

        // Add Avatar and Bubble
        if (!message.isUser()) {
            JLabel botAvatar = createAvatarLabel("AI", ModernTheme.ACCENT_PRIMARY);
            rowPanel.add(botAvatar);
        }

        rowPanel.add(bubbleContent);

        if (message.isUser()) {
            JLabel userAvatar = createAvatarLabel("U", ModernTheme.ACCENT_GRADIENT_END);
            rowPanel.add(userAvatar);
        }

        add(rowPanel, BorderLayout.CENTER);
    }

    private void buildSystemBubble() {
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);

        JLabel sysLabel = new JLabel(message.getContent());
        sysLabel.setFont(ModernTheme.FONT_SMALL);
        sysLabel.setForeground(ModernTheme.TEXT_MUTED);
        sysLabel.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 12, 1),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        centerPanel.add(sysLabel);
        add(centerPanel, BorderLayout.CENTER);
    }

    private JLabel createAvatarLabel(String initial, Color bg) {
        JLabel label = new JLabel(initial, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setPreferredSize(new Dimension(32, 32));
        label.setFont(ModernTheme.FONT_SMALL_BOLD);
        label.setForeground(Color.WHITE);
        return label;
    }

    private Color getConfidenceColor(ChatMessage.ConfidenceLevel level) {
        if (level == null) return ModernTheme.TEXT_MUTED;
        return switch (level) {
            case HIGH -> ModernTheme.COLOR_SUCCESS;
            case MEDIUM -> ModernTheme.COLOR_INFO;
            case LOW -> ModernTheme.COLOR_WARNING;
            default -> ModernTheme.TEXT_MUTED;
        };
    }

    private int calculateTextWidth(String text) {
        if (text == null) return 100;
        FontMetrics fm = getFontMetrics(ModernTheme.FONT_BODY);
        int max = 0;
        for (String line : text.split("\n")) {
            int w = fm.stringWidth(line);
            if (w > max) max = w;
        }
        return Math.min(MAX_BUBBLE_WIDTH, max);
    }
}
