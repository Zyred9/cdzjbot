package com.bot.bots.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.*;
import com.bot.bots.database.service.*;
import com.bot.bots.sender.AsyncSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class BroadcastController {

    private static final long MAX_IMAGE_SIZE = 20 * 1024 * 1024L;

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
    public Map<String, Object> send(@RequestParam("categoryId") String categoryIdStr,
                                    @RequestParam(value = "content", required = false) String content,
                                    @RequestParam(value = "image", required = false) MultipartFile image,
                                    HttpSession session) {
        if (StrUtil.isBlank(categoryIdStr)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "分类不能为空");
            return result;
        }
        Long categoryId;
        try {
            categoryId = Long.parseLong(categoryIdStr);
        } catch (NumberFormatException e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "分类参数错误");
            return result;
        }

        boolean hasImage = Objects.nonNull(image) && !image.isEmpty();
        boolean hasText = StrUtil.isNotBlank(content);
        if (!hasImage && !hasText) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "图片和文字至少填写一项");
            return result;
        }

        byte[] imageBytes = null;
        if (hasImage) {
            if (image.getSize() > MAX_IMAGE_SIZE) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("msg", "图片大小不能超过 20MB");
                return result;
            }
            String contentType = image.getContentType();
            if (StrUtil.isBlank(contentType) || !contentType.startsWith("image/")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("msg", "仅支持图片文件");
                return result;
            }
            try {
                imageBytes = image.getBytes();
            } catch (Exception e) {
                log.error("[群发] 读取上传图片失败，用户id：{}，分类id：{}", LoginController.getLoginUserId(session), categoryId, e);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("msg", "图片读取失败");
                return result;
            }
        }

        List<Long> chatIds = this.broadcastCategoryService.getChatIdsByCategoryId(categoryId);
        if (CollUtil.isEmpty(chatIds)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("msg", "该分类下没有群组");
            return result;
        }

        String filename = hasImage ? StrUtil.blankToDefault(image.getOriginalFilename(), "image.jpg") : null;
        for (Long chatId : chatIds) {
            if (hasImage) {
                AsyncSender.async(SendPhoto.builder()
                        .chatId(chatId.toString())
                        .photo(new InputFile(new ByteArrayInputStream(imageBytes), filename))
                        .caption(hasText ? content : null)
                        .build());
            } else {
                AsyncSender.async(SendMessage.builder().chatId(chatId.toString()).text(content).build());
            }
        }

        Long senderId = LoginController.getLoginUserId(session);
        BroadcastLog broadcastLog = new BroadcastLog()
                .setCategoryId(categoryId)
                .setContent(hasText ? content : null)
                .setHasImage(hasImage ? 1 : 0)
                .setSenderId(senderId)
                .setGroupCount(chatIds.size())
                .setSuccessCount(chatIds.size())
                .setFailCount(0);
        this.broadcastLogService.save(broadcastLog);

        log.info("[群发] 发送完成，用户id：{}，分类id：{}，含图片：{}，目标群数：{}", senderId, categoryId, hasImage, chatIds.size());

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
