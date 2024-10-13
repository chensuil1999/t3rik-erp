package com.t3rik.mobile.mes.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.wm.domain.WmMaterialStock

/**
 * @Author: Wywzyy
 * @Date: 2024-10-12-11:05
 * @Description:
 */
interface IWmsStockMobService {
    fun getParamByCurrentIndex(currentIndex: String?): List<String>
    /**
     * 根据传入的前端页码，返回数据
     */
    fun getPageByCurrentIndex(wmMaterialStock: WmMaterialStock, page: Page<WmMaterialStock>): Page<WmMaterialStock>
    fun getWmMaterialStockDetailById(materialStockId: Long): WmMaterialStock
}