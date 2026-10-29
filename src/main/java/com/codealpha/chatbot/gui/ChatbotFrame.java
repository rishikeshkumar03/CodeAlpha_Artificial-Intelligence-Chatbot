package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.engine.ChatbotEngine;
import com.codealpha.chatbot.model.ChatMessage;
import com.codealpha.chatbot.service.ChatHistoryService;
import com.codealpha.chatbot.service.FAQService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * Main application window for the CodeAlpha AI Chatbot.
 * Manages modern header navigation, conversation view, side drawer, input bar, and footer.
 */
public class ChatbotFrame extends JFrame {

    private final ChatbotEngine chatbotEngine;
    private final FAQService faqService;
    private final ChatHistoryService historyService;

    private ChatPanel chatPanel;
    private FAQManagerPanel faqManagerPanel;
    private SidebarPanel sidebarPanel;
    private CardLayout centerCardLayout;
    private JPanel centerCardContainer;

    private JTextField messageInputField;
    private JButton sendButton;
    private JButton btnNavChat;
    private JButton btnNavFaq;
    private JButton btnToggleSidebar;

    private static final String VIEW_CHAT = "VIEW_CHAT";
    private static final String VIEW_FAQ = "VIEW_FAQ";
    private String currentView = VIEW_CHAT;

    public ChatbotFrame(ChatbotEngine chatbotEngine, FAQService faqService, ChatHistoryService historyService) {
        this.chatbotEngine = chatbotEngine;
        this.faqService = faqService;
        this.historyService = historyService;

        setTitle("CodeAlpha AI Chatbot - Educational Internship Project");
        setSize(1100, 720);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ModernTheme.BG_DARK);
        setLayout(new BorderLayout());

