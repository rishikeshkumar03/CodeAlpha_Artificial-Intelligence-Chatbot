package com.codealpha.chatbot.gui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Modern dark-mode color palette, fonts, styling helpers, and custom UI components.
 */
public final class ModernTheme {

    private ModernTheme() {}

    // Core Backgrounds
    public static final Color BG_DARK = new Color(13, 17, 23);           // #0D1117 - Deepest background
    public static final Color BG_CARD = new Color(22, 27, 34);           // #161B22 - Panels, cards, header
    public static final Color BG_INPUT = new Color(30, 36, 46);          // #1E242E - Text field background
    public static final Color BG_HOVER = new Color(38, 44, 56);          // Hover state

    // Accent Colors
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241);   // #6366F1 - Indigo
    public static final Color ACCENT_HOVER = new Color(79, 70, 229);      // #4F46E5 - Darker Indigo
    public static final Color ACCENT_GRADIENT_START = new Color(99, 102, 241);
    public static final Color ACCENT_GRADIENT_END = new Color(139, 92, 246); // #8B5CF6 - Violet
    public static final Color ACCENT_CYAN = new Color(56, 189, 248);     // #38BDF8 - Sky Blue

    // Message Bubble Colors
    public static final Color BUBBLE_USER = new Color(79, 70, 229);      // Deep Indigo
    public static final Color BUBBLE_BOT = new Color(28, 33, 44);        // Charcoal Slate
    public static final Color BUBBLE_BOT_BORDER = new Color(45, 55, 72); // Subtle border

    // Status Colors
    public static final Color COLOR_SUCCESS = new Color(16, 185, 129);   // #10B981 - Green
    public static final Color COLOR_WARNING = new Color(245, 158, 11);   // #F59E0B - Amber
    public static final Color COLOR_DANGER = new Color(239, 68, 68);     // #EF4444 - Rose Red
    public static final Color COLOR_INFO = new Color(59, 130, 246);      // #3B82F6 - Blue

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // #F8FAFC - Near White
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // #94A3B8 - Slate
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748B - Muted Slate
    public static final Color TEXT_HIGHLIGHT = new Color(165, 180, 252); // Soft Indigo

    // Borders
    public static final Color BORDER_SUBTLE = new Color(33, 38, 45);     // Dark border
    public static final Color BORDER_FOCUS = new Color(99, 102, 241);

    // Fonts
    public static final Font FONT_HEADER_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 11);
    public static final Font FONT_CODE = new Font("Consolas", Font.PLAIN, 12);

    /**
     * Configures high-quality anti-aliasing rendering hints on a Graphics2D context.
     */
    public static void applyQualityRendering(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Creates a styled modern button with rounded corners and hover effect.
     */
    public static JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityRendering(g2);
                if (getModel().isPressed()) {
                    g2.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bgColor.brighter());
                } else {
                    g2.setColor(bgColor);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BODY_BOLD);
        button.setForeground(textColor);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(7, 14, 7, 14));
        return button;
    }

    /**
     * Creates a styled outline button with subtle hover background.
     */
    public static JButton createOutlineButton(String text, Color borderColor, Color textColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                applyQualityRendering(g2);
                if (getModel().isRollover() || getModel().isPressed()) {
                    g2.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 35));
                    g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_SMALL_BOLD);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(5, 11, 5, 11));
        return button;
    }

    /**
     * Custom sleek dark scrollbar UI.
     */
    public static class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(55, 65, 81);
            this.trackColor = BG_DARK;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityRendering(g2);
            g2.setColor(isThumbRollover() ? new Color(99, 102, 241, 180) : thumbColor);
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2, thumbBounds.width - 4, thumbBounds.height - 4, 8, 8);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    /**
     * Applies rounded border with custom padding and line color.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            applyQualityRendering(g2);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x + thickness / 2, y + thickness / 2,
                    width - thickness - 1, height - thickness - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }
    }
}
