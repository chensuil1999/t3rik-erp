package com.t3rik.mes.wm.controller;

import com.t3rik.common.annotation.Log;
import com.t3rik.common.constant.MsgConstants;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.controller.BaseController;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.core.page.TableDataInfo;
import com.t3rik.common.enums.BusinessType;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.common.utils.poi.ExcelUtil;
import com.t3rik.mes.pro.domain.ProTask;
import com.t3rik.mes.pro.domain.ProWorkorder;
import com.t3rik.mes.pro.service.IProTaskService;
import com.t3rik.mes.pro.service.IProWorkorderService;
import com.t3rik.mes.wm.domain.WmOutsourceIssue;
import com.t3rik.mes.wm.domain.WmOutsourceRecpt;
import com.t3rik.mes.wm.domain.WmOutsourceRecptLine;
import com.t3rik.mes.wm.domain.tx.OutsourceRecptTxBean;
import com.t3rik.mes.wm.service.IStorageCoreService;
import com.t3rik.mes.wm.service.IWmOutsourceRecptLineService;
import com.t3rik.mes.wm.service.IWmOutsourceRecptService;
import com.t3rik.mes.wm.utils.WmWarehouseUtil;
import com.t3rik.system.strategy.AutoCodeUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 外协入库单Controller
 *
 * @author yinjinlu
 * @date 2023-10-30
 */
@RestController
@RequestMapping("/mes/wm/outsourcerecpt")
public class WmOutsourceRecptController extends BaseController {
    @Autowired
    private IWmOutsourceRecptService wmOutsourceRecptService;

    @Autowired
    private IWmOutsourceRecptLineService wmOutsourceRecptLineService;

    @Autowired
    private IStorageCoreService storageCoreService;

    @Autowired
    private IProWorkorderService proWorkorderService;

    @Autowired
    private IProTaskService proTaskService;
    @Resource
    private WmWarehouseUtil warehouseUtil;

    @Autowired
    private AutoCodeUtil autoCodeUtil;

