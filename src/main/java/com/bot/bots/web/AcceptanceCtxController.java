package com.bot.bots.web;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.AcceptanceCtx;
import com.bot.bots.database.entity.Tag;
import com.bot.bots.database.enums.CategoryEnum;
import com.bot.bots.database.enums.ForbidTypeEnum;
import com.bot.bots.database.enums.MaterialEnum;
import com.bot.bots.database.service.AcceptanceCtxService;
import com.bot.bots.database.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 承兑报备数据 PC端展示 Controller
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AcceptanceCtxController {

    private final AcceptanceCtxService acceptanceCtxService;
    private final TagService tagService;

    private static final LinkedHashMap<String, String> COLUMN_DEFS = new LinkedHashMap<>();

    static {
        COLUMN_DEFS.put("id", "主键ID");
        COLUMN_DEFS.put("userId", "用户ID");
        COLUMN_DEFS.put("username", "用户名");
        COLUMN_DEFS.put("nickname", "用户昵称");
        COLUMN_DEFS.put("address", "地址");
        COLUMN_DEFS.put("customerType", "客户类型");
        COLUMN_DEFS.put("tagName", "标签");
        COLUMN_DEFS.put("categories", "分类");
        COLUMN_DEFS.put("intervalInput", "区间值");
        COLUMN_DEFS.put("rate", "汇率");
        COLUMN_DEFS.put("materials", "料性");
        COLUMN_DEFS.put("forbids", "禁止");
        COLUMN_DEFS.put("airborne", "是否空降");
        COLUMN_DEFS.put("station", "是否驻点");
        COLUMN_DEFS.put("move", "是否移动");
        COLUMN_DEFS.put("follow", "是否跟车");
        COLUMN_DEFS.put("location", "经纬度");
    }

    @GetMapping("/pc/acceptance")
    public String pageAcceptance(Model model, HttpSession session) {
        Long userId = LoginController.getLoginUserId(session);
        model.addAttribute("userId", userId);
        return "acceptance/list";
    }

    @ResponseBody
    @GetMapping("/api/acceptance/{id}")
    public AcceptanceCtx getById(@PathVariable Long id) {
        return acceptanceCtxService.getById(id);
    }

    @ResponseBody
    @PutMapping("/api/acceptance/{id}")
    public boolean update(@PathVariable Long id, @RequestBody AcceptanceCtx body) {
        return acceptanceCtxService.updateById(body);
    }

    @ResponseBody
    @DeleteMapping("/api/acceptance/{id}")
    public boolean delete(@PathVariable Long id) {
        return acceptanceCtxService.removeById(id);
    }

    @ResponseBody
    @GetMapping("/api/acceptance/list")
    public Page<AcceptanceCtx> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Integer customerType,
            @RequestParam(required = false) String tagName) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));

        List<Long> tagIds = null;
        if (StrUtil.isNotBlank(tagName)) {
            List<Tag> tags = tagService.list(Wrappers.<Tag>lambdaQuery()
                    .like(Tag::getName, tagName));
            if (CollUtil.isNotEmpty(tags)) {
                tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
            } else {
                return new Page<>(pageNo, pageSize);
            }
        }

        return acceptanceCtxService.page(Page.of(pageNo, pageSize), Wrappers.<AcceptanceCtx>lambdaQuery()
                .eq(Objects.nonNull(userId), AcceptanceCtx::getUserId, userId)
                .eq(StrUtil.isNotBlank(username), AcceptanceCtx::getUsername, username)
                .like(StrUtil.isNotBlank(nickname), AcceptanceCtx::getNickname, nickname)
                .like(StrUtil.isNotBlank(address), AcceptanceCtx::getAddress, address)
                .eq(Objects.nonNull(customerType), AcceptanceCtx::getCustomerType, customerType)
                .in(CollUtil.isNotEmpty(tagIds), AcceptanceCtx::getTagId, CollUtil.isNotEmpty(tagIds) ? tagIds : null)
                .orderByDesc(AcceptanceCtx::getId));
    }

    @PostMapping("/api/acceptance/export")
    public void export(@RequestParam String columns, @RequestBody List<Long> ids, HttpServletResponse response) throws IOException {
        List<String> selectedCols = Arrays.stream(columns.split(","))
                .map(String::trim)
                .filter(COLUMN_DEFS::containsKey)
                .collect(Collectors.toList());
        if (selectedCols.isEmpty()) {
            response.setStatus(400);
            response.getWriter().write("no valid columns");
            return;
        }
        if (CollUtil.isEmpty(ids)) {
            response.setStatus(400);
            response.getWriter().write("no selected ids");
            return;
        }

        List<AcceptanceCtx> selectedData = acceptanceCtxService.listByIds(ids);
        Map<Long, AcceptanceCtx> dataMap = selectedData.stream()
                .collect(Collectors.toMap(AcceptanceCtx::getId, Function.identity()));
        List<AcceptanceCtx> allData = ids.stream()
                .map(dataMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Tag> tagMap = Collections.emptyMap();
        boolean needTag = selectedCols.contains("tagName");
        if (needTag) {
            tagMap = tagService.list().stream()
                    .collect(Collectors.toMap(Tag::getId, Function.identity(), (a, b) -> a));
        }

        List<List<String>> headers = new ArrayList<>();
        for (String col : selectedCols) {
            headers.add(Collections.singletonList(COLUMN_DEFS.get(col)));
        }

        List<List<Object>> data = new ArrayList<>();
        for (AcceptanceCtx ctx : allData) {
            List<Object> row = new ArrayList<>();
            for (String col : selectedCols) {
                row.add(extractCellValue(ctx, col, tagMap));
            }
            data.add(row);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("承兑报备导出", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream())
                .head(headers)
                .sheet("承兑报备")
                .doWrite(data);
    }

    private Object extractCellValue(AcceptanceCtx ctx, String col, Map<Long, Tag> tagMap) {
        switch (col) {
            case "id": return ctx.getId();
            case "userId": return ctx.getUserId();
            case "username": return ctx.getUsername();
            case "nickname": return ctx.getNickname();
            case "address": return ctx.getAddress();
            case "customerType":
                return Objects.equals(1, ctx.getCustomerType()) ? "合作"
                        : Objects.equals(2, ctx.getCustomerType()) ? "未合作" : "";
            case "tagName": {
                Tag tag = tagMap.get(ctx.getTagId());
                return tag != null ? tag.getName() : "";
            }
            case "categories":
                return CollUtil.isEmpty(ctx.getCategories()) ? ""
                        : ctx.getCategories().stream().map(CategoryEnum::getDesc).collect(Collectors.joining("，"));
            case "intervalInput": return ctx.getIntervalInput();
            case "rate": return ctx.getRate();
            case "materials":
                return CollUtil.isEmpty(ctx.getMaterials()) ? ""
                        : ctx.getMaterials().stream().map(MaterialEnum::getDesc).collect(Collectors.joining("，"));
            case "forbids":
                return CollUtil.isEmpty(ctx.getForbids()) ? ""
                        : ctx.getForbids().stream().map(ForbidTypeEnum::getDesc).collect(Collectors.joining("，"));
            case "airborne": return Boolean.TRUE.equals(ctx.getAirborne()) ? "是" : "否";
            case "station": return Boolean.TRUE.equals(ctx.getStation()) ? "是" : "否";
            case "move": return Boolean.TRUE.equals(ctx.getMove()) ? "是" : "否";
            case "follow": return Boolean.TRUE.equals(ctx.getFollow()) ? "是" : "否";
            case "location": return ctx.getLocation();
            default: return "";
        }
    }
}
