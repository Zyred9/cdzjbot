package com.bot.bots.web;

import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 后台管理登录拦截器
 * <p>页面请求 /pc/** 未登录 → 302 跳转 /login</p>
 * <p>AJAX/API 请求 /api/** 未登录 → 返回 401 JSON</p>
 *
 * @author zyred
 * @since 1.0
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Long userId = LoginController.getLoginUserId(session);
        if (userId != null) {
            return true;
        }

        if (isAjaxRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String body = JSONUtil.toJsonStr(Map.of("code", 401, "msg", "登录已过期，请重新登录"));
            response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            return false;
        }

        response.sendRedirect("/login");
        return false;
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return true;
        }
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }
        String uri = request.getRequestURI();
        return uri.startsWith("/api/");
    }
}
