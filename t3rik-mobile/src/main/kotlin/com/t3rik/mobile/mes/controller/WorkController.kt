package com.t3rik.mobile.mes.controller

import com.t3rik.common.constant.UserConstants
import com.t3rik.common.core.controller.BaseController
import com.t3rik.common.core.page.TableDataInfo
import com.t3rik.mes.pro.domain.ProTask
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.pro.service.IProTaskService
import com.t3rik.mobile.mes.service.IProWorkOrderService
import io.swagger.annotations.ApiOperation
import jakarta.annotation.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping(UserConstants.MOBILE_PATH + "/work")
class WorkController : BaseController() {

    @Resource
    lateinit var proWorkOrderService: IProWorkOrderService
    /**
     * 查询任务列表
     */
    @ApiOperation("查询生产工单列表")
    @GetMapping("/list")
    fun getProWorkOrderList(proWorkOrder: ProWorkorder): TableDataInfo {
        //println("qq: " + task)
        return getDataTableWithPage(proWorkOrderService.getPageByCurrentIndex(proWorkOrder, getMPPage(proWorkOrder))) }
}