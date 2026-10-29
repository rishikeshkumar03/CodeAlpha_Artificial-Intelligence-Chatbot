package com.codealpha.chatbot.gui;

import com.codealpha.chatbot.model.FAQ;
import com.codealpha.chatbot.service.FAQService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modern FAQ knowledge-base management panel supporting live search, category filtering,
 * add/edit/delete CRUD dialogs, and CSV import/export.
 */
public class FAQManagerPanel extends JPanel {

    private final FAQService faqService;
    private final Runnable onDataChanged;

    private JTable faqTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilterBox;

    // Detail view fields
    private JLabel lblDetailId;
    private JLabel lblDetailCategory;
    private JLabel lblDetailWeight;
    private JTextArea txtDetailQuestion;
    private JTextArea txtDetailAlts;
    private JTextArea txtDetailKeywords;
    private JTextArea txtDetailAnswer;

    private List<FAQ> currentDisplayList = new ArrayList<>();

    public FAQManagerPanel(FAQService faqService, Runnable onDataChanged) {
        this.faqService = faqService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout());
        setBackground(ModernTheme.BG_DARK);
        buildUI();
        refreshTableData();
    }

    private void buildUI() {
        // Top Toolbar
        JPanel topBar = new JPanel(new BorderLayout(10, 10));
        topBar.setBackground(ModernTheme.BG_CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Search and Category Filter
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("🔍 Search:");
        searchLabel.setFont(ModernTheme.FONT_BODY_BOLD);
        searchLabel.setForeground(ModernTheme.TEXT_PRIMARY);

        searchField = new JTextField(18);
        searchField.setFont(ModernTheme.FONT_BODY);
        searchField.setBackground(ModernTheme.BG_INPUT);
        searchField.setForeground(ModernTheme.TEXT_PRIMARY);
        searchField.setCaretColor(ModernTheme.TEXT_PRIMARY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 8, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterData(); }
            @Override public void removeUpdate(DocumentEvent e) { filterData(); }
            @Override public void changedUpdate(DocumentEvent e) { filterData(); }
        });

        JLabel catLabel = new JLabel("Category:");
        catLabel.setFont(ModernTheme.FONT_BODY_BOLD);
        catLabel.setForeground(ModernTheme.TEXT_PRIMARY);

        categoryFilterBox = new JComboBox<>();
        categoryFilterBox.setFont(ModernTheme.FONT_BODY);
        categoryFilterBox.setBackground(ModernTheme.BG_INPUT);
        categoryFilterBox.setForeground(ModernTheme.TEXT_PRIMARY);
        categoryFilterBox.addActionListener(e -> filterData());
        populateCategoryFilter();

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(catLabel);
        searchPanel.add(categoryFilterBox);

        // Action Buttons
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton btnAdd = ModernTheme.createStyledButton("+ Add FAQ", ModernTheme.ACCENT_PRIMARY, Color.WHITE);
        btnAdd.addActionListener(e -> showAddFaqDialog());

        JButton btnEdit = ModernTheme.createOutlineButton("✏️ Edit", ModernTheme.ACCENT_CYAN, ModernTheme.TEXT_PRIMARY);
        btnEdit.addActionListener(e -> showEditFaqDialog());

        JButton btnDelete = ModernTheme.createOutlineButton("🗑️ Delete", ModernTheme.COLOR_DANGER, ModernTheme.COLOR_DANGER);
        btnDelete.addActionListener(e -> deleteSelectedFaq());

        JButton btnImport = ModernTheme.createOutlineButton("📥 Import", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnImport.addActionListener(e -> importCSV());

        JButton btnExport = ModernTheme.createOutlineButton("📤 Export", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
        btnExport.addActionListener(e -> exportCSV());

        actionButtons.add(btnAdd);
        actionButtons.add(btnEdit);
        actionButtons.add(btnDelete);
        actionButtons.add(btnImport);
        actionButtons.add(btnExport);

        topBar.add(searchPanel, BorderLayout.WEST);
        topBar.add(actionButtons, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Split Pane: Table on Top/Left, Details on Bottom/Right
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        splitPane.setResizeWeight(0.60);

        // Table setup
        String[] columnNames = {"ID", "Category", "Primary Question", "Weight"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        faqTable = new JTable(tableModel);
        faqTable.setFont(ModernTheme.FONT_BODY);
        faqTable.setRowHeight(28);
        faqTable.setBackground(ModernTheme.BG_DARK);
        faqTable.setForeground(ModernTheme.TEXT_PRIMARY);
        faqTable.setSelectionBackground(ModernTheme.BG_HOVER);
        faqTable.setSelectionForeground(ModernTheme.ACCENT_CYAN);
        faqTable.setGridColor(ModernTheme.BORDER_SUBTLE);
        faqTable.setShowGrid(true);
        faqTable.setFillsViewportHeight(true);

        JTableHeader header = faqTable.getTableHeader();
        header.setFont(ModernTheme.FONT_BODY_BOLD);
        header.setBackground(ModernTheme.BG_CARD);
        header.setForeground(ModernTheme.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER_SUBTLE));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        faqTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        faqTable.getColumnModel().getColumn(0).setMaxWidth(110);
        faqTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        faqTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        faqTable.getColumnModel().getColumn(1).setMaxWidth(220);

        faqTable.getColumnModel().getColumn(2).setPreferredWidth(450);

        faqTable.getColumnModel().getColumn(3).setPreferredWidth(70);
        faqTable.getColumnModel().getColumn(3).setMaxWidth(90);
        faqTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        faqTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailView();
            }
        });

        JScrollPane tableScroll = new JScrollPane(faqTable);
        tableScroll.setBorder(null);
        tableScroll.getVerticalScrollBar().setUI(new ModernTheme.ModernScrollBarUI());
        tableScroll.getViewport().setBackground(ModernTheme.BG_DARK);
        splitPane.setTopComponent(tableScroll);

        // Details Panel
        JPanel detailPanel = createDetailPanel();
        splitPane.setBottomComponent(detailPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(ModernTheme.BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Details Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        headerPanel.setOpaque(false);

        lblDetailId = new JLabel("ID: -");
        lblDetailId.setFont(ModernTheme.FONT_BODY_BOLD);
        lblDetailId.setForeground(ModernTheme.ACCENT_CYAN);

        lblDetailCategory = new JLabel("Category: -");
        lblDetailCategory.setFont(ModernTheme.FONT_BODY);
        lblDetailCategory.setForeground(ModernTheme.TEXT_SECONDARY);

        lblDetailWeight = new JLabel("Weight: 1.0");
        lblDetailWeight.setFont(ModernTheme.FONT_BODY);
        lblDetailWeight.setForeground(ModernTheme.TEXT_MUTED);

        headerPanel.add(lblDetailId);
        headerPanel.add(lblDetailCategory);
        headerPanel.add(lblDetailWeight);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Details Fields Grid
        JPanel fieldsGrid = new JPanel(new GridLayout(2, 2, 12, 8));
        fieldsGrid.setOpaque(false);

        txtDetailQuestion = createReadOnlyTextArea("Primary Question will appear here...");
        txtDetailAlts = createReadOnlyTextArea("Alternative Questions...");
        txtDetailKeywords = createReadOnlyTextArea("Keywords...");
        txtDetailAnswer = createReadOnlyTextArea("Full Answer will appear here...");

        fieldsGrid.add(createLabeledBox("Primary Question", txtDetailQuestion));
        fieldsGrid.add(createLabeledBox("Alternative Phrasings", txtDetailAlts));
        fieldsGrid.add(createLabeledBox("Associated Keywords", txtDetailKeywords));
        fieldsGrid.add(createLabeledBox("Stored Answer", txtDetailAnswer));

        panel.add(fieldsGrid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createLabeledBox(String label, JTextArea textArea) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setFont(ModernTheme.FONT_SMALL_BOLD);
        lbl.setForeground(ModernTheme.TEXT_SECONDARY);

        JScrollPane sp = new JScrollPane(textArea);
        sp.setBorder(new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 8, 1));
        sp.getVerticalScrollBar().setUI(new ModernTheme.ModernScrollBarUI());
        sp.getViewport().setBackground(ModernTheme.BG_INPUT);

        box.add(lbl, BorderLayout.NORTH);
        box.add(sp, BorderLayout.CENTER);
        return box;
    }

    private JTextArea createReadOnlyTextArea(String initialText) {
        JTextArea area = new JTextArea(initialText);
        area.setFont(ModernTheme.FONT_BODY);
        area.setBackground(ModernTheme.BG_INPUT);
        area.setForeground(ModernTheme.TEXT_PRIMARY);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        return area;
    }

    private void populateCategoryFilter() {
        categoryFilterBox.removeAllItems();
        categoryFilterBox.addItem("All Categories");
        for (String cat : faqService.getCategories()) {
            categoryFilterBox.addItem(cat);
        }
    }

    public void refreshTableData() {
        populateCategoryFilter();
        filterData();
    }

    private void filterData() {
        String query = searchField != null ? searchField.getText() : "";
        String cat = (categoryFilterBox != null && categoryFilterBox.getSelectedItem() != null)
                ? (String) categoryFilterBox.getSelectedItem() : "All Categories";

        currentDisplayList = faqService.searchFAQs(query, cat);
        tableModel.setRowCount(0);

        for (FAQ faq : currentDisplayList) {
            tableModel.addRow(new Object[]{
                    faq.getId(),
                    faq.getCategory(),
                    faq.getPrimaryQuestion(),
                    faq.getWeight()
            });
        }

        if (!currentDisplayList.isEmpty()) {
            faqTable.setRowSelectionInterval(0, 0);
            updateDetailView();
        } else {
            clearDetailView();
        }
    }

    private void updateDetailView() {
        int selectedRow = faqTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentDisplayList.size()) {
            FAQ faq = currentDisplayList.get(selectedRow);
            lblDetailId.setText("ID: " + faq.getId());
            lblDetailCategory.setText("Category: " + faq.getCategory());
            lblDetailWeight.setText("Weight: " + faq.getWeight());
            txtDetailQuestion.setText(faq.getPrimaryQuestion());
            txtDetailAlts.setText(String.join("\n• ", faq.getAlternativeQuestions()));
            txtDetailKeywords.setText(String.join(", ", faq.getKeywords()));
            txtDetailAnswer.setText(faq.getAnswer());
        }
    }

    private void clearDetailView() {
        lblDetailId.setText("ID: -");
        lblDetailCategory.setText("Category: -");
        lblDetailWeight.setText("Weight: -");
        txtDetailQuestion.setText("");
        txtDetailAlts.setText("");
        txtDetailKeywords.setText("");
        txtDetailAnswer.setText("");
    }

    private void showAddFaqDialog() {
        FAQEditDialog dialog = new FAQEditDialog((Frame) SwingUtilities.getWindowAncestor(this), null, faqService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTableData();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void showEditFaqDialog() {
        int row = faqTable.getSelectedRow();
        if (row < 0 || row >= currentDisplayList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an FAQ from the table to edit.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FAQ selected = currentDisplayList.get(row);
        FAQEditDialog dialog = new FAQEditDialog((Frame) SwingUtilities.getWindowAncestor(this), selected, faqService);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshTableData();
            if (onDataChanged != null) onDataChanged.run();
        }
    }

    private void deleteSelectedFaq() {
        int row = faqTable.getSelectedRow();
        if (row < 0 || row >= currentDisplayList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an FAQ to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        FAQ selected = currentDisplayList.get(row);
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete FAQ '" + selected.getId() + "' (" + selected.getPrimaryQuestion() + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                faqService.deleteFAQ(selected.getId());
                refreshTableData();
                if (onDataChanged != null) onDataChanged.run();
                JOptionPane.showMessageDialog(this, "FAQ deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error deleting FAQ: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Knowledge Base CSV");
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                int imported = faqService.importFAQs(selected);
                refreshTableData();
                if (onDataChanged != null) onDataChanged.run();
                JOptionPane.showMessageDialog(this, "Successfully imported " + imported + " new/updated FAQs.", "Import Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("faqs_backup.csv"));
        fileChooser.setDialogTitle("Export Knowledge Base to CSV");
        int res = fileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            try {
                faqService.exportFAQs(selected);
                JOptionPane.showMessageDialog(this, "Knowledge base successfully exported to " + selected.getName(), "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Inner Dialog for Adding or Editing an FAQ.
     */
    private static class FAQEditDialog extends JDialog {
        private final FAQService faqService;
        private final FAQ existingFaq;
        private boolean saved = false;

        private JTextField tfId;
        private JComboBox<String> cbCategory;
        private JTextField tfPrimaryQuestion;
        private JTextArea taAltQuestions;
        private JTextField tfKeywords;
        private JTextArea taAnswer;
        private JSpinner spWeight;

        public FAQEditDialog(Frame owner, FAQ existingFaq, FAQService faqService) {
            super(owner, existingFaq == null ? "Add New FAQ" : "Edit FAQ: " + existingFaq.getId(), true);
            this.existingFaq = existingFaq;
            this.faqService = faqService;

            setSize(560, 580);
            setLocationRelativeTo(owner);
            getContentPane().setBackground(ModernTheme.BG_DARK);
            setLayout(new BorderLayout());

            buildDialogUI();
        }

        public boolean isSaved() {
            return saved;
        }

        private void buildDialogUI() {
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
            formPanel.setBackground(ModernTheme.BG_DARK);
            formPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

            // ID & Weight
            JPanel topRow = new JPanel(new GridLayout(1, 2, 15, 0));
            topRow.setOpaque(false);

            String idVal = existingFaq != null ? existingFaq.getId() : faqService.generateNextId();
            tfId = createField(idVal);
            if (existingFaq != null) tfId.setEditable(false);

            spWeight = new JSpinner(new SpinnerNumberModel(
                    existingFaq != null ? existingFaq.getWeight() : 1.0,
                    0.1, 5.0, 0.1
            ));
            spWeight.setFont(ModernTheme.FONT_BODY);

            topRow.add(createLabeledComponent("FAQ ID:", tfId));
            topRow.add(createLabeledComponent("Priority Weight (0.1 - 5.0):", spWeight));
            formPanel.add(topRow);
            formPanel.add(Box.createVerticalStrut(10));

            // Category
            cbCategory = new JComboBox<>();
            cbCategory.setEditable(true);
            cbCategory.setFont(ModernTheme.FONT_BODY);
            cbCategory.setBackground(ModernTheme.BG_INPUT);
            cbCategory.setForeground(ModernTheme.TEXT_PRIMARY);
            for (String cat : faqService.getCategories()) {
                cbCategory.addItem(cat);
            }
            if (existingFaq != null) {
                cbCategory.setSelectedItem(existingFaq.getCategory());
            }
            formPanel.add(createLabeledComponent("Category (Select or type new):", cbCategory));
            formPanel.add(Box.createVerticalStrut(10));

            // Primary Question
            tfPrimaryQuestion = createField(existingFaq != null ? existingFaq.getPrimaryQuestion() : "");
            formPanel.add(createLabeledComponent("Primary Question *:", tfPrimaryQuestion));
            formPanel.add(Box.createVerticalStrut(10));

            // Alternative Questions
            String altsText = existingFaq != null ? String.join("; ", existingFaq.getAlternativeQuestions()) : "";
            taAltQuestions = createTextArea(altsText, 3);
            formPanel.add(createLabeledComponent("Alternative Questions (separated by semicolon ';'):", new JScrollPane(taAltQuestions)));
            formPanel.add(Box.createVerticalStrut(10));

            // Keywords
            String kwText = existingFaq != null ? String.join(", ", existingFaq.getKeywords()) : "";
            tfKeywords = createField(kwText);
            formPanel.add(createLabeledComponent("Keywords (separated by comma or semicolon):", tfKeywords));
            formPanel.add(Box.createVerticalStrut(10));

            // Answer
            String ansText = existingFaq != null ? existingFaq.getAnswer() : "";
            taAnswer = createTextArea(ansText, 5);
            formPanel.add(createLabeledComponent("Answer *:", new JScrollPane(taAnswer)));

            add(formPanel, BorderLayout.CENTER);

            // Bottom Buttons
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            btnPanel.setBackground(ModernTheme.BG_CARD);
            btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER_SUBTLE));

            JButton btnCancel = ModernTheme.createOutlineButton("Cancel", ModernTheme.TEXT_MUTED, ModernTheme.TEXT_SECONDARY);
            btnCancel.addActionListener(e -> dispose());

            JButton btnSave = ModernTheme.createStyledButton("Save FAQ", ModernTheme.ACCENT_PRIMARY, Color.WHITE);
            btnSave.addActionListener(e -> saveFaq());

            btnPanel.add(btnCancel);
            btnPanel.add(btnSave);
            add(btnPanel, BorderLayout.SOUTH);
        }

        private void saveFaq() {
            String id = tfId.getText().trim();
            String category = (String) cbCategory.getSelectedItem();
            String question = tfPrimaryQuestion.getText().trim();
            String answer = taAnswer.getText().trim();

            if (question.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Primary Question is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (answer.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Answer is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> alts = new ArrayList<>();
            for (String alt : taAltQuestions.getText().split(";")) {
                if (!alt.trim().isEmpty()) alts.add(alt.trim());
            }

            List<String> keywords = new ArrayList<>();
            for (String kw : tfKeywords.getText().split("[,;]")) {
                if (!kw.trim().isEmpty()) keywords.add(kw.trim().toLowerCase());
            }

            double weight = ((Number) spWeight.getValue()).doubleValue();

            FAQ faq = new FAQ(id, question, alts, keywords, category != null ? category.trim() : "General Knowledge", answer, weight);

            try {
                if (existingFaq == null) {
                    faqService.addFAQ(faq);
                } else {
                    faqService.updateFAQ(faq);
                }
                saved = true;
                dispose();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save FAQ: " + ex.getMessage(), "Storage Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private JPanel createLabeledComponent(String label, JComponent comp) {
            JPanel p = new JPanel(new BorderLayout(0, 4));
            p.setOpaque(false);
            JLabel l = new JLabel(label);
            l.setFont(ModernTheme.FONT_SMALL_BOLD);
            l.setForeground(ModernTheme.TEXT_SECONDARY);
            p.add(l, BorderLayout.NORTH);
            p.add(comp, BorderLayout.CENTER);
            return p;
        }

        private JTextField createField(String text) {
            JTextField f = new JTextField(text);
            f.setFont(ModernTheme.FONT_BODY);
            f.setBackground(ModernTheme.BG_INPUT);
            f.setForeground(ModernTheme.TEXT_PRIMARY);
            f.setCaretColor(ModernTheme.TEXT_PRIMARY);
            f.setBorder(BorderFactory.createCompoundBorder(
                    new ModernTheme.RoundedBorder(ModernTheme.BORDER_SUBTLE, 8, 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            return f;
        }

        private JTextArea createTextArea(String text, int rows) {
            JTextArea a = new JTextArea(text, rows, 20);
            a.setFont(ModernTheme.FONT_BODY);
            a.setBackground(ModernTheme.BG_INPUT);
            a.setForeground(ModernTheme.TEXT_PRIMARY);
            a.setCaretColor(ModernTheme.TEXT_PRIMARY);
            a.setLineWrap(true);
            a.setWrapStyleWord(true);
            a.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
            return a;
        }
    }
}
