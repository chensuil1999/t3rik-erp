package com.t3rik.mes.wm.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.wm.domain.WmItemRecpt;
import com.t3rik.mes.wm.domain.WmRtVendor;
import com.t3rik.mes.wm.domain.tx.ItemRecptTxBean;
import com.t3rik.mes.wm.domain.tx.ProductSalseTxBean;
import com.t3rik.mes.wm.domain.tx.RtVendorTxBean;
import com.t3rik.mes.wm.service.IStorageCoreService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.t3rik.mes.wm.mapper.WmProductSalseMapper;
import com.t3rik.mes.wm.domain.WmProductSalse;
import com.t3rik.mes.wm.service.IWmProductSalseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 销售出库单Service业务层处理
 * 
 * @author yinjinlu
 * @date 2022-10-04
 */
@Service
public class WmProductSalseServiceImpl implements IWmProductSalseService 
{
    @Autowired
    private WmProductSalseMapper wmProductSalseMapper;

    @Resource
    private IStorageCoreService storageCoreService;

    /**
     * 查询销售出库单
     * 
     * @param salseId 销售出库单主键
     * @return 销售出库单
     */
    @Override
    public WmProductSalse selectWmProductSalseBySalseId(Long salseId)
    {
        return wmProductSalseMapper.selectWmProductSalseBySalseId(salseId);
    }

    /**
     * 查询销售出库单列表
     * 
     * @param wmProductSalse 销售出库单
     * @return 销售出库单
     */
    @Override
    public List<WmProductSalse> selectWmProductSalseList(WmProductSalse wmProductSalse)
    {
        return wmProductSalseMapper.selectWmProductSalseList(wmProductSalse);
    }

    @Override
    public List<ProductSalseTxBean> getTxBeans(Long salseId) {
        return wmProductSalseMapper.getTxBeans(salseId);
    }

    @Override
    public String checkUnique(WmProductSalse wmProductSalse) {
        WmProductSalse salse = wmProductSalseMapper.checkUnique(wmProductSalse);
        Long salseId = wmProductSalse.getSalseId() ==null? -1L:wmProductSalse.getSalseId();
        if(StringUtils.isNotNull(salse) && salseId.longValue() != salse.getSalseId().longValue()){
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增销售出库单
     * 
     * @param wmProductSalse 销售出库单
     * @return 结果
     */
    @Override
    public int insertWmProductSalse(WmProductSalse wmProductSalse)
    {
        wmProductSalse.setCreateTime(DateUtils.getNowDate());
        wmProductSalse.setCreateBy(getUsername());
        return wmProductSalseMapper.insertWmProductSalse(wmProductSalse);
    }

    /**
     * 修改销售出库单
     * 
     * @param wmProductSalse 销售出库单
     * @return 结果
     */
    @Override
    public int updateWmProductSalse(WmProductSalse wmProductSalse)
    {
        wmProductSalse.setUpdateTime(DateUtils.getNowDate());
        wmProductSalse.setUpdateBy(getUsername());
        return wmProductSalseMapper.updateWmProductSalse(wmProductSalse);
    }

    /**
     * 批量删除销售出库单
     * 
     * @param salseIds 需要删除的销售出库单主键
     * @return 结果
     */
    @Override
    public int deleteWmProductSalseBySalseIds(Long[] salseIds)
    {
        return wmProductSalseMapper.deleteWmProductSalseBySalseIds(salseIds);
    }

    /**
     * 删除销售出库单信息
     * 
     * @param salseId 销售出库单主键
     * @return 结果
     */
    @Override
    public int deleteWmProductSalseBySalseId(Long salseId)
    {
        return wmProductSalseMapper.deleteWmProductSalseBySalseId(salseId);
    }

    @Transactional
    @Override
    public void reverseexecute(Long salseId) {
        // 构造Transaction事务，并执行库存更新逻辑bean.
        List<ProductSalseTxBean> beans = this.getTxBeans(salseId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        for(ProductSalseTxBean irbs : beans) {
            irbs.setCountPackage(-irbs.getCountPackage());
            irbs.setTransactionQuantity(irbs.getTransactionQuantity().multiply(BigDecimal.valueOf(-1)));
        }
        // 调用库存核心
        storageCoreService.processProductSalse(beans);
        WmProductSalse wmProductSalse = this.selectWmProductSalseBySalseId(salseId);
        wmProductSalse.setStatus(OrderStatusEnum.REVERSAL.getCode());
        this.updateWmProductSalse(wmProductSalse);
    }

    @Transactional
    @Override
    public void execute(Long salseId) {
        List<ProductSalseTxBean> beans = this.getTxBeans(salseId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        storageCoreService.processProductSalse(beans);
        WmProductSalse wmProductSalse = this.selectWmProductSalseBySalseId(salseId);
        wmProductSalse.setStatus(OrderStatusEnum.FINISHED.getCode());
        this.updateWmProductSalse(wmProductSalse);
    }


}
