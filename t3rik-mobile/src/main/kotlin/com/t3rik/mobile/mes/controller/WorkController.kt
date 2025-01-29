package com.t3rik.mobile.mes.controller

import cn.hutool.core.date.DateUtil
import com.t3rik.common.annotation.RepeatSubmit
import com.t3rik.common.constant.MsgConstants
import com.t3rik.common.constant.UserConstants
import com.t3rik.common.core.controller.BaseController
import com.t3rik.common.core.domain.AjaxResult
import com.t3rik.common.core.page.TableDataInfo
import com.t3rik.common.enums.mes.OrderStatusEnum
import com.t3rik.common.utils.DateUtils
import com.t3rik.mes.pro.domain.ProTask
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.pro.service.IProRouteProcessService
import com.t3rik.mes.pro.service.IProTaskService
import com.t3rik.mes.pro.service.IProWorkorderBomService
import com.t3rik.mobile.common.ktextend.isNonPositive
import com.t3rik.mobile.mes.service.IProWorkOrderService
import com.t3rik.system.strategy.AutoCodeUtil
import io.swagger.annotations.ApiOperation
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal


@RestController
@RequestMapping(UserConstants.MOBILE_PATH + "/work")
class WorkController : BaseController() {

    @Resource
    lateinit var proWorkOrderService: IProWorkOrderService
    @Resource
    lateinit var proWorkorderBomService: IProWorkorderBomService
    @Resource
    lateinit var proTaskService: IProTaskService

    @Resource
    lateinit var  autoCodeUtil: AutoCodeUtil;

//    @Resource
//    lateinit var mdProductBomService: IMdProductBomService
@Resource
lateinit var proRouteProcessService: IProRouteProcessService

    /**
     * 查询任务列表
     */
    @ApiOperation("查询生产工单列表")
    @GetMapping("/list")
    fun getProWorkOrderList(proWorkOrder: ProWorkorder): TableDataInfo {
        return getDataTableWithPage(proWorkOrderService.getPageByCurrentIndex(proWorkOrder, getMPPage(proWorkOrder)))
    }

    @ApiOperation("查询生产工单详细信息")
    @GetMapping("/{workOrderId}")
    fun getWorkOrderDetail(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
        val proWorkOrderDetail = this.proWorkOrderService.getWorkOrderDetailById(workOrderId)
        return AjaxResult.success(proWorkOrderDetail)
    }

    @ApiOperation("新增工单（草稿）")
    @RepeatSubmit
    @PostMapping
    fun addWorkorder(@RequestBody proWorkOrder: ProWorkorder): AjaxResult {
        if(proWorkOrder.remark == "") {
            AjaxResult.error(MsgConstants.PARAM_ERROR)
        }
        if (proWorkOrder.getParentId() == null || proWorkOrder.getParentId().compareTo(0) == 0) {
            proWorkOrder.setAncestors("0");
        }
        proWorkOrder.workorderCode = autoCodeUtil.genSerialCode(UserConstants.WORKORDER_CODE, "")
        proWorkOrder.workorderName = username + "-建于-" + DateUtils.getDate();
        proWorkOrder.orderSource = "ORDER"
        proWorkOrder.productId = 9
        proWorkOrder.productCode = "CP00045"
        proWorkOrder.productName = "测试品"
        proWorkOrder.unitOfMeasure = "ZHI"
        proWorkOrder.quantity = BigDecimal(10)
        proWorkOrder.requestDate = DateUtils.getNowDate()
        proWorkOrder.productSpc = "测试"
        proWorkOrder.status = OrderStatusEnum.PREPARE.code
        proWorkOrder.workorderType ="SELF"
        return AjaxResult.success(proWorkOrderService.addWorkorder(proWorkOrder))
    }

    @ApiOperation("查询生产工单对应BOM物料信息")
    @GetMapping("/bom/{workOrderId}")
    fun getBom(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
        val pwb = proWorkorderBomService.selectProWorkorderBomBywId(workOrderId)
        return AjaxResult.success(pwb)
    }

    @ApiOperation("查询生产工单对应生产任务料信息")
    @GetMapping("/protask/{workOrderId}")
    fun getProtask(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常route
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }

        val pt = proTaskService.lambdaQuery()
                .eq(ProTask::getWorkorderId, workOrderId).orderByAsc(ProTask::getTaskCode).list()
        return AjaxResult.success(pt)
    }

    /**
     *
     */
    @ApiOperation("查询生产工单对应产品工艺路线")
    @GetMapping("/route/{workOrderId}")
    fun getRoute(@PathVariable workOrderId: Long): AjaxResult {
        // 小于等于0 抛异常
        workOrderId.isNonPositive { MsgConstants.PARAM_ERROR }
        val proute = proRouteProcessService.selectProRouteProcessByWorkorderId(workOrderId)
        return AjaxResult.success(proute)
    }

}