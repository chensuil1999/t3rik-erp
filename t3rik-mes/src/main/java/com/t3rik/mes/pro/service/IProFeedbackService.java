package com.t3rik.mes.pro.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.t3rik.mes.pro.domain.ProFeedback;
import com.t3rik.mes.pro.domain.ProTask;

import java.util.List;

/**
 * 生产报工记录Service接口
 *
 * @author yinjinlu
 * @date 2022-07-10
 */
public interface IProFeedbackService extends IService<ProFeedback> {
    /**
     * 查询生产报工记录
     *
     * @param recordId 生产报工记录主键
     * @return 生产报工记录
     */
    public ProFeedback selectProFeedbackByRecordId(Long recordId);

    /**
     * 查询生产报工记录列表
     *
     * @param proFeedback 生产报工记录
     * @return 生产报工记录集合
     */
    public List<ProFeedback> selectProFeedbackList(ProFeedback proFeedback);

    /**
     * 新增生产报工记录
     *
     * @param proFeedback 生产报工记录
     * @return 结果
     */
    public int insertProFeedback(ProFeedback proFeedback);

    /**
     * 修改生产报工记录
     *
     * @param proFeedback 生产报工记录
     * @return 结果
     */
    public int updateProFeedback(ProFeedback proFeedback);

    /**
     * 批量删除生产报工记录
     *
     * @param recordIds 需要删除的生产报工记录主键集合
     * @return 结果
     */
    public int deleteProFeedbackByRecordIds(Long[] recordIds);

    /**
     * 删除生产报工记录信息
     *
     * @param recordId 生产报工记录主键
     * @return 结果
     */
    public int deleteProFeedbackByRecordId(Long recordId);


    /**
     * 完成报工
     */
    void executeFeedback(ProFeedback feedback, ProTask task);

    /**
     * 报工冲销
     * @param feedback
     * @param task
     */
    void reverseFeedback(ProFeedback feedback, ProTask task);
}
