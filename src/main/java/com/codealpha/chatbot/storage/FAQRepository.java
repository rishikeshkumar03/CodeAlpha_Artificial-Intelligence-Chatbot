package com.codealpha.chatbot.storage;

import com.codealpha.chatbot.model.FAQ;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository for managing persistent storage and in-memory caching of FAQ records.
 */
public class FAQRepository {

    private static final String CSV_HEADER_ID = "ID";
    private static final String CSV_HEADER_CATEGORY = "Category";
    private static final String CSV_HEADER_PRIMARY_QUESTION = "PrimaryQuestion";
    private static final String CSV_HEADER_ALT_QUESTIONS = "AlternativeQuestions";
    private static final String CSV_HEADER_KEYWORDS = "Keywords";
    private static final String CSV_HEADER_ANSWER = "Answer";
    private static final String CSV_HEADER_WEIGHT = "Weight";

    private final File dataFile;
    private final CSVStorage csvStorage;
    private final Map<String, FAQ> faqCache;

    public FAQRepository(File dataFile) {
        this.dataFile = dataFile;
        this.csvStorage = new CSVStorage();
        this.faqCache = new LinkedHashMap<>();
    }

    public FAQRepository(File dataFile, CSVStorage csvStorage) {
        this.dataFile = dataFile;
        this.csvStorage = csvStorage != null ? csvStorage : new CSVStorage();
        this.faqCache = new LinkedHashMap<>();
    }

    /**
     * Initializes the repository by loading from file or seeding defaults.
     */
    public synchronized void initialize() throws IOException {
        faqCache.clear();
        if (dataFile.exists() && dataFile.length() > 0) {
            loadFromFile(dataFile);
        } else {
            // Seed default starter FAQs and persist to data file
            seedDefaultFaqs();
            saveToFile(dataFile);
        }
    }

