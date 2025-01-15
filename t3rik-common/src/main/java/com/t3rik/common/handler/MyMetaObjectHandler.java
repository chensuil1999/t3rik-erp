package com.t3rik.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.t3rik.common.utils.DateUtils;
import com.t3rik.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * mp自动填充
 *
 * @author t3rik
 * @date 2024/4/22 16:42
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 创建人
     */
    public static final String CREATE_USER = "createBy";
    /**
     * 修改人
     */
    public static final String UPDATE_USER = "updateBy";
    /**
     * 创建时间
     */
    public static final String CREATE_TIME = "createTime";
    /**
     * 修改时间
     */
    public static final String UPDATE_TIME = "updateTime";

    /**
     * 创建人id
     */
    public static final String CREATE_USER_ID = "createUserId";

    /**
     * 修改人id
     */
    public static final String UPDATE_USER_ID = "updateUserId";

    /**
     * 新增时执行
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 判断是否未登录,如果未登录,直接返回
        if (SecurityUtils.getAuthentication() == null) {
            //System.out.println("没有登录");
            return;
        }
        Date now = DateUtils.getNowDate();
        Long currentUserId = SecurityUtils.getLoginUser().getUserId();
        String currentUsername = SecurityUtils.getLoginUser().getUsername();
        // 创建人id
        if (ifHasField(metaObject, CREATE_USER_ID)) {
            this.setFieldValByName(CREATE_USER_ID, currentUserId, metaObject);
            //System.out.println("oooooo: " + this.getFieldValByName(CREATE_USER_ID, metaObject));
        }
        // 创建人姓名
        if (ifHasField(metaObject, CREATE_USER)) {
//            metaObject.setValue(CREATE_USER, currentUsername);
            this.setFieldValByName(CREATE_USER, currentUsername, metaObject);
            System.out.println("oooooo: " + this.getFieldValByName(CREATE_USER, metaObject));
        }
        // 创建时间
        if (ifHasField(metaObject, CREATE_TIME)) {
            this.setFieldValByName(CREATE_TIME, now, metaObject);
//            metaObject.setValue(CREATE_TIME, now);
        }
        // 修改人id
        if (ifHasField(metaObject, UPDATE_USER_ID)) {
            this.setFieldValByName(UPDATE_USER_ID, currentUserId, metaObject);
//            metaObject.setValue(UPDATE_USER_ID, currentUserId);
        }
        // 修改人姓名
        if (ifHasField(metaObject, UPDATE_USER)) {
            this.setFieldValByName(UPDATE_USER, currentUsername, metaObject);
//            metaObject.setValue(UPDATE_USER, currentUsername);
            //System.out.println("oooooo: " + UPDATE_USER);
        }
        // 修改时间
        if (ifHasField(metaObject, UPDATE_TIME)) {
            this.setFieldValByName(UPDATE_TIME, now, metaObject);
//            metaObject.setValue(UPDATE_TIME, now);
        }
    }

    /**
     * 修改时执行
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 判断是否未登录,如果未登录,直接返回
        if (SecurityUtils.getAuthentication() == null) {
            //System.out.println("没有登录");
            return;
        }
        String currentUsername = SecurityUtils.getLoginUser().getUsername();
        // 修改人id
        if (ifHasField(metaObject, UPDATE_USER_ID)) {
            this.setFieldValByName(UPDATE_USER_ID, SecurityUtils.getLoginUser().getUserId(), metaObject);
//            metaObject.setValue(UPDATE_USER_ID, SecurityUtils.getLoginUser().getUserId());
        }
        // 修改人姓名
        if (ifHasField(metaObject, UPDATE_USER)) {
            this.setFieldValByName(UPDATE_USER, currentUsername, metaObject);
//            metaObject.setValue(UPDATE_USER, currentUsername);
//            System.out.println("oooooo: " + this.getFieldValByName(UPDATE_USER, metaObject));
        }
        // 修改时间
        if (ifHasField(metaObject, UPDATE_TIME)) {
            this.setFieldValByName(UPDATE_TIME, DateUtils.getNowDate(), metaObject);
//            metaObject.setValue(UPDATE_TIME, new Date());
        }
    }

    /**
     * 校验是否存在字段
     *
     * @param metaObject metaObject
     * @param filedName  字段名
     * @return
     */
    private boolean ifHasField(MetaObject metaObject, String filedName) {
        if (metaObject.hasGetter(filedName)) {
            //System.out.println("iiiiii: " + filedName);
            return true;
        } else if (metaObject.hasGetter("et." + filedName)) {
            //System.out.println("kkkkkk: " + filedName);
            return true;
        }
        return false;
    }
}
