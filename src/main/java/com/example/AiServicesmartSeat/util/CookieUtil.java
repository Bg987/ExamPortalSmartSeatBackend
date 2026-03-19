package com.example.AiServicesmartSeat.util;


import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class CookieUtil {

    private JwtUtil jwt;

    public jakarta.servlet.http.Cookie setCookie(Long Id,String role){

        return cookieSetting("AUTH_JWT",jwt.generateToken(Id,role),(24*60*60));
    }
    public jakarta.servlet.http.Cookie delCookie(String cookieName){

        return cookieSetting(cookieName,"",0);

    }
    public jakarta.servlet.http.Cookie cookieSetting(String cookieName, String cookieData, int age) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(cookieName, cookieData);

        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(age);
        cookie.setSecure(true);
        // 'Lax' is usually fine for local, but some older browsers
        // prefer no SameSite attribute on plain HTTP
        //cookie.setSecure(true);
//        cookie.setAttribute("SameSite", "None");
        cookie.setDomain("proxy-0xaq.onrender.com");
        // REQUIRED for Cross-Site cookie persistence
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}

