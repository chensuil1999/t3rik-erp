package com.t3rik.mes.wm.controller;

import com.t3rik.common.annotation.Log;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.controller.BaseController;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.core.page.TableDataInfo;
import com.t3rik.common.enums.BusinessType;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.common.utils.poi.ExcelUtil;
import com.t3rik.mes.pro.domain.ProTask;
import com.t3rik.mes.pro.domain.ProWorkorder;
import com.t3rik.mes.wm.domain.WmItemConsume;
import com.t3rik.mes.wm.domain.WmItemConsumeLine;
import com.t3rik.mes.wm.domain.WmOutsourceRecpt;
import com.t3rik.mes.wm.domain.WmOutsourceRecptLine;
import com.t3rik.mes.wm.domain.tx.ItemConsumeTxBean;
import com.t3rik.mes.wm.domain.tx.OutsourceRecptTxBean;
import com.t3rik.mes.wm.service.IStorageCoreService;
import com.t3rik.mes.wm.service.IWmItemConsumeLineService;
import com.t3rik.mes.wm.service.IWmItemConsumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * 物料消耗记录Controller
 * 
 * @author yinjinlu
 * @date 2022-09-19
 */
@RestController
@RequestMapping("/mes/wm/itemconsume")
public class WmItemConsumeController extends BaseController
{
    @Autowired
    private IWmItemConsumeService wmItemConsumeService;

    @Autowired
    private IWmItemConsumeLineService wmItemConsumeLineService;

    @Autowired
    private IStorageCoreService storageCoreService;

    /**
     * 查询物料消耗记录列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmItemConsume wmItemConsume)
    {
        startPage();
        List<WmItemConsume> list = wmItemConsumeService.selectWmItemConsumeList(wmItemConsume);
        return getDataTable(list);
    }

    /**
     * 导出物料消耗记录列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:export')")
    @Log(title = "物料消耗记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmItemConsume wmItemConsume)
    {
        List<WmItemConsume> list = wmItemConsumeService.selectWmItemConsumeList(wmItemConsume);
        ExcelUtil<WmItemConsume> util = new ExcelUtil<WmItemConsume>(WmItemConsume.class);
        util.exportExcel(response, list, "物料消耗记录数据");
    }

    /**
     * 获取物料消耗记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(wmItemConsumeService.selectWmItemConsumeByRecordId(recordId));
    }

    /**
     * 新增物料消耗记录
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:add')")
    @Log(title = "物料消耗记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WmItemConsume wmItemConsume)
    {
        if(wmItemConsumeService.insertWmItemConsume(wmItemConsume) > 0) {
            wmItemConsumeService.generateItemConsumeLine(wmItemConsume);
            return AjaxResult.success();
        }
        return AjaxResult.error();
    }

    /**
     * 修改物料消耗记录
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:edit')")
    @Log(title = "物料消耗记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmItemConsume wmItemConsume)
    {
        return toAjax(wmItemConsumeService.updateWmItemConsume(wmItemConsume));
    }

    /**
     * 删除物料消耗记录
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:remove')")
    @Log(title = "物料消耗记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(wmItemConsumeService.deleteWmItemConsumeByRecordIds(recordIds));
    }

    /**
     * 执行消耗
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemconsume:edit')")
    @Log(title = "物料消耗记录执行", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/{recordId}")
    public AjaxResult execute(@PathVariable Long recordId) {
        WmItemConsume wmIc = wmItemConsumeService.selectWmItemConsumeByRecordId(recordId);

        List<WmItemConsumeLine> lines = wmItemConsumeLineService.selectWmItemConsumeLineByRecordId(recordId);
        if (CollectionUtils.isEmpty(lines)) {
            return AjaxResult.error("请指定消耗的物资！");
        }
        List<ItemConsumeTxBean> beans = wmItemConsumeService.getTxBeans(recordId);
        storageCoreService.processItemConsume(beans);
        wmIc.setStatus(UserConstants.ORDER_STATUS_FINISHED);
        wmItemConsumeService.updateWmItemConsume(wmIc);
        return AjaxResult.success();
    }
}
