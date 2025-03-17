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
import com.t3rik.mes.wm.domain.WmItemRecpt;
import com.t3rik.mes.wm.domain.WmRtSalse;
import com.t3rik.mes.wm.domain.WmRtSalseLine;
import com.t3rik.mes.wm.domain.WmRtVendor;
import com.t3rik.mes.wm.domain.tx.RtVendorTxBean;
import com.t3rik.mes.wm.mapper.WmTransactionMapper;
import com.t3rik.mes.wm.service.IStorageCoreService;
import com.t3rik.mes.wm.service.IWmRtVendorLineService;
import com.t3rik.mes.wm.service.IWmRtVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

/**
 * 供应商退货Controller
 * 
 * @author yinjinlu
 * @date 2022-06-13
 */
@RestController
@RequestMapping("/mes/wm/rtvendor")
public class WmRtVendorController extends BaseController
{
    @Autowired
    private IWmRtVendorService wmRtVendorService;

    @Autowired
    private IWmRtVendorLineService wmRtVendorLineService;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    /**
     * 查询供应商退货列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmRtVendor wmRtVendor)
    {
        startPage();
        List<WmRtVendor> list = wmRtVendorService.selectWmRtVendorList(wmRtVendor);
        return getDataTable(list);
    }

    /**
     * 导出供应商退货列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:export')")
    @Log(title = "供应商退货", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmRtVendor wmRtVendor)
    {
        List<WmRtVendor> list = wmRtVendorService.selectWmRtVendorList(wmRtVendor);
        ExcelUtil<WmRtVendor> util = new ExcelUtil<WmRtVendor>(WmRtVendor.class);
        util.exportExcel(response, list, "供应商退货数据");
    }

    /**
     * 获取供应商退货详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:query')")
    @GetMapping(value = "/{rtId}")
    public AjaxResult getInfo(@PathVariable("rtId") Long rtId)
    {
        return AjaxResult.success(wmRtVendorService.selectWmRtVendorByRtId(rtId));
    }

    /**
     * 新增供应商退货
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:add')")
    @Log(title = "供应商退货", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmRtVendor wmRtVendor)
    {
        if(UserConstants.NOT_UNIQUE.equals(wmRtVendorService.checkCodeUnique(wmRtVendor))){
            return AjaxResult.error("退货单编号已经存在！");
        }
        return toAjax(wmRtVendorService.insertWmRtVendor(wmRtVendor));
    }

    /**
     * 修改供应商退货
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:edit')")
    @Log(title = "供应商退货", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmRtVendor wmRtVendor)
    {
        if(UserConstants.NOT_UNIQUE.equals(wmRtVendorService.checkCodeUnique(wmRtVendor))){
            return AjaxResult.error("退货单编号已经存在！");
        }
        return toAjax(wmRtVendorService.updateWmRtVendor(wmRtVendor));
    }

    /**
     * 删除供应商退货
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:remove')")
    @Log(title = "供应商退货", businessType = BusinessType.DELETE)
	@Transactional
    @DeleteMapping("/{rtIds}")
    public AjaxResult remove(@PathVariable Long[] rtIds)
    {
        if (rtIds.length == 0) {
            return AjaxResult.error(MsgConstants.PARAM_ERROR);
        }
        for (Long rtId:rtIds) {
            WmRtVendor rtVendor = wmRtVendorService.selectWmRtVendorByRtId(rtId);
            if(UserConstants.ORDER_STATUS_FINISHED.equals(rtVendor.getStatus())){
                return AjaxResult.error("无法删除完成单据!");
            }
            wmRtVendorLineService.deleteByRtId(rtId);
            wmTransactionMapper.deleteWmTransactionByTypeAndSourceId(rtId,"RTV");
        }
        return toAjax(wmRtVendorService.deleteWmRtVendorByRtIds(rtIds));
    }

    /**
     * 执行退货
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:edit')")
    @Log(title = "供应商退货单", businessType = BusinessType.UPDATE)
    @PutMapping("/{rtId}")
    public AjaxResult execute(@PathVariable Long rtId){
        //判断单据状态
        WmRtVendor wmRtVendor = wmRtVendorService.selectWmRtVendorByRtId(rtId);
        if(StringUtils.isNull(wmRtVendor)) {
            return AjaxResult.error("不存在该单据");
        }
        if(!OrderStatusEnum.PREPARE.getCode().equals(wmRtVendor.getStatus())) {
            return AjaxResult.error("此单据无法执行退货");
        }
        wmRtVendorService.execute(rtId);
        return AjaxResult.success();
    }

    /**
     * 执行冲销
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:rtvendor:edit')")
    @Log(title = "供应商退货单", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/reverse/{rtId}")
    public AjaxResult reversexecute(@PathVariable Long rtId){

        if (!StringUtils.isNotNull(rtId)) {
            return AjaxResult.error("供应商退货单不存在");
        }
        WmRtVendor rtVendor = wmRtVendorService.selectWmRtVendorByRtId(rtId);
        if(!OrderStatusEnum.FINISHED.getCode().equals(rtVendor.getStatus())) {
            return AjaxResult.error("只能冲销完成单据");
        }
        wmRtVendorService.reverseexecute(rtId);
        return AjaxResult.success();

    }


}
