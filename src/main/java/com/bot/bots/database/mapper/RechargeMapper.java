package com.bot.bots.database.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bot.bots.database.entity.Recharge;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author admin
 * @since v 0.0.1
 */
@SuppressWarnings("all")
public interface RechargeMapper extends BaseMapper<Recharge> {

    @Select("""
        <script> 
            select IFNULL(SUM(cny_amount), 0)
            from t_recharge 
            where receive_status = 1
            <if test='userId != null'> 
                and user_id = #{userId} 
            </if> 
        </script>    
    """)
    BigDecimal selectSumRecharge(@Param("userId") Long userId);

    @Select("""
        <script> 
            select IFNULL(SUM(cny_amount), 0)
            from t_recharge 
            where receive_status = 1
            and create_time BETWEEN #{start} and #{end}
            <if test='userId != null'> 
                and user_id = #{userId} 
            </if> 
        </script>    
    """)
    BigDecimal selectTodaySumRecharge(@Param("start")LocalDateTime start, @Param("end")LocalDateTime end, @Param("userId") Long userId);
}
