package com.t3rik.mes.wm.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.t3rik.common.annotation.Log;
import com.t3rik.common.constant.MsgConstants;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.controller.BaseController;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.core.page.TableDataInfo;
import com.t3rik.common.enums.BusinessType;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.common.utils.poi.ExcelUtil;
import com.t3rik.mes.wm.domain.WmProductSalse;
import com.t3rik.mes.wm.domain.WmProductSalseLine;
import com.t3rik.mes.wm.domain.WmRtVendor;
import com.t3rik.mes.wm.domain.tx.ProductSalseTxBean;
import com.t3rik.mes.wm.mapper.WmTransactionMapper;
import com.t3rik.mes.wm.service.IStorageCoreService;
import com.t3rik.mes.wm.service.IWmProductSalseLineService;
import com.t3rik.mes.wm.service.IWmProductSalseService;
import com.t3rik.mes.wm.utils.WmWarehouseUtil;
import com.t3rik.system.strategy.AutoCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 销售出库单Controller
 *
 * @author yinjinlu
 * @date 2022-10-04
 */
@RestController
@RequestMapping("/mes/wm/productsalse")
public class WmProductSalseController extends BaseController {
    @Resource
    private IWmProductSalseService wmProductSalseService;

    @Resource
    private IWmProductSalseLineService wmProductSalseLineService;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    @Resource
    private WmWarehouseUtil warehouseUtil;
    //
    @Autowired
    private AutoCodeUtil autoCodeUtil;

    /**
     * 查询销售出库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmProductSalse wmProductSalse) {
        startPage();
        List<WmProductSalse> list = wmProductSalseService.selectWmProductSalseList(wmProductSalse);
        return getDataTable(list);
    }

    /**
     * 导出销售出库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:export')")
    @Log(title = "销售出库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmProductSalse wmProductSalse) {
        List<WmProductSalse> list = wmProductSalseService.selectWmProductSalseList(wmProductSalse);
        ExcelUtil<WmProductSalse> util = new ExcelUtil<WmProductSalse>(WmProductSalse.class);
        util.exportExcel(response, list, "销售出库单数据");
    }

    /**
     * 获取销售出库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:query')")
    @GetMapping(value = "/{salseId}")
    public AjaxResult getInfo(@PathVariable("salseId") Long salseId) {
        return AjaxResult.success(wmProductSalseService.selectWmProductSalseBySalseId(salseId));
    }

    /**
     * 新增销售出库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:add')")
    @Log(title = "销售出库单", businessType = BusinessType.INSERT)
    @Transactional
    @PostMapping
    public AjaxResult add(@RequestBody WmProductSalse wmProductSalse) {
        if (UserConstants.NOT_UNIQUE.equals(wmProductSalseService.checkUnique(wmProductSalse))) {
            return AjaxResult.error("出库单编号已存在！");
        }
        // 设置仓库信息
        this.warehouseUtil.setWarehouseInfo(wmProductSalse);
        autoCodeUtil.saveSerialCode(UserConstants.PRODUCTSALSE_CODE, null);
        return toAjax(wmProductSalseService.insertWmProductSalse(wmProductSalse));
    }

    /**
     * 修改销售出库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:edit')")
    @Log(title = "销售出库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmProductSalse wmProductSalse) {
        if (UserConstants.NOT_UNIQUE.equals(wmProductSalseService.checkUnique(wmProductSalse))) {
            return AjaxResult.error("出库单编号已存在！");
        }
        if(UserConstants.ORDER_STATUS_FINISHED.equals(wmProductSalse.getStatus())) {
                return AjaxResult.error("归档数据！无法修改");
        }
        // 设置仓库信息
        this.warehouseUtil.setWarehouseInfo(wmProductSalse);
        return toAjax(wmProductSalseService.updateWmProductSalse(wmProductSalse));
    }

    /**
     * 删除销售出库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:remove')")
    @Log(title = "销售出库单", businessType = BusinessType.DELETE)
    @Transactional
    @DeleteMapping("/{salseIds}")
    public AjaxResult remove(@PathVariable Long[] salseIds) {
        if (salseIds.length == 0) {
            return AjaxResult.error(MsgConstants.PARAM_ERROR);
        }
        for (Long salseId : salseIds) {
            WmProductSalse wps = wmProductSalseService.selectWmProductSalseBySalseId(salseId);
            if(UserConstants.ORDER_STATUS_FINISHED.equals(wps.getStatus()))
            {
                return AjaxResult.error("存在归档数据！无法删除");
            }
            wmProductSalseLineService.deleteBySalseId(salseId);
            wmTransactionMapper.deleteWmTransactionByTypeAndSourceId(salseId,"PSALSE");
        }
        return toAjax(wmProductSalseService.deleteWmProductSalseBySalseIds(salseIds));
    }

    /**
     * 执行出库
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:edit')")
    @Log(title = "销售出库单", businessType = BusinessType.UPDATE)
    @PutMapping("/{salseId}")
    public AjaxResult execute(@PathVariable Long salseId) {
        //判断单据状态
        WmProductSalse salse = wmProductSalseService.selectWmProductSalseBySalseId(salseId);
        if(StringUtils.isNull(salseId)) {
            return AjaxResult.error("不存在该单据");
        }
        if(!OrderStatusEnum.PREPARE.getCode().equals(salse.getStatus())) {
            return AjaxResult.error("此单据无法执行出库");
        }
        wmProductSalseService.execute(salseId);
        return AjaxResult.success();
    }

    /**
     * 执行出库
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:productsalse:edit')")
    @Log(title = "销售出库单", businessType = BusinessType.UPDATE)
    @PutMapping("/reverse/{salseId}")
    public AjaxResult reversexecute(@PathVariable Long salseId) {
        if (!StringUtils.isNotNull(salseId)) {
            return AjaxResult.error("销售出库单不存在");
        }
        WmProductSalse salse = wmProductSalseService.selectWmProductSalseBySalseId(salseId);
        if(!OrderStatusEnum.FINISHED.getCode().equals(salse.getStatus())) {
            return AjaxResult.error("只能冲销完成单据");
        }
        wmProductSalseService.reverseexecute(salseId);
        return AjaxResult.success();
    }
}
