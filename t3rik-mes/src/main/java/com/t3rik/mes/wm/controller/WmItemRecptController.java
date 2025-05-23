package com.t3rik.mes.wm.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.t3rik.common.annotation.Log;
import com.t3rik.common.constant.MsgConstants;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.controller.BaseController;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.core.page.TableDataInfo;
import com.t3rik.common.enums.BusinessType;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.common.utils.poi.ExcelUtil;
import com.t3rik.mes.pro.domain.ProFeedback;
import com.t3rik.mes.wm.domain.WmItemRecpt;
import com.t3rik.mes.wm.domain.WmItemRecptLine;
import com.t3rik.mes.wm.mapper.WmTransactionMapper;
import com.t3rik.mes.wm.service.IWmItemRecptLineService;
import com.t3rik.mes.wm.service.IWmItemRecptService;
import com.t3rik.mes.wm.utils.WmWarehouseUtil;
import com.t3rik.system.strategy.AutoCodeUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 物料入库单Controller
 * (已重构)
 *
 * @author t3rik
 * @date 2024-08-27
 */
@RestController
@RequestMapping("/mes/wm/itemrecpt")
public class WmItemRecptController extends BaseController {
    @Resource
    private IWmItemRecptService wmItemRecptService;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    @Resource
    private IWmItemRecptLineService wmItemRecptLineService;

    @Resource
    private WmWarehouseUtil warehouseUtil;

    @Autowired
    private AutoCodeUtil autoCodeUtil;

    /**
     * 查询物料入库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmItemRecpt wmItemRecpt) {
        startPage();
        List<WmItemRecpt> list = wmItemRecptService.selectWmItemRecptList(wmItemRecpt);
        return getDataTable(list);
    }

    /**
     * 导出物料入库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:export')")
    @Log(title = "物料入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmItemRecpt wmItemRecpt) {
        List<WmItemRecpt> list = wmItemRecptService.selectWmItemRecptList(wmItemRecpt);
        ExcelUtil<WmItemRecpt> util = new ExcelUtil<WmItemRecpt>(WmItemRecpt.class);
        util.exportExcel(response, list, "物料入库单数据");
    }

    /**
     * 获取物料入库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:query')")
    @GetMapping(value = "/{recptId}")
    public AjaxResult getInfo(@PathVariable("recptId") Long recptId) {
        return AjaxResult.success(wmItemRecptService.selectWmItemRecptByRecptId(recptId));
    }

    /**
     * 新增物料入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:add')")
    @Log(title = "物料入库单", businessType = BusinessType.INSERT)
    @Transactional
    @PostMapping
    public AjaxResult add(@RequestBody WmItemRecpt wmItemRecpt) {
        if (UserConstants.NOT_UNIQUE.equals(wmItemRecptService.checkRecptCodeUnique(wmItemRecpt))) {
            return AjaxResult.error(MsgConstants.CODE_ALREADY_EXISTS);
        }
        // 设置仓库相关信息
        warehouseUtil.setWarehouseInfo(wmItemRecpt);
        autoCodeUtil.saveSerialCode(UserConstants.ITEMRECPT_CODE, null);
        return toAjax(wmItemRecptService.insertWmItemRecpt(wmItemRecpt));
    }

    /**
     * 确认入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:edit')")
    @Log(title = "物料入库单", businessType = BusinessType.UPDATE)
    @PutMapping("/confirm")
    public AjaxResult confirm(@RequestBody WmItemRecpt wmItemRecpt) {
        // 参数校验
        Optional.ofNullable(wmItemRecpt.getRecptId()).orElseThrow(() -> new BusinessException(MsgConstants.PARAM_ERROR));
        // 检查有没有入库单行
        List<WmItemRecptLine> lines =
                this.wmItemRecptLineService
                        .lambdaQuery()
                        .eq(WmItemRecptLine::getRecptId, wmItemRecpt.getRecptId())
                        .list();
        if (CollUtil.isEmpty(lines)) {
            return AjaxResult.error("请添加入库单行");
        }
        wmItemRecpt.setStatus(UserConstants.ORDER_STATUS_CONFIRMED);
        // 设置仓库相关信息
        warehouseUtil.setWarehouseInfo(wmItemRecpt);
        return toAjax(wmItemRecptService.updateWmItemRecpt(wmItemRecpt));
    }


    /**
     * 修改物料入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:edit')")
    @Log(title = "物料入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmItemRecpt wmItemRecpt) {
        // 设置仓库相关信息
        warehouseUtil.setWarehouseInfo(wmItemRecpt);
        return toAjax(wmItemRecptService.updateWmItemRecpt(wmItemRecpt));
    }

    /**
     * 执行入库
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:edit')")
    @Log(title = "物料入库单", businessType = BusinessType.UPDATE)
    @PutMapping("/{recptId}")
    public AjaxResult execute(@PathVariable Long recptId) {
        WmItemRecpt recpt = wmItemRecptService.selectWmItemRecptByRecptId(recptId);
        Optional.ofNullable(recpt).orElseThrow(() -> new BusinessException(MsgConstants.PARAM_ERROR));
        if(!OrderStatusEnum.PREPARE.getCode().equals(recpt.getStatus())) {
            return AjaxResult.error("此单据无法执行出库");
        }
        this.wmItemRecptService.execute(recptId);
        return AjaxResult.success();
    }

    /**
     * 执行冲销
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:edit')")
    @Log(title = "物料入库单", businessType = BusinessType.UPDATE)
    @PutMapping("/reverse/{recptId}")
    public AjaxResult reversexecute(@PathVariable Long recptId) {
        if (!StringUtils.isNotNull(recptId)) {
            return AjaxResult.error("采购入库单据不存在");
        }
        WmItemRecpt recpt = wmItemRecptService.selectWmItemRecptByRecptId(recptId);
        if(!OrderStatusEnum.FINISHED.getCode().equals(recpt.getStatus())) {
            return AjaxResult.error("只能冲销完成单据");
        }
        this.wmItemRecptService.reverseexecute(recptId);
        return AjaxResult.success();
    }


    /**
     * 删除物料入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:itemrecpt:remove')")
    @Log(title = "物料入库单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recptIds}")
    @Transactional
    public AjaxResult remove(@PathVariable Long[] recptIds) {
        if (recptIds.length == 0) {
            return AjaxResult.error(MsgConstants.PARAM_ERROR);
        }
        Long ircCount = wmItemRecptService.lambdaQuery().eq(WmItemRecpt::getStatus, OrderStatusEnum.FINISHED.getCode())
                .in(WmItemRecpt::getRecptId, recptIds).count();
        // 退料数量
        if(ircCount> 0) {
            return AjaxResult.error("存在已完成存档数据，无法删除!");
        }
        List<Long> ids = Arrays.stream(recptIds).toList();
        for(Long lids : ids) {
            this.wmItemRecptLineService.deleteByRecptId(lids);
            wmTransactionMapper.deleteWmTransactionByTypeAndSourceId(lids,"IR");
        }
        this.wmItemRecptService.deleteWmItemRecptByRecptIds(recptIds);
        return AjaxResult.success();
    }
}
