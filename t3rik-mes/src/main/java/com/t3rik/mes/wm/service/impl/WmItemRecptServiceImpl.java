package com.t3rik.mes.wm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.enums.mes.OrderStatusEnum;
import com.t3rik.common.exception.BusinessException;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.wm.domain.WmItemRecpt;
import com.t3rik.mes.wm.domain.WmProductSalse;
import com.t3rik.mes.wm.domain.tx.ItemRecptTxBean;
import com.t3rik.mes.wm.mapper.WmItemRecptMapper;
import com.t3rik.mes.wm.service.IStorageCoreService;
import com.t3rik.mes.wm.service.IWmItemRecptService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 物料入库单Service业务层处理
 *
 * @author yinjinlu
 * @date 2022-05-22
 */
@Service
public class WmItemRecptServiceImpl extends ServiceImpl<WmItemRecptMapper, WmItemRecpt> implements IWmItemRecptService {

    @Resource
    private WmItemRecptMapper wmItemRecptMapper;
    @Resource
    private IStorageCoreService storageCoreService;

    /**
     * 查询物料入库单
     *
     * @param recptId 物料入库单主键
     * @return 物料入库单
     */
    @Override
    public WmItemRecpt selectWmItemRecptByRecptId(Long recptId) {
        return wmItemRecptMapper.selectWmItemRecptByRecptId(recptId);
    }

    /**
     * 查询物料入库单列表
     *
     * @param wmItemRecpt 物料入库单
     * @return 物料入库单
     */
    @Override
    public List<WmItemRecpt> selectWmItemRecptList(WmItemRecpt wmItemRecpt) {
        return wmItemRecptMapper.selectWmItemRecptList(wmItemRecpt);
    }

    @Override
    public String checkRecptCodeUnique(WmItemRecpt wmItemRecpt) {
        WmItemRecpt itemRecpt = wmItemRecptMapper.checkRecptCodeUnique(wmItemRecpt);
        Long recptId = wmItemRecpt.getRecptId() == null ? -1L : wmItemRecpt.getRecptId();
        if (StringUtils.isNotNull(itemRecpt) && itemRecpt.getRecptId().longValue() != recptId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 新增物料入库单
     *
     * @param wmItemRecpt 物料入库单
     * @return 结果
     */
    @Override
    public int insertWmItemRecpt(WmItemRecpt wmItemRecpt) {
        wmItemRecpt.setCreateTime(DateUtils.getNowDate());
        wmItemRecpt.setCreateBy(getUsername());
        return wmItemRecptMapper.insertWmItemRecpt(wmItemRecpt);
    }

    /**
     * 修改物料入库单
     *
     * @param wmItemRecpt 物料入库单
     * @return 结果
     */
    @Override
    public int updateWmItemRecpt(WmItemRecpt wmItemRecpt) {
        wmItemRecpt.setUpdateTime(DateUtils.getNowDate());
        wmItemRecpt.setUpdateBy(getUsername());
        return wmItemRecptMapper.updateWmItemRecpt(wmItemRecpt);
    }

    /**
     * 批量删除物料入库单
     *
     * @param recptIds 需要删除的物料入库单主键
     * @return 结果
     */
    @Override
    public int deleteWmItemRecptByRecptIds(Long[] recptIds) {
        return wmItemRecptMapper.deleteWmItemRecptByRecptIds(recptIds);
    }

    /**
     * 删除物料入库单信息
     *
     * @param recptId 物料入库单主键
     * @return 结果
     */
    @Override
    public int deleteWmItemRecptByRecptId(Long recptId) {
        return wmItemRecptMapper.deleteWmItemRecptByRecptId(recptId);
    }

    @Override
    public List<ItemRecptTxBean> getTxBeans(Long receptId) {
        return wmItemRecptMapper.getTxBeans(receptId);
    }

    /**
     * 执行
     *
     * @param recptId
     */
    @Transactional
    @Override
    public void execute(Long recptId) {

        // 构造Transaction事务，并执行库存更新逻辑
        List<ItemRecptTxBean> beans = this.getTxBeans(recptId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        // 调用库存核心
        storageCoreService.processItemRecpt(beans);
        // 更新单据状态
        WmItemRecpt wmItemRecpt = this.selectWmItemRecptByRecptId(recptId);
        wmItemRecpt.setStatus(OrderStatusEnum.FINISHED.getCode());
        this.updateWmItemRecpt(wmItemRecpt);
    }

    @Transactional
    @Override
    public void reverseexecute(Long recptId) {
        // 构造Transaction事务，并执行库存更新逻辑
        List<ItemRecptTxBean> beans = this.getTxBeans(recptId);
        if (CollectionUtils.isEmpty(beans)) {
            throw new BusinessException("没有需要处理的单行");
        }
        for(ItemRecptTxBean irbs : beans) {
            irbs.setAttr4(-irbs.getAttr4());
            irbs.setTransactionQuantity(irbs.getTransactionQuantity().multiply(BigDecimal.valueOf(-1)));
        }
        // 调用库存核心
        storageCoreService.processItemRecpt(beans);
        // 更新单据状态
        WmItemRecpt wmItemRecpt = this.selectWmItemRecptByRecptId(recptId);
        wmItemRecpt.setStatus(OrderStatusEnum.REVERSAL.getCode());
        this.updateWmItemRecpt(wmItemRecpt);
    }
}
