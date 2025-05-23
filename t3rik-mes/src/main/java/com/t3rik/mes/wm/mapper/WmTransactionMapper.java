package com.t3rik.mes.wm.mapper;

import java.util.List;
import com.t3rik.mes.wm.domain.WmTransaction;
import org.apache.ibatis.annotations.Param;
/**
 * 库存事务Mapper接口
 * 
 * @author yinjinlu
 * @date 2022-05-24
 */
public interface WmTransactionMapper 
{
    /**
     * 查询库存事务
     * 
     * @param transactionId 库存事务主键
     * @return 库存事务
     */
    public WmTransaction selectWmTransactionByTransactionId(Long transactionId);

    /**
     * 查询库存事务列表
     * 
     * @param wmTransaction 库存事务
     * @return 库存事务集合
     */
    public List<WmTransaction> selectWmTransactionList(WmTransaction wmTransaction);

    /**
     * 新增库存事务
     * 
     * @param wmTransaction 库存事务
     * @return 结果
     */
    public int insertWmTransaction(WmTransaction wmTransaction);

    /**
     * 修改库存事务
     * 
     * @param wmTransaction 库存事务
     * @return 结果
     */
    public int updateWmTransaction(WmTransaction wmTransaction);

    /**
     * 删除库存事务
     * 
     * @param transactionId 库存事务主键
     * @return 结果
     */
    public int deleteWmTransactionByTransactionId(Long transactionId);

    /**
     * 删除库存事务
     *
     * @param  库存事务主键
     * @return 结果
     */
    public int deleteWmTransactionByTypeAndSourceId(@Param("sourceDocId") Long sourceDocId,
                                                    @Param("sourceDocType") String type  );

    /**
     * 批量删除库存事务
     * 
     * @param transactionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWmTransactionByTransactionIds(Long[] transactionIds);
}
