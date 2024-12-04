package com.chensoul.test.security.data;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {
    private final UserInfoService userInfoService;

    @GetMapping("/admin-only/userinfo")
    public UserInfo info() {
        return userInfoService.findByUsername("admin");
    }

    @GetMapping("/public/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