        buildUI();
    }

    private void buildUI() {
        // 1. Top Header Bar
        JPanel topHeader = createHeaderPanel();
        add(topHeader, BorderLayout.NORTH);

        // 2. Center Content (CardLayout: Chat View or FAQ Manager View)
        centerCardLayout = new CardLayout();
        centerCardContainer = new JPanel(centerCardLayout);
        centerCardContainer.setBackground(ModernTheme.BG_DARK);

        chatPanel = new ChatPanel();
        faqManagerPanel = new FAQManagerPanel(faqService, () -> {
            if (sidebarPanel != null) {
                sidebarPanel.updateStats(historyService.getCurrentSession());
            }
        });

        // Chat View Wrapper (ChatPanel + Bottom Input Bar)
        JPanel chatViewWrapper = new JPanel(new BorderLayout());
        chatViewWrapper.setBackground(ModernTheme.BG_DARK);
        chatViewWrapper.add(chatPanel, BorderLayout.CENTER);

        JPanel inputBar = createInputBar();
        chatViewWrapper.add(inputBar, BorderLayout.SOUTH);

        centerCardContainer.add(chatViewWrapper, VIEW_CHAT);
        centerCardContainer.add(faqManagerPanel, VIEW_FAQ);
        add(centerCardContainer, BorderLayout.CENTER);

        // 3. Right Sidebar
        sidebarPanel = new SidebarPanel(faqService, this::handleSuggestedQuestion);
        add(sidebarPanel, BorderLayout.EAST);

        // 4. Footer
        JPanel footer = createFooterPanel();
        add(footer, BorderLayout.SOUTH);

        // Send welcome message to session
        historyService.recordMessage(new ChatMessage(ChatMessage.Sender.SYSTEM, "Session started."));
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(15, 0));
        header.setBackground(ModernTheme.BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        // Brand Title & Status Pill
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("🤖", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel titleLabel = new JLabel("CodeAlpha AI Chatbot");
        titleLabel.setFont(ModernTheme.FONT_HEADER_TITLE);
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);

        // Offline Status Badge
        JPanel statusBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        statusBadge.setBackground(new Color(16, 185, 129, 25));
        statusBadge.setBorder(new ModernTheme.RoundedBorder(new Color(16, 185, 129, 90), 12, 1));

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        dot.setForeground(ModernTheme.COLOR_SUCCESS);

        JLabel statusText = new JLabel("Local Mode (Offline)");
        statusText.setFont(ModernTheme.FONT_SMALL_BOLD);
        statusText.setForeground(ModernTheme.COLOR_SUCCESS);

        statusBadge.add(dot);
        statusBadge.add(statusText);

        brandPanel.add(logoIcon);
        brandPanel.add(titleLabel);
        brandPanel.add(statusBadge);

        // Right Navigation Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        btnNavChat = ModernTheme.createStyledButton("💬 Chat", ModernTheme.ACCENT_PRIMARY, Color.WHITE);
        btnNavChat.addActionListener(e -> switchView(VIEW_CHAT));

        btnNavFaq = ModernTheme.createOutlineButton("📚 FAQ Manager", ModernTheme.ACCENT_CYAN, ModernTheme.TEXT_PRIMARY);
        btnNavFaq.addActionListener(e -> switchView(VIEW_FAQ));

        JButton btnNewChat = ModernTheme.createOutlineButton("➕ New Chat", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnNewChat.addActionListener(e -> startNewChat());

        JButton btnClear = ModernTheme.createOutlineButton("🧹 Clear", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnClear.addActionListener(e -> clearChat());

        JButton btnExport = ModernTheme.createOutlineButton("📤 Export", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnExport.addActionListener(e -> exportChatHistory());

        btnToggleSidebar = ModernTheme.createOutlineButton("📊 Drawer", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnToggleSidebar.addActionListener(e -> toggleSidebar());

        controls.add(btnNavChat);
        controls.add(btnNavFaq);
        controls.add(btnNewChat);
        controls.add(btnClear);
        controls.add(btnExport);
        controls.add(btnToggleSidebar);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JPanel createInputBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(ModernTheme.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 18, 12, 18)
        ));

        // Rounded container for text field
        JPanel inputWrapper = new JPanel(new BorderLayout(8, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                ModernTheme.applyQualityRendering(g2);
                g2.setColor(ModernTheme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(ModernTheme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(new EmptyBorder(4, 12, 4, 12));

        messageInputField = new JTextField();
        messageInputField.setFont(ModernTheme.FONT_BODY);
        messageInputField.setForeground(ModernTheme.TEXT_PRIMARY);
        messageInputField.setCaretColor(ModernTheme.TEXT_PRIMARY);
        messageInputField.setBackground(ModernTheme.BG_INPUT);
        messageInputField.setOpaque(false);
        messageInputField.setBorder(null);

        // Enter key to send
        messageInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        inputWrapper.add(messageInputField, BorderLayout.CENTER);

        // Send Button
        sendButton = ModernTheme.createStyledButton("Send ➢", ModernTheme.ACCENT_PRIMARY, Color.WHITE);
        sendButton.setPreferredSize(new Dimension(95, 38));
        sendButton.addActionListener(e -> sendMessage());

        bar.add(inputWrapper, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);
        return bar;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
        footer.setBackground(ModernTheme.BG_CARD);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER_SUBTLE));

        JLabel copyright = new JLabel("Internship Project at CodeAlpha | © 2026 CodeAlpha. All Rights Reserved.");
        copyright.setFont(ModernTheme.FONT_SMALL);
        copyright.setForeground(ModernTheme.TEXT_MUTED);

        footer.add(copyright);
        return footer;
    }

    private void switchView(String view) {
        this.currentView = view;
        centerCardLayout.show(centerCardContainer, view);
        if (VIEW_CHAT.equals(view)) {
            btnNavChat.setBackground(ModernTheme.ACCENT_PRIMARY);
            btnNavChat.setForeground(Color.WHITE);
            messageInputField.requestFocusInWindow();
        } else {
            faqManagerPanel.refreshTableData();
        }
    }

    private void toggleSidebar() {
        sidebarPanel.setVisible(!sidebarPanel.isVisible());
        revalidate();
        repaint();
    }

    private void handleSuggestedQuestion(String question) {
        if (!VIEW_CHAT.equals(currentView)) {
            switchView(VIEW_CHAT);
        }
        messageInputField.setText(question);
        sendMessage();
    }

    private void sendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        // Clear input field immediately
        messageInputField.setText("");
        messageInputField.setEnabled(false);
        sendButton.setEnabled(false);

        // Create and display user message
        ChatMessage userMsg = new ChatMessage(ChatMessage.Sender.USER, text);
        chatPanel.addMessage(userMsg);
        historyService.recordMessage(userMsg);
        sidebarPanel.updateStats(historyService.getCurrentSession());

        // Show typing indicator
        chatPanel.showTypingIndicator();

        // Process query asynchronously using SwingWorker
        SwingWorker<ChatMessage, Void> worker = new SwingWorker<>() {
            @Override
            protected ChatMessage doInBackground() throws Exception {
                // Brief natural response delay for UX feel (300ms)
                Thread.sleep(300);
                return chatbotEngine.processQuery(text);
            }

            @Override
            protected void done() {
                chatPanel.hideTypingIndicator();
                messageInputField.setEnabled(true);
                sendButton.setEnabled(true);
                messageInputField.requestFocusInWindow();

                try {
                    ChatMessage botMsg = get();
                    chatPanel.addMessage(botMsg);
                    historyService.recordMessage(botMsg);
                    sidebarPanel.updateStats(historyService.getCurrentSession());
                } catch (InterruptedException | ExecutionException e) {
                    ChatMessage errorMsg = new ChatMessage(ChatMessage.Sender.BOT,
                            "An unexpected error occurred while processing: " + e.getMessage());
                    chatPanel.addMessage(errorMsg);
                }
            }
        };

        worker.execute();
    }

    private void startNewChat() {
        historyService.startNewSession();
        chatPanel.clearChat();
        sidebarPanel.updateStats(historyService.getCurrentSession());
        if (!VIEW_CHAT.equals(currentView)) {
            switchView(VIEW_CHAT);
        }
        messageInputField.requestFocusInWindow();
    }

    private void clearChat() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear the conversation display?",
                "Clear Chat",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm == JOptionPane.YES_OPTION) {
            chatPanel.clearChat();
            historyService.clearHistory();
            sidebarPanel.updateStats(historyService.getCurrentSession());
        }
    }

    private void exportChatHistory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("codealpha_chat_export.md"));
        chooser.setDialogTitle("Export Conversation History");
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File dest = chooser.getSelectedFile();
            try {
                historyService.exportConversation(dest);
                JOptionPane.showMessageDialog(this,
                        "Conversation history exported successfully to:\n" + dest.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export chat: " + ex.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
