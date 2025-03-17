package com.t3rik.mes.wm.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.wm.domain.WmProductSalse;
import com.t3rik.mes.wm.domain.tx.ProductSalseTxBean;
import com.t3rik.mes.wm.domain.tx.RtSalseTxBean;
import com.t3rik.mes.wm.service.IStorageCoreService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.t3rik.mes.wm.mapper.WmRtSalseMapper;
import com.t3rik.mes.wm.domain.WmRtSalse;
import com.t3rik.mes.wm.service.IWmRtSalseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 产品销售退货单Service业务层处理
 * 
 * @author yinjinlu
 * @date 2022-10-06
 */
@Service
public class WmRtSalseServiceImpl implements IWmRtSalseService 
{
    @Autowired
    private WmRtSalseMapper wmRtSalseMapper;

    @Resource
    private IStorageCoreService storageCoreService;


    /**
     * 查询产品销售退货单
     * 
     * @param rtId 产品销售退货单主键
     * @return 产品销售退货单
     */
    @Override
    public WmRtSalse selectWmRtSalseByRtId(Long rtId)
    {
        return wmRtSalseMapper.selectWmRtSalseByRtId(rtId);
    }

    /**
     * 查询产品销售退货单列表
     * 
     * @param wmRtSalse 产品销售退货单
     * @return 产品销售退货单
     */
    @Override
    public List<WmRtSalse> selectWmRtSalseList(WmRtSalse wmRtSalse)
    {
        return wmRtSalseMapper.selectWmRtSalseList(wmRtSalse);
    }

    @Override
    public List<RtSalseTxBean> getTxBeans(Long rtId) {
        return wmRtSalseMapper.getTxBeans(rtId);
    }

    @Override
    public String checkUnique(WmRtSalse wmRtSalse) {
        WmRtSalse salse = wmRtSalseMapper.checkUnique(wmRtSalse);
        Long rtId = wmRtSalse.getRtId() == null? -1L: wmRtSalse.getRtId();
        if(StringUtils.isNotNull(salse) && rtId.longValue() != salse.getRtId().longValue()){
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增产品销售退货单
     * 
     * @param wmRtSalse 产品销售退货单
     * @return 结果
     */
    @Override
    public int insertWmRtSalse(WmRtSalse wmRtSalse)
    {
        wmRtSalse.setCreateTime(DateUtils.getNowDate());
        wmRtSalse.setCreateBy(getUsername());
        return wmRtSalseMapper.insertWmRtSalse(wmRtSalse);
    }

    /**
     * 修改产品销售退货单
     * 
     * @param wmRtSalse 产品销售退货单
     * @return 结果
     */
    @Override
    public int updateWmRtSalse(WmRtSalse wmRtSalse)
    {
        wmRtSalse.setUpdateTime(DateUtils.getNowDate());
//        wmRtSalse.setCreateTime(DateUtils.getNowDate());
        wmRtSalse.setUpdateBy(getUsername());
        return wmRtSalseMapper.updateWmRtSalse(wmRtSalse);
    }

    /**
     * 批量删除产品销售退货单
     * 
     * @param rtIds 需要删除的产品销售退货单主键
     * @return 结果
     */
    @Override
    public int deleteWmRtSalseByRtIds(Long[] rtIds)
    {
        return wmRtSalseMapper.deleteWmRtSalseByRtIds(rtIds);
    }

    /**
     * 删除产品销售退货单信息
     * 
     * @param rtId 产品销售退货单主键
     * @return 结果
     */
    @Override
    public int deleteWmRtSalseByRtId(Long rtId)
    {
        return wmRtSalseMapper.deleteWmRtSalseByRtId(rtId);
    }

    @Transactional
    @Override
    public void reverseexecute(Long rtId) {
        // 构造Transaction事务，并执行库存更新逻辑bean.
        List<RtSalseTxBean> beans = this.getTxBeans(rtId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        for(RtSalseTxBean irbs : beans) {
            irbs.setAttr4(-irbs.getAttr4());
            irbs.setTransactionQuantity(irbs.getTransactionQuantity().multiply(BigDecimal.valueOf(-1)));
        }
        // 调用库存核心
        storageCoreService.processRtSalse(beans);
        WmRtSalse wmRtSalse = this.selectWmRtSalseByRtId(rtId);
        wmRtSalse.setStatus(OrderStatusEnum.REVERSAL.getCode());
        this.updateWmRtSalse(wmRtSalse);
    }

    @Transactional
    @Override
    public void execute(Long rtId) {
        List<RtSalseTxBean> beans = this.getTxBeans(rtId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        storageCoreService.processRtSalse(beans);
        WmRtSalse wmRtSalse = this.selectWmRtSalseByRtId(rtId);
        wmRtSalse.setStatus(OrderStatusEnum.FINISHED.getCode());
        this.updateWmRtSalse(wmRtSalse);

    }
}
