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

// JDBC 관련 import
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CakeDesignPanel extends JPanel {

    // --- [1. 변수 선언] ---
    private static final int CREAM_WIDTH = 60;
    private static final int CREAM_HEIGHT = 60;
    private static final int FRUIT_WIDTH = 50;
    private static final int FRUIT_HEIGHT = 50;

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
    private Image cakeSaveImage;   // ⚠️ 케이크 저장 이미지 변수 추가
    private Image letterSaveImage; // ⚠️ 편지 저장 이미지 변수 유지
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
                handleMouseClick(e.getX(), e.getY());
            }
        });
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
            // ⚠️ 수정 2-1: 다음 상태를 'cake_save'로 변경 (케이크 저장 화면) ⚠️
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
        } else if (currentState.equals("cake_save")) {
            // ⚠️ 수정 2-2: 케이크 저장 화면에서 다음 버튼 클릭 시 'letter_selection'으로 이동 ⚠️
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
            // ⚠️ 수정 2-3: 편지 작성 후 다음 버튼 클릭 시 'letter_save'로 이동 (편지 저장 화면) ⚠️
            if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "letter_save";
                toggleInputFields(false);
                repaint();
            }
        } else if (currentState.equals("letter_save")) {
            if (isClickInArea(x, y, 321, 471, 350, 420)) {
                // saveCakeImage();
            }
            else if (isClickInArea(x, y, 40, 180, 460, 520)) {
                currentState = "letter_write"; // 편지 작성으로 돌아가기
                toggleInputFields(true);
                repaint();
            }
            // ⚠️ 수정 2-4: 편지 저장 후 다음 버튼 클릭 시 시작 화면으로 돌아가며 초기화 ⚠️
            else if (isClickInArea(x, y, 601, 751, 441, 541)) {
                currentState = "start";
                selectedBreadType = "none";
                decorations.clear();
                repaint();
            }
        }
    }

    // --- [4. 인증 로직] ---

    /** 회원가입 로직 */
    private void performSignup(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = password;

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "회원가입 성공! 이제 로그인해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);

            currentState = "login";
            toggleAuthFields(false, "signup");
            toggleAuthFields(true, "login");
            repaint();

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                JOptionPane.showMessageDialog(this, "이미 존재하는 아이디입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            } else {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 로그인 로직 */
    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 모두 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "SELECT password FROM users WHERE username = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                if (storedPassword.equals(password)) {
                    JOptionPane.showMessageDialog(this, username + "님, 로그인 성공!", "환영", JOptionPane.INFORMATION_MESSAGE);

                    currentState = "bread_selection";
                    selectedBreadType = "none";
                    toggleAuthFields(false, "login");
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this, "비밀번호가 일치하지 않습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "존재하지 않는 아이디입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
        }
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

            // ⚠️ 수정 1-1: cake_save.png 로드
            cakeSaveImage = loadImage("img/cake_save.png");
            // ⚠️ 수정 1-2: letter_save.jpg 로드
            letterSaveImage = loadImage("img/letter_save.jpg");

            for (int i = 0; i < 9; i++) letterImages[i] = loadImage("img/letter" + (i + 1) + ".png");

            creamChocoImg = loadImage("img/Cream_Chocolate.png", CREAM_WIDTH, CREAM_HEIGHT);
            creamStrawImg = loadImage("img/Cream_Strawberry.png", CREAM_WIDTH, CREAM_HEIGHT);
            creamWhiteImg = loadImage("img/Cream_White.png", CREAM_WIDTH, CREAM_HEIGHT);

            fruitBananaImg = loadImage("img/fruit_banana.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitGrapeImg = loadImage("img/fruit_grapes.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitStrawImg = loadImage("img/fruit_strawberry.png", FRUIT_WIDTH, FRUIT_HEIGHT);
            fruitOrangeImg = loadImage("img/fruit_orange.png", FRUIT_WIDTH, FRUIT_HEIGHT);

        } catch (Exception e) {
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
    protected void paintComponent(Graphics g) {
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
        else if (currentState.equals("cake_save")) bg = cakeSaveImage;   // ⚠️ cake_save.png 표시
        else if (currentState.equals("letter_save")) bg = letterSaveImage; // ⚠️ letter_save.jpg 표시

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