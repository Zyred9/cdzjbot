package com.bot.bots.web;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;

/**
 * PC 后台管理系统登录 Controller
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequiredArgsConstructor
public class LoginController {

    public static final String SESSION_KEY = "LOGIN_USER_ID";

    private final ConfigService configService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (getLoginUserId(session) != null) {
            return "redirect:/pc/team";
        }
        return "login";
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        if (getLoginUserId(session) != null) {
            return "redirect:/pc/team";
        }
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam("userId") Long userId,
                          @RequestParam("password") String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Config config = this.configService.queryConfig();

        if (!config.hasEdit(userId)) {
            redirectAttributes.addFlashAttribute("error", "您没有权限访问后台管理系统");
            return "redirect:/login";
        }

        User user = this.userService.getById(userId);
        if (Objects.isNull(user) || StrUtil.isBlank(user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "您的账号尚未设置登录密码，请联系管理员");
            return "redirect:/login";
        }

        String hashed = DigestUtil.md5Hex(password);
        if (!StrUtil.equals(hashed, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "密码错误");
            return "redirect:/login";
        }

        session.setAttribute(SESSION_KEY, userId);
        return "redirect:/pc/team";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/api/session/check")
    @ResponseBody
    public Map<String, Object> checkSession(HttpSession session) {
        Long userId = getLoginUserId(session);
        if (userId != null) {
            return Map.of("code", 0, "data", Map.of("userId", userId));
        }
        return Map.of("code", 401, "msg", "未登录");
    }

    public static Long getLoginUserId(HttpSession session) {
        Object attr = session.getAttribute(SESSION_KEY);
        if (attr instanceof Long) {
            return (Long) attr;
        }
        return null;
    }
}
