// CakeDesignPanel.java

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// 💡 Thread/Concurrency 관련 import
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import java.util.concurrent.TimeUnit; // ExecutorService 종료 시 사용

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;

// JDBC 관련 import (데이터베이스, I/O, Network 관련 처리의 일종)
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CakeDesignPanel extends JPanel { // 💡 GUI (JPanel 상속)

    // --- [1. 변수 선언] ---
    private static final int CREAM_WIDTH = 60;
    private static final int CREAM_HEIGHT = 60;
    private static final int FRUIT_WIDTH = 60;
    private static final int FRUIT_HEIGHT = 60;

    // 💡 Thread (ExecutorService 선언)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // 이미지 변수들
    private Image startImage;
    private Image loginBackgroundImage;
    private Image signupBackgroundImage;
    private Image breadSelectionImage;
    private Image creamSelectionImage;
    private Image fruitSelectionImage;

    private Image breadBasicImage, breadChocoImage, breadStrawberryImage;

    private Image letterSelectionImage;
    private Image letterWriteImage;
    private Image cakeSaveImage;
    private Image letterSaveImage;
    private Image[] letterImages = new Image[9];

    private Image creamChocoImg, creamStrawImg, creamWhiteImg;
    private Image fruitBananaImg, fruitGrapeImg, fruitStrawImg, fruitOrangeImg;

    private String currentState;
    private String selectedBreadType = "none";
    private String selectedTool = "none";
    private int selectedLetterNumber = 0;
    private ArrayList<Placement> decorations = new ArrayList<>();

    private int cakeX = 0, cakeY = 0, cakeWidth = 0, cakeHeight = 0;

    // 로그인/회원가입 필드
    private JTextField loginIdField;
    private JPasswordField loginPwField;
    private JTextField signupIdField;
    private JPasswordField signupPwField;

    private JTextField dateField;
    private JTextField toField;
    private JTextPane bodyPane;
    private JTextField fromField;

    private final Color TEXT_COLOR = new Color(80, 50, 40);
    private final Color SELECTION_COLOR = new Color(255, 200, 200);
    private final Font BOLD_FONT = new Font("Malgun Gothic", Font.BOLD, 16);
    private final Font FIELD_FONT = new Font("Malgun Gothic", Font.PLAIN, 18);
    private final Font BODY_FONT = new Font("Malgun Gothic", Font.PLAIN, 17);

    static class Placement {
        int x, y;
        Image image;
        String type;
        public Placement(int x, int y, Image image, String type) {
            this.x = x; this.y = y; this.image = image; this.type = type;
        }
    }

    // --- [2. 생성자] ---
    public CakeDesignPanel() {
        this.setLayout(null);
        loadImages();
        currentState = "start";

        loginIdField = createStyledInputField("아이디");
        loginPwField = createStyledPasswordInput();

        signupIdField = createStyledInputField("새 아이디");
        signupPwField = createStyledPasswordInput();

        this.add(loginIdField);
        this.add(loginPwField);
        this.add(signupIdField);
        this.add(signupPwField);

        dateField = createStyledTextField(JTextField.RIGHT, "2024. 12. 25");
        toField = createStyledTextField(JTextField.LEFT, "To. ");
        toField.addActionListener(e -> bodyPane.requestFocus());
        fromField = createStyledTextField(JTextField.RIGHT, "From. ");

        bodyPane = new JTextPane();
        bodyPane.setOpaque(false);
        bodyPane.setVisible(false);
        bodyPane.setSelectionColor(SELECTION_COLOR);

        StyledDocument doc = bodyPane.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, "Malgun Gothic");
        StyleConstants.setFontSize(style, 17);
        StyleConstants.setForeground(style, TEXT_COLOR);
        StyleConstants.setLineSpacing(style, 0.5f);
        bodyPane.setParagraphAttributes(style, true);

        this.add(dateField);
        this.add(toField);
        this.add(bodyPane);
        this.add(fromField);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // ⚠️ 콘솔 좌표 출력 ⚠️
                System.out.println("Clicked coordinates: " + e.getX() + ", " + e.getY());
                handleMouseClick(e.getX(), e.getY());
            }
        });

        // 💡 Thread: 프로그램 종료 시 ExecutorService도 안전하게 종료
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown();
            try {
                // 60초 동안 대기하여 스레드가 종료되도록 시도
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // 강제 종료
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("ExecutorService terminated.");
        }));
    }

    // --- [3. 마우스 클릭 로직] ---
    private void handleMouseClick(int x, int y) {
        if (currentState.equals("start")) {
            currentState = "login";
            toggleInputFields(false);
            toggleAuthFields(true, "login");
            repaint();
            return;
        }

        else if (currentState.equals("login")) {
            // [로그인 버튼]
            if (isClickInArea(x, y, 320, 470, 350, 400)) {
                performLogin(loginIdField.getText(), new String(loginPwField.getPassword()));
                return;
            }
            // [회원가입 버튼]
            else if (isClickInArea(x, y, 320, 470, 410, 460)) {
                currentState = "signup";
                toggleAuthFields(false, "login");
                toggleAuthFields(true, "signup");
                repaint();
                return;
            }
            else if (isClickInArea(x, y, 150, 400, 240, 290)) {
                loginIdField.requestFocus();
            }
            else if (isClickInArea(x, y, 150, 400, 300, 350)) {
                loginPwField.requestFocus();
            }
        }

        else if (currentState.equals("signup")) {
            // [회원가입 완료 버튼]
            if (isClickInArea(x, y, 320, 470, 350, 400)) {
                performSignup(signupIdField.getText(), new String(signupPwField.getPassword()));
                return;
            }
            // [로그인으로 돌아가기 버튼]
            else if (isClickInArea(x, y, 320, 470, 410, 460)) {
                currentState = "login";
                toggleAuthFields(false, "signup");
                toggleAuthFields(true, "login");
                repaint();
                return;
            }
            else if (isClickInArea(x, y, 150, 400, 240, 290)) {
                signupIdField.requestFocus();
            }
            else if (isClickInArea(x, y, 150, 400, 300, 350)) {
                signupPwField.requestFocus();
            }
        }

        else if (currentState.equals("bread_selection")) {
            // 초코, 딸기, 초코 순서대로
            if (isClickInArea(x, y, 121, 271, 26, 126)) selectedBreadType = "choco";
            else if (isClickInArea(x, y, 312, 462, 26, 126)) selectedBreadType = "strawberry";
            else if (isClickInArea(x, y, 489, 639, 18, 118)) selectedBreadType = "choco";

            else if (isClickInArea(x, y, 601, 751, 441, 541)) {
                if (selectedBreadType.equals("none")) {
                    JOptionPane.showMessageDialog(this, "빵을 먼저 선택해주세요!");
                    return;
                }
                currentState = "cream_selection";
                decorations.clear();
                selectedTool = "none";
                repaint();
            }
            repaint();
        } else if (currentState.equals("cream_selection")) {
            if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "fruit_selection";
                selectedTool = "none";
                repaint();
            }
            else if (isClickInArea(x, y, 119, 269, 39, 139)) selectedTool = "cream_choco";
            else if (isClickInArea(x, y, 314, 464, 42, 142)) selectedTool = "cream_straw";
            else if (isClickInArea(x, y, 496, 646, 38, 138)) selectedTool = "cream_white";

            else {
                if (isInCakeArea(x, y)) {
                    Image img = null;
                    if (selectedTool.equals("cream_choco")) img = creamChocoImg;
                    else if (selectedTool.equals("cream_straw")) img = creamStrawImg;
                    else if (selectedTool.equals("cream_white")) img = creamWhiteImg;
                    if (img != null) {
                        decorations.add(new Placement(x - (img.getWidth(null)/2), y - (img.getHeight(null)/2), img, "cream"));
                        repaint();
                    }
                }
            }
        } else if (currentState.equals("fruit_selection")) {
            if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "cake_save";
                selectedTool = "none";
                repaint();
            }
            else if (isClickInArea(x, y, 168, 238, 53, 123)) selectedTool = "fruit_banana";
            else if (isClickInArea(x, y, 293, 363, 50, 120)) selectedTool = "fruit_grape";
            else if (isClickInArea(x, y, 413, 483, 57, 127)) selectedTool = "fruit_strawberry";
            else if (isClickInArea(x, y, 547, 617, 51, 121)) selectedTool = "fruit_orange";

            else {
                if (isInCakeArea(x, y)) {
                    Image img = null;
                    if (selectedTool.equals("fruit_banana")) img = fruitBananaImg;
                    else if (selectedTool.equals("fruit_grape")) img = fruitGrapeImg;
                    else if (selectedTool.equals("fruit_strawberry")) img = fruitStrawImg;
                    else if (selectedTool.equals("fruit_orange")) img = fruitOrangeImg;
                    if (img != null) {
                        decorations.add(new Placement(x - (img.getWidth(null)/2), y - (img.getHeight(null)/2), img, "fruit"));
                        repaint();
                    }
                }
            }
        }
        // 케이크 저장 화면 (cake_save.png)
        else if (currentState.equals("cake_save")) {
            // [케이크 저장 버튼] 클릭 영역: (337, 360) ~ (437, 400)
            if (isClickInArea(x, y, 337, 437, 360, 400)) {
                saveCakeImage(); // 케이크 이미지 저장 메서드 호출
                return;
            }
            // [다음] 버튼 클릭 시 letter_selection으로 이동
            if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "letter_selection";
                repaint();
            }
        }
        else if (currentState.equals("letter_selection")) {
            int clickedLetter = 0;
            if (isClickInArea(x, y, 142, 242, 60, 160)) clickedLetter = 1;
            else if (isClickInArea(x, y, 336, 436, 60, 160)) clickedLetter = 2;
            else if (isClickInArea(x, y, 538, 638, 61, 161)) clickedLetter = 3;
            else if (isClickInArea(x, y, 141, 241, 203, 303)) clickedLetter = 4;
            else if (isClickInArea(x, y, 337, 437, 206, 306)) clickedLetter = 5;
            else if (isClickInArea(x, y, 536, 636, 212, 312)) clickedLetter = 6;
            else if (isClickInArea(x, y, 141, 241, 353, 453)) clickedLetter = 7;
            else if (isClickInArea(x, y, 340, 440, 353, 453)) clickedLetter = 8;
            else if (isClickInArea(x, y, 540, 640, 350, 450)) clickedLetter = 9;

            if (clickedLetter != 0) {
                selectedLetterNumber = clickedLetter;
                currentState = "letter_write";
                toggleInputFields(true);
                toField.requestFocus();
                repaint();
            }
        } else if (currentState.equals("letter_write")) {
            if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "letter_save";
                toggleInputFields(false);
                repaint();
            }
        }
        // 편지 저장 화면 (letter_save.jpg)
        else if (currentState.equals("letter_save")) {
            // [편지 저장 버튼] 클릭 영역: (337, 437, 360, 400)
            if (isClickInArea(x, y, 337, 437, 360, 400)) {
                saveLetterImage(); // 편지 이미지 저장 메서드 호출
                return;
            }
            // [편지 작성으로 돌아가기]
            else if (isClickInArea(x, y, 40, 180, 460, 520)) {
                currentState = "letter_write";
                toggleInputFields(true);
                repaint();
            }
            // [처음으로]
            else if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "start";
                selectedBreadType = "none";
                decorations.clear();
                repaint();
            }
        }
    }

    // 💡 이미지 저장 기능 (I/O, File)
    private void saveCakeImage() {
        int width = getWidth();
        int height = getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, width, height);

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Image breadBase = null;
        if ("basic".equals(selectedBreadType)) breadBase = breadBasicImage;
        else if ("choco".equals(selectedBreadType)) breadBase = breadChocoImage;
        else if ("strawberry".equals(selectedBreadType)) breadBase = breadStrawberryImage;

        if (breadBase != null) {
            int imgW = breadBase.getWidth(this);
            int imgH = breadBase.getHeight(this);
            if (imgW > 0 && imgH > 0) {
                int maxW = 520; int maxH = 370;
                double widthRatio = (double) maxW / imgW;
                double heightRatio = (double) maxH / imgH;
                double scale = Math.min(widthRatio, heightRatio);
                int finalW = (int) (imgW * scale);
                int finalH = (int) (imgH * scale);

                int x = (width - finalW) / 2;
                int y = (height - finalH) / 2 + 90;

                g2.drawImage(breadBase, x, y, finalW, finalH, null);

                for (Placement p : decorations) {
                    g2.drawImage(p.image, p.x, p.y, null);
                }
            }
        }

        g2.dispose();

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("케이크 이미지를 저장할 위치를 선택하세요.");
        fileChooser.setSelectedFile(new File("MyCake_" + System.currentTimeMillis() + ".png"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".png")) {
                    fileToSave = new File(filePath + ".png");
                }

                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(this,
                        "케이크가 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(),
                        "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) { // 💡 예외처리 (IOException)
                JOptionPane.showMessageDialog(this,
                        "이미지 저장 중 오류가 발생했습니다: " + ex.getMessage(),
                        "저장 오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "이미지 저장이 취소되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    /**
     * 현재 작성된 편지 (편지지 + 텍스트만)를 이미지 파일로 저장합니다.
     */
    private void saveLetterImage() {
        if (selectedLetterNumber == 0) {
            JOptionPane.showMessageDialog(this, "먼저 편지지를 선택하고 내용을 작성해야 저장할 수 있습니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 이미지 크기를 편지지 크기(405x304)로 설정
        int targetWidth = 405;
        int targetHeight = 304;

        BufferedImage image = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        // 렌더링 품질 설정
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 흰색 배경으로 채우기 (요청대로 주변 배경 이미지 제거)
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, targetWidth, targetHeight);

        // 2. 편지지 이미지 그리기 (새 이미지의 (0, 0)에 맞춤)
        Image selectedLetterImage = letterImages[selectedLetterNumber - 1];
        if (selectedLetterImage != null) {

            int lx = 0;
            int ly = 0;

            g2.drawImage(selectedLetterImage, lx, ly, targetWidth, targetHeight, null);

            // 3. 텍스트 필드 내용 그리기 (좌표는 lx, ly=0을 기준으로 계산)
            g2.setColor(TEXT_COLOR);
            FontMetrics fm;

            // --- Date Field (우측 정렬) ---
            g2.setFont(BOLD_FONT);
            fm = g2.getFontMetrics();
            String dateText = dateField.getText();
            int dateFieldLeft = targetWidth - 160;
            int dateFieldWidth = 140;
            int dateY = ly + 18 + fm.getAscent();
            // 우측 정렬된 X 좌표 계산
            g2.drawString(dateText, dateFieldLeft + dateFieldWidth - fm.stringWidth(dateText), dateY);

            // --- From Field (우측 정렬) ---
            String fromText = fromField.getText();
            int fromFieldLeft = targetWidth - 160;
            int fromFieldWidth = 140;
            int fromY = ly + targetHeight - 40 + fm.getAscent();
            g2.drawString(fromText, fromFieldLeft + fromFieldWidth - fm.stringWidth(fromText), fromY);

            // --- To Field (좌측 정렬) ---
            String toText = toField.getText();
            int toX = lx + 25;
            int toY = ly + 45 + fm.getAscent();
            g2.drawString(toText, toX, toY);

            // --- Body Pane (JTextPane) 내용 그리기 ---
            String bodyText;
            try { // 💡 예외처리 (BadLocationException)
                bodyText = bodyPane.getDocument().getText(0, bodyPane.getDocument().getLength());
            } catch (BadLocationException e) {
                bodyText = "";
            }

            // Body Pane 영역 정의 (편지지 내 텍스트 영역)
            int bodyX = lx + 25;
            int bodyYStart = ly + 85;
            int bodyWidth = targetWidth - 50;
            int bodyHeight = targetHeight - 130;

            // 폰트 설정 (BODY_FONT 사용)
            g2.setFont(BODY_FONT);
            fm = g2.getFontMetrics();
            int lineHeight = fm.getHeight();
            int currentY = bodyYStart + fm.getAscent();

            // 텍스트를 줄바꿈하여 그리기
            String[] paragraphs = bodyText.split("\n");
            for (String paragraph : paragraphs) {
                String currentLine = "";
                // 단어(또는 띄어쓰기) 단위로 래핑
                for (String word : paragraph.split(" ")) {
                    String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;

                    if (fm.stringWidth(testLine) < bodyWidth) {
                        currentLine = testLine;
                    } else {
                        // 현재 줄 출력
                        if (currentY < bodyYStart + bodyHeight) {
                            g2.drawString(currentLine, bodyX, currentY);
                            currentY += lineHeight;
                        } else {
                            break; // 영역 초과
                        }
                        // 다음 줄 시작
                        currentLine = word;
                    }
                }
                // 단락의 마지막 줄 처리 (또는 영역 초과 전까지)
                if (!currentLine.trim().isEmpty() && currentY < bodyYStart + bodyHeight) {
                    g2.drawString(currentLine, bodyX, currentY);
                    currentY += lineHeight;
                }

                // 단락 간 간격 (새 줄)
                if (currentY < bodyYStart + bodyHeight) {
                    currentY += lineHeight / 3; // 약간의 단락 간격
                }
                if(currentY >= bodyYStart + bodyHeight) break;
            }
        }

        g2.dispose();

        // 4. JFileChooser를 사용하여 저장 경로를 사용자에게 묻습니다.
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("편지 이미지를 저장할 위치를 선택하세요.");
        fileChooser.setSelectedFile(new File("MyLetter_" + System.currentTimeMillis() + ".png"));

        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".png")) {
                    fileToSave = new File(filePath + ".png");
                }

                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(this,
                        "편지가 성공적으로 저장되었습니다:\n" + fileToSave.getAbsolutePath(),
                        "저장 완료", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) { // 💡 예외처리 (IOException)
                JOptionPane.showMessageDialog(this,
                        "이미지 저장 중 오류가 발생했습니다: " + ex.getMessage(),
                        "저장 오류", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "이미지 저장이 취소되었습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }


    // --- [4. 인증 로직] ---

    /** * 회원가입 로직 💡 Thread를 이용한 비동기 처리 및 예외처리 강화
     */
    private void performSignup(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 💡 Thread: 데이터베이스 작업을 백그라운드 스레드로 실행 (GUI 멈춤 방지)
        executorService.submit(() -> { // 💡 Thread
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                String hashedPassword = password;

                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);

                pstmt.executeUpdate();

                // 💡 GUI: 작업 완료 후 GUI 업데이트는 EDT에서 안전하게 실행
                SwingUtilities.invokeLater(() -> { // 💡 Thread/GUI
                    JOptionPane.showMessageDialog(this, "회원가입 성공! 이제 로그인해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                    currentState = "login";
                    toggleAuthFields(false, "signup");
                    toggleAuthFields(true, "login");
                    repaint();
                });

            } catch (SQLException e) { // 💡 예외처리: DB 관련 예외 처리
                // GUI 업데이트는 EDT에서 실행
                SwingUtilities.invokeLater(() -> {
                    if (e.getMessage().contains("UNIQUE constraint failed")) {
                        JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.", "오류", JOptionPane.ERROR_MESSAGE);
                    } else {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    /** * 로그인 로직 💡 Thread를 이용한 비동기 처리 및 예외처리 강화
     */
    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 💡 Thread: 데이터베이스 작업을 백그라운드 스레드로 실행
        executorService.submit(() -> { // 💡 Thread
            String sql = "SELECT password FROM users WHERE username = ?";

            try (Connection conn = DatabaseUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();

                final String message;
                final boolean success;

                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (storedPassword.equals(password)) {
                        message = username + "님, 로그인 성공!";
                        success = true;
                    } else {
                        message = "비밀번호가 일치하지 않습니다.";
                        success = false;
                    }
                } else {
                    message = "존재하지 않는 아이디입니다.";
                    success = false;
                }

                // 💡 GUI: GUI 업데이트는 EDT에서 안전하게 실행
                SwingUtilities.invokeLater(() -> { // 💡 Thread/GUI
                    JOptionPane.showMessageDialog(this, message, success ? "환영" : "오류", success ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
                    if (success) {
                        currentState = "bread_selection";
                        selectedBreadType = "none";
                        toggleAuthFields(false, "login");
                        repaint();
                    }
                });

            } catch (SQLException e) { // 💡 예외처리: DB 관련 예외 처리
                e.printStackTrace();
                // GUI 업데이트는 EDT에서 실행
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }


    // --- [5. 유틸리티 메서드] ---

    private JTextField createStyledInputField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setFont(FIELD_FONT);
        field.setHorizontalAlignment(JTextField.LEFT);
        field.setVisible(false);

        return field;
    }

    private JPasswordField createStyledPasswordInput() {
        JPasswordField field = new JPasswordField();
        field.setFont(FIELD_FONT);
        field.setHorizontalAlignment(JPasswordField.LEFT);
        field.setVisible(false);

        return field;
    }

    private void toggleAuthFields(boolean show, String type) {
        if (type.equals("login")) {
            loginIdField.setVisible(show);
            loginPwField.setVisible(show);
            loginIdField.setBounds(250, 250, 300, 30);
            loginPwField.setBounds(250, 310, 300, 30);
            if (show) loginIdField.requestFocus();
        } else if (type.equals("signup")) {
            signupIdField.setVisible(show);
            signupPwField.setVisible(show);
            signupIdField.setBounds(250, 250, 300, 30);
            signupPwField.setBounds(250, 310, 300, 30);
            if (show) signupIdField.requestFocus();
        }
        if (!show) {
            loginIdField.setText("");
            loginPwField.setText("");
            signupIdField.setText("");
            signupPwField.setText("");
        }
    }

    private boolean isInCakeArea(int x, int y) {
        if (cakeWidth == 0 || cakeHeight == 0) return false;
        return (x >= cakeX && x <= cakeX + cakeWidth) &&
                (y >= cakeY && y <= cakeY + cakeHeight);
    }

    // 파일명을 img/ 경로에 맞춰 수정했습니다.
    private void loadImages() {
        try {
            startImage = loadImage("img/background_start.jpg");
            loginBackgroundImage = loadImage("img/login_background.png");
            signupBackgroundImage = loadImage("img/signup_background.png");
            breadSelectionImage = loadImage("img/bread_selection.png");
            creamSelectionImage = loadImage("img/cream_selection.png");
            fruitSelectionImage = loadImage("img/fruit_selection.png");

            breadBasicImage = loadImage("img/Bread_Basic.png");
            breadChocoImage = loadImage("img/Bread_Choco.png");
            breadStrawberryImage = loadImage("img/Bread_Strawberry.png");

            letterSelectionImage = loadImage("img/letter_selection.png");
            letterWriteImage = loadImage("img/letter_write.png");
            cakeSaveImage = loadImage("img/cake_save.png");
            letterSaveImage = loadImage("img/letter_save.jpg");

            for (int i = 0; i < 9; i++) letterImages[i] = loadImage("img/letter" + (i + 1) + ".png");

            creamChocoImg = loadImage("img/Cream_Chocolate.png", CREAM_WIDTH, CREAM_HEIGHT);
            creamStrawImg = loadImage("img/Cream_Strawberry.png", CREAM_WIDTH, CREAM_HEIGHT);
            creamWhiteImg = loadImage("img/Cream_White.png", CREAM_WIDTH, CREAM_HEIGHT);

            fruitBananaImg = loadImage("img/fruit_banana.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitGrapeImg = loadImage("img/fruit_grapes.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitStrawImg = loadImage("img/fruit_strawberry.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitOrangeImg = loadImage("img/fruit_orange.png", FRUIT_WIDTH, FRUIT_HEIGHT);

        } catch (Exception e) { // 💡 예외처리 (Exception)
            System.err.println("이미지 로드 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // loadImage 헬퍼 메서드 1: 원본 사이즈 로드
    private Image loadImage(String fileName) {
        try {
            java.net.URL url = getClass().getResource(fileName);
            if (url == null) {
                System.err.println("경고: 이미지를 찾을 수 없습니다! 파일명: " + fileName);
            }
            if (url != null) {
                return ImageIO.read(url);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // loadImage 헬퍼 메서드 2: 크기 조정하여 로드
    private Image loadImage(String fileName, int w, int h) {
        Image img = loadImage(fileName);
        return (img != null) ? new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH)).getImage() : null;
    }

    private JTextField createStyledTextField(int alignment, String defaultText) {
        JTextField field = new JTextField(defaultText);
        field.setOpaque(false); field.setBorder(null);
        field.setForeground(TEXT_COLOR); field.setFont(BOLD_FONT);
        field.setSelectionColor(SELECTION_COLOR); field.setHorizontalAlignment(alignment);
        field.setVisible(false); return field;
    }

    private void toggleInputFields(boolean show) {
        dateField.setVisible(show); toField.setVisible(show);
        bodyPane.setVisible(show); fromField.setVisible(show);
        if (!show) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        }
    }

    private boolean isClickInArea(int x, int y, int x1, int x2, int y1, int y2) {
        return (x >= x1 && x <= x2) && (y >= y1 && y <= y2);
    }

    // --- [6. 화면 그리기] ---
    @Override
    protected void paintComponent(Graphics g) { // 💡 GUI
        super.paintComponent(g);

        toggleInputFields(false);

        if (currentState.equals("login")) {
            if (loginBackgroundImage != null) g.drawImage(loginBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(Color.LIGHT_GRAY); g.fillRect(0, 0, getWidth(), getHeight()); }
            toggleAuthFields(true, "login");
            return;
        }
        else if (currentState.equals("signup")) {
            if (signupBackgroundImage != null) g.drawImage(signupBackgroundImage, 0, 0, getWidth(), getHeight(), this);
            else { g.setColor(Color.PINK); g.fillRect(0, 0, getWidth(), getHeight()); }
            toggleAuthFields(true, "signup");
            return;
        }

        toggleAuthFields(false, "login");
        toggleAuthFields(false, "signup");

        if (currentState.equals("bread_selection")) {
            if (breadSelectionImage != null) g.drawImage(breadSelectionImage, 0, 0, getWidth(), getHeight(), this);

            Image overlayImg = null;
            if ("basic".equals(selectedBreadType)) overlayImg = breadBasicImage;
            else if ("choco".equals(selectedBreadType)) overlayImg = breadChocoImage;
            else if ("strawberry".equals(selectedBreadType)) overlayImg = breadStrawberryImage;

            if (!selectedBreadType.equals("none")) {
                drawCenteredImage(g, overlayImg);
            }
            return;
        }

        if (currentState.equals("cream_selection") || currentState.equals("fruit_selection")) {
            if (currentState.equals("cream_selection")) {
                if (creamSelectionImage != null) g.drawImage(creamSelectionImage, 0, 0, getWidth(), getHeight(), this);
            } else {
                if (fruitSelectionImage != null) g.drawImage(fruitSelectionImage, 0, 0, getWidth(), getHeight(), this);
            }

            Image breadBase = null;
            if ("basic".equals(selectedBreadType)) breadBase = breadBasicImage;
            else if ("choco".equals(selectedBreadType)) breadBase = breadChocoImage;
            else if ("strawberry".equals(selectedBreadType)) breadBase = breadStrawberryImage;
            drawCenteredImage(g, breadBase);

            for (Placement p : decorations) {
                g.drawImage(p.image, p.x, p.y, this);
            }
            return;
        }

        Image bg = null;
        if (currentState.equals("start")) bg = startImage;
        else if (currentState.equals("letter_selection")) bg = letterSelectionImage;
        else if (currentState.equals("letter_write")) bg = letterWriteImage;
        else if (currentState.equals("cake_save")) bg = cakeSaveImage;
        else if (currentState.equals("letter_save")) bg = letterSaveImage;

        if (bg != null) g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);

        if (currentState.equals("letter_write") && selectedLetterNumber != 0) {
            Image selectedLetterImage = letterImages[selectedLetterNumber - 1];
            if (selectedLetterImage != null) {
                int targetWidth = 405; int targetHeight = 304;
                int lx = (getWidth() - targetWidth) / 2;
                int ly = (getHeight() - targetHeight) / 2;
                g.drawImage(selectedLetterImage, lx, ly, targetWidth, targetHeight, this);

                dateField.setBounds(lx + targetWidth - 160, ly + 18, 140, 25);
                toField.setBounds(lx + 25, ly + 45, 200, 30);
                bodyPane.setBounds(lx + 25, ly + 85, targetWidth - 50, targetHeight - 130);
                fromField.setBounds(lx + targetWidth - 160, ly + targetHeight - 40, 140, 30);
                toggleInputFields(true);
            }
        }
    }

    private void drawCenteredImage(Graphics g, Image img) {
        if (img != null) {
            int imgW = img.getWidth(this);
            int imgH = img.getHeight(this);
            if (imgW > 0 && imgH > 0) {
                int maxW = 520; int maxH = 370;
                double widthRatio = (double) maxW / imgW;
                double heightRatio = (double) maxH / imgH;
                double scale = Math.min(widthRatio, heightRatio);
                int finalW = (int) (imgW * scale);
                int finalH = (int) (imgH * scale);
                int x = (getWidth() - finalW) / 2;
                int y = (getHeight() - finalH) / 2 + 90;

                this.cakeX = x; this.cakeY = y;
                this.cakeWidth = finalW; this.cakeHeight = finalH;

                g.drawImage(img, x, y, finalW, finalH, this);
            }
        }
    }
}