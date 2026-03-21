package com.qkyd.framework.aspectj;

import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.qkyd.common.annotation.DataScope;
import com.qkyd.common.core.domain.BaseEntity;
import com.qkyd.common.core.domain.entity.SysRole;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.core.domain.model.LoginUser;
import com.qkyd.common.core.text.Convert;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.framework.security.context.PermissionContextHolder;

/**
 * 鏁版嵁杩囨护澶勭悊
 *
 * @author qkyd
 */
@Aspect
@Component
public class DataScopeAspect
{
    /**
     * 鍏ㄩ儴鏁版嵁鏉冮檺
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 鑷畾鏁版嵁鏉冮檺
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 閮ㄩ棬鏁版嵁鏉冮檺
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 閮ㄩ棬鍙婁互涓嬫暟鎹潈闄?
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 浠呮湰浜烘暟鎹潈闄?
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 鏁版嵁鏉冮檺杩囨护鍏抽敭瀛?
     */
    public static final String DATA_SCOPE = "dataScope";

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope) throws Throwable
    {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope)
    {
        // 鑾峰彇褰撳墠鐨勭敤鎴?
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNotNull(loginUser))
        {
            SysUser currentUser = loginUser.getUser();
            // 濡傛灉鏄秴绾х鐞嗗憳锛屽垯涓嶈繃婊ゆ暟鎹?
            if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin())
            {
                String permission = StringUtils.defaultIfEmpty(controllerDataScope.permission(), PermissionContextHolder.getContext());
                dataScopeFilter(joinPoint, currentUser, controllerDataScope.deptAlias(),
                        controllerDataScope.userAlias(), permission);
            }
        }
    }

    /**
     * 鏁版嵁鑼冨洿杩囨护
     *
     * @param joinPoint 鍒囩偣
     * @param user 鐢ㄦ埛
     * @param deptAlias 閮ㄩ棬鍒悕
     * @param userAlias 鐢ㄦ埛鍒悕
     * @param permission 鏉冮檺瀛楃
     */
    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias, String permission)
    {
        StringBuilder sqlString = new StringBuilder();
        List<String> conditions = new ArrayList<String>();

        for (SysRole role : user.getRoles())
        {
            String dataScope = role.getDataScope();
            if (!DATA_SCOPE_CUSTOM.equals(dataScope) && conditions.contains(dataScope))
            {
                continue;
            }
            if (StringUtils.isNotEmpty(permission) && StringUtils.isNotEmpty(role.getPermissions())
                    && !StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                continue;
            }
            if (DATA_SCOPE_ALL.equals(dataScope))
            {
                sqlString = new StringBuilder();
                conditions.add(dataScope);
                break;
            }
            else if (DATA_SCOPE_CUSTOM.equals(dataScope))
            {
                sqlString.append(StringUtils.format(
                        " OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias,
                        role.getRoleId()));
            }
            else if (DATA_SCOPE_DEPT.equals(dataScope))
            {
                sqlString.append(StringUtils.format(" OR {}.dept_id = {} ", deptAlias, user.getDeptId()));
            }
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
            {
                sqlString.append(StringUtils.format(
                        " OR {}.dept_id IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )",
                        deptAlias, user.getDeptId(), user.getDeptId()));
            }
            else if (DATA_SCOPE_SELF.equals(dataScope))
            {
                if (StringUtils.isNotBlank(userAlias))
                {
                    sqlString.append(StringUtils.format(" OR {}.user_id = {} ", userAlias, user.getUserId()));
                }
                else
                {
                    // 鏁版嵁鏉冮檺涓轰粎鏈汉涓旀病鏈塽serAlias鍒悕涓嶆煡璇换浣曟暟鎹?
                    sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
                }
            }
            conditions.add(dataScope);
        }

        // 澶氳鑹叉儏鍐典笅锛屾墍鏈夎鑹查兘涓嶅寘鍚紶閫掕繃鏉ョ殑鏉冮檺瀛楃锛岃繖涓椂鍊檚qlString涔熶細涓虹┖锛屾墍浠ヨ闄愬埗涓€涓?涓嶆煡璇换浣曟暟鎹?
        if (StringUtils.isEmpty(conditions))
        {
            sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
        }

        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                baseEntity.getParams().put(DATA_SCOPE, " AND (" + sqlString.substring(4) + ")");
            }
        }
    }

    /**
     * 鎷兼帴鏉冮檺sql鍓嶅厛娓呯┖params.dataScope鍙傛暟闃叉娉ㄥ叆
     */
    private void clearDataScope(final JoinPoint joinPoint)
    {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE, "");
        }
    }
}


