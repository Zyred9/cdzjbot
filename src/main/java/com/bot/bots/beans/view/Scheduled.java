package com.bot.bots.beans.view;

import com.bot.bots.database.enums.TaskNode;
import com.bot.bots.database.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@Setter
@Getter
@Accessors(chain = true)
public class Scheduled {

    private TaskType type;
    private TaskNode node;

    public static Scheduled build(TaskType type, TaskNode node) {
        return new Scheduled().setType(type).setNode(node);
    }
}
