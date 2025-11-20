// MainApp.java (예시)

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class main {
    public static void main(String[] args) {

        // 1. 데이터베이스 초기화 및 users 테이블 생성
        // 이 코드가 없으면 테이블이 생성되지 않아 회원가입이 실패합니다.
        DatabaseUtil.initializeDatabase();

        // 2. GUI 시작
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("My Cake For You");
            CakeDesignPanel panel = new CakeDesignPanel();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.setSize(800, 600);
            frame.setResizable(false); // 크기 조절 방지 (레이아웃 고정)
            frame.setVisible(true);
        });
    }
}