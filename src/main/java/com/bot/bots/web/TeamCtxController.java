package com.bot.bots.web;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.database.entity.Config;
import com.bot.bots.database.entity.TeamCtx;
import com.bot.bots.database.service.ConfigService;
import com.bot.bots.database.service.TeamCtxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * 车队报备数据 PC端展示 Controller
 *
 * 页面路径:
 * - GET /pc/team
 * 数据接口:
 * - GET /api/team/list
 *
 * 仅提供查询与刷新能力。
 *
 * @author zyred
 * @since 1.0
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class TeamCtxController {

    private final ConfigService configService;
    private final TeamCtxService teamCtxService;

    @GetMapping("/pc/team")
    public String pageTeam(Model model, @RequestParam(value = "userId", required = false, defaultValue = "0") Long userId) {
        Config config = this.configService.queryConfig();
        if (config.hasEdit(userId)) {
            model.addAttribute("userId", userId);
            return "team/list";
        }
        return "error/error";
    }



    /**
     * 根据ID查询
     */
    @GetMapping("/api/team/{id}")
    @ResponseBody
    public TeamCtx getById(@PathVariable Long id) {
        return teamCtxService.getById(id);
    }

    /**
     * 根据ID更新
     * <pre>
     * 请求示例：
     * PUT /api/team/1
     * Content-Type: application/json
     * Body: TeamCtx JSON（包含需要更新的字段）
     * </pre>
     */
    @PutMapping("/api/team/{id}")
    @ResponseBody
    public boolean update(@PathVariable Long id, @RequestBody TeamCtx body) {
        body.setId(id);
        return teamCtxService.updateById(body);
    }

    /**
     * 根据ID删除
     */
    @DeleteMapping("/api/team/{id}")
    @ResponseBody
    public boolean delete(@PathVariable Long id) {
        return teamCtxService.removeById(id);
    }


    /**
     * <pre>
     * 查询参数:
     * - pageNo, pageSize
     * - userId, username, nickname, address
     * 示例:
     * GET /api/team/list?pageNo=1&pageSize=10&username=test
     * </pre>
     */
    @GetMapping("/api/team/list")
    @ResponseBody
    public Page<TeamCtx> list(
            @RequestParam(defaultValue = "1") long pageNo,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String address
    ) {
        pageNo = Math.max(1, pageNo);
        pageSize = Math.max(1, Math.min(100, pageSize));

        LambdaQueryWrapper<TeamCtx> qw = new LambdaQueryWrapper<>();
        if (userId != null) qw.eq(TeamCtx::getUserId, userId);
        if (username != null && !username.isBlank()) qw.like(TeamCtx::getUsername, username);
        if (nickname != null && !nickname.isBlank()) qw.like(TeamCtx::getNickname, nickname);
        if (address != null && !address.isBlank()) qw.like(TeamCtx::getAddress, address);

        return teamCtxService.page(Page.of(pageNo, pageSize), qw);
    }
}