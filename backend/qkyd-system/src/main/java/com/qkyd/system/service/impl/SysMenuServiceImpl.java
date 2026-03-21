package com.qkyd.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.constant.UserConstants;
import com.qkyd.common.core.domain.TreeSelect;
import com.qkyd.common.core.domain.entity.SysMenu;
import com.qkyd.common.core.domain.entity.SysRole;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.system.domain.vo.MetaVo;
import com.qkyd.system.domain.vo.RouterVo;
import com.qkyd.system.mapper.SysMenuMapper;
import com.qkyd.system.mapper.SysRoleMapper;
import com.qkyd.system.mapper.SysRoleMenuMapper;
import com.qkyd.system.service.ISysMenuService;

/**
 * 鑿滃崟 涓氬姟灞傚鐞?
 * 
 * @author qkyd
 */
@Service
public class SysMenuServiceImpl implements ISysMenuService
{
    public static final String PREMISSION_STRING = "perms[\"{0}\"]";

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    /**
     * 鏍规嵁鐢ㄦ埛鏌ヨ绯荤粺鑿滃崟鍒楄〃
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鑿滃崟鍒楄〃
     */
    @Override
    public List<SysMenu> selectMenuList(Long userId)
    {
        return selectMenuList(new SysMenu(), userId);
    }

