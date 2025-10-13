package com.bot.bots.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.PartnerCtx;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.PartnerCtxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 合作方报备数据 PC端展示 Controller
 *
 * 页面路径:
 * - GET /pc/partner
 * 数据接口:
 * - GET /api/partner/list
 *
 * 仅提供查询与刷新能力。
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class PartnerCtxController {

    private final ConfigService configService;
    private final PartnerCtxService partnerCtxService;

    @GetMapping("/pc/partner")
    public String pagePartner(Model model, @RequestParam(value = "userId", required = false, defaultValue = "0") Long userId) {
        Config config = this.configService.queryConfig();
        if (config.hasEdit(userId)) {
            model.addAttribute("userId", userId);
            return "partner/list";
        }
        return "error/error";
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/api/partner/{id}")
    @ResponseBody
    public PartnerCtx getById(@PathVariable Long id) {
        return partnerCtxService.getById(id);
    }

    /**
     * 根据ID更新
     * <pre>
     * 请求示例：
     * PUT /api/partner/1
     * Content-Type: application/json
     * Body: PartnerCtx JSON（包含需要更新的字段）
     * </pre>
     */
    @PutMapping("/api/partner/{id}")
    @ResponseBody
    public boolean update(@PathVariable Long id, @RequestBody PartnerCtx body) {
        body.setId(id);
        return partnerCtxService.updateById(body);
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/api/partner/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Long id) {
        return partnerCtxService.removeById(id);
    }

    /**
     * <pre>
     * 查询参数:
     * - pageNo, pageSize
     * - userId, username, nickname, address
     * 示例:
     * GET /api/partner/list?pageNo=1&pageSize=10&username=test
     * </pre>
     */
    @GetMapping("/api/partner/list")
    @ResponseBody
    public Page<PartnerCtx> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String address
    ) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));

        LambdaQueryWrapper<PartnerCtx> qw = new LambdaQueryWrapper<>();
        if (userId != null) qw.eq(PartnerCtx::getUserId, userId);
        if (username != null && !username.isBlank()) qw.like(PartnerCtx::getUsername, username);
        if (nickname != null && !nickname.isBlank()) qw.like(PartnerCtx::getNickname, nickname);
        if (address != null && !address.isBlank()) qw.like(PartnerCtx::getAddress, address);

        return partnerCtxService.page(Page.of(pageNo, pageSize), qw);
    }
}
