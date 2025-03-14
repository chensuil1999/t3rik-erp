package com.t3rik.common.enums.mes;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 单据通用状态
 *
 * @author t3rik
 * @date 2024-05-24
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PREPARE("PREPARE", "草稿", 10),
    CONFIRMED("CONFIRMED", "未排产", 20),
    PRODUCING("PRODUCING", "排产中", 21),
    APPROVING("APPROVING", "审批中", 30),
    APPROVED("APPROVED", "审批通过", 40),
    REFUSE("REFUSE", "审批拒绝", 50),
    FINISHED("FINISHED", "已完成", 60),
    REVERSAL("REVERSAL", "已冲销", 61);
//    REVERSAL("REVERSAL", "已完成", 60);


    private final String code;
    private final String desc;
    private final Integer sort;

    public static final Map<String, OrderStatusEnum> WORK_ORDER_STATUS_ENUM_MAP;

    static {
        WORK_ORDER_STATUS_ENUM_MAP = new HashMap<>();
        for (OrderStatusEnum value : OrderStatusEnum.values()) {
            WORK_ORDER_STATUS_ENUM_MAP.put(value.getCode(), value);
        }
    }

    public static OrderStatusEnum getEnumByCode(String code) {
        return WORK_ORDER_STATUS_ENUM_MAP.get(code);
    }
} 