    /**
     * 鏌ヨ绯荤粺鑿滃崟鍒楄〃
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 鑿滃崟鍒楄〃
     */
    @Override
    public List<SysMenu> selectMenuList(SysMenu menu, Long userId)
    {
        List<SysMenu> menuList = null;
        // 绠＄悊鍛樻樉绀烘墍鏈夎彍鍗曚俊鎭?
        if (SysUser.isAdmin(userId))
        {
            menuList = menuMapper.selectMenuList(menu);
        }
        else
        {
            menu.getParams().put("userId", userId);
            menuList = menuMapper.selectMenuListByUserId(menu);
        }
        return menuList;
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鏉冮檺
     * 
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺鍒楄〃
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId)
    {
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 鏍规嵁瑙掕壊ID鏌ヨ鏉冮檺
     * 
     * @param roleId 瑙掕壊ID
     * @return 鏉冮檺鍒楄〃
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(Long roleId)
    {
        List<String> perms = menuMapper.selectMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms)
        {
            if (StringUtils.isNotEmpty(perm))
            {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鑿滃崟
     * 
     * @param userId 鐢ㄦ埛鍚嶇О
     * @return 鑿滃崟鍒楄〃
     */
    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId)
    {
        List<SysMenu> menus = null;
        if (SecurityUtils.isAdmin(userId))
        {
            menus = menuMapper.selectMenuTreeAll();
        }
        else
        {
            menus = menuMapper.selectMenuTreeByUserId(userId);
        }
        return getChildPerms(menus, 0);
    }

    /**
     * 鏍规嵁瑙掕壊ID鏌ヨ鑿滃崟鏍戜俊鎭?
     * 
     * @param roleId 瑙掕壊ID
     * @return 閫変腑鑿滃崟鍒楄〃
     */
    @Override
    public List<Long> selectMenuListByRoleId(Long roleId)
    {
        SysRole role = roleMapper.selectRoleById(roleId);
        return menuMapper.selectMenuListByRoleId(roleId, role.isMenuCheckStrictly());
    }

    /**
     * 鏋勫缓鍓嶇璺敱鎵€闇€瑕佺殑鑿滃崟
     * 
     * @param menus 鑿滃崟鍒楄〃
     * @return 璺敱鍒楄〃
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus)
    {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenu menu : menus)
        {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
            List<SysMenu> cMenus = menu.getChildren();
            if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType()))
            {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            }
            else if (isMenuFrame(menu))
            {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(StringUtils.capitalize(menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            else if (menu.getParentId().intValue() == 0 && isInnerLink(menu))
            {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(StringUtils.capitalize(routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 鏋勫缓鍓嶇鎵€闇€瑕佹爲缁撴瀯
     * 
     * @param menus 鑿滃崟鍒楄〃
     * @return 鏍戠粨鏋勫垪琛?
     */
    @Override
    public List<SysMenu> buildMenuTree(List<SysMenu> menus)
    {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        List<Long> tempList = menus.stream().map(SysMenu::getMenuId).collect(Collectors.toList());
        for (Iterator<SysMenu> iterator = menus.iterator(); iterator.hasNext();)
        {
            SysMenu menu = (SysMenu) iterator.next();
            // 濡傛灉鏄《绾ц妭鐐? 閬嶅巻璇ョ埗鑺傜偣鐨勬墍鏈夊瓙鑺傜偣
            if (!tempList.contains(menu.getParentId()))
            {
                recursionFn(menus, menu);
                returnList.add(menu);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = menus;
        }
        return returnList;
    }

    /**
     * 鏋勫缓鍓嶇鎵€闇€瑕佷笅鎷夋爲缁撴瀯
     * 
     * @param menus 鑿滃崟鍒楄〃
     * @return 涓嬫媺鏍戠粨鏋勫垪琛?
     */
    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus)
    {
        List<SysMenu> menuTrees = buildMenuTree(menus);
        return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 鏍规嵁鑿滃崟ID鏌ヨ淇℃伅
     * 
     * @param menuId 鑿滃崟ID
     * @return 鑿滃崟淇℃伅
     */
    @Override
    public SysMenu selectMenuById(Long menuId)
    {
        return menuMapper.selectMenuById(menuId);
    }

    /**
     * 鏄惁瀛樺湪鑿滃崟瀛愯妭鐐?
     * 
     * @param menuId 鑿滃崟ID
     * @return 缁撴灉
     */
    @Override
    public boolean hasChildByMenuId(Long menuId)
    {
        int result = menuMapper.hasChildByMenuId(menuId);
        return result > 0;
    }

    /**
     * 鏌ヨ鑿滃崟浣跨敤鏁伴噺
     * 
     * @param menuId 鑿滃崟ID
     * @return 缁撴灉
     */
    @Override
    public boolean checkMenuExistRole(Long menuId)
    {
        int result = roleMenuMapper.checkMenuExistRole(menuId);
        return result > 0;
    }

    /**
     * 鏂板淇濆瓨鑿滃崟淇℃伅
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    @Override
    public int insertMenu(SysMenu menu)
    {
        return menuMapper.insertMenu(menu);
    }

    /**
     * 淇敼淇濆瓨鑿滃崟淇℃伅
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    @Override
    public int updateMenu(SysMenu menu)
    {
        return menuMapper.updateMenu(menu);
    }

    /**
     * 鍒犻櫎鑿滃崟绠＄悊淇℃伅
     * 
     * @param menuId 鑿滃崟ID
     * @return 缁撴灉
     */
    @Override
    public int deleteMenuById(Long menuId)
    {
        return menuMapper.deleteMenuById(menuId);
    }

    /**
     * 鏍￠獙鑿滃崟鍚嶇О鏄惁鍞竴
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    @Override
    public boolean checkMenuNameUnique(SysMenu menu)
    {
        Long menuId = StringUtils.isNull(menu.getMenuId()) ? -1L : menu.getMenuId();
        SysMenu info = menuMapper.checkMenuNameUnique(menu.getMenuName(), menu.getParentId());
        if (StringUtils.isNotNull(info) && info.getMenuId().longValue() != menuId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 鑾峰彇璺敱鍚嶇О
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 璺敱鍚嶇О
     */
    public String getRouteName(SysMenu menu)
    {
        String routerName = StringUtils.capitalize(menu.getPath());
        // 闈炲閾惧苟涓旀槸涓€绾х洰褰曪紙绫诲瀷涓虹洰褰曪級
        if (isMenuFrame(menu))
        {
            routerName = StringUtils.EMPTY;
        }
        return routerName;
    }

    /**
     * 鑾峰彇璺敱鍦板潃
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 璺敱鍦板潃
     */
    public String getRouterPath(SysMenu menu)
    {
        String routerPath = menu.getPath();
        // 鍐呴摼鎵撳紑澶栫綉鏂瑰紡
        if (menu.getParentId().intValue() != 0 && isInnerLink(menu))
        {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 闈炲閾惧苟涓旀槸涓€绾х洰褰曪紙绫诲瀷涓虹洰褰曪級
        if (0 == menu.getParentId().intValue() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame()))
        {
            routerPath = "/" + menu.getPath();
        }
        // 闈炲閾惧苟涓旀槸涓€绾х洰褰曪紙绫诲瀷涓鸿彍鍗曪級
        else if (isMenuFrame(menu))
        {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 鑾峰彇缁勪欢淇℃伅
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁勪欢淇℃伅
     */
    public String getComponent(SysMenu menu)
    {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu))
        {
            component = menu.getComponent();
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0 && isInnerLink(menu))
        {
            component = UserConstants.INNER_LINK;
        }
        else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu))
        {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }

    /**
     * 鏄惁涓鸿彍鍗曞唴閮ㄨ烦杞?
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    public boolean isMenuFrame(SysMenu menu)
    {
        return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 鏄惁涓哄唴閾剧粍浠?
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    public boolean isInnerLink(SysMenu menu)
    {
        return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.getPath());
    }

    /**
     * 鏄惁涓簆arent_view缁勪欢
     * 
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    public boolean isParentView(SysMenu menu)
    {
        return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 鏍规嵁鐖惰妭鐐圭殑ID鑾峰彇鎵€鏈夊瓙鑺傜偣
     * 
     * @param list 鍒嗙被琛?
     * @param parentId 浼犲叆鐨勭埗鑺傜偣ID
     * @return String
     */
    public List<SysMenu> getChildPerms(List<SysMenu> list, int parentId)
    {
        List<SysMenu> returnList = new ArrayList<SysMenu>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext();)
        {
            SysMenu t = (SysMenu) iterator.next();
            // 涓€銆佹牴鎹紶鍏ョ殑鏌愪釜鐖惰妭鐐笽D,閬嶅巻璇ョ埗鑺傜偣鐨勬墍鏈夊瓙鑺傜偣
            if (t.getParentId() == parentId)
            {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 閫掑綊鍒楄〃
     * 
     * @param list 鍒嗙被琛?
     * @param t 瀛愯妭鐐?
     */
    private void recursionFn(List<SysMenu> list, SysMenu t)
    {
        // 寰楀埌瀛愯妭鐐瑰垪琛?
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 寰楀埌瀛愯妭鐐瑰垪琛?
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t)
    {
        List<SysMenu> tlist = new ArrayList<SysMenu>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext())
        {
            SysMenu n = (SysMenu) it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 鍒ゆ柇鏄惁鏈夊瓙鑺傜偣
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t)
    {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 鍐呴摼鍩熷悕鐗规畩瀛楃鏇挎崲
     * 
     * @return 鏇挎崲鍚庣殑鍐呴摼鍩熷悕
     */
    public String innerLinkReplaceEach(String path)
    {
        return StringUtils.replaceEach(path, new String[] { Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":" },
                new String[] { "", "", "", "/", "/" });
    }
}


