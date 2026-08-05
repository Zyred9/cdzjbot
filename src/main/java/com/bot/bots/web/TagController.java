package com.bot.bots.web;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.Tag;
import com.bot.bots.database.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * 标签管理 Controller
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/pc/tag")
    public String pageTag(Model model, HttpSession session) {
        Long userId = LoginController.getLoginUserId(session);
        model.addAttribute("userId", userId);
        return "tag/list";
    }

    @ResponseBody
    @GetMapping("/api/tag/list")
    public Page<Tag> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String name) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));
        return tagService.page(Page.of(pageNo, pageSize), Wrappers.<Tag>lambdaQuery()
                .like(StrUtil.isNotBlank(name), Tag::getName, name)
                .orderByDesc(Tag::getCreateTime));
    }

    @ResponseBody
    @GetMapping("/api/tag/{id}")
    public Tag getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @ResponseBody
    @PostMapping("/api/tag")
    public boolean create(@RequestBody Tag body) {
        body.setCreateTime(LocalDateTime.now());
        return tagService.save(body);
    }

    @ResponseBody
    @PutMapping("/api/tag/{id}")
    public boolean update(@PathVariable Long id, @RequestBody Tag body) {
        return tagService.updateById(body);
    }

    @ResponseBody
    @DeleteMapping("/api/tag/{id}")
    public boolean delete(@PathVariable Long id) {
        return tagService.removeById(id);
    }

    @ResponseBody
    @GetMapping("/api/tag/all")
    public java.util.List<Tag> all() {
        return tagService.list(Wrappers.<Tag>lambdaQuery().orderByDesc(Tag::getCreateTime));
    }
}