    /**
     * Returns an unmodifiable list of all currently loaded FAQs.
     */
    public synchronized List<FAQ> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(faqCache.values()));
    }

    /**
     * Finds an FAQ by its unique identifier.
     */
    public synchronized Optional<FAQ> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(faqCache.get(id.trim()));
    }

    /**
     * Saves or updates an FAQ. Automatically writes to disk.
     */
    public synchronized void save(FAQ faq) throws IOException {
        if (faq == null || faq.getId() == null || faq.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("FAQ and FAQ ID cannot be null or empty");
        }
        faqCache.put(faq.getId().trim(), faq);
        saveToFile(dataFile);
    }

    /**
     * Deletes an FAQ by ID. Automatically writes to disk.
     */
    public synchronized boolean deleteById(String id) throws IOException {
        if (id == null) return false;
        FAQ removed = faqCache.remove(id.trim());
        if (removed != null) {
            saveToFile(dataFile);
            return true;
        }
        return false;
    }

    /**
     * Checks if an ID already exists.
     */
    public synchronized boolean existsById(String id) {
        return id != null && faqCache.containsKey(id.trim());
    }

    /**
     * Generates a new unique sequential ID.
     */
    public synchronized String generateNextId() {
        int maxIndex = 0;
        for (String id : faqCache.keySet()) {
            if (id.startsWith("FAQ-")) {
                try {
                    int num = Integer.parseInt(id.substring(4).trim());
                    if (num > maxIndex) maxIndex = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("FAQ-%03d", maxIndex + 1);
    }

    /**
     * Loads FAQs from an external CSV file.
     */
    public synchronized void loadFromFile(File file) throws IOException {
        List<List<String>> records = csvStorage.readCSV(file);
        if (records.isEmpty()) return;

        // Skip header if present
        int startIndex = 0;
        List<String> firstRow = records.get(0);
        if (!firstRow.isEmpty() && firstRow.get(0).equalsIgnoreCase(CSV_HEADER_ID)) {
            startIndex = 1;
        }

        for (int i = startIndex; i < records.size(); i++) {
            List<String> row = records.get(i);
            FAQ faq = parseFaqFromRow(row);
            if (faq != null) {
                faqCache.put(faq.getId(), faq);
            }
        }
    }

    /**
     * Saves all cached FAQs to the target CSV file.
     */
    public synchronized void saveToFile(File file) throws IOException {
        List<List<String>> rows = new ArrayList<>();

        // Add Header
        rows.add(Arrays.asList(
                CSV_HEADER_ID,
                CSV_HEADER_CATEGORY,
                CSV_HEADER_PRIMARY_QUESTION,
                CSV_HEADER_ALT_QUESTIONS,
                CSV_HEADER_KEYWORDS,
                CSV_HEADER_ANSWER,
                CSV_HEADER_WEIGHT
        ));

        // Add Data Rows
        for (FAQ faq : faqCache.values()) {
            rows.add(Arrays.asList(
                    faq.getId(),
                    faq.getCategory(),
                    faq.getPrimaryQuestion(),
                    String.join(";", faq.getAlternativeQuestions()),
                    String.join(";", faq.getKeywords()),
                    faq.getAnswer(),
                    String.valueOf(faq.getWeight())
            ));
        }

        csvStorage.writeCSV(file, rows);
    }

    /**
     * Imports FAQs from an external CSV file, merging or overwriting existing IDs.
     */
    public synchronized int importFromCSV(File file) throws IOException {
        int initialCount = faqCache.size();
        loadFromFile(file);
        saveToFile(dataFile);
        return faqCache.size() - initialCount;
    }

    /**
     * Exports all current FAQs to a designated external file.
     */
    public synchronized void exportToCSV(File file) throws IOException {
        saveToFile(file);
    }

    /**
     * Resets the repository back to the starter FAQ knowledge base.
     */
    public synchronized void resetToDefaults() throws IOException {
        faqCache.clear();
        seedDefaultFaqs();
        saveToFile(dataFile);
    }

    private FAQ parseFaqFromRow(List<String> row) {
        if (row.size() < 6) {
            return null;
        }
        try {
            String id = row.get(0).trim();
            String category = row.get(1).trim();
            String primaryQuestion = row.get(2).trim();

            List<String> altQuestions = new ArrayList<>();
            if (!row.get(3).trim().isEmpty()) {
                String[] parts = row.get(3).split(";");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) altQuestions.add(part.trim());
                }
            }

            List<String> keywords = new ArrayList<>();
            if (!row.get(4).trim().isEmpty()) {
                String[] parts = row.get(4).split(";");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) keywords.add(part.trim());
                }
            }

            String answer = row.get(5).trim();

            double weight = 1.0;
            if (row.size() >= 7 && !row.get(6).trim().isEmpty()) {
                try {
                    weight = Double.parseDouble(row.get(6).trim());
                } catch (NumberFormatException e) {
                    weight = 1.0;
                }
            }

            return new FAQ(id, primaryQuestion, altQuestions, keywords, category, answer, weight);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Populates the cache with 35+ rich, educational starter FAQs across all required categories.
     */
    private void seedDefaultFaqs() {
        addSeedFaq("FAQ-001", "Artificial Intelligence",
                "What is artificial intelligence?",
                List.of("Explain AI in simple words", "Define artificial intelligence", "What does AI mean?", "Tell me about AI"),
                List.of("ai", "artificial", "intelligence", "machine", "reasoning"),
                "Artificial Intelligence (AI) is a branch of computer science focused on building smart systems capable of performing tasks that typically require human intelligence, such as reasoning, problem solving, visual perception, and natural language understanding.",
                1.0);

        addSeedFaq("FAQ-002", "Artificial Intelligence",
                "What is machine learning?",
                List.of("Explain ML", "Define machine learning", "How does machine learning work?", "Difference between AI and ML"),
                List.of("machine", "learning", "ml", "algorithms", "data", "training"),
                "Machine Learning (ML) is a subset of Artificial Intelligence where computer algorithms learn patterns directly from data and improve their performance through experience without being explicitly programmed for every scenario.",
                1.0);

        addSeedFaq("FAQ-003", "Artificial Intelligence",
                "What is natural language processing?",
                List.of("What is NLP?", "Explain natural language processing", "How do chatbots understand language?", "Define NLP"),
                List.of("nlp", "natural", "language", "processing", "linguistics", "text"),
                "Natural Language Processing (NLP) is an interdisciplinary field of computer science and artificial intelligence concerned with giving computers the ability to understand, interpret, manipulate, and generate human language.",
                1.0);

        addSeedFaq("FAQ-004", "Artificial Intelligence",
                "What is deep learning?",
                List.of("Explain deep learning", "What are deep neural networks?", "Define deep learning"),
                List.of("deep", "learning", "neural", "networks", "layers"),
                "Deep Learning is a specialized subfield of machine learning based on artificial neural networks with multiple layers (deep architectures) that model complex representations in images, speech, and text.",
                1.0);

        addSeedFaq("FAQ-005", "Java Programming",
                "What is Java?",
                List.of("Explain Java programming", "What is the Java language?", "Tell me about Java", "Why use Java?"),
                List.of("java", "programming", "language", "jvm", "platform"),
                "Java is a high-level, class-based, object-oriented programming language designed according to the 'Write Once, Run Anywhere' (WORA) philosophy, meaning compiled Java bytecode runs on any system supporting a Java Virtual Machine (JVM).",
                1.0);

        addSeedFaq("FAQ-006", "Java Programming",
                "What are the four pillars of OOP in Java?",
                List.of("Explain OOP concepts", "What is object oriented programming in Java?", "Pillars of OOP", "Four pillars of object oriented programming"),
                List.of("oop", "pillars", "encapsulation", "inheritance", "polymorphism", "abstraction"),
                "The four fundamental pillars of Object-Oriented Programming (OOP) are:\n1. Encapsulation (bundling data and methods, restricting direct access)\n2. Abstraction (hiding implementation details and showing essential features)\n3. Inheritance (deriving new classes from existing classes for code reuse)\n4. Polymorphism (the ability of an object or method to take many forms).",
                1.0);

        addSeedFaq("FAQ-007", "Java Programming",
                "What is the difference between JDK, JRE, and JVM?",
                List.of("Explain JDK vs JRE vs JVM", "What is JVM?", "What is JRE?", "What is JDK?"),
                List.of("jdk", "jre", "jvm", "compiler", "runtime", "virtual"),
                "• JVM (Java Virtual Machine) executes compiled bytecode (.class files).\n• JRE (Java Runtime Environment) provides the JVM plus core runtime libraries to run Java programs.\n• JDK (Java Development Kit) is the complete development suite containing JRE, the Java compiler (javac), and debugging tools.",
                1.0);

        addSeedFaq("FAQ-008", "Java Programming",
                "What is the Java Collections Framework?",
                List.of("Explain Java collections", "What is List Set and Map in Java?", "How do collections work in Java?"),
                List.of("collections", "framework", "list", "set", "map", "arraylist"),
                "The Java Collections Framework (JCF) is a unified architecture in java.util for storing and manipulating groups of objects. It provides standard interfaces (List, Set, Queue, Map) and high-performance implementations like ArrayList, LinkedList, HashSet, TreeSet, and HashMap.",
                1.0);

        addSeedFaq("FAQ-009", "Java Programming",
                "What is garbage collection in Java?",
                List.of("How does Java garbage collection work?", "Explain GC in Java", "Automatic memory management in Java"),
                List.of("garbage", "collection", "gc", "memory", "heap"),
                "Garbage Collection in Java is an automated memory management process where the JVM detects and reclaims heap memory occupied by objects that are no longer reachable or referenced by any active thread in the application.",
                1.0);

        addSeedFaq("FAQ-010", "Java Programming",
                "What is the difference between an abstract class and an interface?",
                List.of("Abstract class vs interface", "When to use abstract class vs interface in Java", "Compare interface and abstract class"),
                List.of("abstract", "class", "interface", "implementation", "methods"),
                "An abstract class can maintain instance state (fields), constructors, and both concrete and abstract methods, supporting single inheritance. An interface defines a contract, supports multiple inheritance of type, and since Java 8/9 can contain default, static, and private methods.",
                1.0);

        addSeedFaq("FAQ-011", "Java Programming",
                "What is multithreading in Java?",
                List.of("Explain threads in Java", "How does multithreading work in Java?", "Concurrency in Java"),
                List.of("multithreading", "thread", "concurrency", "synchronization", "runnable"),
                "Multithreading in Java is a feature that allows concurrent execution of two or more parts of a program (threads) to maximize CPU utilization. In Java, threads can be created by extending the Thread class or implementing the Runnable / Callable interfaces.",
                1.0);

        addSeedFaq("FAQ-012", "Programming Fundamentals",
                "What is an algorithm?",
                List.of("Define algorithm", "Explain what an algorithm is", "What is an algorithm in programming?"),
                List.of("algorithm", "steps", "procedure", "problem", "solving"),
                "An algorithm is a finite, step-by-step sequence of well-defined instructions designed to perform a specific computation, solve a problem, or accomplish a desired task.",
                1.0);

        addSeedFaq("FAQ-013", "Programming Fundamentals",
                "What is a data structure?",
                List.of("Define data structure", "Why are data structures important?", "Explain data structures"),
                List.of("data", "structure", "arrays", "trees", "graphs", "storage"),
                "A data structure is a specialized format for organizing, processing, retrieving, and storing data in computer memory efficiently. Common examples include arrays, linked lists, stacks, queues, hash tables, trees, and graphs.",
                1.0);

        addSeedFaq("FAQ-014", "Programming Fundamentals",
                "What is recursion in programming?",
                List.of("Explain recursive functions", "What is a recursive algorithm?", "How does recursion work?"),
                List.of("recursion", "recursive", "base", "case", "call", "stack"),
                "Recursion is a programming technique where a method solves a problem by calling itself with smaller sub-problems until reaching a base condition that stops further recursive calls.",
                1.0);

        addSeedFaq("FAQ-015", "Programming Fundamentals",
                "What is Big O notation?",
                List.of("Explain time complexity", "What does Big O mean?", "How to measure algorithm complexity?"),
                List.of("big", "notation", "complexity", "time", "space", "asymptotic"),
                "Big O notation is a mathematical notation used in computer science to describe the asymptotic upper bound of an algorithm's execution time or memory space consumption as the input size (n) grows.",
                1.0);

        addSeedFaq("FAQ-016", "Computer Science",
                "What is an operating system?",
                List.of("Explain OS", "What does an operating system do?", "Define operating system"),
                List.of("operating", "system", "os", "hardware", "kernel", "resources"),
                "An Operating System (OS) is system software that manages computer hardware, memory, processor execution, and storage resources, while providing common services and APIs for application programs.",
                1.0);

        addSeedFaq("FAQ-017", "Computer Science",
                "What is a relational database and SQL?",
                List.of("Explain SQL and databases", "What is RDBMS?", "What is SQL used for?"),
                List.of("database", "sql", "rdbms", "tables", "queries", "relational"),
                "A relational database organizes data into structured tables with rows and columns. SQL (Structured Query Language) is the domain-specific standard language used to query, insert, update, and manage relational database systems such as PostgreSQL, MySQL, and Oracle.",
                1.0);

        addSeedFaq("FAQ-018", "Computer Science",
                "What is the difference between TCP and UDP?",
                List.of("TCP vs UDP", "Compare TCP and UDP protocols", "Explain networking protocols TCP and UDP"),
                List.of("tcp", "udp", "networking", "protocol", "connection", "packets"),
                "TCP (Transmission Control Protocol) is connection-oriented, reliable, guarantees packet delivery order, and performs error-checking. UDP (User Datagram Protocol) is connectionless, lightweight, and fast, making it ideal for real-time streaming, gaming, and VoIP where occasional packet loss is acceptable.",
                1.0);

        addSeedFaq("FAQ-019", "Computer Science",
                "What is a compiler vs an interpreter?",
                List.of("Compiler vs interpreter", "Difference between compiler and interpreter", "How do compilers work?"),
                List.of("compiler", "interpreter", "source", "bytecode", "machine", "execution"),
                "A compiler translates entire source code into machine code or bytecode ahead of time before execution (e.g., C, C++, javac). An interpreter executes instructions line-by-line on the fly at runtime (e.g., Python, Ruby). Java uses a hybrid approach: compiled to bytecode, then JIT-compiled/interpreted by the JVM.",
                1.0);

        addSeedFaq("FAQ-020", "Technology",
                "What is cloud computing?",
                List.of("Explain cloud computing", "What are cloud services?", "What is AWS, Azure, or GCP?"),
                List.of("cloud", "computing", "servers", "storage", "infrastructure", "saas"),
                "Cloud computing is the on-demand delivery of IT resources—including computing power, database storage, and applications—over the internet with pay-as-you-go pricing, eliminating the need to own and maintain physical data centers.",
                1.0);

        addSeedFaq("FAQ-021", "Technology",
                "What is Git and version control?",
                List.of("What is Git?", "Explain version control", "How does Git work?", "What is GitHub?"),
                List.of("git", "version", "control", "repository", "commits", "branches"),
                "Git is a distributed version control system that tracks modifications in source code over time, enabling multiple developers to collaborate smoothly using branching, merging, and historical snapshots.",
                1.0);

        addSeedFaq("FAQ-022", "Technology",
                "What is an API?",
                List.of("Explain API", "What does API stand for?", "What is a REST API?", "How do APIs work?"),
                List.of("api", "interface", "rest", "endpoints", "http", "json"),
                "An API (Application Programming Interface) is a defined set of rules, protocols, and endpoints that allow different software applications to communicate and exchange data securely.",
                1.0);

        addSeedFaq("FAQ-023", "Technology",
                "What is Docker and containerization?",
                List.of("What is Docker?", "Explain containers", "Why use Docker?"),
                List.of("docker", "containers", "containerization", "virtualization", "images"),
                "Docker is an open-source platform that packages applications and all their dependencies into lightweight, standardized units called containers, guaranteeing consistent execution across local development and production servers.",
                1.0);

        addSeedFaq("FAQ-024", "General Knowledge",
                "What is the difference between frontend and backend?",
                List.of("Frontend vs backend", "Explain client side vs server side", "What does a full stack developer do?"),
                List.of("frontend", "backend", "client", "server", "fullstack", "ui"),
                "Frontend refers to the client-facing user interface and interactions running in the browser or mobile display. Backend refers to the server-side architecture, business logic, APIs, database management, and authentication running behind the scenes.",
                1.0);

        addSeedFaq("FAQ-025", "General Knowledge",
                "What is software engineering?",
                List.of("Define software engineering", "What do software engineers do?", "Software development life cycle"),
                List.of("software", "engineering", "sdlc", "development", "architecture", "testing"),
                "Software engineering is the systematic, disciplined application of engineering principles, methodologies, architecture design, and quality assurance to the development, operation, and maintenance of reliable software systems.",
                1.0);

        addSeedFaq("FAQ-026", "Greetings",
                "How are you doing?",
                List.of("How are you?", "How do you feel?", "Are you doing well?", "How is it going?"),
                List.of("how", "are", "you", "doing", "feeling"),
                "I'm operating smoothly and ready to assist you! What coding or computer science question can I answer for you today?",
                1.0);

        addSeedFaq("FAQ-027", "Greetings",
                "Thank you!",
                List.of("Thanks", "Thanks a lot", "Thank you very much", "I appreciate your help"),
                List.of("thanks", "thank", "you", "appreciate", "grateful"),
                "You are very welcome! Don't hesitate to ask if you have any other questions. Happy learning!",
                1.0);

        addSeedFaq("FAQ-028", "Application Help",
                "How do I train this chatbot with new questions?",
                List.of("How to add new FAQ?", "Can I teach the chatbot?", "How to add question and answer?", "Train chatbot"),
                List.of("train", "teach", "add", "faq", "knowledge", "manage"),
                "To train the chatbot with new knowledge:\n1. Click the 'FAQ Manager' button in the top bar or side panel.\n2. Click '+ Add FAQ'.\n3. Enter the Primary Question, Category, Answer, and optional alternative phrasings/keywords.\n4. Click 'Save FAQ'.\nYour new question is instantly searchable without needing an application restart!",
                1.0);

        addSeedFaq("FAQ-029", "Application Help",
                "How does this chatbot's NLP and matching work?",
                List.of("How do you match questions?", "Explain the NLP algorithm", "Is this a deep learning model?", "Chatbot NLP pipeline"),
                List.of("nlp", "matching", "similarity", "algorithm", "jaccard", "levenshtein"),
                "This chatbot uses a transparent, explainable Java NLP pipeline:\n1. Text Normalization (lowercasing, punctuation cleanup, contraction expansion)\n2. Tokenization & Stop-word Filtering\n3. Intent Recognition\n4. Multi-factor Similarity Matching (Jaccard token set similarity, Levenshtein edit distance, keyword overlap)\n5. Confidence Thresholding & Deterministic Tie-breaking\nIt works completely offline without third-party cloud APIs.",
                1.0);

        addSeedFaq("FAQ-030", "Application Help",
                "Where is the FAQ database saved?",
                List.of("Where are FAQs stored?", "Where is data saved?", "FAQ file location"),
                List.of("storage", "file", "location", "csv", "data", "database"),
                "The knowledge base is saved locally in standard RFC 4180 CSV format at 'data/faqs.csv'. You can edit it through the application's FAQ Manager, or export and import CSV backups anytime.",
                1.0);

        addSeedFaq("FAQ-031", "General Knowledge",
                "What is CodeAlpha?",
                List.of("Who is CodeAlpha?", "About CodeAlpha", "CodeAlpha internship"),
                List.of("codealpha", "internship", "project", "organization"),
                "CodeAlpha is an educational organization providing remote internship programs that empower aspiring software engineers with hands-on, practical software development projects.",
                1.0);

        addSeedFaq("FAQ-032", "Java Programming",
                "What is Java Swing?",
                List.of("Explain Java Swing", "What is Swing in Java?", "GUI development in Java"),
                List.of("swing", "gui", "jframe", "jpanel", "desktop", "ui"),
                "Java Swing is a GUI widget toolkit for Java, providing a rich set of lightweight, platform-independent desktop interface components such as buttons, tables, text areas, and scroll panes, fully written in Java.",
                1.0);
    }

    private void addSeedFaq(String id, String category, String primaryQuestion,
                            List<String> altQuestions, List<String> keywords,
                            String answer, double weight) {
        FAQ faq = new FAQ(id, primaryQuestion, altQuestions, keywords, category, answer, weight);
        faqCache.put(faq.getId(), faq);
    }
}
