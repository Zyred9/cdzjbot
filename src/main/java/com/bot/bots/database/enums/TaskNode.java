package com.bot.bots.database.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Getter
@AllArgsConstructor
public enum TaskNode {

    RECHARGE(0, "充值检测", 10),
    ;

    private final int code;
    private final String desc;
    private final int sleep;

    private static final Map<TaskNode, Integer> times = new HashMap<>();

    static {
        for (TaskNode node : TaskNode.values()) {
            times.put(node, node.getSleep());
        }
    }

    public static int getSleep(TaskNode node) {
        return times.get(node);
    }

    public static void setSleep(TaskNode node, int sleep) {
        times.put(node, sleep);
    }

}
