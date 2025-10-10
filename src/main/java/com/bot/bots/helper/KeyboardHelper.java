package com.bot.bots.helper;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bot.bots.beans.view.ButtonTransfer;
import com.bot.bots.beans.view.KeyboardTransfer;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
public class KeyboardHelper {


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
                        new KeyboardRow(List.of(KeyboardButton.builder().text("承兑报备").build(), KeyboardButton.builder().text("车队报备").build(), KeyboardButton.builder().text("卸货合作商").build())),
                        new KeyboardRow(List.of(KeyboardButton.builder().text("承兑所在地(范用搜索)").build(), KeyboardButton.builder().text("车队所在地(范围搜索)").build(), KeyboardButton.builder().text("卸货所在地").build())),
                        new KeyboardRow(List.of(KeyboardButton.builder().text("骗子曝光").build(), KeyboardButton.builder().text("盘总料方合作洽谈").build())),
                        new KeyboardRow(List.of(KeyboardButton.builder().text("供需方发布").build(), KeyboardButton.builder().text("联系客服").build(), KeyboardButton.builder().text("我的").build()))
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

     public static InlineKeyboardButton buttonText (String name, String callback) {
        return InlineKeyboardButton.builder().text(name).callbackData(callback).build();
    }

    public static String[] arr (String ... k) {
        return k;
    }


}
