package com.qkyd.web.controller.system;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.core.domain.AjaxResult;
import com.qkyd.common.core.domain.entity.SysMenu;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.core.domain.model.LoginBody;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.framework.web.service.SysLoginService;
import com.qkyd.framework.web.service.SysPermissionService;
import com.qkyd.system.service.ISysMenuService;

/**
 * 鐧诲綍楠岃瘉
 * 
 * @author qkyd
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 鐧诲綍鏂规硶
     * 
     * @param loginBody 鐧诲綍淇℃伅
     * @return 缁撴灉
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody)
    {
        AjaxResult ajax = AjaxResult.success();
        // 鐢熸垚浠ょ墝
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 鑾峰彇鐢ㄦ埛淇℃伅
     * 
     * @return 鐢ㄦ埛淇℃伅
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        // 瑙掕壊闆嗗悎
        Set<String> roles = permissionService.getRolePermission(user);
        // 鏉冮檺闆嗗悎
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 鑾峰彇璺敱淇℃伅
     * 
     * @return 璺敱淇℃伅
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}


