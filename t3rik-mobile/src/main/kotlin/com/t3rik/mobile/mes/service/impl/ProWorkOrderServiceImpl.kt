package com.t3rik.mobile.mes.service.impl

//import com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils.like
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.t3rik.common.enums.mes.OrderStatusEnum
import com.t3rik.common.utils.StringUtils
import com.t3rik.mes.pro.domain.ProWorkorder
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
//                    add(OrderStatusEnum.PREPARE.code)
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
            //草稿
            CurrentIndexEnum.PREPARE.code -> {
                return statusList.apply {
                    add(OrderStatusEnum.PREPARE.code)
//                    add(OrderStatusEnum.CONFIRMED.code)
                }
            }
            // 查询全部
            else -> return statusList
        }
    }

    override fun getPageByCurrentIndex(proWorkOrder: ProWorkorder, page: Page<ProWorkorder>): Page<ProWorkorder> {
        val paramByCurrentIndex = this.getParamByCurrentIndex(proWorkOrder.currentIndex)
        //val queryWrapper: LambdaQueryWrapper<ProWorkorder> = LambdaQueryWrapper<ProWorkorder>()
//        queryWrappe .select(ProWorkorder.class, x->x.getColumn().equal("thof_weight"))
        //queryWrapper.
                //LambdaQueryWrapper<ProWorkorder> = Wrapper.<ProWorkOrder>lambdaQuery()
        return this.proWorkOrderService.lambdaQuery()
                .select(ProWorkorder::getWorkorderId,
                        ProWorkorder::getClientOrderId,
                        ProWorkorder::getClientOrderCode,
                        ProWorkorder::getWorkorderCode, ProWorkorder::getWorkorderName,
                        ProWorkorder::getWorkorderType,ProWorkorder::getOrderSource,ProWorkorder::getSourceCode
                        ,ProWorkorder::getProductId
                        ,ProWorkorder::getProductCode
                        ,ProWorkorder::getProductName
                        ,ProWorkorder::getProductSpc
                        ,ProWorkorder::getUnitOfMeasure
                        ,ProWorkorder::getBatchCode
                        ,ProWorkorder::getQuantity
                        ,ProWorkorder::getQuantityProduced
                        ,ProWorkorder::getQuantityChanged
                        ,ProWorkorder::getQuantityScheduled
                        ,ProWorkorder::getClientId
                        ,ProWorkorder::getClientCode
                        ,ProWorkorder::getClientName
                        ,ProWorkorder::getVendorId
                        ,ProWorkorder::getVendorCode
                        ,ProWorkorder::getVendorName
                        ,ProWorkorder::getRequestDate
                        ,ProWorkorder::getFinishDate
                        ,ProWorkorder::getStatus
                        ,ProWorkorder::getAttr1
                        ,ProWorkorder::getAttr2
                        ,ProWorkorder::getAttr3
                        ,ProWorkorder::getAttr4
                        ,ProWorkorder::getCreateTime
                        ,ProWorkorder::getRemark
                        )
                .like(StringUtils.isNotBlank(proWorkOrder.workorderName), ProWorkorder::getWorkorderName, proWorkOrder.workorderName)
                .`in`(CollectionUtils.isNotEmpty(paramByCurrentIndex), ProWorkorder::getStatus, paramByCurrentIndex)
                .eq(ProWorkorder::getDeleted, 0)
                .orderByDesc(ProWorkorder::getCreateTime)
                .orderByAsc(ProWorkorder::getStatus)
                .page(page)
    }

    override fun getWorkOrderDetailById(workOrderId: Long): ProWorkorder {
//        TODO("Not yet implemented")
        return this.proWorkOrderService.lambdaQuery()
                .select(ProWorkorder::getWorkorderId,
                        ProWorkorder::getClientOrderId,
                        ProWorkorder::getClientOrderCode,
                        ProWorkorder::getWorkorderCode, ProWorkorder::getWorkorderName,
                        ProWorkorder::getWorkorderType,ProWorkorder::getOrderSource,ProWorkorder::getSourceCode
                        ,ProWorkorder::getProductId
                        ,ProWorkorder::getProductCode
                        ,ProWorkorder::getProductName
                        ,ProWorkorder::getProductSpc
                        ,ProWorkorder::getUnitOfMeasure
                        ,ProWorkorder::getBatchCode
                        ,ProWorkorder::getQuantity
                        ,ProWorkorder::getQuantityProduced
                        ,ProWorkorder::getQuantityChanged
                        ,ProWorkorder::getQuantityScheduled
                        ,ProWorkorder::getClientId
                        ,ProWorkorder::getClientCode
                        ,ProWorkorder::getClientName
                        ,ProWorkorder::getVendorId
                        ,ProWorkorder::getVendorCode
                        ,ProWorkorder::getVendorName
                        ,ProWorkorder::getRequestDate
                        ,ProWorkorder::getFinishDate
                        ,ProWorkorder::getStatus
                        ,ProWorkorder::getAttr1
                        ,ProWorkorder::getAttr2
                        ,ProWorkorder::getAttr3
                        ,ProWorkorder::getAttr4
                        ,ProWorkorder::getCreateTime
                        ,ProWorkorder::getRemark
                )
                .eq(ProWorkorder::getWorkorderId, workOrderId).one()
    }

    override fun addWorkorder(proWorkOrder: ProWorkorder): Int {
//        TODO("Not yet implemented")
        return proWorkOrderService.insertProWorkorder(proWorkOrder)
    }
}