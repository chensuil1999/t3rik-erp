package com.t3rik.mobile.mes.controller

import com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery
import com.t3rik.common.constant.MsgConstants
import com.t3rik.common.constant.UserConstants
import com.t3rik.common.core.controller.BaseController
import com.t3rik.common.core.domain.AjaxResult
import com.t3rik.common.core.page.TableDataInfo
import com.t3rik.common.exception.BusinessException
import com.t3rik.common.utils.SecurityUtils
import com.t3rik.mes.md.domain.MdProductBom
import com.t3rik.mes.md.service.IMdProductBomService
import com.t3rik.mes.pro.domain.ProTask
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.pro.domain.ProWorkorderBom
import com.t3rik.mes.pro.service.IProTaskService
import com.t3rik.mes.pro.service.IProWorkorderBomService
import com.t3rik.mobile.common.ktextend.isNonPositive
import com.t3rik.mobile.mes.service.IProWorkOrderService
import io.swagger.annotations.ApiOperation
import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(UserConstants.MOBILE_PATH + "/work")
class WorkController : BaseController() {

    @Resource
    lateinit var proWorkOrderService: IProWorkOrderService
    @Resource
    lateinit var proWorkorderBomService: IProWorkorderBomService
    @Resource
    lateinit var proTaskService: IProTaskService
    /**
     * 查询任务列表
     */
    @ApiOperation("查询生产工单列表")
    @GetMapping("/list")
    fun getProWorkOrderList(proWorkOrder: ProWorkorder): TableDataInfo {
        //println("qq: " + proWorkOrder.workorderName)

        return getDataTableWithPage(proWorkOrderService.getPageByCurrentIndex(proWorkOrder, getMPPage(proWorkOrder))) }

    @ApiOperation("查询生产工单详细信息")
    @GetMapping("/{workOrderId}")
    fun getWorkOrderDetail(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
        val proWorkOrderDetail = this.proWorkOrderService.getWorkOrderDetailById(workOrderId)
        return AjaxResult.success(proWorkOrderDetail)
    }

    @ApiOperation("查询生产工单对应BOM物料信息")
    @GetMapping("/bom/{workOrderId}")
    fun getBom(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
//        val proWorkOrderDetail = this.proWorkOrderService.getWorkOrderDetailById(workOrderId)
        val pwb = proWorkorderBomService.lambdaQuery()
                .eq(ProWorkorderBom::getWorkorderId, workOrderId).list()
        return AjaxResult.success(pwb)
    }

    @ApiOperation("查询生产工单对应生产任务料信息")
    @GetMapping("/protask/{workOrderId}")
    fun getProtask(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
//        val proWorkOrderDetail = this.proWorkOrderService.getWorkOrderDetailById(workOrderId)
        val pt = proTaskService.lambdaQuery()
                .eq(ProTask::getWorkorderId, workOrderId).orderByAsc(ProTask::getProcessName).list()
        return AjaxResult.success(pt)
    }
}