    /**
     * 查询外协入库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmOutsourceRecpt wmOutsourceRecpt) {
        startPage();
        List<WmOutsourceRecpt> list = wmOutsourceRecptService.selectWmOutsourceRecptList(wmOutsourceRecpt);
        return getDataTable(list);
    }

    /**
     * 导出外协入库单列表
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:export')")
    @Log(title = "外协入库单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmOutsourceRecpt wmOutsourceRecpt) {
        List<WmOutsourceRecpt> list = wmOutsourceRecptService.selectWmOutsourceRecptList(wmOutsourceRecpt);
        ExcelUtil<WmOutsourceRecpt> util = new ExcelUtil<WmOutsourceRecpt>(WmOutsourceRecpt.class);
        util.exportExcel(response, list, "外协入库单数据");
    }

    /**
     * 获取外协入库单详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:query')")
    @GetMapping(value = "/{recptId}")
    public AjaxResult getInfo(@PathVariable("recptId") Long recptId) {
        return AjaxResult.success(wmOutsourceRecptService.selectWmOutsourceRecptByRecptId(recptId));
    }

    /**
     * 新增外协入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:add')")
    @Log(title = "外协入库单", businessType = BusinessType.INSERT)
    @Transactional
    @PostMapping
    public AjaxResult add(@RequestBody WmOutsourceRecpt wmOutsourceRecpt) {
//        if (UserConstants.NOT_UNIQUE.equals(wmOutsourceRecptService.c(wmOutsourceRecpt))) {
//            return AjaxResult.error(MsgConstants.CODE_ALREADY_EXISTS);
//        }
        warehouseUtil.setWarehouseInfo(wmOutsourceRecpt);
        autoCodeUtil.saveSerialCode("OUTSOURCE_RECPT_CODE", null);
        return toAjax(wmOutsourceRecptService.insertWmOutsourceRecpt(wmOutsourceRecpt));
    }

    /**
     * 修改外协入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:edit')")
    @Log(title = "外协入库单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WmOutsourceRecpt wmOutsourceRecpt) {
        warehouseUtil.setWarehouseInfo(wmOutsourceRecpt);
        return toAjax(wmOutsourceRecptService.updateWmOutsourceRecpt(wmOutsourceRecpt));
    }

    /**
     * 删除外协入库单
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:remove')")
    @Log(title = "外协入库单", businessType = BusinessType.DELETE)
    @Transactional
    @DeleteMapping("/{recptIds}")
    public AjaxResult remove(@PathVariable Long[] recptIds) {
        if (recptIds.length == 0) {
            return AjaxResult.error(MsgConstants.PARAM_ERROR);
        }
        for (Long recordId : recptIds) {
            WmOutsourceRecpt wor = wmOutsourceRecptService.selectWmOutsourceRecptByRecptId(recordId);
            if(UserConstants.ORDER_STATUS_FINISHED.equals(wor.getStatus()))
            {
                return AjaxResult.error("存在归档数据！无法删除");
            }
        }
        for (Long recptId : recptIds) {
            wmOutsourceRecptLineService.deleteWmOutsourceRecptLineByRecptId(recptId);
        }
        return toAjax(wmOutsourceRecptService.deleteWmOutsourceRecptByRecptIds(recptIds));
    }

    /**
     * 执行入库
     *
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:wm:outsourcerecpt:edit')")
    @Log(title = "外协入库单", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/{recptId}")
    public AjaxResult execute(@PathVariable Long recptId) {

        WmOutsourceRecpt recpt = wmOutsourceRecptService.selectWmOutsourceRecptByRecptId(recptId);

        List<WmOutsourceRecptLine> lines = wmOutsourceRecptLineService.selectWmOutsourceRecptLineByRecptId(recptId);
        if (CollectionUtils.isEmpty(lines)) {
            return AjaxResult.error("请指定入库的物资！");
        }

        // 构造Transaction事务，并执行库存更新逻辑
        List<OutsourceRecptTxBean> beans = wmOutsourceRecptService.getTxBeans(recptId);

        // 调用库存核心
        storageCoreService.processOutsourceRecpt(beans);

        // 根据当前入库的物料更新对应的生产工单/生产任务 已生产数量
        // 下面代码存在问题，直接更新工单的生产数，肯定是错误的。因为对应的任务没找到。
        //
        ProWorkorder workorder = proWorkorderService.selectProWorkorderByWorkorderId(recpt.getWorkorderId());
        if (!StringUtils.isNotNull(workorder)) {
            return AjaxResult.error("未找到对应的外协工单/外协任务！");
        }
        ProTask proTask = proTaskService.selectProTaskByTaskId(Long.valueOf(recpt.getAttr2()));
        if (StringUtils.isNotNull(proTask)) {
        // 正常外协入库的产品必须先经过检验，确认合格数量后才能执行入库，并且更新外协工单的进度。此处暂时先直接根据入库数量更新外协工单的生产数量。
        BigDecimal produced = proTask.getQuantityProduced() == null ? new BigDecimal(0) : proTask.getQuantityProduced();
        for (int i = 0; i < lines.size(); i++) {
            WmOutsourceRecptLine line = lines.get(i);
            // 判断入库的物资，如果是生产工单中的产品，则更新已生产数量
            if (line.getItemCode().equals(proTask.getItemCode())) {
                //更新已生产数和符合产品质量数。
//                proTask.setQuantityProduced(produced.add(line.getQuantityRecived()));
//                proTask.setQuantityQuanlify(produced.add(line.getQuantityRecived()));
                produced = produced.add(line.getQuantityRecived());
//                proTask.setStatus();
            }
        }
        proTask.setQuantityProduced(produced);
        proTask.setQuantityQuanlify(produced);
        if(proTask.getStatus().equals(UserConstants.ORDER_STATUS_PREPARE)) {
            proTask.setStatus(UserConstants.ORDER_STATUS_CONFIRMED);
        }
        proTaskService.updateProTask(proTask);
        }
        // 更新单据状态
        recpt.setStatus(UserConstants.ORDER_STATUS_FINISHED);
        wmOutsourceRecptService.updateWmOutsourceRecpt(recpt);
        return AjaxResult.success();
    }

}
