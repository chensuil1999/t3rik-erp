package com.t3rik.mes.pro.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.pro.domain.ProFeedback;
import com.t3rik.mes.pro.domain.ProTask;
import com.t3rik.mes.pro.domain.ProWorkorder;
import com.t3rik.mes.pro.mapper.ProFeedbackMapper;
import com.t3rik.mes.pro.service.IProFeedbackService;
import com.t3rik.mes.pro.service.IProRouteProcessService;
import com.t3rik.mes.pro.service.IProTaskService;
import com.t3rik.mes.pro.service.IProWorkorderService;
import com.t3rik.mes.wm.domain.WmItemConsume;
import com.t3rik.mes.wm.domain.WmProductProduce;
import com.t3rik.mes.wm.domain.WmRtIssue;
import com.t3rik.mes.wm.domain.WmWasteHeader;
import com.t3rik.mes.wm.domain.tx.ItemConsumeTxBean;
import com.t3rik.mes.wm.domain.tx.ProductProductTxBean;
import com.t3rik.mes.wm.mapper.WmTransactionMapper;
import com.t3rik.mes.wm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.t3rik.common.utils.SecurityUtils.getLoginUser;
import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 生产报工记录Service业务层处理
 *
 * @author yinjinlu
 * @date 2022-07-10
 */
@Service
public class ProFeedbackServiceImpl extends ServiceImpl<ProFeedbackMapper, ProFeedback> implements IProFeedbackService {
    @Autowired
    private ProFeedbackMapper proFeedbackMapper;

    @Autowired
    private WmTransactionMapper wmTransactionMapper;

    @Autowired
    private IWmRtIssueService wmRtIssueService;

    @Autowired
    private IWmWasteHeaderService wmWasteHeaderService;

    @Autowired
    private IProTaskService proTaskService;

    @Autowired
    private IProRouteProcessService proRouteProcessService;

    @Autowired
    private IProWorkorderService proWorkorderService;

    @Autowired
    private IWmItemConsumeService wmItemConsumeService;

    @Autowired
    private IWmProductProduceService wmProductProduceService;

    @Autowired
    private IWmProductProduceLineService wmProductProduceLineService;

    @Autowired
    private IStorageCoreService storageCoreService;

    /**
     * 查询生产报工记录
     *
     * @param recordId 生产报工记录主键
     * @return 生产报工记录
     */
    @Override
    public ProFeedback selectProFeedbackByRecordId(Long recordId) {
        return proFeedbackMapper.selectProFeedbackByRecordId(recordId);
    }

    /**
     * 查询生产报工记录列表
     *
     * @param proFeedback 生产报工记录
     * @return 生产报工记录
     */
    @Override
    public List<ProFeedback> selectProFeedbackList(ProFeedback proFeedback) {
        return proFeedbackMapper.selectProFeedbackList(proFeedback);
    }

    /**
     * 新增生产报工记录
     *
     * @param proFeedback 生产报工记录
     * @return 结果
     */
    @Override
    public int insertProFeedback(ProFeedback proFeedback) {
        //System.out.println(DateUtils.getNowDate());
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        proFeedback.setCreateTime(DateUtils.getNowDate());
        proFeedback.setCreateBy(getUsername());

        return proFeedbackMapper.insertProFeedback(proFeedback);
    }

    /**
     * 修改生产报工记录
     *
     * @param proFeedback 生产报工记录
     * @return 结果
     */
    @Override
    public int updateProFeedback(ProFeedback proFeedback) {
        proFeedback.setUpdateTime(DateUtils.getNowDate());
        proFeedback.setUpdateBy(getUsername());
        return proFeedbackMapper.updateProFeedback(proFeedback);
    }

