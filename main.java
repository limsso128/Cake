// main.java

import javax.swing.JFrame;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

// 클래스 이름을 Main으로 변경
public class main {

    public static void main(String[] args) {

        // ⚠️ 1. SQLite DB 파일 생성 및 users 테이블 초기화 ⚠️
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // users 테이블 생성 코드
            String sql = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "password TEXT NOT NULL"
                    + ");";
            stmt.execute(sql);
            System.out.println("SQLite DB 및 users 테이블 준비 완료.");

        } catch (SQLException e) {
            System.err.println("DB 초기화 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
        // ----------------------------------------------------

        JFrame frame = new JFrame("나만의 케이크 만들기");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // CakeDesignPanel 인스턴스 사용
        // CakeDesignPanel 클래스가 현재 파일과 같은 패키지/폴더에 있으므로 바로 사용 가능
        frame.add(new CakeDesignPanel());

        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}