package com.belajar.course;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration; // <--- PENTING: Import ini wajib ada

// Perhatikan bagian dalam kurung (exclude = ...) di bawah ini.
// Ini perintah untuk MEMATIKAN fitur login/security sementara.
@SpringBootApplication
public class CourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseApplication.class, args);
    }

}