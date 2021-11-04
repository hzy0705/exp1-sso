package com.sso.login.controller;

import com.sso.login.pojo.User;
import com.sso.login.utils.LoginCacheUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static Set<User> dbUsers;
    static {
        dbUsers = new HashSet<>();
        dbUsers.add(new User(0,"zhangsan","12345"));
        dbUsers.add(new User(1,"lisi","12345"));
        dbUsers.add(new User(2,"wangwu","12345"));
    }

    @PostMapping
    public String doLogin(User user, HttpSession session, HttpServletResponse response){
        String target = (String) session.getAttribute("target");
        //模拟数据库中通过登录的用户名和密码去查找数据库用户
        Optional<User> first = dbUsers.stream().filter(dbUsers -> dbUsers.getUsername().equals(user.getUsername()) &&
                dbUsers.getPassword().equals(user.getPassword()))
                .findFirst();
        if(first.isPresent()){
            //保存用户登录信息
            String token = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("TOKEN",token);
            cookie.setDomain("codeshop.com");
            response.addCookie(cookie);
            LoginCacheUtil.loginUser.put(token, first.get());
        } else{
            //登录失败
            session.setAttribute("msg","用户名或密码错误");
            return "login";
        }

        //重定向到target地址
        return "redirect:"+target;
    }

    @GetMapping("info")
    public ResponseEntity<User> getUserInfo(String token){
        if(!StringUtils.isEmpty(token)){
            User user = LoginCacheUtil.loginUser.get(token);
            return ResponseEntity.ok(user);
        }else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
