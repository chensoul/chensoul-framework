package com.chensoul.test.security.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserInfoService {
    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    private static final ConcurrentMap<String, UserInfo> USER_INFO_CACHE = new ConcurrentHashMap<>(
            2);

    public UserInfoService(PasswordEncoder encoder) {
        USER_INFO_CACHE.putIfAbsent("admin",
                new UserInfo("admin", encoder.encode("pass"), "the Admin",
                        "public/profiles/admin.svg", ROLE_ADMIN));
        USER_INFO_CACHE.putIfAbsent("user",
                new UserInfo("user", encoder.encode("pass"), "the User",
                        "public/profiles/user.svg", ROLE_USER));
    }

    public UserInfo findByUsername(String username) {
        return USER_INFO_CACHE.get(username);
    }

}
