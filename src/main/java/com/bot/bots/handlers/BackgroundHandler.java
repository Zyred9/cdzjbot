package com.bot.bots.handlers;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.excel.EasyExcel;
import com.bot.bots.config.BotProperties;
import com.bot.bots.database.entity.*;
import com.bot.bots.database.enums.CategoryEnum;
import com.bot.bots.database.enums.CustomerTypeEnum;
import com.bot.bots.database.service.*;
import com.bot.bots.helper.DecimalHelper;
import com.bot.bots.helper.KeyboardHelper;
import com.bot.bots.helper.StrHelper;
import com.bot.bots.sender.AsyncSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackgroundHandler extends AbstractHandler {

    private final UserService userService;
    private final BotProperties properties;
    private final ConfigService configService;
    private final BroadcastCategoryService broadcastCategoryService;
    private final TelegramClient telegramClient;
    private final TagService tagService;
    private final AcceptanceCtxService acceptanceCtxService;

    private static final Pattern RATE_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)");


    @Override
    public boolean support(Update update) {
        return update.hasMessage()
                && (update.getMessage().hasText() || update.getMessage().hasPhoto()
                    || update.getMessage().hasVideo() || update.getMessage().hasDocument())
                && (update.getMessage().getChat().isGroupChat() || update.getMessage().getChat().isSuperGroupChat())
                && this.properties.fromBackground(update.getMessage().getChatId());
    }

    @Override
    protected BotApiMethod<?> execute(Update update) {
        Message message = update.getMessage();

        if (message.hasDocument()) {
            if (this.isExcelFile(message.getDocument())) {
                return this.handleExcelImport(message);
            }
        }

        if (message.hasText()) {
            String text = message.getText();
            List<String> commands = StrUtil.split(text, "#");

        /*
            客服回复#
            &客服回复客服回复客服回复客服回复客服回复客服回复客服回复客服回复客服回复客服回复
            &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr

            盘总回复#
            &盘总回复盘总回复盘总回复盘总回复盘总回复盘总回复盘总回复盘总回复盘总回复
            &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr
        */
            Config config = this.configService.queryConfig();

            if (StrUtil.equalsAny(commands.get(0), "客服回复", "盘总回复")) {
                boolean custom = StrUtil.equalsAny("客服回复", commands.get(0));

                List<String> split = StrUtil.split(text, "&");
                String content = StrUtil.trim(split.get(1));
                String json = KeyboardHelper.parseKeyboard(split.get(2));

                if (custom) {
                    config.setCustomText(content);
                    config.setCustomKeyboard(json);
                } else  {
                    config.setSelfText(content);
                    config.setSelfKeyboard(json);
                }
                this.configService.updateById(config);
                return reply(message);
            }

        /*
            查U&按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr
        */
            if (StrUtil.startWith(commands.get(0), "查U&")) {
                List<String> split = StrUtil.split(text, "&");
                String json = KeyboardHelper.parseKeyboard(split.get(1).trim());
                config.setQueryKeyboard(json);
                this.configService.updateById(config);
                return reply(message);
            }

            // 查看可编辑
            if (StrUtil.equalsAny("查看可编辑", commands.get(0))) {
                List<Long> editable = config.getEditable();
                StringBuilder sb = new StringBuilder("页面可编辑用户：\n");
                for (Long l : editable) {
                    sb.append(l).append("\n");
                }
                return reply(message, sb.toString());
            }

            // 设置可编辑#用户id1,用户id2....
            // 删除可编辑#用户id1,用户id2....
            if (StrUtil.equalsAny("设置可编辑", "删除可编辑", commands.get(0))) {

                boolean add = StrUtil.equalsAny("设置可编辑", commands.get(0));

                String ids = commands.get(1);
                String[] userIds = ids.split("[,，]");

                Set<Long> userIdSet = new HashSet<>();
                for (String userId : userIds) {
                    userIdSet.add(Long.parseLong(userId));
                }

                List<Long> editable = config.getEditable();
                if (add) {
                    editable.addAll(userIdSet);
                } else {
                    editable.removeAll(userIdSet);
                }
                this.configService.updateById(config);
                return reply(message);
            }

            // 设置密码#用户id#新密码
            if (StrUtil.equals(commands.get(0), "设置密码")) {
                long targetUserId = Long.parseLong(commands.get(1));
                String rawPassword = commands.get(2);
                User targetUser = this.userService.getById(targetUserId);
                if (Objects.isNull(targetUser)) {
                    return reply(message, "用户 " + targetUserId + " 不存在");
                }
                targetUser.setPassword(DigestUtil.md5Hex(rawPassword));
                this.userService.updateById(targetUser);
                return reply(message);
            }

            // 余额#用户id#+100
            // 余额#用户id#-100
            if (StrUtil.equals(commands.get(0), "余额")) {
                long userId = Long.parseLong(commands.get(1));
                BigDecimal amount = new BigDecimal(commands.get(2));

                User user = this.userService.getById(userId);
                user.setBalance(user.getBalance().add(amount));
                if (DecimalHelper.lessThan(user.getBalance(), BigDecimal.ZERO)) {
                    user.setBalance(BigDecimal.ZERO);
                }
                this.userService.updateById(user);
                return reply(message);
            }

            // 页面地址
            if (StrUtil.equals(commands.get(0), "页面地址")) {
                return ok(message, this.properties.getWebUrl());
            }

            // 广播#内容
            if (StrUtil.equals(commands.get(0), "广播")) {
                return this.processorBroadcast(message, commands);
            }

            // 群发#分类名#内容
            if (StrUtil.equals(commands.get(0), "群发") && commands.size() >= 3) {
                return this.processorCategoryBroadcast(message, commands);
            }
        }

        // 广播#内容 / 群发#分类名#内容
        if (message.hasPhoto() || message.hasVideo()) {
            String caption = message.getCaption();
            if (StrUtil.isBlank(caption)) {
                return null;
            }
            List<String> commands = StrUtil.split(caption, "#");
            if (StrUtil.equals(commands.get(0), "广播")) {
                return this.processorBroadcast(message, commands);
            }
            if (StrUtil.equals(commands.get(0), "群发") && commands.size() >= 3) {
                return this.processorCategoryBroadcast(message, commands);
            }
        }

        return null;
    }

    private BotApiMethod<?> processorBroadcast(Message message, List<String> commands) {

        if (message.hasText()) {
            String content = commands.get(1);
            List<User> users = this.userService.list();
            for (User user : users) {
                AsyncSender.async(markdown(user.getUserId(), content));
            }
        }

        if (message.hasVideo()) {
            String content = commands.get(1);
            List<User> users = this.userService.list();
            String fileId = message.getVideo().getFileId();
            for (User user : users) {
                AsyncSender.async(video(user.getUserId(), fileId, content));
            }
        }

        if (message.hasPhoto()) {
            String content = commands.get(1);
            List<User> users = this.userService.list();
            String fileId = message.getPhoto().get(0).getFileId();
            for (User user : users) {
                AsyncSender.async(photo(user.getUserId(), fileId, content));
            }
        }

        return null;
    }

    private BotApiMethod<?> processorCategoryBroadcast(Message message, List<String> commands) {
        String categoryName = commands.get(1);
        String content = commands.size() > 2 ? String.join("#", commands.subList(2, commands.size())) : null;

        List<BroadcastCategory> categories = this.broadcastCategoryService.lambdaQuery()
                .eq(BroadcastCategory::getName, categoryName)
                .list();
        if (CollUtil.isEmpty(categories)) {
            return reply(message, "未找到分类：" + categoryName);
        }

        BroadcastCategory category = categories.get(0);
        List<Long> chatIds = this.broadcastCategoryService.getChatIdsByCategoryId(category.getId());
        if (CollUtil.isEmpty(chatIds)) {
            return reply(message, "分类「" + categoryName + "」下没有群组");
        }

        if (message.hasPhoto()) {
            String fileId = message.getPhoto().get(0).getFileId();
            for (Long chatId : chatIds) {
                AsyncSender.async(photo(chatId, fileId, content));
            }
        } else if (message.hasVideo()) {
            String fileId = message.getVideo().getFileId();
            for (Long chatId : chatIds) {
                AsyncSender.async(video(chatId, fileId, content));
            }
        } else {
            for (Long chatId : chatIds) {
                AsyncSender.async(markdown(chatId, content));
            }
        }

        return reply(message, "已向分类「" + categoryName + "」下的 " + chatIds.size() + " 个群发送");
    }

    private boolean isExcelFile(Document document) {
        String fileName = document.getFileName();
        if (StrUtil.isBlank(fileName)) {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls");
    }

    private BotApiMethod<?> handleExcelImport(Message message) {
        try {
            GetFile getFile = GetFile.builder().fileId(message.getDocument().getFileId()).build();
            org.telegram.telegrambots.meta.api.objects.File tgFile = this.telegramClient.execute(getFile);
            String fileUrl = "https://api.telegram.org/file/bot" + this.properties.getToken() + "/" + tgFile.getFilePath();
            byte[] fileBytes = this.downloadFile(fileUrl);

            List<Map<Integer, String>> allRows = EasyExcel.read(new ByteArrayInputStream(fileBytes))
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();

            if (CollUtil.isEmpty(allRows)) {
                return reply(message, "Excel 文件无数据");
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            Map<Integer, String> headRow = allRows.get(0);
            headRow.forEach((index, name) -> {
                if (StrUtil.isNotBlank(name)) {
                    headerIndex.put(name.trim(), index);
                }
            });

            List<Map<Integer, String>> rows = allRows.subList(1, allRows.size());
            if (CollUtil.isEmpty(rows)) {
                return reply(message, "Excel 文件无数据");
            }

            Map<String, Long> tagCache = new HashMap<>();
            List<AcceptanceCtx> records = new ArrayList<>();

            for (Map<Integer, String> row : rows) {
                String customerTag = this.getCellValue(row, 4);
                List<Long> tagIds = this.getOrCreateTags(tagCache, customerTag);

                AcceptanceCtx ctx = new AcceptanceCtx()
                        .setUserId(this.generateId())
                        .setUsername(this.getCellValue(row, 1))
                        .setNickname(this.getCellValue(row, 2))
                        .setCustomerType(this.parseCustomerType(this.getCellValue(row, 3)))
                        .setTagIds(tagIds)
                        .setCategories(this.parseCategories(this.getCellValue(row, headerIndex.get("分类"))))
                        .setAddress(this.mergeAddress(
                                this.getCellValue(row, 5),
                                this.getCellValue(row, 6),
                                this.getCellValue(row, 7),
                                this.getCellValue(row, 8)))
                        .setIntervalInput(this.getCellValue(row, 9))
                        .setRate(this.parseRate(this.getCellValue(row, 10)));
                records.add(ctx);
            }

            this.acceptanceCtxService.saveBatch(records);

            log.info("[Excel导入] 导入完成，共 {} 条记录", records.size());
            return reply(message, "导入完成，共 " + records.size() + " 条记录");

        } catch (TelegramApiException e) {
            log.error("[Excel导入] 文件下载失败", e);
            return reply(message, "文件下载失败：" + e.getMessage());
        } catch (Exception e) {
            log.error("[Excel导入] 导入异常", e);
            return reply(message, "导入失败：" + e.getMessage());
        }
    }

    private String getCellValue(Map<Integer, String> row, Integer index) {
        if (Objects.isNull(index)) {
            return null;
        }
        String value = row.get(index);
        return StrUtil.isBlank(value) ? null : value.trim();
    }

    private Long generateId() {
        return StrHelper.randomUserId();
    }

    private List<Long> getOrCreateTags(Map<String, Long> tagCache, String customerTag) {
        if (StrUtil.isBlank(customerTag)) {
            return null;
        }
        return Arrays.stream(customerTag.split("[,，、;；\\s]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(tagName -> this.getOrCreateTag(tagCache, tagName))
                .collect(Collectors.toList());
    }

    private Long getOrCreateTag(Map<String, Long> tagCache, String tagName) {
        if (StrUtil.isBlank(tagName)) {
            return null;
        }
        if (tagCache.containsKey(tagName)) {
            return tagCache.get(tagName);
        }
        Tag existing = this.tagService.lambdaQuery().eq(Tag::getName, tagName).one();
        if (Objects.nonNull(existing)) {
            tagCache.put(tagName, existing.getId());
            return existing.getId();
        }
        Tag newTag = new Tag()
                .setName(tagName)
                .setColor("black")
                .setCreateTime(LocalDateTime.now());
        this.tagService.save(newTag);
        tagCache.put(tagName, newTag.getId());
        return newTag.getId();
    }

    private List<CategoryEnum> parseCategories(String categoryStr) {
        if (StrUtil.isBlank(categoryStr)) {
            return null;
        }
        return Arrays.stream(categoryStr.split("[,，、;；\\s]+"))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .map(CategoryEnum::fromJson)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Integer parseCustomerType(String type) {
        if (StrUtil.isBlank(type)) {
            return null;
        }
        if (type.contains("已合作") || type.contains("合作")) {
            return CustomerTypeEnum.COOPERATION.getCode();
        }
        if (type.contains("未合作")) {
            return CustomerTypeEnum.NON_COOPERATION.getCode();
        }
        if (type.contains("休息中")) {
            return CustomerTypeEnum.RESTING.getCode();
        }
        return null;
    }

    private BigDecimal parseRate(String rateStr) {
        if (StrUtil.isBlank(rateStr)) {
            return null;
        }
        Matcher m = RATE_PATTERN.matcher(rateStr.trim());
        if (m.find()) {
            return new BigDecimal(m.group(1));
        }
        return null;
    }

    private String mergeAddress(String province, String city, String district, String detail) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(province)) {
            sb.append(province);
        }
        if (StrUtil.isNotBlank(city)) {
            sb.append(city);
        }
        if (StrUtil.isNotBlank(district)) {
            sb.append(district);
        }
        if (StrUtil.isNotBlank(detail)) {
            sb.append(detail);
        }
        return !sb.isEmpty() ? sb.toString() : null;
    }

    private byte[] downloadFile(String fileUrl) throws IOException {
        URLConnection conn = new URL(fileUrl).openConnection();
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
    }
}
