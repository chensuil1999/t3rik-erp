package com.t3rik.mes.wm.domain;

import java.math.BigDecimal;

import com.t3rik.common.annotation.Excel;
import com.t3rik.common.core.domain.BaseEntity;

/**
 * 产品销售出库行对象 wm_product_salse_line
 * 
 * @author yinjinlu
 * @date 2022-10-05
 */
public class WmProductSalseLine extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 行ID */
    private Long lineId;

    /** 出库记录ID */
    @Excel(name = "出库记录ID")
    private Long salseId;

    /** 库存记录ID */
    @Excel(name = "库存记录ID")
    private Long materialStockId;

    /** 产品物料ID */
    @Excel(name = "产品物料ID")
    private Long itemId;

    /** 产品物料编码 */
    @Excel(name = "产品物料编码")
    private String itemCode;

    /** 产品物料名称 */
    @Excel(name = "产品物料名称")
    private String itemName;

    /** 规格型号 */
    @Excel(name = "规格型号")
    private String specification;

    /** 单位 */
    @Excel(name = "单位")
    private String unitOfMeasure;

    /** 出库数量 */
    @Excel(name = "出库数量")
    private BigDecimal quantitySalse;

    /** 批次号 */
    @Excel(name = "批次号")
    private String batchCode;

    /** 生产工单ID */
    @Excel(name = "生产工单ID")
    private Long workorderId;

    /** 生产工单编码 */
    @Excel(name = "生产工单编码")
    private String workorderCode;

    public Long getWorkorderId() {
        return workorderId;
    }

    public void setWorkorderId(Long workorderId) {
        this.workorderId = workorderId;
    }

    public String getWorkorderCode() {
        return workorderCode;
    }

    public void setWorkorderCode(String workorderCode) {
        this.workorderCode = workorderCode;
    }

    /** 仓库ID */
    @Excel(name = "仓库ID")
    private Long warehouseId;

    /** 仓库编码 */
    @Excel(name = "仓库编码")
    private String warehouseCode;

    /** 仓库名称 */
    @Excel(name = "仓库名称")
    private String warehouseName;

    /** 库区ID */
    @Excel(name = "库区ID")
    private Long locationId;

    /** 库区编码 */
    @Excel(name = "库区编码")
    private String locationCode;

    /** 库区名称 */
    @Excel(name = "库区名称")
    private String locationName;

    /** 库位ID */
    @Excel(name = "库位ID")
    private Long areaId;

    /** 库位编码 */
    @Excel(name = "库位编码")
    private String areaCode;

    /** 库位名称 */
    @Excel(name = "库位名称")
    private String areaName;

    /** 件数名称 */
    @Excel(name = "件数")
    private int countOfPackage;
    /** 重量/件 */
    @Excel(name = "重量/件")
    private Double packageOfWeight;
    /** 金额 */
    @Excel(name = "金额")
    private Double amount;

    public Double getItemThOfWeight() {
        return itemThOfWeight;
    }

    public void setItemThOfWeight(Double itemThOfWeight) {
        this.itemThOfWeight = itemThOfWeight;
    }

    @Excel(name = "千斤重系数")
    private Double itemThOfWeight;

    public int getCountOfPackage() {
        return countOfPackage;
    }

    public void setCountOfPackage(int countOfPackage) {
        this.countOfPackage = countOfPackage;
    }

    public Double getPackageOfWeight() {
        return packageOfWeight;
    }

    public void setPackageOfWeight(Double packageOfWeight) {
        this.packageOfWeight = packageOfWeight;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * 是否出厂检验
     */
    private String oqcCheck;

    /**
     * 出厂检验单ID
     */
    private Long oqcId;

    /**
     * 出厂检验单编码
     */
    private String oqcCode;

    /** 预留字段1 */
    private Double attr1;

    /** 预留字段2 */
    private String attr2;

    /** 预留字段3 */
    private Long attr3;

    /** 预留字段4 */
    private Integer attr4;

    public void setLineId(Long lineId) 
    {
        this.lineId = lineId;
    }

    public Long getLineId() 
    {
        return lineId;
    }
    public void setSalseId(Long salseId) 
    {
        this.salseId = salseId;
    }

    public Long getSalseId() 
    {
        return salseId;
    }
    public void setMaterialStockId(Long materialStockId) 
    {
        this.materialStockId = materialStockId;
    }

    public Long getMaterialStockId() 
    {
        return materialStockId;
    }
    public void setItemId(Long itemId) 
    {
        this.itemId = itemId;
    }

    public Long getItemId() 
    {
        return itemId;
    }
    public void setItemCode(String itemCode) 
    {
        this.itemCode = itemCode;
    }

    public String getItemCode() 
    {
        return itemCode;
    }
    public void setItemName(String itemName) 
    {
        this.itemName = itemName;
    }

    public String getItemName() 
    {
        return itemName;
    }
    public void setSpecification(String specification) 
    {
        this.specification = specification;
    }

    public String getSpecification() 
    {
        return specification;
    }
    public void setUnitOfMeasure(String unitOfMeasure) 
    {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getUnitOfMeasure() 
    {
        return unitOfMeasure;
    }
    public void setQuantitySalse(BigDecimal quantitySalse) 
    {
        this.quantitySalse = quantitySalse;
    }

    public BigDecimal getQuantitySalse() 
    {
        return quantitySalse;
    }
    public void setBatchCode(String batchCode) 
    {
        this.batchCode = batchCode;
    }

    public String getBatchCode() 
    {
        return batchCode;
    }
    public void setWarehouseId(Long warehouseId) 
    {
        this.warehouseId = warehouseId;
    }

    public Long getWarehouseId() 
    {
        return warehouseId;
    }
    public void setWarehouseCode(String warehouseCode) 
    {
        this.warehouseCode = warehouseCode;
    }

    public String getWarehouseCode() 
    {
        return warehouseCode;
    }
    public void setWarehouseName(String warehouseName) 
    {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseName() 
    {
        return warehouseName;
    }
    public void setLocationId(Long locationId) 
    {
        this.locationId = locationId;
    }

    public Long getLocationId() 
    {
        return locationId;
    }
    public void setLocationCode(String locationCode) 
    {
        this.locationCode = locationCode;
    }

    public String getLocationCode() 
    {
        return locationCode;
    }
    public void setLocationName(String locationName) 
    {
        this.locationName = locationName;
    }

    public String getLocationName() 
    {
        return locationName;
    }
    public void setAreaId(Long areaId) 
    {
        this.areaId = areaId;
    }

    public Long getAreaId() 
    {
        return areaId;
    }
    public void setAreaCode(String areaCode) 
    {
        this.areaCode = areaCode;
    }

    public String getAreaCode() 
    {
        return areaCode;
    }
    public void setAreaName(String areaName) 
    {
        this.areaName = areaName;
    }

    public String getAreaName() 
    {
        return areaName;
    }

    public String getOqcCheck() {
        return oqcCheck;
    }

    public void setOqcCheck(String oqcCheck) {
        this.oqcCheck = oqcCheck;
    }

    public Long getOqcId() {
        return oqcId;
    }

    public void setOqcId(Long oqcId) {
        this.oqcId = oqcId;
    }

    public String getOqcCode() {
        return oqcCode;
    }

    public void setOqcCode(String oqcCode) {
        this.oqcCode = oqcCode;
    }

    public void setAttr1(Double attr1)
    {
        this.attr1 = attr1;
    }

    public Double getAttr1()
    {
        return attr1;
    }
    public void setAttr2(String attr2) 
    {
        this.attr2 = attr2;
    }

    public String getAttr2() 
    {
        return attr2;
    }
    public void setAttr3(Long attr3) 
    {
        this.attr3 = attr3;
    }

    public Long getAttr3() 
    {
        return attr3;
    }
    public void setAttr4(Integer attr4)
    {
        this.attr4 = attr4;
    }

    public Integer getAttr4()
    {
        return attr4;
    }

    @Override
    public String toString() {
        return "WmProductSalseLine{" +
                "lineId=" + lineId +
                ", salseId=" + salseId +
                ", materialStockId=" + materialStockId +
                ", itemId=" + itemId +
                ", itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", specification='" + specification + '\'' +
                ", unitOfMeasure='" + unitOfMeasure + '\'' +
                ", quantitySalse=" + quantitySalse +
                ", batchCode='" + batchCode + '\'' +
                ", warehouseId=" + warehouseId +
                ", warehouseCode='" + warehouseCode + '\'' +
                ", warehouseName='" + warehouseName + '\'' +
                ", locationId=" + locationId +
                ", locationCode='" + locationCode + '\'' +
                ", locationName='" + locationName + '\'' +
                ", areaId=" + areaId +
                ", areaCode='" + areaCode + '\'' +
                ", areaName='" + areaName + '\'' +
                ", oqcCheck='" + oqcCheck + '\'' +
                ", oqcId=" + oqcId +
                ", oqcCode='" + oqcCode + '\'' +
                ", attr1='" + attr1 + '\'' +
                ", attr2='" + attr2 + '\'' +
                ", attr3=" + attr3 +
                ", attr4=" + attr4 +
                '}';
    }
}
