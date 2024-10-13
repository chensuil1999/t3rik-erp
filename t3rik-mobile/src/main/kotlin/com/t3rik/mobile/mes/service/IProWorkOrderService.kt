package com.t3rik.mobile.mes.service

import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.t3rik.common.core.domain.AjaxResult
import com.t3rik.mes.pro.domain.ProTask
import com.t3rik.mes.pro.domain.ProWorkorder

/**
 * @Author: Wywzyy
 * @Date: 2024-09-25-11:04
 * @Description:
 */
public interface IProWorkOrderService{
    fun getParamByCurrentIndex(currentIndex: String?): List<String>

    /**
     * 根据传入的前端页码，返回数据
     */
    fun getPageByCurrentIndex(proWorkOrder: ProWorkorder, page: Page<ProWorkorder>): Page<ProWorkorder>

    fun getWorkOrderDetailById(workOrderId: Long): ProWorkorder
}