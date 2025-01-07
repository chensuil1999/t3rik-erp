package com.t3rik.mes.wm.domain.tx;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.t3rik.common.core.domain.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 盘点单的txbean
 * @Author: Wywzyy
 * @Date: 2025-01-06-13:08
 * @Description:
 */
@Data
public class TakingTxBean extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 产品物料ID */
    private Long itemId;

    /** 产品物料编码 */
    private String itemCode;

    /** 产品物料名称 */
    private String itemName;

    /** 规格型号 */
    private String specification;

    /** 单位 */
    private String unitOfMeasure;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库编码 */
    private String warehouseCode;

    /** 仓库名称 */
    private String warehouseName;

    /** 库区ID */
    private Long locationId;

    /** 库区编码 */
    private String locationCode;

    /** 库区名称 */
    private String locationName;

    /** 库位ID */
    private Long areaId;

    /** 库位编码 */
    private String areaCode;

    /** 库位名称 */
    private String areaName;

    /** 单据类型 */
    private String sourceDocType;

    /** 单据ID */
    private Long sourceDocId;

    /** 单据编号 */
    private String sourceDocCode;

    /** 单据行ID */
    private Long sourceDocLineId;

    /** 事务数量 */
    private BigDecimal takingQuantity;

    /** 入库日期 */
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date recptDate;

    /** 库存有效期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expireDate;

    private Integer attr4;
    /**只数每件
     *
     */
    private Integer attr3;

}
