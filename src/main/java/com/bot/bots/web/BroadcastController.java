package com.bot.bots.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.*;
import com.bot.bots.database.service.*;
import com.bot.bots.sender.AsyncSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class BroadcastController {

    private final BroadcastGroupService broadcastGroupService;
    private final BroadcastCategoryService broadcastCategoryService;
    private final BroadcastLogService broadcastLogService;

    @GetMapping("/pc/broadcast")
    public String page(Model model, HttpSession session) {
        Long userId = LoginController.getLoginUserId(session);
        model.addAttribute("userId", userId);
        return "broadcast/list";
    }

    // ==================== 群组 ====================

    @GetMapping("/api/broadcast/groups")
    @ResponseBody
    public List<BroadcastGroup> listGroups() {
        return this.broadcastGroupService.listAll();
    }

    @DeleteMapping("/api/broadcast/group/{chatId}")
    @ResponseBody
    public Map<String, Object> removeGroup(@PathVariable Long chatId) {
        this.broadcastGroupService.removeByChatId(chatId);
        return MapUtil.of("success", true);
    }

    // ==================== 分类 ====================

    @GetMapping("/api/broadcast/category/list")
    @ResponseBody
    public List<BroadcastCategory> listCategories() {
        return this.broadcastCategoryService.list(
                Wrappers.<BroadcastCategory>lambdaQuery()
                        .orderByDesc(BroadcastCategory::getCreateTime));
    }

    @GetMapping("/api/broadcast/category/{id}/groups")
    @ResponseBody
    public List<Long> categoryGroups(@PathVariable Long id) {
        return this.broadcastCategoryService.getChatIdsByCategoryId(id);
    }

    @PostMapping("/api/broadcast/category")
    @ResponseBody
    public Map<String, Object> saveCategory(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Assert.notNull(name, "分类名称不能为空");
        @SuppressWarnings("unchecked")
        List<Long> chatIds = (List<Long>) body.get("chatIds");
        Object idObj = body.get("id");
        if (idObj != null) {
            Long id = Long.parseLong(idObj.toString());
            this.broadcastCategoryService.update(id, name, chatIds);
        } else {
            this.broadcastCategoryService.create(name, chatIds);
        }
        return MapUtil.of("success", true);
    }

    @DeleteMapping("/api/broadcast/category/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        this.broadcastCategoryService.deleteWithGroups(id);
        return MapUtil.of("success", true);
    }

    // ==================== 群发 ====================

    @PostMapping("/api/broadcast/send")
    @ResponseBody
    public Map<String, Object> send(@RequestBody Map<String, Object> body, HttpSession session) {
        Long categoryId = Long.parseLong(body.get("categoryId").toString());
        String content = (String) body.get("content");
        Assert.notNull(categoryId, "分类不能为空");
        Assert.notNull(content, "内容不能为空");

        List<Long> chatIds = this.broadcastCategoryService.getChatIdsByCategoryId(categoryId);
        if (CollUtil.isEmpty(chatIds)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "该分类下没有群组");
            return result;
        }

        for (Long chatId : chatIds) {
            AsyncSender.async(SendMessage.builder().chatId(chatId.toString()).text(content).build());
        }

        Long senderId = LoginController.getLoginUserId(session);
        BroadcastLog log = new BroadcastLog()
                .setCategoryId(categoryId)
                .setContent(content)
                .setSenderId(senderId)
                .setGroupCount(chatIds.size())
                .setSuccessCount(chatIds.size())
                .setFailCount(0);
        this.broadcastLogService.save(log);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("groupCount", chatIds.size());
        result.put("msg", "已向 " + chatIds.size() + " 个群发送");
        return result;
    }

    // ==================== 发送记录 ====================

    @GetMapping("/api/broadcast/log/list")
    @ResponseBody
    public Page<BroadcastLog> logList(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));
        return this.broadcastLogService.page(Page.of(pageNo, pageSize),
                Wrappers.<BroadcastLog>lambdaQuery()
                        .orderByDesc(BroadcastLog::getCreateTime));
    }
}
