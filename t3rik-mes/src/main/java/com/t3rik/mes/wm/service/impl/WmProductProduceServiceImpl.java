package com.t3rik.mes.wm.service.impl;

import java.util.Date;
import java.util.List;

import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.mes.md.domain.MdWorkstation;
import com.t3rik.mes.md.mapper.MdWorkstationMapper;
import com.t3rik.mes.pro.domain.ProFeedback;
import com.t3rik.mes.pro.domain.ProProcess;
import com.t3rik.mes.pro.domain.ProTask;
import com.t3rik.mes.pro.domain.ProWorkorder;
import com.t3rik.mes.pro.mapper.ProProcessMapper;
import com.t3rik.mes.pro.mapper.ProTaskMapper;
import com.t3rik.mes.pro.mapper.ProWorkorderMapper;
import com.t3rik.mes.wm.domain.WmProductProduceLine;
import com.t3rik.mes.wm.domain.tx.ProductProductTxBean;
import com.t3rik.mes.wm.mapper.WmProductProduceLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.t3rik.mes.wm.mapper.WmProductProduceMapper;
import com.t3rik.mes.wm.domain.WmProductProduce;
import com.t3rik.mes.wm.service.IWmProductProduceService;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 产品产出记录Service业务层处理
 * 
 * @author yinjinlu
 * @date 2022-09-21
 */
@Service
public class WmProductProduceServiceImpl implements IWmProductProduceService 
{
    @Autowired
    private WmProductProduceMapper wmProductProduceMapper;

    @Autowired
    private WmProductProduceLineMapper wmProductProduceLineMapper;

    @Autowired
    private ProWorkorderMapper proWorkorderMapper;

    @Autowired
    private ProTaskMapper proTaskMapper;

    @Autowired
    private MdWorkstationMapper mdWorkstationMapper;

    @Autowired
    private ProProcessMapper proProcessMapper;

    /**
     * 查询产品产出记录
     * 
     * @param recordId 产品产出记录主键
     * @return 产品产出记录
     */
    @Override
    public WmProductProduce selectWmProductProduceByRecordId(Long recordId)
    {
        return wmProductProduceMapper.selectWmProductProduceByRecordId(recordId);
    }

    /**
     * 查询产品产出记录列表
     * 
     * @param wmProductProduce 产品产出记录
     * @return 产品产出记录
     */
    @Override
    public List<WmProductProduce> selectWmProductProduceList(WmProductProduce wmProductProduce)
    {
        return wmProductProduceMapper.selectWmProductProduceList(wmProductProduce);
    }

    @Override
    public List<WmProductProduce> selectWmProductProduceByFeedbackCode(String feedbackCode) {
        return wmProductProduceMapper.selectWmProductProduceByFeedbackCode(feedbackCode);
    }

    /**
     * 新增产品产出记录
     * 
     * @param wmProductProduce 产品产出记录
     * @return 结果
     */
    @Override
    public int insertWmProductProduce(WmProductProduce wmProductProduce)
    {
        wmProductProduce.setCreateTime(DateUtils.getNowDate());
        return wmProductProduceMapper.insertWmProductProduce(wmProductProduce);
    }

    /**
     * 修改产品产出记录
     * 
     * @param wmProductProduce 产品产出记录
     * @return 结果
     */
    @Override
    public int updateWmProductProduce(WmProductProduce wmProductProduce)
    {
        wmProductProduce.setUpdateTime(DateUtils.getNowDate());
        return wmProductProduceMapper.updateWmProductProduce(wmProductProduce);
    }

    /**
     * 批量删除产品产出记录
     * 
     * @param recordIds 需要删除的产品产出记录主键
     * @return 结果
     */
    @Override
    public int deleteWmProductProduceByRecordIds(Long[] recordIds)
    {
        return wmProductProduceMapper.deleteWmProductProduceByRecordIds(recordIds);
    }

    /**
     * 删除产品产出记录信息
     * 
     * @param recordId 产品产出记录主键
     * @return 结果
     */
    @Override
    public int deleteWmProductProduceByRecordId(Long recordId)
    {
        return wmProductProduceMapper.deleteWmProductProduceByRecordId(recordId);
    }

    @Override
    public int deleteWmProductProduceByFeedbackCode(Long recordId) {
        return 0;
    }

    /**
     * 根据报工单生成
     * @param feedback
     * @return
     */
    @Override
    public WmProductProduce generateProductProduce(ProFeedback feedback) {
        ProWorkorder workorder = proWorkorderMapper.selectProWorkorderByWorkorderId(feedback.getWorkorderId());
        MdWorkstation workstation = mdWorkstationMapper.selectMdWorkstationByWorkstationId(feedback.getWorkstationId());
        ProProcess process = proProcessMapper.selectProProcessByProcessId(workstation.getProcessId());
        ProTask task = proTaskMapper.selectProTaskByTaskId(feedback.getTaskId());
        //生成单据头信息
        WmProductProduce productProduce = new WmProductProduce();
        productProduce.setWorkorderId(feedback.getWorkorderId());
        productProduce.setWorkorderCode(feedback.getWorkorderCode());
        productProduce.setWorkorderName(feedback.getWorkorderName());

        productProduce.setTaskId(feedback.getTaskId());
        productProduce.setTaskCode(task.getTaskCode());
        productProduce.setTaskName(task.getTaskName());

        productProduce.setWorkstationId(feedback.getWorkstationId());
        productProduce.setWorkstationCode(workstation.getWorkstationCode());
        productProduce.setWorkstationName(workstation.getWorkstationName());

        productProduce.setProcessId(process.getProcessId());
        productProduce.setProcessCode(process.getProcessCode());
        productProduce.setProcessName(process.getProcessName());
        productProduce.setProduceDate(new Date());
        productProduce.setAttr1(feedback.getFeedbackCode());
        productProduce.setCreateTime(DateUtils.getNowDate());
        productProduce.setCreateBy(getUsername());
        productProduce.setStatus(UserConstants.ORDER_STATUS_PREPARE);
        wmProductProduceMapper.insertWmProductProduce(productProduce);

        //生成单据行信息; 以后如果是在生产过程中产生多种副产品可以在这里添加更多的行信息进行支持
        WmProductProduceLine line = new WmProductProduceLine();
        line.setRecordId(productProduce.getRecordId());
        line.setItemId(feedback.getItemId());
        line.setItemCode(feedback.getItemCode());
        line.setItemName(feedback.getItemName());
        line.setSpecification(feedback.getSpecification());
        line.setUnitOfMeasure(feedback.getUnitOfMeasure());
        line.setQuantityProduce(feedback.getQuantityFeedback());
        line.setCreateTime(DateUtils.getNowDate());
        line.setCreateBy(getUsername());
        line.setAttr1(feedback.getFeedbackCode());
        line.setAttr2(feedback.getAttr2());
        line.setAttr3(feedback.getAttr3());
        line.setAttr4(feedback.getAttr4());
        line.setMaincnt(feedback.getMaincnt());
        line.setSeccnt(feedback.getSeccnt());
        line.setBatchCode(workorder.getBatchCode());

        wmProductProduceLineMapper.insertWmProductProduceLine(line);
        return productProduce;
    }

    @Override
    public List<ProductProductTxBean> getTxBeans(Long recordId) {
        return wmProductProduceMapper.getTxBeans(recordId);
    }


}
