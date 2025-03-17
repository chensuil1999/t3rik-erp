package com.t3rik.mes.wm.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.core.domain.AjaxResult;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.wm.domain.WmItemRecpt;
import com.t3rik.mes.wm.domain.tx.ItemRecptTxBean;
import com.t3rik.mes.wm.domain.tx.RtVendorTxBean;
import com.t3rik.mes.wm.mapper.WmItemRecptMapper;
import com.t3rik.mes.wm.service.IStorageCoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.t3rik.mes.wm.mapper.WmRtVendorMapper;
import com.t3rik.mes.wm.domain.WmRtVendor;
import com.t3rik.mes.wm.service.IWmRtVendorService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 供应商退货Service业务层处理
 * 
 * @author yinjinlu
 * @date 2022-06-13
 */
@Service
public class WmRtVendorServiceImpl extends ServiceImpl<WmRtVendorMapper, WmRtVendor> implements IWmRtVendorService
{
    @Autowired
    private WmRtVendorMapper wmRtVendorMapper;

    @Autowired
    private IStorageCoreService storageCoreService;
    /**
     * 查询供应商退货
     * 
     * @param rtId 供应商退货主键
     * @return 供应商退货
     */
    @Override
    public WmRtVendor selectWmRtVendorByRtId(Long rtId)
    {
        return wmRtVendorMapper.selectWmRtVendorByRtId(rtId);
    }

    /**
     * 查询供应商退货列表
     * 
     * @param wmRtVendor 供应商退货
     * @return 供应商退货
     */
    @Override
    public List<WmRtVendor> selectWmRtVendorList(WmRtVendor wmRtVendor)
    {
        return wmRtVendorMapper.selectWmRtVendorList(wmRtVendor);
    }

    @Override
    public String checkCodeUnique(WmRtVendor wmRtVendor) {
        WmRtVendor rt = wmRtVendorMapper.checkCodeUnique(wmRtVendor);
        Long rtId = wmRtVendor.getRtId() ==null?-1L:wmRtVendor.getRtId();
        if(StringUtils.isNotNull(rt) && rt.getRtId().longValue() != rtId.longValue()){
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增供应商退货
     * 
     * @param wmRtVendor 供应商退货
     * @return 结果
     */
    @Override
    public int insertWmRtVendor(WmRtVendor wmRtVendor)
    {
        wmRtVendor.setCreateTime(DateUtils.getNowDate());
        wmRtVendor.setCreateBy(getUsername());
        return wmRtVendorMapper.insertWmRtVendor(wmRtVendor);
    }

    /**
     * 修改供应商退货
     * 
     * @param wmRtVendor 供应商退货
     * @return 结果
     */
    @Override
    public int updateWmRtVendor(WmRtVendor wmRtVendor)
    {
        wmRtVendor.setUpdateTime(DateUtils.getNowDate());
        wmRtVendor.setUpdateBy(getUsername());
        return wmRtVendorMapper.updateWmRtVendor(wmRtVendor);
    }

    /**
     * 批量删除供应商退货
     * 
     * @param rtIds 需要删除的供应商退货主键
     * @return 结果
     */
    @Override
    public int deleteWmRtVendorByRtIds(Long[] rtIds)
    {
        return wmRtVendorMapper.deleteWmRtVendorByRtIds(rtIds);
    }

    /**
     * 删除供应商退货信息
     * 
     * @param rtId 供应商退货主键
     * @return 结果
     */
    @Override
    public int deleteWmRtVendorByRtId(Long rtId)
    {
        return wmRtVendorMapper.deleteWmRtVendorByRtId(rtId);
    }

    @Override
    public List<RtVendorTxBean> getTxBeans(Long rtId) {
        return wmRtVendorMapper.getTxBeans(rtId);
    }

    @Transactional
    @Override
    public void execute(Long rtId) {
        //构造事务Bean
        List<RtVendorTxBean> beans = this.getTxBeans(rtId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        //调用库存核心
        storageCoreService.processRtVendor(beans);
        //更新单据状态
        WmRtVendor rtVendor = this.selectWmRtVendorByRtId(rtId);
        rtVendor.setStatus(OrderStatusEnum.FINISHED.getCode());
        this.updateWmRtVendor(rtVendor);
    }

    @Transactional
    @Override
    public void reverseexecute(Long rtId) {
        // 构造Transaction事务，并执行库存更新逻辑
        List<RtVendorTxBean> beans = this.getTxBeans(rtId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        for(RtVendorTxBean irbs : beans) {
            irbs.setAttr4(-irbs.getAttr4());
            irbs.setTransactionQuantity(irbs.getTransactionQuantity().multiply(BigDecimal.valueOf(-1)));
        }
        // 调用库存核心
        storageCoreService.processRtVendor(beans);
        //更新单据状态
        WmRtVendor rtVendor = this.selectWmRtVendorByRtId(rtId);
        rtVendor.setStatus(OrderStatusEnum.REVERSAL.getCode());
        this.updateWmRtVendor(rtVendor);
    }
}