    /**
     * 批量删除生产报工记录
     *
     * @param recordIds 需要删除的生产报工记录主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteProFeedbackByRecordIds(Long[] recordIds) {
        // 废料数量
        Long wastCount = wmWasteHeaderService.lambdaQuery().in(WmWasteHeader::getRecordId, recordIds).count();
        // 退料数量
        Long rtIssuecount = wmRtIssueService.lambdaQuery().in(WmRtIssue::getRecordId, recordIds).count();
        if (wastCount > 0 || rtIssuecount > 0) {
            throw new BusinessException("该记录已关联废料或退料，不能删除!");
        }
        // 删除退料、废料记录
        wmRtIssueService.deleteWmRtIssueByRtIds(recordIds);
        wmWasteHeaderService.delWmWasteHeaderIds(List.of(recordIds));
        ProFeedback pfb = this.selectProFeedbackByRecordId(recordIds[0]);
        if (pfb.getStatus().equals(OrderStatusEnum.REVERSAL.getCode())) {
            List<WmProductProduce> wppList = wmProductProduceService.selectWmProductProduceByFeedbackCode(pfb.getFeedbackCode());
            for(WmProductProduce wpp : wppList) {
                wmProductProduceService.deleteWmProductProduceByRecordId(wpp.getRecordId());
                wmProductProduceLineService.deleteByRecordId(wpp.getRecordId());
                wmTransactionMapper.deleteWmTransactionByTypeAndSourceId(wpp.getRecordId(),"PRODUCT_PRODUCT");
            }
        }

        return proFeedbackMapper.deleteProFeedbackByRecordIds(recordIds);
    }

    /**
     * 删除生产报工记录信息
     *
     * @param recordId 生产报工记录主键
     * @return 结果
     */
    @Override
    public int deleteProFeedbackByRecordId(Long recordId) {
        return proFeedbackMapper.deleteProFeedbackByRecordId(recordId);
    }

    /**
     * 完成报工
     *
     * @param feedback
     * @param task
     */
    @Transactional
    @Override
    public void executeFeedback(ProFeedback feedback, ProTask task) {
        ProWorkorder workorder = proWorkorderService.selectProWorkorderByWorkorderId(feedback.getWorkorderId());

        // 更新生产任务的生产数量
        BigDecimal quantityProduced, quantityQuanlify, quantityUnquanlify;
        quantityQuanlify = task.getQuantityQuanlify() == null ? new BigDecimal(0) : task.getQuantityQuanlify();
        quantityUnquanlify = task.getQuantityUnquanlify() == null ? new BigDecimal(0) : task.getQuantityUnquanlify();
        quantityProduced = task.getQuantityProduced() == null ? new BigDecimal(0) : task.getQuantityProduced();
        task.setQuantityProduced(quantityProduced.add(feedback.getQuantityFeedback()));
        task.setQuantityQuanlify(quantityQuanlify.add(feedback.getQuantityQualified()));
        task.setQuantityUnquanlify(quantityUnquanlify.add(feedback.getQuantityUnquanlified()));
        if(task.getStatus().equals(UserConstants.ORDER_STATUS_PREPARE)) {
            task.setStatus(UserConstants.ORDER_STATUS_CONFIRMED);
        }
        proTaskService.updateProTask(task);
        // 如果是关键工序，则更新当前工单的已生产数量，进行产品产出动作
        if (proRouteProcessService.checkKeyProcess(feedback)) {
            // 更新生产工单的生产数量
            BigDecimal produced = workorder.getQuantityProduced() == null ? new BigDecimal(0) : workorder.getQuantityProduced();
            BigDecimal feedBackQuantity = feedback.getQuantityFeedback() == null ? new BigDecimal(0) : feedback.getQuantityFeedback();
            workorder.setQuantityProduced(produced.add(feedBackQuantity));
            proWorkorderService.updateProWorkorder(workorder);

            // 生成产品产出记录单
            WmProductProduce productRecord = wmProductProduceService.generateProductProduce(feedback);
            // 执行产品产出入主库（原来是出入线边库，我直接到主仓。因为旭虹并不需要线边库，多一道操作没必要）
            executeProductProduce(productRecord);
        }

        // 根据当前工序的物料BOM配置，进行物料消耗
        // 先生成消耗单。
        //WmItemConsume itemConsume = wmItemConsumeService.generateItemConsume(feedback);
        //if (StringUtils.isNotNull(itemConsume)) {
            // 再执行库存消耗动作
            //executeItemConsume(itemConsume);
        //}
        // 更新报工单的状态
        feedback.setRecordUserId(getLoginUser().getUserId());
        feedback.setRecordUser(getLoginUser().getUsername());
        feedback.setRecordNick(getLoginUser().getUser().getNickName());
        feedback.setStatus(OrderStatusEnum.FINISHED.getCode());
        this.updateProFeedback(feedback);
    }

