package com.t3rik.mobile.mes.service.impl

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.t3rik.common.enums.mes.OrderStatusEnum
import com.t3rik.common.utils.SecurityUtils
import com.t3rik.common.utils.StringUtils
import com.t3rik.mes.pro.domain.ProTask
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.pro.service.IProTaskService
import com.t3rik.mes.pro.service.IProWorkorderService
import com.t3rik.mobile.common.enums.CurrentIndexEnum
import com.t3rik.mobile.mes.service.IProWorkOrderService
import jakarta.annotation.Resource
import org.springframework.stereotype.Service

/**
 * @Author: Wywzyy
 * @Date: 2024-09-25-11:05
 * @Description:
 */
@Service
class ProWorkOrderServiceImpl : IProWorkOrderService{
    @Resource
    lateinit var proWorkOrderService: IProWorkorderService
    override fun getParamByCurrentIndex(currentIndex: String?): List<String> {
        val statusList = mutableListOf<String>()
        // 类似switch
        when (currentIndex) {
            // 未处理查询草稿和未排产
            CurrentIndexEnum.UNPROCESSED.code -> {
                return statusList.apply {
                    add(OrderStatusEnum.PREPARE.code)
                    add(OrderStatusEnum.CONFIRMED.code)
                }
            }
            // 已排产工单
            CurrentIndexEnum.PROCESSED.code -> {
                return statusList.apply {
                    add(OrderStatusEnum.PRODUCING.code)
                    //add(OrderStatusEnum.FINISHED.code)
                }
            }
            // 已完成的工单
            CurrentIndexEnum.FINISHED.code -> {
                return statusList.apply {
                    add(OrderStatusEnum.FINISHED.code)
                }
            }
            // 查询全部
            else -> return statusList
        }
    }

    override fun getPageByCurrentIndex(proWorkOrder: ProWorkorder, page: Page<ProWorkorder>): Page<ProWorkorder> {
        val paramByCurrentIndex = this.getParamByCurrentIndex(proWorkOrder.currentIndex)
        return this.proWorkOrderService.lambdaQuery()
                //.eq(proWorkOrder::get, SecurityUtils.getUserId())
                .like(StringUtils.isNotBlank(proWorkOrder.workorderName), ProWorkorder::getWorkorderName, proWorkOrder.workorderName)
                .`in`(CollectionUtils.isNotEmpty(paramByCurrentIndex), ProWorkorder::getStatus, paramByCurrentIndex)
                .orderByDesc(ProWorkorder::getCreateTime)
                .orderByAsc(ProWorkorder::getStatus)
                .page(page)
    }
}