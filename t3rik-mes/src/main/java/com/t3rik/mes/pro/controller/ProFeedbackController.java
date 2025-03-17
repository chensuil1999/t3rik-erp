package com.t3rik.mes.pro.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
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
import com.t3rik.mes.md.domain.MdWorkstation;
import com.t3rik.mes.md.service.IMdWorkstationService;
import com.t3rik.mes.pro.domain.ProFeedback;
import com.t3rik.mes.pro.domain.ProRouteProcess;
import com.t3rik.mes.pro.domain.ProTask;
import com.t3rik.mes.pro.service.IProFeedbackService;
import com.t3rik.mes.pro.service.IProRouteProcessService;
import com.t3rik.mes.pro.service.IProTaskService;
import com.t3rik.system.strategy.AutoCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 生产报工记录Controller
 *
 * @author yinjinlu
 * @date 2022-07-10
 */
@RestController
@RequestMapping("/mes/pro/feedback")
public class ProFeedbackController extends BaseController {
    @Autowired
    private IProFeedbackService proFeedbackService;
    @Autowired
    private IMdWorkstationService mdWorkstationService;
    @Autowired
    private IProTaskService proTaskService;
    @Autowired
    private IProRouteProcessService proRouteProcessService;
    @Autowired
    private AutoCodeUtil autoCodeUtil;

