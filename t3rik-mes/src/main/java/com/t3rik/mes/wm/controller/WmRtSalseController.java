package com.t3rik.mes.wm.controller;

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
import com.t3rik.mes.wm.domain.WmRtIssue;
import com.t3rik.mes.wm.domain.WmRtSalse;
import com.t3rik.mes.wm.domain.WmRtSalseLine;
import com.t3rik.mes.wm.domain.tx.RtSalseTxBean;
import com.t3rik.mes.wm.mapper.WmTransactionMapper;
import com.t3rik.mes.wm.service.*;
import com.t3rik.mes.wm.utils.WmWarehouseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 产品销售退货单Controller
 *
 * @author yinjinlu
 * @date 2022-10-06
 */
@RestController
@RequestMapping("/mes/wm/rtsalse")
public class WmRtSalseController extends BaseController {
    @Resource
    private IWmRtSalseService wmRtSalseService;

    @Resource
    private IWmRtSalseLineService wmRtSalseLineService;

    @Resource
    private WmWarehouseUtil warehouseUtil;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    /**
     * 查询产品销售退货单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmRtSalse wmRtSalse) {
        startPage();
        List<WmRtSalse> list = wmRtSalseService.selectWmRtSalseList(wmRtSalse);
        return getDataTable(list);
    }

    /**
     * 导出产品销售退货单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:export')")
    @Log(title = "产品销售退货单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmRtSalse wmRtSalse) {
        List<WmRtSalse> list = wmRtSalseService.selectWmRtSalseList(wmRtSalse);
        ExcelUtil<WmRtSalse> util = new ExcelUtil<WmRtSalse>(WmRtSalse.class);
        util.exportExcel(response, list, "产品销售退货单数据");
    }

    /**
     * 获取产品销售退货单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:query')")
    @GetMapping(value = "/{rtId}")
    public AjaxResult getInfo(@PathVariable("rtId") Long rtId) {
        return AjaxResult.success(wmRtSalseService.selectWmRtSalseByRtId(rtId));
    }

    /**
     * 新增产品销售退货单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:add')")
    @Log(title = "产品销售退货单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmRtSalse wmRtSalse) {
        if (UserConstants.NOT_UNIQUE.equals(wmRtSalseService.checkUnique(wmRtSalse))) {
            return AjaxResult.error("退货单号已存在!");
        }
        // 设置仓库信息
        this.warehouseUtil.setWarehouseInfo(wmRtSalse);
        return toAjax(wmRtSalseService.insertWmRtSalse(wmRtSalse));
    }

    /**
     * 修改产品销售退货单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:edit')")
    @Log(title = "产品销售退货单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmRtSalse wmRtSalse) {
        if (UserConstants.NOT_UNIQUE.equals(wmRtSalseService.checkUnique(wmRtSalse))) {
            return AjaxResult.error("退货单号已存在!");
        }
        // 设置仓库信息
        this.warehouseUtil.setWarehouseInfo(wmRtSalse);
        return toAjax(wmRtSalseService.updateWmRtSalse(wmRtSalse));
    }

    /**
     * 删除产品销售退货单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:remove')")
    @Log(title = "产品销售退货单", businessType = BusinessType.DELETE)
    @Transactional
    @DeleteMapping("/{rtIds}")
    public AjaxResult remove(@PathVariable Long[] rtIds) {
        if (rtIds.length == 0) {
            return AjaxResult.error(MsgConstants.PARAM_ERROR);
        }
        for (Long rtId : rtIds) {
            WmRtSalse wrs = wmRtSalseService.selectWmRtSalseByRtId(rtId);
            if(UserConstants.ORDER_STATUS_FINISHED.equals(wrs.getStatus()))
            {
                return AjaxResult.error("存在归档数据！无法删除");
            }
            wmRtSalseLineService.deleteByRtId(rtId);
            wmTransactionMapper.deleteWmTransactionByTypeAndSourceId(rtId,"RTSALSE");
        }
        return toAjax(wmRtSalseService.deleteWmRtSalseByRtIds(rtIds));
    }

    /**
     * 执行退货
     *
     * @param rtId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:edit')")
    @Log(title = "产品销售退货单", businessType = BusinessType.UPDATE)
    @PutMapping("/{rtId}")
    public AjaxResult execute(@PathVariable Long rtId) {
        //判断单据状态
        WmRtSalse rtSalse = wmRtSalseService.selectWmRtSalseByRtId(rtId);
        if(StringUtils.isNull(rtId)) {
            return AjaxResult.error("不存在该单据");
        }
        if(!OrderStatusEnum.PREPARE.getCode().equals(rtSalse.getStatus())) {
            return AjaxResult.error("此单据无法执行出库");
        }
        wmRtSalseService.execute(rtId);
        return AjaxResult.success();
    }

    /**
     * 冲销
     *
     * @param rtId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtsalse:edit')")
    @Log(title = "产品销售退货单", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/reverse/{rtId}")
    public AjaxResult reversexecute(@PathVariable Long rtId) {
        if (!StringUtils.isNotNull(rtId)) {
            return AjaxResult.error("销售出库单不存在");
        }
        WmRtSalse rtSalse = wmRtSalseService.selectWmRtSalseByRtId(rtId);
        if(!OrderStatusEnum.FINISHED.getCode().equals(rtSalse.getStatus())) {
            return AjaxResult.error("只能冲销完成单据");
        }
        wmRtSalseService.reverseexecute(rtId);
        return AjaxResult.success();
    }

}