    /**
     * 报工冲销
     *
     * @param feedback
     * @param task
     */
    @Transactional
    @Override
    public void reverseFeedback(ProFeedback feedback, ProTask task) {
        ProWorkorder workorder = proWorkorderService.selectProWorkorderByWorkorderId(feedback.getWorkorderId());

        // 更新生产任务的生产数量
        BigDecimal quantityProduced, quantityQuanlify, quantityUnquanlify;
        quantityQuanlify = task.getQuantityQuanlify() == null ? new BigDecimal(0) : task.getQuantityQuanlify();
        quantityUnquanlify = task.getQuantityUnquanlify() == null ? new BigDecimal(0) : task.getQuantityUnquanlify();
        quantityProduced = task.getQuantityProduced() == null ? new BigDecimal(0) : task.getQuantityProduced();
        task.setQuantityProduced(quantityProduced.subtract(feedback.getQuantityFeedback()));
        task.setQuantityQuanlify(quantityQuanlify.subtract(feedback.getQuantityQualified()));
        task.setQuantityUnquanlify(quantityUnquanlify.subtract(feedback.getQuantityUnquanlified()));

        proTaskService.updateProTask(task);
        // 如果是关键工序，则更新当前工单的已生产数量，进行产品产出动作
        if (proRouteProcessService.checkKeyProcess(feedback)) {
            // 更新生产工单的生产数量
            BigDecimal produced = workorder.getQuantityProduced() == null ? new BigDecimal(0) : workorder.getQuantityProduced();
            BigDecimal feedBackQuantity = feedback.getQuantityFeedback() == null ? new BigDecimal(0) : feedback.getQuantityFeedback();
            workorder.setQuantityProduced(produced.subtract(feedBackQuantity));
            proWorkorderService.updateProWorkorder(workorder);

            // 生成产品产出记录单
            Integer rawAttr3 = feedback.getAttr3();
            Integer rawSeccnt = feedback.getSeccnt();
            BigDecimal rawQuantity = feedback.getQuantityFeedback();
            feedback.setAttr3(~rawAttr3 + 1);
            feedback.setSeccnt(~rawSeccnt + 1);
            feedback.setQuantityFeedback(rawQuantity.multiply(BigDecimal.valueOf(-1)));
            WmProductProduce productRecord = wmProductProduceService.generateProductProduce(feedback);
            // 执行产品产出入主库（原来是出入线边库，我直接到主仓。因为旭虹并不需要线边库，多一道操作没必要）
            feedback.setAttr3(rawAttr3);
            feedback.setSeccnt(rawSeccnt);
            feedback.setQuantityFeedback(rawQuantity);
            executeProductProduce(productRecord);
        }
        feedback.setRemark("此单据已冲销");
        feedback.setStatus(OrderStatusEnum.REVERSAL.getCode());
        this.updateProFeedback(feedback);
    }


    /**
     * 执行物料消耗库存动作
     *
     * @param record
     */
    private void executeItemConsume(WmItemConsume record) {
        // 需要在此处进行分批次领料的线边库扣减
        List<ItemConsumeTxBean> beans = wmItemConsumeService.getTxBeans(record.getRecordId());
        storageCoreService.processItemConsume(beans);
        record.setStatus(UserConstants.ORDER_STATUS_FINISHED);
        wmItemConsumeService.updateWmItemConsume(record);
    }

    /**
     * 执行产品产出入线边库动作
     *
     * @param record
     */
    private void executeProductProduce(WmProductProduce record) {
        List<ProductProductTxBean> beans = wmProductProduceService.getTxBeans(record.getRecordId());
        storageCoreService.processProductProduce(beans);
        record.setStatus(UserConstants.ORDER_STATUS_FINISHED);
        wmProductProduceService.updateWmProductProduce(record);
    }
}
