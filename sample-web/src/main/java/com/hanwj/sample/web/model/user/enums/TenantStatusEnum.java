package com.hanwj.sample.web.model.user.enums;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户状态枚举
 *
 * @author Hanwenjie
 */
public enum TenantStatusEnum implements IEnum<Integer> {
    /**
     * 正常
     */
    NORMAL(0, "正常"),
    /**
     * 禁用
     */
    FREEZE(9, "禁用"),

    /**
     * 未知枚举（属性校验）
     */
    UNDEFINED(-9999, "未定义");

    /**
     * 枚举值
     */
    private final int value;

    /**
     * 描述
     */
    private final String description;

    TenantStatusEnum(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 获得枚举
     * @param val 枚举值
     * @return TenantStatusEnum
     */
    @JsonCreator
    public static TenantStatusEnum fromValue(Integer val) {
        for (TenantStatusEnum e : values()) {
            if (e.value == val) {
                return e;
            }
        }
        if(BeanUtil.isNotEmpty(val)){
            return UNDEFINED;
        }
        return null;
    }

    /**
     *
     * @param val 枚举值
     * @return TenantStatusEnum
     */
    @JsonCreator
    public static TenantStatusEnum fromValue(String val) {
        for (TenantStatusEnum e : values()) {
            if (e.value == Integer.parseInt(val)) {
                return e;
            }
        }
        if(StrUtil.isNotEmpty(val)){
            return UNDEFINED;
        }
        return null;
    }

    @JsonValue
    @Override
    public Integer getValue() {
        return value;
    }

    @JsonIgnore
    public String getDescription() {
        return description;
    }

}
