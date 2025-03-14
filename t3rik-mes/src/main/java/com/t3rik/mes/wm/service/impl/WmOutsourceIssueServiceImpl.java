package com.t3rik.mes.wm.service.impl;

import java.util.List;

import com.t3rik.common.constant.UserConstants;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.StringUtils;
import com.t3rik.mes.wm.domain.WmIssueHeader;
import com.t3rik.mes.wm.domain.tx.OutsourceIssueTxBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.t3rik.mes.wm.mapper.WmOutsourceIssueMapper;
import com.t3rik.mes.wm.domain.WmOutsourceIssue;
import com.t3rik.mes.wm.service.IWmOutsourceIssueService;

import static com.t3rik.common.utils.SecurityUtils.getUsername;

/**
 * 外协领料单头Service业务层处理
 *WmOutsourceIssueMapper
 * @author yinjinlu
 * @date 2023-10-30
 */
@Service
public class WmOutsourceIssueServiceImpl implements IWmOutsourceIssueService {

    @Autowired
    private WmOutsourceIssueMapper wmOutsourceIssueMapper;

    /**
     * 查询外协领料单头
     *
     * @param issueId 外协领料单头主键
     * @return 外协领料单头
     */
    @Override
    public WmOutsourceIssue selectWmOutsourceIssueByIssueId(Long issueId) {
        return wmOutsourceIssueMapper.selectWmOutsourceIssueByIssueId(issueId);
    }

    /**
     * 查询外协领料单头列表
     *
     * @param wmOutsourceIssue 外协领料单头
     * @return 外协领料单头
     */
    @Override
    public List<WmOutsourceIssue> selectWmOutsourceIssueList(WmOutsourceIssue wmOutsourceIssue) {
        return wmOutsourceIssueMapper.selectWmOutsourceIssueList(wmOutsourceIssue);
    }

    /**
     * 新增外协领料单头
     *
     * @param wmOutsourceIssue 外协领料单头
     * @return 结果
     */
    @Override
    public int insertWmOutsourceIssue(WmOutsourceIssue wmOutsourceIssue) {
        wmOutsourceIssue.setCreateTime(DateUtils.getNowDate());
        wmOutsourceIssue.setCreateBy(getUsername());
        return wmOutsourceIssueMapper.insertWmOutsourceIssue(wmOutsourceIssue);
    }

    /**
     * 修改外协领料单头
     *
     * @param wmOutsourceIssue 外协领料单头
     * @return 结果
     */
    @Override
    public int updateWmOutsourceIssue(WmOutsourceIssue wmOutsourceIssue) {
        wmOutsourceIssue.setUpdateTime(DateUtils.getNowDate());
        wmOutsourceIssue.setUpdateBy(getUsername());
        return wmOutsourceIssueMapper.updateWmOutsourceIssue(wmOutsourceIssue);
    }

    @Override
    public String checkOutsourceIssueCodeUnique(WmOutsourceIssue wmOutsourceIssue) {
        WmOutsourceIssue header = wmOutsourceIssueMapper.checkOutsourceIssueCodeUnique(wmOutsourceIssue);
        Long headerId = wmOutsourceIssue.getIssueId() == null ? -1l : wmOutsourceIssue.getIssueId();
        if (StringUtils.isNotNull(header) && headerId.longValue() != header.getIssueId().longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 批量删除外协领料单头
     *
     * @param issueIds 需要删除的外协领料单头主键
     * @return 结果
     */
    @Override
    public int deleteWmOutsourceIssueByIssueIds(Long[] issueIds) {
        return wmOutsourceIssueMapper.deleteWmOutsourceIssueByIssueIds(issueIds);
    }

    /**
     * 删除外协领料单头信息
     *
     * @param issueId 外协领料单头主键
     * @return 结果
     */
    @Override
    public int deleteWmOutsourceIssueByIssueId(Long issueId) {
        return wmOutsourceIssueMapper.deleteWmOutsourceIssueByIssueId(issueId);
    }

    @Override
    public List<OutsourceIssueTxBean> getTxBeans(Long issueId) {
        return wmOutsourceIssueMapper.getTxBeans(issueId);
    }
}