    /**
     * 查询生产报工记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ProFeedback proFeedback) {
        startPage();
        List<ProFeedback> list = proFeedbackService.selectProFeedbackList(proFeedback);
        return getDataTable(list);
    }

    /**
     * 导出生产报工记录列表
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:export')")
    @Log(title = "生产报工记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProFeedback proFeedback) {
        List<ProFeedback> list = proFeedbackService.selectProFeedbackList(proFeedback);
        ExcelUtil<ProFeedback> util = new ExcelUtil<>(ProFeedback.class);
        util.exportExcel(response, list, "生产报工记录数据");
    }

    /**
     * 获取生产报工记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId) {
        return AjaxResult.success(proFeedbackService.selectProFeedbackByRecordId(recordId));
    }

    /**
     * 新增生产报工记录
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:add')")
    @Log(title = "生产报工记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProFeedback proFeedback) {
        ProTask task = this.proTaskService.getById(proFeedback.getTaskId());
        if (StringUtils.isNull(task)) {
            return AjaxResult.error("当前报工对应的生产任务不存在！");
        }
        MdWorkstation workstation = mdWorkstationService.selectMdWorkstationByWorkstationId(proFeedback.getWorkstationId());
        if (StringUtils.isNull(workstation)) {
            return AjaxResult.error("当前生产任务对应的工作站不存在！");
        }
        proFeedback.setProcessId(workstation.getProcessId());
        proFeedback.setProcessCode(workstation.getProcessCode());
        proFeedback.setProcessName(workstation.getProcessName());
        // 检查对应的工艺路线和工序配置
        if (StringUtils.isNull(proFeedback.getRouteId()) && StringUtils.isNull(proFeedback.getProcessId())) {
            return AjaxResult.error("当前生产任务对应的工艺工序配置无效！");
        }
        ProRouteProcess param = new ProRouteProcess();
        param.setRouteId(proFeedback.getRouteId());
        param.setProcessId(proFeedback.getProcessId());
        List<ProRouteProcess> processes = proRouteProcessService.selectProRouteProcessList(param);
        if (CollectionUtil.isEmpty(processes)) {
            return AjaxResult.error("未找到生产任务对应的工艺工序配置！");
        }

        // 检查数量
        if (UserConstants.YES.equals(proFeedback.getIsCheck())) {
            if (StringUtils.isNull(proFeedback.getQuantityUncheck())) {
                return AjaxResult.error("请输入待检测数量!");
            }
        } else {
            if (StringUtils.isNull(proFeedback.getQuantityQualified()) || StringUtils.isNull(proFeedback.getQuantityUnquanlified())) {
                return AjaxResult.error("请输入合格品和不良品数量！");
            }
        }
        String feedbackCode = autoCodeUtil.genSerialCode(UserConstants.FEEDBACK_CODE, "");
        proFeedback.setFeedbackCode(feedbackCode);
//        proFeedback.setCreateBy(getUsername());
//        System.out.println("我的新增" + proFeedback);
        proFeedback.setQuantity(task.getQuantity());
        proFeedbackService.insertProFeedback(proFeedback);
        return AjaxResult.success(proFeedback.getRecordId());
    }

    /**
     * 修改生产报工记录
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:edit')")
    @Log(title = "生产报工记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProFeedback proFeedback) {
        MdWorkstation workstation = mdWorkstationService.selectMdWorkstationByWorkstationId(proFeedback.getWorkstationId());
        if (StringUtils.isNotNull(workstation)) {
            proFeedback.setProcessId(workstation.getProcessId());
            proFeedback.setProcessCode(workstation.getProcessCode());
            proFeedback.setProcessName(workstation.getProcessName());
        } else {
            return AjaxResult.error("当前生产任务对应的工作站不存在！");
        }

        // 检查对应的工艺路线和工序配置
        if (StringUtils.isNotNull(proFeedback.getRouteId()) && StringUtils.isNotNull(proFeedback.getProcessId())) {
            ProRouteProcess param = new ProRouteProcess();
            param.setRouteId(proFeedback.getRouteId());
            param.setProcessId(proFeedback.getProcessId());
            List<ProRouteProcess> processes = proRouteProcessService.selectProRouteProcessList(param);
            if (CollectionUtil.isEmpty(processes)) {
                return AjaxResult.error("未找到生产任务对应的工艺工序配置！");
            }
        } else {
            return AjaxResult.error("当前生产任务对应的工艺工序配置无效！");
        }

        // 检查数量
        if (UserConstants.YES.equals(proFeedback.getIsCheck())) {
            if (!StringUtils.isNotNull(proFeedback.getQuantityUncheck())) {
                return AjaxResult.error("当前工作站报工需要经过质检确认，请输入待检测数量!");
            }
        } else {
            if (!StringUtils.isNotNull(proFeedback.getQuantityQualified()) || !StringUtils.isNotNull(proFeedback.getQuantityUnquanlified())) {
                return AjaxResult.error("请输入合格品和不良品数量！");
            }
        }
        return toAjax(proFeedbackService.updateProFeedback(proFeedback));
    }

    /**
     * 删除生产报工记录
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:remove')")
    @Log(title = "生产报工记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) {
        Long feedbackCount = proFeedbackService.lambdaQuery().eq(ProFeedback::getStatus, OrderStatusEnum.FINISHED.getCode())
                .in(ProFeedback::getRecordId, recordIds).count();
        // 退料数量
        if(feedbackCount> 0) {
            return AjaxResult.error("该记录已完成存档，无法删除!");
            //throw new BusinessException("该记录已完成存档，不能删除!");
        }
        return toAjax(proFeedbackService.deleteProFeedbackByRecordIds(recordIds));
    }

    /**
     * 生产报工审批拒绝
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:edit')")
    @Log(title = "生产报工审批拒绝", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/refuse/{recordId}")
    public AjaxResult refuse(@PathVariable("recordId") Long recordId) {
        if (StringUtils.isNull(recordId)) {
            return AjaxResult.error("请先保存单据");
        }
        ProFeedback feedback = proFeedbackService.selectProFeedbackByRecordId(recordId);
        // 不存在报工记录，抛异常
        Assert.notNull(feedback, () -> new BusinessException(MsgConstants.PARAM_ERROR));
        ProTask task = proTaskService.selectProTaskByTaskId(feedback.getTaskId());
        // 判断当前生产任务的状态，如果已经完成则不能再报工
        if (OrderStatusEnum.FINISHED.getCode().equals(task.getStatus())) {
            return AjaxResult.error("当前生产工单的状态为已完成，不能继续报工，请刷新生产任务列表！");
        }
        // 审批拒绝
        this.proFeedbackService.lambdaUpdate()
                .set(ProFeedback::getStatus, OrderStatusEnum.REFUSE.getCode())
                .eq(ProFeedback::getRecordId, recordId)
                .update();
        return AjaxResult.success();
    }


    /**
     * 执行报工
     * 1.更新生产任务和生产工单的进度
     * 2.物料消耗（旭虹不使用物料消耗,因为无法确定消耗系数）
     * 3.产品产出
     *
     * @param recordId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:edit')")
    @Log(title = "生产报工执行", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/{recordId}/{fdcnt}/{maincnt}/{seccnt}/{js}")
    public AjaxResult execute(@PathVariable("recordId") Long recordId, @PathVariable("fdcnt") Long fdcnt,
                              @PathVariable("maincnt") Integer maincnt, @PathVariable("seccnt") Integer seccnt,
                              @PathVariable("js") Integer js) {

        if (!StringUtils.isNotNull(recordId)) {
            return AjaxResult.error("请先保存单据");
        }
        if (!StringUtils.isNotNull(fdcnt) || fdcnt.compareTo(Long.valueOf(1)) <= 0) {
            return AjaxResult.error("报工总数异常，不能小于1");
        }
        if (!StringUtils.isNotNull(maincnt) || maincnt.compareTo(Integer.valueOf(0)) == 0 ) {
            return AjaxResult.error("报工只数异常，不能为0");
        }
        if (!StringUtils.isNotNull(js) || js.compareTo(Integer.valueOf(0)) == 0 ) {
            return AjaxResult.error("报工件数异常，不能为0");
        }
        ProFeedback feedback = proFeedbackService.selectProFeedbackByRecordId(recordId);
        if(!OrderStatusEnum.APPROVING.getCode().equals(feedback.getStatus())) {
            return AjaxResult.error("只能执行报工审核单据");
        }
        ProTask task = proTaskService.selectProTaskByTaskId(feedback.getTaskId());
        // 判断当前生产任务的状态，如果已经完成则不能再报工
        if (UserConstants.ORDER_STATUS_FINISHED.equals(task.getStatus())) {
            return AjaxResult.error("当前生产工单的状态为已完成，不能继续报工，请刷新生产任务列表！");
        }
        // 仍旧有待检数量时不能执行
        if (StringUtils.isNotNull(feedback.getQuantityUncheck()) && feedback.getQuantityUncheck().compareTo(BigDecimal.ZERO) > 0) {
            return AjaxResult.error("当前报工单未完成检验（待检数量大于0），无法执行报工！");
        }
        feedback.setQuantity(task.getQuantity());
        if (feedback.getQuantityFeedback().compareTo(BigDecimal.valueOf(fdcnt)) != 0
        || feedback.getMaincnt().compareTo(maincnt) != 0
        || feedback.getSeccnt().compareTo(seccnt) != 0
                || feedback.getAttr3().compareTo(js) != 0) {
            //注释的代码是赞成修改
//            feedback.setQuantityFeedback(BigDecimal.valueOf(fdcnt));
//            feedback.setQuantityQualified(BigDecimal.valueOf(fdcnt));
//            feedback.setMaincnt(Integer.valueOf(maincnt));
//            feedback.setSeccnt(Integer.valueOf(seccnt));
//            feedback.setAttr3(Integer.valueOf(js));
////            System.out.println("我改变了: " + feedback);
//            proFeedbackService.updateById(feedback);
            //不使用上述代码了，直接若数据有出入则直接返回
            return AjaxResult.error("当前报工单据与原始单据有出入，无法报工入库！");
        }
        this.proFeedbackService.executeFeedback(feedback, task);
        return AjaxResult.success();
    }

    /**
     * 报工冲销
     * 1.冲销生产任务和生产工单的进度
     * 2.物料消耗（旭虹不使用物料消耗,因为无法确定消耗系数）
     * 3.产品产出冲销
     *
     * @param recordId
     * @return
     */
    @PreAuthorize("@ss.hasPermi('mes:pro:feedback:edit')")
    @Log(title = "生产报工冲销", businessType = BusinessType.UPDATE)
    @Transactional
    @PutMapping("/reverse/{recordId}")
    public AjaxResult delFeedbackReverse(@PathVariable("recordId") Long recordId) {

        if (!StringUtils.isNotNull(recordId)) {
            return AjaxResult.error("冲销单据不存在");
        }
        ProFeedback feedback = proFeedbackService.selectProFeedbackByRecordId(recordId);
        if(!OrderStatusEnum.FINISHED.getCode().equals(feedback.getStatus())) {
            return AjaxResult.error("只能冲销完成单据");
        }
        ProTask task = proTaskService.selectProTaskByTaskId(feedback.getTaskId());
        // 判断当前生产任务的状态，如果已经完成则不能再冲销
        if (UserConstants.ORDER_STATUS_FINISHED.equals(task.getStatus())) {
            return AjaxResult.error("当前生产工单的状态为已完成，不能冲销，请刷新生产任务列表！");
        }
        // 仍旧有待检数量时不能执行
        if (StringUtils.isNotNull(feedback.getQuantityUncheck()) && feedback.getQuantityUncheck().compareTo(BigDecimal.ZERO) > 0) {
            return AjaxResult.error("当前报工单未完成检验（待检数量大于0），无法执行冲销！");
        }
        feedback.setQuantity(task.getQuantity());
        this.proFeedbackService.reverseFeedback(feedback, task);
        return AjaxResult.success();
    }

}
