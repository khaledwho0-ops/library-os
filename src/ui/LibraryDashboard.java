package com.library.ui;

import com.library.dao.BookDAO;
import com.library.model.Book;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LibraryDashboard extends JFrame {

    // --- THEMES ---
    private Color bgColor = new Color(10, 10, 10);
    private Color panelColor = new Color(20, 20, 20);
    private Color neonMain = new Color(0, 255, 255);
    private Color neonSec = new Color(57, 255, 20);
    private Color textWhite = Color.WHITE;

    // --- STATE ---
    private boolean isCozyMode = false;
    private boolean isHypeMode = false;
    private boolean isAdminUnlocked = false;
    private boolean isRainActive = false;
    private boolean isBossMode = false;
    private List<Book> shoppingCart = new ArrayList<>();
    private Map<Integer, Boolean> borrowedStatus = new HashMap<>();
    private Timer readingTimer, hypeTimer, tickerTimer, rainTimer;
    private int secondsReading = 0;
    private int tickerX = 1600;

    // --- RAIN PARTICLES ---
    private List<Point> rainDrops = new ArrayList<>();

    // --- COMPONENTS ---
    private BookDAO bookDAO;
    private DefaultTableModel tableModel;
    private JTable bookTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel imageLabel, titleLabel, authorLabel, priceLabel, statsLabel, quoteLabel, timerLabel, tickerLabel;
    private JPanel chartPanel, mainPanel, bossPanel;
    private JTextArea hackerConsole;
    private JTextField searchField;
    private JButton btnDelete, btnUnlock, btnCheckout;
    private JLayeredPane layeredPane;
    private JPanel rainPanel;

    public LibraryDashboard() {
        setTitle("LIBRARY OS - GOD MODE (FINAL VERSION)");
        setSize(1650, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bookDAO = new BookDAO();

        // 1. LAYERED PANE
        layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 1650, 1000);
        setContentPane(layeredPane);

        // 2. MAIN UI PANEL
        mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBackground(bgColor);
        mainPanel.setBounds(0, 0, 1650, 1000);

        // --- TOP: Ticker + Controls ---
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(createTickerPanel(), BorderLayout.NORTH);
        topContainer.add(createTopControlPanel(), BorderLayout.CENTER);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // --- CENTER: Split View ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTablePanel(), createDetailPanel());
        splitPane.setDividerLocation(1000);
        splitPane.setBackground(bgColor);
        splitPane.setBorder(null);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // --- BOTTOM: Analytics + Hacker Console ---
        JPanel bottomContainer = new JPanel(new BorderLayout());
        chartPanel = createChartPanel();
        bottomContainer.add(chartPanel, BorderLayout.CENTER);
        bottomContainer.add(createHackerConsole(), BorderLayout.SOUTH);
        mainPanel.add(bottomContainer, BorderLayout.SOUTH);

        // Add Main Panel to Layer 0
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);

        // 3. BOSS MODE PANEL
        bossPanel = createBossPanel();
        bossPanel.setBounds(0, 0, 1650, 1000);
        bossPanel.setVisible(false);
        layeredPane.add(bossPanel, JLayeredPane.POPUP_LAYER);

        // 4. RAIN LAYER
        rainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if(isRainActive) {
                    g.setColor(new Color(0, 255, 255, 100)); // Cyan Rain
                    for(Point p : rainDrops) {
                        g.drawLine(p.x, p.y, p.x, p.y + 10);
                    }
                }
            }
        };
        rainPanel.setOpaque(false);
        rainPanel.setBounds(0, 0, 1650, 1000);
        layeredPane.add(rainPanel, JLayeredPane.PALETTE_LAYER);

        // 5. WATERMARK LAYER (FIXED LINE BELOW)
        JLabel watermark = new JLabel("KKK");
        watermark.setFont(new Font("Arial", Font.BOLD, 400));
        watermark.setForeground(new Color(255, 255, 255, 10));
        watermark.setBounds(400, 200, 1000, 600);
        layeredPane.add(watermark, JLayeredPane.DRAG_LAYER); // FIXED: DRAG_LAYER (All Caps)

        // Start All Systems
        startReadingTimer();
        setupHypeTimer();
        startTicker();
        startRainSystem();

        logToConsole("SYSTEM INITIALIZED...");
        logToConsole("CONNECTING TO DATABASE... [OK]");
        logToConsole("LOADING GOD MODE ASSETS... [OK]");

        refreshTable();
        setVisible(true);
    }

    // --- HACKER CONSOLE ---
    private JScrollPane createHackerConsole() {
        hackerConsole = new JTextArea(5, 20);
        hackerConsole.setBackground(Color.BLACK);
        hackerConsole.setForeground(new Color(0, 255, 0));
        hackerConsole.setFont(new Font("Consolas", Font.PLAIN, 12));
        hackerConsole.setEditable(false);
        JScrollPane scroll = new JScrollPane(hackerConsole);
        scroll.setBorder(new TitledBorder(new LineBorder(Color.GREEN), "SYSTEM LOG"));
        return scroll;
    }

    private void logToConsole(String msg) {
        String timestamp = LocalTime.now().toString().substring(0, 8);
        hackerConsole.append("[" + timestamp + "] >> " + msg + "\n");
        hackerConsole.setCaretPosition(hackerConsole.getDocument().getLength());
    }

    // --- STOCK TICKER ---
    private JPanel createTickerPanel() {
        JPanel panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(1650, 30));
        panel.setBackground(Color.BLACK);

        tickerLabel = new JLabel("LOADING MARKET DATA...");
        tickerLabel.setForeground(Color.YELLOW);
        tickerLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        tickerLabel.setBounds(tickerX, 5, 2000, 20);

        panel.add(tickerLabel);
        return panel;
    }

    private void startTicker() {
        tickerTimer = new Timer(20, e -> {
            tickerX -= 2;
            if(tickerX < -1500) tickerX = getWidth();
            tickerLabel.setBounds(tickerX, 5, 2000, 20);

            if(new Random().nextInt(100) == 1) {
                tickerLabel.setText("LIVE MARKET: JAVA +5.2%  |  MANGA +12.4%  |  NOVELS -1.1%  |  ONE PIECE STOCK 🚀  |  BITCOIN? NO, BOOKCOIN!  |  READING IS POWER");
            }
        });
        tickerTimer.start();
    }

    // --- RAIN SYSTEM ---
    private void startRainSystem() {
        for(int i=0; i<100; i++) rainDrops.add(new Point(new Random().nextInt(1650), new Random().nextInt(1000)));

        rainTimer = new Timer(30, e -> {
            if(isRainActive) {
                for(Point p : rainDrops) {
                    p.y += 15;
                    if(p.y > 1000) {
                        p.y = 0;
                        p.x = new Random().nextInt(1650);
                    }
                }
                rainPanel.repaint();
            }
        });
        rainTimer.start();
    }

    // --- BOSS MODE ---
    private JPanel createBossPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        JLabel toolbar = new JLabel("  File   Home   Insert   Page Layout   Formulas   Data   Review   View");
        toolbar.setOpaque(true);
        toolbar.setBackground(new Color(33, 115, 70));
        toolbar.setForeground(Color.WHITE);
        toolbar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toolbar.setPreferredSize(new Dimension(1650, 40));

        String[] cols = {"A", "B", "C", "D", "E", "F", "G"};
        DefaultTableModel sheetModel = new DefaultTableModel(cols, 50);
        JTable sheet = new JTable(sheetModel);
        sheet.setGridColor(Color.LIGHT_GRAY);
        sheet.setShowGrid(true);

        for(int i=0; i<50; i++) {
            sheetModel.setValueAt("Expense " + i, i, 0);
            sheetModel.setValueAt(new Random().nextInt(1000), i, 1);
            sheetModel.setValueAt("Q3 Report", i, 2);
        }

        JButton btnBack = new JButton("EXIT BOSS MODE");
        btnBack.addActionListener(e -> toggleBossMode());

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(sheet), BorderLayout.CENTER);
        panel.add(btnBack, BorderLayout.SOUTH);

        return panel;
    }

    private void toggleBossMode() {
        isBossMode = !isBossMode;
        bossPanel.setVisible(isBossMode);
        mainPanel.setVisible(!isBossMode);
        if(isBossMode) setTitle("Microsoft Excel - Q3_Financial_Report.xlsx");
        else setTitle("LIBRARY OS - GOD MODE (FINAL VERSION)");
    }

    // --- TOP PANEL ---
    private JPanel createTopControlPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(bgColor);
        container.setBorder(new MatteBorder(0, 0, 2, 0, neonSec));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controls.setBackground(bgColor);

        JLabel logo = new JLabel("⚡ LIBRARY OS");
        logo.setFont(new Font("Impact", Font.BOLD, 28));
        logo.setForeground(neonSec);

        searchField = new JTextField(12);
        searchField.setBorder(new TitledBorder(new LineBorder(neonMain), "Live Search"));
        searchField.setBackground(panelColor);
        searchField.setForeground(textWhite);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                updateStats();
                logToConsole("SEARCH QUERY: " + text);
            }
        });

        JButton btnCozy = createNeonButton("☕ COZY", Color.ORANGE);
        btnCozy.addActionListener(e -> toggleCozyMode());

        JButton btnLucky = createNeonButton("🎲 LUCKY", Color.MAGENTA);
        btnLucky.addActionListener(e -> imFeelingLucky());

        JButton btnFan = createNeonButton("⛩️ FAN CORNER", Color.PINK);
        btnFan.addActionListener(e -> openFanCorner());

        timerLabel = new JLabel("⏱️ 00:00");
        timerLabel.setForeground(Color.LIGHT_GRAY);
        timerLabel.setFont(new Font("Consolas", Font.BOLD, 16));

        controls.add(logo);
        controls.add(searchField);
        controls.add(btnCozy);
        controls.add(btnLucky);
        controls.add(btnFan);
        controls.add(timerLabel);

        JPanel quotesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quotesPanel.setBackground(panelColor);
        quoteLabel = new JLabel("Random Quote: 'Books are a uniquely portable magic.'");
        quoteLabel.setForeground(Color.GRAY);
        quoteLabel.setFont(new Font("Serif", Font.ITALIC, 14));
        quotesPanel.add(quoteLabel);

        container.add(controls, BorderLayout.CENTER);
        container.add(quotesPanel, BorderLayout.SOUTH);

        return container;
    }

    // --- TABLE PANEL ---
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(panelColor);

        String[] columns = {"ID", "Title", "Author", "Genre", "Price", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };

        bookTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);

        bookTable.setBackground(panelColor);
        bookTable.setForeground(textWhite);
        bookTable.setSelectionBackground(new Color(50, 50, 50));
        bookTable.setSelectionForeground(neonMain);
        bookTable.setRowHeight(30);
        bookTable.setShowGrid(true);
        bookTable.setGridColor(Color.DARK_GRAY);

        bookTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int viewRow = bookTable.getSelectedRow();
                if (viewRow != -1) {
                    int modelRow = bookTable.convertRowIndexToModel(viewRow);
                    String title = (String) tableModel.getValueAt(modelRow, 1);
                    new Thread(() -> loadBookDetails(title)).start();
                    generateRandomQuote();
                }
            }
        });

        JScrollPane scroll = new JScrollPane(bookTable);
        scroll.getViewport().setBackground(panelColor);
        scroll.setBorder(new LineBorder(neonSec, 1));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // --- DETAIL PANEL ---
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        imageLabel = new JLabel("Select Book", SwingConstants.CENTER);
        imageLabel.setForeground(Color.GRAY);
        imageLabel.setPreferredSize(new Dimension(200, 300));
        imageLabel.setMaximumSize(new Dimension(200, 300));
        imageLabel.setBorder(new LineBorder(Color.DARK_GRAY, 2));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleLabel = new JLabel(" ");
        titleLabel.setForeground(neonMain);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        authorLabel = new JLabel(" ");
        authorLabel.setForeground(textWhite);
        authorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        priceLabel = new JLabel(" ");
        priceLabel.setForeground(neonSec);
        priceLabel.setFont(new Font("Consolas", Font.BOLD, 24));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnPanel = new JPanel(new GridLayout(6, 2, 8, 8));
        btnPanel.setBackground(bgColor);
        btnPanel.setMaximumSize(new Dimension(300, 260));

        JButton btnBorrow = createNeonButton("BORROW", neonMain);
        JButton btnReturn = createNeonButton("RETURN", Color.ORANGE);
        JButton btnCart = createNeonButton("ADD TO CART 🛒", Color.YELLOW);
        btnCheckout = createNeonButton("CHECKOUT ($0)", Color.GREEN);
        JButton btnMood = createNeonButton("🧠 MOOD SCAN", Color.PINK);
        JButton btnHype = createNeonButton("🔥 HYPE MODE", Color.RED);
        JButton btnRate = createNeonButton("⭐ RATE BOOK", Color.CYAN);
        btnDelete = createNeonButton("DELETE (LOCKED)", Color.GRAY);
        btnDelete.setEnabled(false);
        btnUnlock = createNeonButton("🔓 UNLOCK ADMIN", Color.WHITE);
        JButton btnScan = createNeonButton("🔦 LASER SCAN", Color.LIGHT_GRAY);

        JButton btnRain = createNeonButton("🌧️ RAIN", Color.BLUE);
        JButton btnBoss = createNeonButton("👔 BOSS MODE", Color.DARK_GRAY);

        btnBorrow.addActionListener(e -> borrowBook());
        btnReturn.addActionListener(e -> returnBook());
        btnCart.addActionListener(e -> addToCart());
        btnCheckout.addActionListener(e -> checkoutCart());
        btnDelete.addActionListener(e -> deleteCurrentBook());
        btnUnlock.addActionListener(e -> unlockAdmin());
        btnScan.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "BEEP! ISBN: " + (new Random().nextInt(900000) + 100000));
            logToConsole("SCANNED ISBN: " + (new Random().nextInt(900000)));
        });
        btnMood.addActionListener(e -> scanMood());
        btnHype.addActionListener(e -> toggleHypeMode());
        btnRate.addActionListener(e -> rateBook());
        btnRain.addActionListener(e -> {
            isRainActive = !isRainActive;
            logToConsole("WEATHER SYSTEM TOGGLED: " + isRainActive);
        });
        btnBoss.addActionListener(e -> toggleBossMode());

        btnPanel.add(btnBorrow);
        btnPanel.add(btnReturn);
        btnPanel.add(btnCart);
        btnPanel.add(btnCheckout);
        btnPanel.add(btnMood);
        btnPanel.add(btnHype);
        btnPanel.add(btnRate);
        btnPanel.add(btnScan);
        btnPanel.add(btnRain);
        btnPanel.add(btnBoss);
        btnPanel.add(btnDelete);
        btnPanel.add(btnUnlock);

        panel.add(imageLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(titleLabel);
        panel.add(authorLabel);
        panel.add(priceLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnPanel);

        return panel;
    }

    private JPanel createChartPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(panelColor);
                g.fillRect(0, 0, getWidth(), getHeight());

                List<Book> books = bookDAO.getAllBooks();
                if(books.isEmpty()) return;

                int totalBooks = books.size();
                int borrowedCount = 0;
                for(Boolean status : borrowedStatus.values()) if(!status) borrowedCount++;
                int available = totalBooks - borrowedCount;
                double totalValue = books.stream().mapToDouble(Book::getPrice).sum();

                int barMax = getWidth() - 100;
                int barHeight = 40;
                g.setColor(Color.DARK_GRAY);
                g.fillRect(50, 50, barMax, barHeight);
                int availWidth = (int)((double)available / totalBooks * barMax);
                g.setColor(neonSec);
                g.fillRect(50, 50, availWidth, barHeight);

                g.setFont(new Font("Impact", Font.PLAIN, 18));
                g.setColor(textWhite);
                g.drawString("INVENTORY: " + available + " / " + totalBooks + " Available", 50, 40);
                g.setColor(Color.YELLOW);
                g.drawString("ASSETS: $" + String.format("%.2f", totalValue), 50, 130);
                g.setFont(new Font("Consolas", Font.PLAIN, 12));
                g.setColor(Color.GRAY);
                g.drawString("System: LibraryOS KKK | Fanbase Ver | Admin: " + (isAdminUnlocked ? "Active" : "Locked"), getWidth() - 350, 130);
            }
            @Override
            public Dimension getPreferredSize() { return new Dimension(getWidth(), 150); }
        };
    }

    private void updateStats() { if (chartPanel != null) chartPanel.repaint(); }

    // --- LOGIC METHODS ---
    private void openFanCorner() {
        String[] facts = {"One Piece is longer than the Bible!", "Java is named after Coffee!", "Levi Ackerman cleans when stressed."};
        JOptionPane.showMessageDialog(this, facts[new Random().nextInt(facts.length)], "⛩️ FAN CORNER", JOptionPane.INFORMATION_MESSAGE);
        logToConsole("USER ACCESSED FAN CORNER");
    }

    private void imFeelingLucky() {
        if(tableModel.getRowCount() > 0) {
            int randomRow = new Random().nextInt(tableModel.getRowCount());
            bookTable.setRowSelectionInterval(randomRow, randomRow);
            bookTable.scrollRectToVisible(bookTable.getCellRect(randomRow, 0, true));
            String title = (String) tableModel.getValueAt(randomRow, 1);
            loadBookDetails(title);
            logToConsole("LUCKY ROLL: " + title);
        }
    }

    private void scanMood() {
        String[] moods = {"Happy", "Sad", "Hyper", "Studious"};
        int choice = JOptionPane.showOptionDialog(this, "How are you feeling?", "MOOD", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, moods, moods[0]);
        String filter = "";
        if(choice == 0) filter = "Novel";
        if(choice == 1) filter = "Blue|Utopia";
        if(choice == 2) filter = "Manga|Attack";
        if(choice == 3) filter = "Tech|Java";
        searchField.setText(filter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + filter));
        logToConsole("MOOD FILTER APPLIED: " + moods[choice]);
    }

    private void rateBook() {
        if(titleLabel.getText().trim().isEmpty()) return;
        String[] stars = {"⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐"};
        int rating = JOptionPane.showOptionDialog(this, "Rate " + titleLabel.getText(), "Review", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, stars, stars[4]);
        if(rating >= 0) logToConsole("RATED BOOK: " + (rating+1) + " STARS");
    }

    private void setupHypeTimer() {
        hypeTimer = new Timer(100, e -> {
            Color randColor = new Color(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255));
            mainPanel.setBackground(randColor);
            createDetailPanel().setBackground(randColor);
        });
    }

    private void toggleHypeMode() {
        isHypeMode = !isHypeMode;
        if(isHypeMode) hypeTimer.start();
        else { hypeTimer.stop(); mainPanel.setBackground(bgColor); }
        logToConsole("HYPE MODE: " + isHypeMode);
    }

    private void borrowBook() {
        int row = bookTable.getSelectedRow();
        if(row == -1) return;
        int id = (int) tableModel.getValueAt(bookTable.convertRowIndexToModel(row), 0);
        if(borrowedStatus.getOrDefault(id, true)) {
            borrowedStatus.put(id, false);
            refreshTable();
            logToConsole("BORROWED BOOK ID: " + id);
            JOptionPane.showMessageDialog(this, "Book Borrowed!");
        } else JOptionPane.showMessageDialog(this, "Already Borrowed!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void returnBook() {
        int row = bookTable.getSelectedRow();
        if(row == -1) return;
        int id = (int) tableModel.getValueAt(bookTable.convertRowIndexToModel(row), 0);
        borrowedStatus.put(id, true);
        refreshTable();
        logToConsole("RETURNED BOOK ID: " + id);
        JOptionPane.showMessageDialog(this, "Book Returned.");
    }

    private void addToCart() {
        int row = bookTable.getSelectedRow();
        if(row == -1) return;
        String title = (String) tableModel.getValueAt(bookTable.convertRowIndexToModel(row), 1);
        for(Book b : bookDAO.getAllBooks()) {
            if(b.getTitle().equals(title)) {
                shoppingCart.add(b);
                updateCartButton();
                logToConsole("ADDED TO CART: " + title);
                Toolkit.getDefaultToolkit().beep();
                break;
            }
        }
    }

    private void checkoutCart() {
        if(shoppingCart.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty!"); return; }
        double total = shoppingCart.stream().mapToDouble(Book::getPrice).sum();

        if(total > 100) JOptionPane.showMessageDialog(this, "🏆 ACHIEVEMENT UNLOCKED: BIG SPENDER!\nYou spent over $100!");

        JOptionPane.showMessageDialog(this, "TOTAL BILL: $" + String.format("%.2f", total) + "\nPayment Successful!");
        logToConsole("CHECKOUT COMPLETE: $" + total);
        shoppingCart.clear();
        updateCartButton();
    }

    private void updateCartButton() {
        double total = shoppingCart.stream().mapToDouble(Book::getPrice).sum();
        btnCheckout.setText("CHECKOUT ($" + String.format("%.0f", total) + ")");
    }

    private void deleteCurrentBook() {
        int row = bookTable.getSelectedRow();
        if(row != -1) {
            int modelRow = bookTable.convertRowIndexToModel(row);
            int id = (int) tableModel.getValueAt(modelRow, 0);
            String title = (String) tableModel.getValueAt(modelRow, 1);
            if(JOptionPane.showConfirmDialog(this, "Delete " + title + "?") == JOptionPane.YES_OPTION) {
                Book b = new Book(id, title, "", "", 0, "");
                bookDAO.deleteBook(b);
                refreshTable();
                logToConsole("DELETED BOOK: " + title);
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Book> books = bookDAO.getAllBooks();
        for (Book b : books) {
            if(!borrowedStatus.containsKey(b.getId())) borrowedStatus.put(b.getId(), true);
            String status = borrowedStatus.get(b.getId()) ? "Available" : "Borrowed";
            tableModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getGenre(), b.getPrice(), status});
        }
        updateStats();
    }

    private void toggleCozyMode() {
        isCozyMode = !isCozyMode;
        if(isCozyMode) {
            bgColor = new Color(60, 40, 30);
            panelColor = new Color(80, 60, 50);
            neonMain = Color.ORANGE;
            neonSec = Color.YELLOW;
            textWhite = new Color(255, 240, 220);
        } else {
            bgColor = new Color(10, 10, 10);
            panelColor = new Color(20, 20, 20);
            neonMain = new Color(0, 255, 255);
            neonSec = new Color(57, 255, 20);
            textWhite = Color.WHITE;
        }
        mainPanel.setBackground(bgColor);
        SwingUtilities.updateComponentTreeUI(this);
        logToConsole("THEME CHANGED: " + (isCozyMode ? "COZY" : "NEON"));
    }

    private void unlockAdmin() {
        String pass = JOptionPane.showInputDialog(this, "Enter Password:");
        if("1234".equals(pass)) {
            isAdminUnlocked = true;
            btnDelete.setEnabled(true);
            btnDelete.setForeground(Color.RED);
            btnUnlock.setVisible(false);
            chartPanel.repaint();
            logToConsole("ADMIN ACCESS GRANTED");
        }
    }

    private void startReadingTimer() {
        readingTimer = new Timer(1000, e -> {
            secondsReading++;
            int min = secondsReading / 60;
            int sec = secondsReading % 60;
            timerLabel.setText(String.format("⏱️ %02d:%02d", min, sec));
        });
        readingTimer.start();
    }

    private void generateRandomQuote() {
        String[] quotes = {"Dattebayo! - Naruto", "I'm gonna be King of the Pirates!", "Winter is Coming.", "Code never lies.", "Keep calm and read Manga."};
        quoteLabel.setText("Quote: '" + quotes[new Random().nextInt(quotes.length)] + "'");
    }

    private JButton createNeonButton(String text, Color c) {
        JButton btn = new JButton(text);
        btn.setBackground(panelColor);
        btn.setForeground(c);
        btn.setBorder(new LineBorder(c));
        btn.setFocusPainted(false);
        return btn;
    }

    private void loadBookDetails(String title) {
        Book foundBook = null;
        for (Book b : bookDAO.getAllBooks()) {
            if (b.getTitle().equals(title)) { foundBook = b; break; }
        }
        if (foundBook == null) return;
        final Book b = foundBook;

        SwingUtilities.invokeLater(() -> {
            titleLabel.setText(b.getTitle());
            authorLabel.setText(b.getAuthor());
            priceLabel.setText("$" + b.getPrice());
            imageLabel.setText("LOADING...");
            imageLabel.setIcon(null);
            logToConsole("FETCHING DATA FOR: " + b.getTitle());
        });

        try {
            String path = b.getImagePath();
            if (path == null || !path.startsWith("http")) return;
            URL url = new URL(path);
            Image finalImage = null;
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                try (InputStream is = conn.getInputStream()) { finalImage = ImageIO.read(is); }
            } catch (Exception e1) {}

            if (finalImage == null) {
                finalImage = Toolkit.getDefaultToolkit().createImage(url);
                MediaTracker tracker = new MediaTracker(new Component(){});
                tracker.addImage(finalImage, 0);
                tracker.waitForAll(2000);
            }

            if (finalImage != null && finalImage.getWidth(null) > 0) {
                Image scaled = finalImage.getScaledInstance(200, 300, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(scaled);
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setText("");
                    imageLabel.setIcon(icon);
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> { imageLabel.setIcon(null); imageLabel.setText("BLOCKED"); });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LibraryDashboard::new);
    }
}