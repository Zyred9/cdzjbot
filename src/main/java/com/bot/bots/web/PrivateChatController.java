package com.bot.bots.web;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.User;
import com.bot.bots.database.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 私聊用户列表 PC 端 Controller
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class PrivateChatController {

    private final UserService userService;

    @GetMapping("/pc/privatechat")
    public String page(Model model, HttpSession session) {
        Long userId = LoginController.getLoginUserId(session);
        model.addAttribute("userId", userId);
        return "privatechat/list";
    }

    @GetMapping("/api/privatechat/list")
    @ResponseBody
    public Page<User> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                .eq(userId != null, User::getUserId, userId)
                .eq(StrUtil.isNotBlank(username), User::getUsername, username)
                .like(StrUtil.isNotBlank(nickname), User::getNickname, nickname)
                .orderByDesc(User::getUserId);
        return userService.page(Page.of(pageNo, pageSize), wrapper);
    }
}
