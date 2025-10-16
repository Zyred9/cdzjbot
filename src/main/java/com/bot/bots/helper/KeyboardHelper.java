package com.bot.bots.helper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bot.bots.beans.view.ButtonTransfer;
import com.bot.bots.beans.view.KeyboardTransfer;
import com.bot.bots.database.entity.Address;
import com.bot.bots.database.enums.CategoryEnum;
import com.bot.bots.database.enums.ForbidTypeEnum;
import com.bot.bots.database.enums.MaterialEnum;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.util.*;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class KeyboardHelper {

    public static InlineKeyboardMarkup buildSearchKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonUrl("\uD83D\uDC81\uD83C\uDFFB\u200d♀️联系承兑", "https://t.me/CDZJkefu"))
                )).build();
    }

    public static InlineKeyboardMarkup buildCommitSupplementQueryKeyboard(int query) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("⤵️立即查询", "privacy#supplement#to_query#"+query), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildSupplementCommitedKeyboard(String name, int query) {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(buttonText(name, "privacy#supplement#commited#"+query)))
                .build();
    }

    public static InlineKeyboardMarkup buildCommitSupplementReportKeyboard(int query) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("⤵️立即提交报备", "privacy#supplement#commit#"+query), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildSupplementKeyboard(int query) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText("✍️补充要求", "privacy#supplement#input#"+query)
                        ),
                        row(buttonText("\uD83D\uDD1C跳过补充", "privacy#supplement#confirm#"+query), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildPacketKeyboard(Boolean packet, int query) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(packet == null ? "是" :  packet ? "✅是" : "是", "privacy#packet#true#"+query),
                                buttonText(packet == null ? "否" : !packet ? "✅否" : "否", "privacy#packet#false#"+query)
                        ),
                        row(buttonText("\uD83D\uDD1C 下一步", "privacy#packet#confirm#"+query), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildModelKeyboard(List<String> models, int query) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(CollUtil.isNotEmpty(models) && models.contains("专群") ? "✅" + "专群" : "专群", "privacy#model#1#"+query),
                                buttonText(CollUtil.isNotEmpty(models) && models.contains("面交") ? "✅" + "面交" : "面交", "privacy#model#2#"+query)
                        ),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#model#confirm#"+query), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildScopeKeyboard(int query, Integer choose) {
        String chooseValue = String.valueOf(choose);

        String[] arr = {"100", "200", "300", "custom", "confirm", "delete"};
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        rows.add(row);

        int idx = 0, max = 4;
        for (String val : arr) {
            if (idx == max) {
                row = new InlineKeyboardRow();
                rows.add(row);
                idx = 0;
            }
            if (Objects.equals(val, "delete")) {
                row.add(cancelButton());
            } else {

                String name = val + "公里";
                if (StrUtil.equals(val, "confirm")) {
                    name = "⤵️查询";
                }
                if (StrUtil.equals(val, "custom")) {
                    name = "✍️自定义公里数";
                }
                if (Objects.equals(chooseValue, val)) {
                    name = "✅" + val + "公里";
                }

                row.add(
                        buttonText(name, StrUtil.join("#", "privacy#scope", val, query))
                );
            }
            idx ++;
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(rows).build();
    }

    public static InlineKeyboardMarkup buildExchangeReportKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(row(buttonText("✅提交报备", "privacy#commit#exchange_report"), cancelButton()))
                .build();
    }

    public static InlineKeyboardMarkup buildFollowKeyboard(Boolean follow) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(follow == null ? "是" :  follow ? "✅是" : "是", "privacy#follow#true"),
                                buttonText(follow == null ? "否" : !follow ? "✅否" : "否", "privacy#follow#false")
                        ),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#follow#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildMoveKeyboard(Boolean move) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(move == null ? "是" :  move ? "✅是" : "是", "privacy#move#true"),
                                buttonText(move == null ? "否" : !move ? "✅否" : "否", "privacy#move#false")
                        ),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#move#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildStationKeyboard(Boolean station) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(station == null ? "是" :  station ? "✅是" : "是", "privacy#station#true"),
                                buttonText(station == null ? "否" : !station ? "✅否" : "否", "privacy#station#false")
                        ),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#station#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildAirborneKeyboard(Boolean airborne) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(
                                buttonText(airborne == null ? "是" : ( airborne ? "✅是" : "是"), "privacy#airborne#true"),
                                buttonText(airborne == null ? "否" : (!airborne ? "✅否" : "否"), "privacy#airborne#false")
                        ),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#airborne#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildForbidKeyboard(List<ForbidTypeEnum> choose) {
        choose = choose == null ? Collections.emptyList() : choose;
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        rows.add(row);


        int n = 0, max = 3;
        for (ForbidTypeEnum value : ForbidTypeEnum.values()) {
            String name = choose.contains(value) ? "✅" + value.getDesc() : value.getDesc();

            if (n == max) {
                row = new InlineKeyboardRow();
                rows.add(row);
                n = 0;
            }
            row.add(buttonText(name, value.getCallback()));
            n ++;
        }

        rows.add(row(buttonText("\uD83D\uDD1C下一步", "privacy#forbid#confirm"), cancelButton()));
        return InlineKeyboardMarkup.builder()
                .keyboard(rows).build();
    }

    public static InlineKeyboardMarkup buildMaterialRemarksKeyboard(MaterialEnum choose) {

        InlineKeyboardRow row = new InlineKeyboardRow();
        for (MaterialEnum value : MaterialEnum.values()) {
            String name = choose == value ? "✅" + value.getDesc() : value.getDesc();
            row.add(buttonText(name, value.getCallback()));
        }

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row,
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#material#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildRateIncrementKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("汇率+0.05", "privacy#input#rate#increment"), buttonText("汇率-0.05", "privacy#input#rate#subtract")),
                        row(buttonText("\uD83D\uDD1C下一步", "privacy#input#rate#confirm"), cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildInputIntervalKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("请输入你的接单额区间（例 1万至100万）", "privacy#input#interval#input")),
                        row(cancelButton())
                )).build();
    }

    public static InlineKeyboardMarkup buildCategoryKeyboard(List<CategoryEnum> enums, int query) {
        enums = enums == null ? Collections.emptyList() : enums;

        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        rows.add(row);
        CategoryEnum[] values = CategoryEnum.values();


        int idx = 0, max = 3;
        for (CategoryEnum category : values) {
            if (idx == max) {
                row = new InlineKeyboardRow();
                rows.add(row);
                idx = 0;
            }

            boolean contains = enums.contains(category);
            String name = contains ? "✅" + category.getDesc() : category.getDesc();

            row.add(buttonText(name, category.getCallback() + query));
            idx ++;
        }

        InlineKeyboardRow last = new InlineKeyboardRow();
        last.add(buttonText("\uD83D\uDD1C下一步", "privacy#input#interval#show#" + query));  // 输入金额区间
        last.add(cancelButton());
        rows.add(last);
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }



    public static InlineKeyboardMarkup buildCountyKeyboard(Page<Address> countyList, String cityCode, String provinceCode, int query) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        rows.add(row);

        int idx = 0, max = 3;
        List<Address> records = countyList.getRecords();
        for (Address addr : records) {
            if (idx == max) {
                row = new InlineKeyboardRow();
                rows.add(row);
                idx = 0;
            }
            row.add(buttonText(addr.getCountyName(), StrUtil.join("#", "privacy#category", addr.getCountyCode(), countyList.getCurrent(), query)));
            idx ++;
        }

        if (countyList.hasNext() || countyList.hasPrevious()) {
            InlineKeyboardRow pageRow = new InlineKeyboardRow();
            if (countyList.hasPrevious()) {
                pageRow.add(buttonText("上一页", StrUtil.join("#", "privacy#county", cityCode, (countyList.getCurrent() - 1), query)));
            }
            if (countyList.hasNext()) {
                pageRow.add(buttonText("下一页", StrUtil.join("#", "privacy#county", cityCode, (countyList.getCurrent() + 1), query)));
            }
            rows.add(pageRow);
        }

        InlineKeyboardRow back = new InlineKeyboardRow();
        back.add(buttonText("返回上一级", StrUtil.join("#", "privacy#city", provinceCode, 1, query)));
        back.add(cancelButton());
        rows.add(back);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup buildCityKeyboard(Page<Address> cityList, String provinceCode, int query) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow row = new InlineKeyboardRow();
        rows.add(row);

        int idx = 0, max = 3;
        List<Address> records = cityList.getRecords();
        for (Address addr : records) {
            if (idx == max) {
                row = new InlineKeyboardRow();
                rows.add(row);
                idx = 0;
            }
            row.add(buttonText(addr.getCityName(), StrUtil.join("#", "privacy#county", addr.getCityCode(), cityList.getCurrent(), query)));
            idx ++;
        }

        if (cityList.hasNext() || cityList.hasPrevious()) {
            InlineKeyboardRow pageRow = new InlineKeyboardRow();
            if (cityList.hasPrevious()) {
                pageRow.add(buttonText("上一页", StrUtil.join("#", "privacy#city", provinceCode, (cityList.getCurrent() - 1), query)));
            }
            if (cityList.hasNext()) {
                pageRow.add(buttonText("下一页", StrUtil.join("#", "privacy#city", provinceCode, (cityList.getCurrent() + 1), query)));
            }
            rows.add(pageRow);
        }

        InlineKeyboardRow back = new InlineKeyboardRow();
        back.add(buttonText("返回上一级", StrUtil.join("#", "privacy#province", provinceCode, 1, query)));
        back.add(cancelButton());
        rows.add(back);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    public static InlineKeyboardMarkup buildProvinceKeyboard(List<Address> address, int query) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        InlineKeyboardRow r = new InlineKeyboardRow();
        rows.add(r);
        int idx = 0, max = 4;
        for (Address addr : address) {
            if (idx == max) {
                r = new InlineKeyboardRow();
                rows.add(r);
                idx = 0;
            }
            r.add(buttonText(addr.getProvinceName(), StrUtil.join("#", "privacy#city", addr.getProvinceCode(), 1, query)));
            idx ++;
        }
        rows.add(new InlineKeyboardRow(cancelButton()));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }


    public static InlineKeyboardMarkup buildAuditPublishKeyboard(Long id) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("✅通过发布", "group#publish#true#" + id)),
                        row(buttonText("❌拒绝发布", "group#publish#false#" + id))
                )).build();
    }


    public static InlineKeyboardMarkup buildPublishChannelKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonUrl("骗子曝光", "https://t.me/CDZJpianzi"),
                                buttonUrl("供需频道", "https://t.me/CDZJgongxu")),
                        row(buttonUrl("联系客服", "https://t.me/CDZJkefu"))
                )).build();
    }

    /**
     * 骗子曝光审核键盘：通过 / 拒绝
     * callback: group#expose#(true|false)#id
     */
    public static InlineKeyboardMarkup buildPzExposeAuditKeyboard(Long id) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("✅通过发布", "group#expose#true#" + id)),
                        row(buttonText("❌拒绝发布", "group#expose#false#" + id))
                )).build();
    }


    public static InlineKeyboardMarkup buildCommonDel(String text) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText(text, "delete"))
                )).build();
    }


    public static ReplyKeyboard buildStartKeyboard() {
        return ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboard(List.of(
                        new KeyboardRow(List.of(
                                KeyboardButton.builder().text("\uD83D\uDC81\uD83C\uDFFB\u200d♂️承兑报备").build(),
                                KeyboardButton.builder().text("\uD83D\uDE97车队报备").build(),
                                KeyboardButton.builder().text("\uD83E\uDDCF\uD83C\uDFFB\u200d♂️卸货合作商").build())
                        ),
                        new KeyboardRow(List.of(
                                KeyboardButton.builder().text("\uD83D\uDC81\uD83C\uDFFC\u200d♀️承兑所在地").build(),
                                KeyboardButton.builder().text("\uD83D\uDE95车队所在地").build(),
                                KeyboardButton.builder().text("\uD83E\uDDCF\uD83C\uDFFB\u200d♀️卸货所在地").build())
                        ),
                        new KeyboardRow(List.of(
                                KeyboardButton.builder().text("\uD83D\uDFE2供需发布").build(),
                                KeyboardButton.builder().text("\uD83D\uDD0D查汇率").build(),
                                KeyboardButton.builder().text("\uD83D\uDD0E骗子曝光").build())
                        ),
                        new KeyboardRow(List.of(
                                KeyboardButton.builder().text("\uD83D\uDCF2合作洽谈").build(),
                                KeyboardButton.builder().text("\uD83D\uDCAC联系客服").build(),
                                KeyboardButton.builder().text("❤️我的余额").build())
                        )
                )).build();
    }

    public static InlineKeyboardMarkup keyboard (String json) {
        InlineKeyboardMarkup markup = null;
        if (StrUtil.isNotBlank(json)) {
            KeyboardTransfer transfer = JSONUtil.toBean(json, KeyboardTransfer.class);
            if (Objects.nonNull(transfer)) {
                List<InlineKeyboardRow> rows = new ArrayList<>(transfer.getKeyboard().size());
                for (List<ButtonTransfer> buttonTransfers : transfer.getKeyboard()) {
                    InlineKeyboardRow row = new InlineKeyboardRow();
                    for (ButtonTransfer buttonTransfer : buttonTransfers) {
                        row.add(InlineKeyboardButton.builder().text(buttonTransfer.getText()).url(buttonTransfer.getUrl()).build());
                    }
                    rows.add(row);
                }
                markup = InlineKeyboardMarkup.builder().keyboard(rows).build();
            }
        }
        return markup;
    }

    /*
        客服回复#
        &内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容内容
        &按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr

        @param keyboardCommand 传入（按钮1#https://t.me/xmkfjqr|按钮2#https://t.me/xmkfjqr$按钮3#https://t.me/xmkfjqr）
    */

    public static String parseKeyboard(String keyboardCommand) {
        String keyboardJson = "{}";
        if (StrUtil.isNotBlank(keyboardCommand)) {
            // &24小时客服#https://t.me/|24小时客服#https://t.me/$24小时客服#https://t.me/
            List<String> keyboardLines = StrUtil.split(keyboardCommand, "$");
            List<InlineKeyboardRow> rows = new ArrayList<>(keyboardLines.size());
            for (String keyboardLine : keyboardLines) {
                // 24小时客服#https://t.me/|24小时客服#https://t.me/
                List<String> row = StrUtil.split(keyboardLine, "|");
                InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
                for (String buttonLine : row) {
                    List<String> buttons = StrUtil.split(buttonLine, "#");
                    keyboardRow.add(InlineKeyboardButton.builder().text(buttons.get(0)).url(buttons.get(1)).build());
                }
                rows.add(keyboardRow);
            }
            InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                    .keyboard(rows)
                    .build();
            keyboardJson = JSONUtil.toJsonStr(markup);
        }
        return keyboardJson;
    }



    public static InlineKeyboardRow row (InlineKeyboardButton ... buttons) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.addAll(Arrays.asList(buttons));
        return row;
    }

    public static InlineKeyboardRow rowChosen (String name) {
        return new InlineKeyboardRow(InlineKeyboardButton.builder()
                .text(name)
                .switchInlineQueryChosenChat(SwitchInlineQueryChosenChat.builder()
                        .allowGroupChats(true)
                        .allowUserChats(true)
                        .allowChannelChats(true)
                        .build())
                .build());
    }

    public static InlineKeyboardButton chosenButton (String name, String defaultVal) {
        return InlineKeyboardButton.builder()
                .text(name)
                .switchInlineQueryChosenChat(SwitchInlineQueryChosenChat.builder()
                        .allowGroupChats(true)
                        .allowUserChats(true)
                        .allowChannelChats(true)
                        .build())
                .switchInlineQuery(defaultVal)
                .build();
    }

    public static InlineKeyboardRow row (String[] names, String[] callbacks) {
        InlineKeyboardRow row = new InlineKeyboardRow();
        for (int i = 0; i < names.length; i++) {
            row.add(buttonText(names[i], callbacks[i]));
        }
        return row;
    }

    public static InlineKeyboardButton buttonUrl (String name, String url) {
        return InlineKeyboardButton.builder().url(url).text(name).build();
    }

    public static InlineKeyboardButton cancelButton () {
        return InlineKeyboardButton.builder().text("❌取消").callbackData("delete").build();
    }

     public static InlineKeyboardButton buttonText (String name, String callback) {
        return InlineKeyboardButton.builder().text(name).callbackData(callback).build();
    }


    public static InlineKeyboardMarkup buildLoadKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        row(buttonText("\uD83D\uDEA5正在查询中...", "privacy#loading"))
                )).build();
    }
}
