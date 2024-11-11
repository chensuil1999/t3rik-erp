package com.t3rik.mobile.mes.controller

import com.t3rik.common.constant.UserConstants
import com.t3rik.common.core.controller.BaseController
import com.t3rik.common.core.page.TableDataInfo
import com.t3rik.mes.pro.domain.ProWorkorder
import com.t3rik.mes.wm.domain.WmMaterialStock
import com.t3rik.mes.wm.service.IWmMaterialStockService
import com.t3rik.mobile.mes.service.IProWorkOrderService
import com.t3rik.mobile.mes.service.IWmsStockMobService
import io.swagger.annotations.ApiOperation
import jakarta.annotation.Resource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author: Wywzyy
 * @Date: 2024-10-12-10:56
 * @Description:
 */
@RestController
@RequestMapping(UserConstants.MOBILE_PATH + "/wms")
class WmsMobController : BaseController()  {
    @Resource
    lateinit var wmsStockMobService: IWmsStockMobService

    @ApiOperation("查询库存列表")
    @GetMapping("/list")
    fun getProWorkOrderList(wmMaterialStock: WmMaterialStock): TableDataInfo {
        return getDataTableWithPage(wmsStockMobService.getPageByCurrentIndex(wmMaterialStock, getMPPage(wmMaterialStock))) }
}