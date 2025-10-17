package com.bot.bots.web;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.AcceptanceCtx;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.service.AcceptanceCtxService;
import com.bot.bots.database.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 承兑报备数据 PC端展示 Controller
 *
 * 页面路径:
 * - GET /pc/acceptance
 * 数据接口:
 * - GET /api/acceptance/list
 *
 * 仅提供查询与刷新能力。
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AcceptanceCtxController {

    private final ConfigService configService;
    private final AcceptanceCtxService acceptanceCtxService;

    @GetMapping("/pc/acceptance")
    public String pageAcceptance(Model model, @RequestParam(value = "userId", required = false, defaultValue = "0") Long userId) {
        Config config = this.configService.queryConfig();
        if (config.hasEdit(userId)) {
            model.addAttribute("userId", userId);
            return "acceptance/list";
        }
        return "error/error";
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/api/acceptance/{id}")
    @ResponseBody
    public AcceptanceCtx getById(@PathVariable Long id) {
        return acceptanceCtxService.getById(id);
    }

    /**
     * 根据ID更新
     * <pre>
     * 请求示例：
     * PUT /api/acceptance/1
     * Content-Type: application/json
     * Body: AcceptanceCtx JSON（包含需要更新的字段）
     * </pre>
     */
    @PutMapping("/api/acceptance/{id}")
    @ResponseBody
    public boolean update(@PathVariable Long id, @RequestBody AcceptanceCtx body) {
        return acceptanceCtxService.updateById(body);
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/api/acceptance/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Long id, @RequestParam("userId") Long userId) {
        return acceptanceCtxService.removeById(id);
    }


    /**
     * <pre>
     * 查询参数:
     * - pageNo, pageSize
     * - userId, username, nickname, address
     * 示例:
     * GET /api/acceptance/list?pageNo=1&pageSize=10&username=test
     * </pre>
     */
    @GetMapping("/api/acceptance/list")
    @ResponseBody
    public Page<AcceptanceCtx> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String address) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));
        return acceptanceCtxService.page(Page.of(pageNo, pageSize), Wrappers.<AcceptanceCtx>lambdaQuery()
                .eq(Objects.nonNull(userId), AcceptanceCtx::getUserId, userId)
                .eq(StrUtil.isNotBlank(username), AcceptanceCtx::getUsername, username)
                .like(StrUtil.isNotBlank(nickname), AcceptanceCtx::getNickname, nickname)
                .like(StrUtil.isNotBlank(address), AcceptanceCtx::getAddress, address));
    }
}