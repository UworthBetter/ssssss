package com.qkyd.system.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.qkyd.common.annotation.DataScope;
import com.qkyd.common.constant.UserConstants;
import com.qkyd.common.core.domain.entity.SysRole;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.exception.ServiceException;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.spring.SpringUtils;
import com.qkyd.system.domain.SysRoleDept;
import com.qkyd.system.domain.SysRoleMenu;
import com.qkyd.system.domain.SysUserRole;
import com.qkyd.system.mapper.SysRoleDeptMapper;
import com.qkyd.system.mapper.SysRoleMapper;
import com.qkyd.system.mapper.SysRoleMenuMapper;
import com.qkyd.system.mapper.SysUserRoleMapper;
import com.qkyd.system.service.ISysRoleService;

/**
 * 閻熸瑦甯熸竟?濞戞挻鑹炬慨鐔轰沪閸屾凹妲遍柣?
 * 
 * @author qkyd
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService
{
    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 闁哄秷顫夊畵渚€寮堕垾鍙夘偨闁告帒妫濋妴澶愬蓟閵夘煈鍤勯悷娆愬笩婢瑰﹪寮悧鍫濈ウ
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 閻熸瑦甯熸竟濠囧极閻楀牆绁﹂梻鍡楁閹孩绌遍埄鍐х礀
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysRole> selectRoleList(SysRole role)
    {
        return roleMapper.selectRoleList(role);
    }

    /**
     * 闁哄秷顫夊畵渚€鎮介妸锕€鐓旾D闁哄被鍎撮妤冩喆閹烘洖顥?
     * 
     * @param userId 闁活潿鍔嶉崺姹璂
     * @return 閻熸瑦甯熸竟濠囧礆濡ゅ嫨鈧?
     */
    @Override
    public List<SysRole> selectRolesByUserId(Long userId)
    {
        List<SysRole> userRoles = roleMapper.selectRolePermissionByUserId(userId);
        List<SysRole> roles = selectRoleAll();
        for (SysRole role : roles)
        {
            for (SysRole userRole : userRoles)
            {
                if (role.getRoleId().longValue() == userRole.getRoleId().longValue())
                {
                    role.setFlag(true);
                    break;
                }
            }
        }
        return roles;
    }

    /**
     * 闁哄秷顫夊畵渚€鎮介妸锕€鐓旾D闁哄被鍎撮妤呭级閸愵喗顎?
     * 
     * @param userId 闁活潿鍔嶉崺姹璂
     * @return 闁哄鍟村娲礆濡ゅ嫨鈧?
     */
    @Override
    public Set<String> selectRolePermissionByUserId(Long userId)
    {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms)
        {
            if (StringUtils.isNotNull(perm))
            {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 闁哄被鍎撮妤呭箥閳ь剟寮垫径搴健闁?
     * 
     * @return 閻熸瑦甯熸竟濠囧礆濡ゅ嫨鈧?
     */
    @Override
    public List<SysRole> selectRoleAll()
    {
        return SpringUtils.getAopProxy(this).selectRoleList(new SysRole());
    }

    /**
     * 闁哄秷顫夊畵渚€鎮介妸锕€鐓旾D闁兼儳鍢茶ぐ鍥╂喆閹烘洖顥忛梺顐㈩槹鐎氥劌顩奸崱妤€鐏欓悶?
     * 
     * @param userId 闁活潿鍔嶉崺姹璂
     * @return 闂侇偄顦懙鎴犳喆閹烘洖顥廔D闁告帗顨夐妴?
     */
    @Override
    public List<Long> selectRoleListByUserId(Long userId)
    {
        return roleMapper.selectRoleListByUserId(userId);
    }

    /**
     * 闂侇偅淇虹换鍐喆閹烘洖顥廔D闁哄被鍎撮妤冩喆閹烘洖顥?
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @return 閻熸瑦甯熸竟濠勨偓鐢殿攰閽栧嫭绌遍埄鍐х礀
     */
    @Override
    public SysRole selectRoleById(Long roleId)
    {
        return roleMapper.selectRoleById(roleId);
    }

    /**
     * 闁哄稄绻濋悰娆戞喆閹烘洖顥忛柛姘Ф琚ㄩ柡鍕靛灠閹線宕娆戭伇
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleNameUnique(role.getRoleName());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 闁哄稄绻濋悰娆戞喆閹烘洖顥忛柡澶婂暣濡炬椽寮伴姘剨闁哥儐鍨粩?
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role)
    {
        Long roleId = StringUtils.isNull(role.getRoleId()) ? -1L : role.getRoleId();
        SysRole info = roleMapper.checkRoleKeyUnique(role.getRoleKey());
        if (StringUtils.isNotNull(info) && info.getRoleId().longValue() != roleId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 闁哄稄绻濋悰娆戞喆閹烘洖顥忛柡鍕靛灠閹線宕楁担绛嬪晠闁瑰灝绉崇紞?
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     */
    @Override
    public void checkRoleAllowed(SysRole role)
    {
        if (StringUtils.isNotNull(role.getRoleId()) && role.isAdmin())
        {
            throw new ServiceException("涓嶅厑璁告搷浣滆秴绾х鐞嗗憳瑙掕壊");
        }
    }

    /**
     * 闁哄稄绻濋悰娆戞喆閹烘洖顥忛柡鍕靛灠閹線寮垫径瀣闁硅鍠楀鍫ユ⒔?
     * 
     * @param roleId 閻熸瑦甯熸竟濂縟
     */
    @Override
    public void checkRoleDataScope(Long roleId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysRole role = new SysRole();
            role.setRoleId(roleId);
            List<SysRole> roles = SpringUtils.getAopProxy(this).selectRoleList(role);
            if (StringUtils.isEmpty(roles))
            {
                throw new ServiceException("没有权限访问角色数据");
            }
        }
    }

    /**
     * 闂侇偅淇虹换鍐喆閹烘洖顥廔D闁哄被鍎撮妤冩喆閹烘洖顥忓ù锝堟硶閺併倝寮导鏉戞
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int countUserRoleByRoleId(Long roleId)
    {
        return userRoleMapper.countUserRoleByRoleId(roleId);
    }

    /**
     * 闁哄倹婢橀·鍐╃┍濠靛棛鎽犻悷娆愬笩婢瑰﹥绌遍埄鍐х礀
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    @Transactional
    public int insertRole(SysRole role)
    {
        // 闁哄倹婢橀·鍐喆閹烘洖顥忓ǎ鍥ｅ墲娴?
        roleMapper.insertRole(role);
        return insertRoleMenu(role);
    }

    /**
     * 濞ｅ浂鍠楅弫鍏肩┍濠靛棛鎽犻悷娆愬笩婢瑰﹥绌遍埄鍐х礀
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    @Transactional
    public int updateRole(SysRole role)
    {
        // 濞ｅ浂鍠楅弫鑲╂喆閹烘洖顥忓ǎ鍥ｅ墲娴?
        roleMapper.updateRole(role);
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸姘﹁ぐ宥夊础閺囩偛褰犻柤?
        roleMenuMapper.deleteRoleMenuByRoleId(role.getRoleId());
        return insertRoleMenu(role);
    }

    /**
     * 濞ｅ浂鍠楅弫鑲╂喆閹烘洖顥忛柣妯垮煐閳?
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int updateRoleStatus(SysRole role)
    {
        return roleMapper.updateRole(role);
    }

    /**
     * 濞ｅ浂鍠楅弫濂稿极閻楀牆绁﹂柡澶婂暣濡剧儤绌遍埄鍐х礀
     * 
     * @param role 閻熸瑦甯熸竟濠冪┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    @Transactional
    public int authDataScope(SysRole role)
    {
        // 濞ｅ浂鍠楅弫鑲╂喆閹烘洖顥忓ǎ鍥ｅ墲娴?
        roleMapper.updateRole(role);
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸閰ｉ崕鎾⒒閵娿儱褰犻柤?
        roleDeptMapper.deleteRoleDeptByRoleId(role.getRoleId());
        // 闁哄倹婢橀·鍐喆閹烘洖顥忛柛婊冪焸閸庢挳姊婚妸銈勭箚闁诡収鍨界槐娆撳极閻楀牆绁﹂柡澶婂暣濡炬椽鏁?
        return insertRoleDept(role);
    }

    /**
     * 闁哄倹婢橀·鍐喆閹烘洖顥忛柤鎸庣矊瀹曠喐绌遍埄鍐х礀
     * 
     * @param role 閻熸瑦甯熸竟濠勨偓鐢殿攰閽?
     */
    public int insertRoleMenu(SysRole role)
    {
        int rows = 1;
        // 闁哄倹婢橀·鍐偨閵婏箑鐓曞☉鎾虫唉椤鎳濋懠顒夊悁闁?
        List<SysRoleMenu> list = new ArrayList<SysRoleMenu>();
        for (Long menuId : role.getMenuIds())
        {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(role.getRoleId());
            rm.setMenuId(menuId);
            list.add(rm);
        }
        if (list.size() > 0)
        {
            rows = roleMenuMapper.batchRoleMenu(list);
        }
        return rows;
    }

    /**
     * 闁哄倹婢橀·鍐喆閹烘洖顥忛梺顔哄姂濡剚绌遍埄鍐х礀(闁轰胶澧楀畵渚€寮堕崘顔筋€?
     *
     * @param role 閻熸瑦甯熸竟濠勨偓鐢殿攰閽?
     */
    public int insertRoleDept(SysRole role)
    {
        int rows = 1;
        // 闁哄倹婢橀·鍐喆閹烘洖顥忓☉鎾抽叄閸庢挳姊婚…鎺旂闁轰胶澧楀畵渚€寮堕崘顔筋€欓柨娑橆槺椤撴悂鎮?
        List<SysRoleDept> list = new ArrayList<SysRoleDept>();
        for (Long deptId : role.getDeptIds())
        {
            SysRoleDept rd = new SysRoleDept();
            rd.setRoleId(role.getRoleId());
            rd.setDeptId(deptId);
            list.add(rd);
        }
        if (list.size() > 0)
        {
            rows = roleDeptMapper.batchRoleDept(list);
        }
        return rows;
    }

    /**
     * 闂侇偅淇虹换鍐喆閹烘洖顥廔D闁告帞濞€濞呭海鎲撮幒鏇烆棌
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @return 缂備焦鎸婚悘?
     */
    @Override
    @Transactional
    public int deleteRoleById(Long roleId)
    {
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸姘﹁ぐ宥夊础閺囩偛褰犻柤?
        roleMenuMapper.deleteRoleMenuByRoleId(roleId);
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸閰ｉ崕鎾⒒閵娿儱褰犻柤?
        roleDeptMapper.deleteRoleDeptByRoleId(roleId);
        return roleMapper.deleteRoleById(roleId);
    }

    /**
     * 闁归潧缍婇崳娲礆閻樼粯鐝熼悷娆愬笩婢瑰﹥绌遍埄鍐х礀
     * 
     * @param roleIds 闂傚洠鍋撻悷鏇氱閸ㄥ綊姊介妶鍥ㄧ暠閻熸瑦甯熸竟濂岲
     * @return 缂備焦鎸婚悘?
     */
    @Override
    @Transactional
    public int deleteRoleByIds(Long[] roleIds)
    {
        for (Long roleId : roleIds)
        {
            checkRoleAllowed(new SysRole(roleId));
            checkRoleDataScope(roleId);
            SysRole role = selectRoleById(roleId);
            if (countUserRoleByRoleId(roleId) > 0)
            {
                throw new ServiceException(String.format("%1$s鐎瑰憡褰冮崹搴ㄦ煀?濞戞挸绉烽崗姗€宕氶悩缁樼彑", role.getRoleName()));
            }
        }
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸姘﹁ぐ宥夊础閺囩偛褰犻柤?
        roleMenuMapper.deleteRoleMenu(roleIds);
        // 闁告帞濞€濞呭海鎲撮幒鏇烆棌濞戞挸閰ｉ崕鎾⒒閵娿儱褰犻柤?
        roleDeptMapper.deleteRoleDept(roleIds);
        return roleMapper.deleteRoleByIds(roleIds);
    }

    /**
     * 闁告瑦鐗楃粔鐑藉箳閸喐缍€闁活潿鍔嶉崺娑氭喆閹烘洖顥?
     * 
     * @param userRole 闁活潿鍔嶉崺娑㈠椽瀹€鍐炬健闁艰褰冮崣褔鎳曢弬鍙ョ箚闁?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole)
    {
        return userRoleMapper.deleteUserRoleInfo(userRole);
    }

    /**
     * 闁归潧缍婇崳娲矗閺嶃劎啸闁瑰搫鐗婂鍫ユ偨閵婏箑鐓曢悷娆愬笩婢?
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @param userIds 闂傚洠鍋撻悷鏇氱瑜板洤鈽夐崼鐔锋埧闁哄鍟板▓鎴︽偨閵婏箑鐓曢柡浣哄瀹撲浮D
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds)
    {
        return userRoleMapper.deleteUserRoleInfos(roleId, userIds);
    }

    /**
     * 闁归潧缍婇崳娲焻婢跺顏ラ柟鍝勭墛濞煎牓鎮介妸锕€鐓曢悷娆愬笩婢?
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @param userIds 闂傚洠鍋撻悷鏇氱劍瀹稿潡寮堕崘顏呯暠闁活潿鍔嶉崺娑㈠极閻楀牆绁D
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds)
    {
        // 闁哄倹婢橀·鍐偨閵婏箑鐓曞☉鎾虫唉椤鎳濋懠顒夊悁闁?
        List<SysUserRole> list = new ArrayList<SysUserRole>();
        for (Long userId : userIds)
        {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            list.add(ur);
        }
        return userRoleMapper.batchUserRole(list);
    }
}


