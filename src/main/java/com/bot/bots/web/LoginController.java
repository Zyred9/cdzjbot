package com.bot.bots.web;

import cn.hutool.core.util.StrUtil;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.UserService;
import com.bot.bots.helper.PasswordHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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

    /** 连续失败最大次数，达到后锁定 */
    private static final int MAX_FAIL_TIMES = 5;

    /** 锁定时长（5 分钟） */
    private static final long LOCK_DURATION_MILLIS = 5 * 60 * 1000L;

    /** 各用户连续登录失败次数 */
    private static final ConcurrentHashMap<Long, Integer> LOGIN_FAIL_COUNT = new ConcurrentHashMap<>();

    /** 各用户锁定截止时间戳 */
    private static final ConcurrentHashMap<Long, Long> LOGIN_LOCK_UNTIL = new ConcurrentHashMap<>();

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
                          HttpServletRequest request,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        Long lockUntil = LOGIN_LOCK_UNTIL.get(userId);
        if (Objects.nonNull(lockUntil) && lockUntil > System.currentTimeMillis()) {
            redirectAttributes.addFlashAttribute("error", "尝试次数过多，请稍后再试");
            return "redirect:/login";
        }
        if (Objects.nonNull(lockUntil) && lockUntil <= System.currentTimeMillis()) {
            LOGIN_LOCK_UNTIL.remove(userId);
            LOGIN_FAIL_COUNT.remove(userId);
        }

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

        if (!PasswordHelper.matches(password, user.getPassword())) {
            int failCount = LOGIN_FAIL_COUNT.merge(userId, 1, Integer::sum);
            if (failCount >= MAX_FAIL_TIMES) {
                LOGIN_LOCK_UNTIL.put(userId, System.currentTimeMillis() + LOCK_DURATION_MILLIS);
                LOGIN_FAIL_COUNT.remove(userId);
                redirectAttributes.addFlashAttribute("error", "尝试次数过多，请稍后再试");
            } else {
                redirectAttributes.addFlashAttribute("error", "密码错误");
            }
            return "redirect:/login";
        }

        LOGIN_FAIL_COUNT.remove(userId);
        LOGIN_LOCK_UNTIL.remove(userId);

        // 旧版无盐 MD5 校验通过后自动升级为 BCrypt
        if (!user.getPassword().startsWith("$2")) {
            user.setPassword(PasswordHelper.hash(password));
            this.userService.updateById(user);
        }

        // 会话固定防护：登录成功后更换会话 ID，防止攻击者固定会话
        request.changeSessionId();
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
