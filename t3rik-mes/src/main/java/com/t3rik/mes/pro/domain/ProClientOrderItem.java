package com.t3rik.mes.pro.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.t3rik.common.annotation.Excel;
import com.t3rik.common.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 客户订单材料
 * 对象 pro_client_order_item
 *
 * @author t3rik
 * @date 2024-05-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName(value = "pro_client_order_item")
public class ProClientOrderItem extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId
    private Long orderItemId;

    /**
     * 工单ID
     */
    private Long workorderId;

    /**
     * 工单编码
     */
    @Excel(name = "工单编码")
    private String workorderCode;

    /**
     * 工单名称
     */
    @Excel(name = "工单名称")
    private String workorderName;

    /**
     * 客户订单表id
     */
    @Excel(name = "客户订单表id")
    // @NotNull(message = MsgConstants.PARAM_ERROR)
    // @Min(value = 1, message = MsgConstants.PARAM_ERROR)
    private Long clientOrderId;


    /**
     * 物料产品ID
     */
    @Excel(name = "物料产品ID")
    private Long itemId;

    /**
     * 产品物料编码
     */
    @Excel(name = "物料编码")
    private String itemCode;

    /**
     * 产品物料名称
     */
    @Excel(name = "物料名称")
    private String itemName;

    /**
     * 规格型号
     */
    @Excel(name = "规格型号")
    private String specification;

    /**
     * 单位
     */
    @Excel(name = "单位")
    private String unitOfMeasure;


    /**
     * 物料使用数量
     */
    @Excel(name = "物料使用数量")
    private BigDecimal quantity;


    /**
     * 层级，字典表：mes_item_level
     */
    @Excel(name = "层级，字典表：mes_item_level")
    private String level;


    /**
     * 预留字段1
     */
    @Excel(name = "预留字段1")
    private String attr1;


    /**
     * 预留字段2
     */
    @Excel(name = "预留字段2")
    private String attr2;


    /**
     * 预留字段3
     */
    @Excel(name = "预留字段3")
    private Long attr3;


    /**
     * 预留字段4
     */
    @Excel(name = "预留字段4")
    private Long attr4;
}
