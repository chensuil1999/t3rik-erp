package com.t3rik.mobile.mes.service.impl

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils
import com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.t3rik.common.enums.mes.OrderStatusEnum
import com.t3rik.common.utils.StringUtils
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.pro.service.IProWorkorderService
import com.t3rik.mes.wm.domain.WmMaterialStock
import com.t3rik.mes.wm.service.IWmMaterialStockService
import com.t3rik.mobile.common.enums.CurrentIndexEnum
import com.t3rik.mobile.mes.service.IWmsStockMobService
import jakarta.annotation.Resource
import org.springframework.stereotype.Service

/**
 * @Author: Wywzyy
 * @Date: 2024-10-12-11:12
 * @Description:
 */
@Service
class WmsStockMobServiceImpl : IWmsStockMobService {
    @Resource
    lateinit var wmMaterialStockService: IWmMaterialStockService
    override fun getParamByCurrentIndex(currentIndex: String?): List<String> {
//        TODO("Not yet implemented")
        val statusList = mutableListOf<String>()
        // 类似switch
        when (currentIndex) {
            // 主仓
            CurrentIndexEnum.UNPROCESSED.code -> {
                return statusList.apply {
                    add("4")
                    //add(OrderStatusEnum.FINISHED.code)
                }
            }
            // 线边虚拟仓
            CurrentIndexEnum.PROCESSED.code -> {
                return statusList.apply {
                    add("1")
                }
            }
            // 废料仓
            CurrentIndexEnum.FINISHED.code -> {
                return statusList.apply {
                    add("2")
                }
            }
            // 查询全部
            else -> return statusList
        }

    }

    override fun getPageByCurrentIndex(wmMaterialStock: WmMaterialStock, page: Page<WmMaterialStock>): Page<WmMaterialStock> {
//        TODO("Not yet implemented")
        val paramByCurrentIndex = this.getParamByCurrentIndex(wmMaterialStock.currentIndex)
        //println("oooo: " + paramByCurrentIndex)
        return this.wmMaterialStockService.lambdaQuery()
                .like(StringUtils.isNotBlank(wmMaterialStock.itemName), WmMaterialStock::getItemName, wmMaterialStock.itemName)
                .`in`(CollectionUtils.isNotEmpty(paramByCurrentIndex), WmMaterialStock::getWarehouseId, paramByCurrentIndex)
                .ne(WmMaterialStock::getQuantityOnhand, 0)
                .orderByDesc(WmMaterialStock::getRecptDate)
                .orderByAsc(WmMaterialStock::getWarehouseCode)
                .page(page)
    }

    override fun getWmMaterialStockDetailById(materialStockId: Long): WmMaterialStock {
//        TODO("Not yet implemented")
        return this.wmMaterialStockService.lambdaQuery()
                .eq(WmMaterialStock::getMaterialStockId, materialStockId).one()
    }
}