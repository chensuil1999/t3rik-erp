package com.t3rik.mobile.mes.controller

import com.t3rik.common.constant.MsgConstants
import com.t3rik.common.constant.UserConstants
import com.t3rik.common.core.controller.BaseController
import com.t3rik.common.core.domain.AjaxResult
import com.t3rik.mes.md.domain.MdItem
import com.t3rik.mes.md.service.IMdItemService
import com.t3rik.mes.pro.service.IProTaskService
import com.t3rik.mobile.common.ktextend.isNonPositive
import com.t3rik.mobile.mes.service.IMdItemMobileService
import io.swagger.annotations.ApiOperation
import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author: Wywzyy
 * @Date: 2024-11-04-15:36
 * @Description:
 */
@RestController
@RequestMapping(UserConstants.MOBILE_PATH + "/md")
class MdItemMobileController : BaseController()   {
    @Resource
    lateinit var mdItemMobileService: IMdItemService

//    @Resource
//    lateinit var proTaskService: IProTaskService
    @ApiOperation("查询生产工单对应生产任务料信息")
    @GetMapping("/{mdItemId}")
    fun getItemInfo(@PathVariable mdItemId: Long): AjaxResult {
        // 小于等于0 抛异常
        mdItemId.isNonPositive { MsgConstants.PARAM_ERROR }
//        val proWorkOrderDetail = this.proWorkOrderService.getWorkOrderDetailById(workOrderId)
        val mdit = this.mdItemMobileService.lambdaQuery().eq(MdItem::getItemId, mdItemId).one()
        return AjaxResult.success(mdit)
    }
}