// CakeDesignException.java

/**
 * 프로그램의 특정 오류(예: 유효성 검사 실패 등)를 처리하기 위한 사용자 정의 예외 클래스입니다.
 * java.lang.Exception을 상속받아 Checked Exception으로 동작합니다.
 */
public class CakeDesignException extends Exception { // 💡 상속 (Inheritance)

    public CakeDesignException(String message) {
        super(message);
    }

    public CakeDesignException(String message, Throwable cause) {
        super(message, cause);
    }
}