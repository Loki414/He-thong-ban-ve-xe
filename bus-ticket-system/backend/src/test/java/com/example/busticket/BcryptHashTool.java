package com.example.busticket;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Chạy một lần để in hash BCrypt (giống backend).
 * Terminal: mvn -q -DskipTests exec:java -Dexec.mainClass="com.example.busticket.BcryptHashTool" -Dexec.classpathScope=test
 */
public class BcryptHashTool {
    public static void main(String[] args) {
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();
        System.out.println("-- admin123 --");
        System.out.println(enc.encode("admin123"));
        System.out.println("-- 123456 --");
        System.out.println(enc.encode("123456"));
    }
}
