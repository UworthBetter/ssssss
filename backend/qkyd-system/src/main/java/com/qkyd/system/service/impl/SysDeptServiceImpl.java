package com.qkyd.system.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.qkyd.common.annotation.DataScope;
import com.qkyd.common.constant.UserConstants;
import com.qkyd.common.core.domain.TreeSelect;
import com.qkyd.common.core.domain.entity.SysDept;
import com.qkyd.common.core.domain.entity.SysRole;
import com.qkyd.common.core.domain.entity.SysUser;
import com.qkyd.common.core.text.Convert;
import com.qkyd.common.exception.ServiceException;
import com.qkyd.common.utils.SecurityUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.spring.SpringUtils;
import com.qkyd.system.mapper.SysDeptMapper;
import com.qkyd.system.mapper.SysRoleMapper;
import com.qkyd.system.service.ISysDeptService;

/**
 * 闂侇喓鍔戝Λ顒傜不閿涘嫭鍊?闁哄牆绉存慨鐔衡偓鍦仧楠?
 * 
 * @author qkyd
 */
@Service
public class SysDeptServiceImpl implements ISysDeptService
{
    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private SysRoleMapper roleMapper;

    /**
     * 闁哄被鍎撮妤呮焾閵娾晜锛岀紒鐙呯磿閹﹪寮悧鍫濈ウ
     * 
     * @param dept 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     * @return 闂侇喓鍔戝Λ顒佺┍閳╁啩绱栭梻鍡楁閹?
     */
    @Override
    @DataScope(deptAlias = "d")
    public List<SysDept> selectDeptList(SysDept dept)
    {
        return deptMapper.selectDeptList(dept);
    }

    /**
     * 闁哄被鍎撮妤呮焾閵娾晜锛岄柡宥嗗灩缁劑寮搁崟顏冪箚闁?
     * 
     * @param dept 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     * @return 闂侇喓鍔戝Λ顒勫冀閹存粈绻嗛柟顓у灦濞夛箓宕?
     */
    @Override
    public List<TreeSelect> selectDeptTreeList(SysDept dept)
    {
        List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
        return buildDeptTreeSelect(depts);
    }

    /**
     * 闁哄瀚紓鎾诲礈瀹ュ浂浼傞柟纰樺亾闂傚洠鍋撻悷鏇氱劍閻栬尙绱掗幘瀵糕偓?
     * 
     * @param depts 闂侇喓鍔戝Λ顒勫礆濡ゅ嫨鈧?
     * @return 闁哄秵鍨圭划銊╁几閸曨偄鐏欓悶?
     */
    @Override
    public List<SysDept> buildDeptTree(List<SysDept> depts)
    {
        List<SysDept> returnList = new ArrayList<SysDept>();
        List<Long> tempList = depts.stream().map(SysDept::getDeptId).collect(Collectors.toList());
        for (SysDept dept : depts)
        {
            // 濠碘€冲€归悘澶愬及椤栫偑鈧﹦鐥婵☆參鎮? 闂侇剙绉村鑽ゆ嫚閵壯冪厬闁煎搫鍊婚崑锝夋儍閸曨剙顣查柡鍫濐槸閻℃瑩鎳為崒婊冧化
            if (!tempList.contains(dept.getParentId()))
            {
                recursionFn(depts, dept);
                returnList.add(dept);
            }
        }
        if (returnList.isEmpty())
        {
            returnList = depts;
        }
        return returnList;
    }

    /**
     * 闁哄瀚紓鎾诲礈瀹ュ浂浼傞柟纰樺亾闂傚洠鍋撻悷鏇氭缁楀懘骞忔径瀣煇缂備焦鎸婚悗?
     * 
     * @param depts 闂侇喓鍔戝Λ顒勫礆濡ゅ嫨鈧?
     * @return 濞戞挸顑嗘刊娲冀閹寸姷娉㈤柡瀣閸亞鎮?
     */
    @Override
    public List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts)
    {
        List<SysDept> deptTrees = buildDeptTree(depts);
        return deptTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 闁哄秷顫夊畵浣烘喆閹烘洖顥廔D闁哄被鍎撮妤呮焾閵娾晜锛岄柡宥嗗灣娣囧﹪骞?
     * 
     * @param roleId 閻熸瑦甯熸竟濂岲
     * @return 闂侇偄顦懙鎴︽焾閵娾晜锛岄柛鎺擃殙閵?
     */
    @Override
    public List<Long> selectDeptListByRoleId(Long roleId)
    {
        SysRole role = roleMapper.selectRoleById(roleId);
        return deptMapper.selectDeptListByRoleId(roleId, role.isDeptCheckStrictly());
    }

    /**
     * 闁哄秷顫夊畵渚€鏌堥妸鈺傦紝ID闁哄被鍎撮妤佺┍閳╁啩绱?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞丏
     * @return 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     */
    @Override
    public SysDept selectDeptById(Long deptId)
    {
        return deptMapper.selectDeptById(deptId);
    }

    /**
     * 闁哄秷顫夊畵涓闁哄被鍎撮妤呭箥閳ь剟寮垫径濠勬憤闂侇喓鍔戝Λ顒勬晬閸噥鍔€閻㈩垰鎽滄慨鎼佸箑娓氬﹦绀?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞丏
     * @return 閻庢稒鍔欓崕鎾⒒閵婏附娈?
     */
    @Override
    public int selectNormalChildrenDeptById(Long deptId)
    {
        return deptMapper.selectNormalChildrenDeptById(deptId);
    }

    /**
     * 闁哄嫷鍨伴幆浣衡偓娑櫭﹢顏嗏偓娑欏姌婵☆參鎮?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞丏
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public boolean hasChildByDeptId(Long deptId)
    {
        int result = deptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }

    /**
     * 闁哄被鍎撮妤呮焾閵娾晜锛岄柡鍕靛灠閹胶鈧稒锚濠€顏堟偨閵婏箑鐓?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞丏
     * @return 缂備焦鎸婚悘?true 閻庢稒锚濠€?false 濞戞挸绉撮悺銊╁捶?
     */
    @Override
    public boolean checkDeptExistUser(Long deptId)
    {
        int result = deptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    /**
     * 闁哄稄绻濋悰娆撴焾閵娾晜锛岄柛姘Ф琚ㄩ柡鍕靛灠閹線宕娆戭伇
     * 
     * @param dept 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public boolean checkDeptNameUnique(SysDept dept)
    {
        Long deptId = StringUtils.isNull(dept.getDeptId()) ? -1L : dept.getDeptId();
        SysDept info = deptMapper.checkDeptNameUnique(dept.getDeptName(), dept.getParentId());
        if (StringUtils.isNotNull(info) && info.getDeptId().longValue() != deptId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 闁哄稄绻濋悰娆撴焾閵娾晜锛岄柡鍕靛灠閹線寮垫径瀣闁硅鍠楀鍫ユ⒔?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞瞕
     */
    @Override
    public void checkDeptDataScope(Long deptId)
    {
        if (!SysUser.isAdmin(SecurityUtils.getUserId()))
        {
            SysDept dept = new SysDept();
            dept.setDeptId(deptId);
            List<SysDept> depts = SpringUtils.getAopProxy(this).selectDeptList(dept);
            if (StringUtils.isEmpty(depts))
            {
                throw new ServiceException("娌℃湁鏉冮檺璁块棶閮ㄩ棬鏁版嵁");
            }
        }
    }

    /**
     * 闁哄倹婢橀·鍐╃┍濠靛棛鎽犻梺顔哄姂濡剚绌遍埄鍐х礀
     * 
     * @param dept 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int insertDept(SysDept dept)
    {
        SysDept info = deptMapper.selectDeptById(dept.getParentId());
        // 濠碘€冲€归悘澶愭偉閹澘螡闁绘劙鈧稓鐟濆☉鎾跺劋椤掓粎鏁崫銉バ﹂柟?闁告帗鐟ょ粭澶愬礂娴ｇ瓔鍟呴柡鍌涙緲椤ゅ啰鈧稒鍔樻俊顓㈡倷?
        if (!UserConstants.DEPT_NORMAL.equals(info.getStatus()))
        {
            throw new ServiceException("部门停用，不允许新增子部门");
        }
        dept.setAncestors(info.getAncestors() + "," + dept.getParentId());
        return deptMapper.insertDept(dept);
    }

    /**
     * 濞ｅ浂鍠楅弫鍏肩┍濠靛棛鎽犻梺顔哄姂濡剚绌遍埄鍐х礀
     * 
     * @param dept 闂侇喓鍔戝Λ顒佺┍閳╁啩绱?
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int updateDept(SysDept dept)
    {
        SysDept newParentDept = deptMapper.selectDeptById(dept.getParentId());
        SysDept oldDept = deptMapper.selectDeptById(dept.getDeptId());
        if (StringUtils.isNotNull(newParentDept) && StringUtils.isNotNull(oldDept))
        {
            String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
            String oldAncestors = oldDept.getAncestors();
            dept.setAncestors(newAncestors);
            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }
        int result = deptMapper.updateDept(dept);
        if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StringUtils.isNotEmpty(dept.getAncestors())
                && !StringUtils.equals("0", dept.getAncestors()))
        {
            // 濠碘€冲€归悘澶屾嫚閵夆晛鍔ラ梻鍌樺妽濡叉悂宕ラ婊勬殢闁绘鍩栭埀顑跨筏缁辨繈宕氬▎蹇斿剻闁活潿鍔忛姘舵焾閵娾晜锛岄柣銊ュ婢у秹寮垫径澶岀憪缂佺嫏鍥у姤闂?
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    /**
     * 濞ｅ浂鍠楅弫鑲╂嫚閵夆晛鍔ラ梻鍌樺妿濞堟垿鎮ラ崜渚€鐛撻梺顔哄姂濡剟鎮╅懜纰樺亾?
     * 
     * @param dept 鐟滅増鎸告晶鐘绘焾閵娾晜锛?
     */
    private void updateParentDeptStatusNormal(SysDept dept)
    {
        String ancestors = dept.getAncestors();
        Long[] deptIds = Convert.toLongArray(ancestors);
        deptMapper.updateDeptStatusNormal(deptIds);
    }

    /**
     * 濞ｅ浂鍠楅弫鑲┾偓娑欏姇閸樻挾妲愰悩鎻掑綘缂?
     * 
     * @param deptId 閻炴凹鍋傞幈銊╁绩閸︻厽鐣遍梺顔哄姂濡悂D
     * @param newAncestors 闁哄倹澹嗗▓鎴︽偉缁傛坏闂傚棗妫楅幃?
     * @param oldAncestors 闁哄唲鍛暠闁绘牜顪侱闂傚棗妫楅幃?
     */
    public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors)
    {
        List<SysDept> children = deptMapper.selectChildrenDeptById(deptId);
        for (SysDept child : children)
        {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }
        if (children.size() > 0)
        {
            deptMapper.updateDeptChildren(children);
        }
    }

    /**
     * 闁告帞濞€濞呭酣鏌堥妸鈺傦紝缂佺媴绱曢幃濠冪┍閳╁啩绱?
     * 
     * @param deptId 闂侇喓鍔戝Λ鐞丏
     * @return 缂備焦鎸婚悘?
     */
    @Override
    public int deleteDeptById(Long deptId)
    {
        return deptMapper.deleteDeptById(deptId);
    }

    /**
     * 闂侇偅甯掔紞濠囧礆濡ゅ嫨鈧?
     */
    private void recursionFn(List<SysDept> list, SysDept t)
    {
        // 鐎电増顨呴崺宀€鈧稒鍔樻俊顓㈡倷閻熸澘鐏欓悶?
        List<SysDept> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysDept tChild : childList)
        {
            if (hasChild(list, tChild))
            {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 鐎电増顨呴崺宀€鈧稒鍔樻俊顓㈡倷閻熸澘鐏欓悶?
     */
    private List<SysDept> getChildList(List<SysDept> list, SysDept t)
    {
        List<SysDept> tlist = new ArrayList<SysDept>();
        Iterator<SysDept> it = list.iterator();
        while (it.hasNext())
        {
            SysDept n = (SysDept) it.next();
            if (StringUtils.isNotNull(n.getParentId()) && n.getParentId().longValue() == t.getDeptId().longValue())
            {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 闁告帇鍊栭弻鍥及椤栨碍鍎婇柡鍫濐槸閻℃瑩鎳為崒婊冧化
     */
    private boolean hasChild(List<SysDept> list, SysDept t)
    {
        return getChildList(list, t).size() > 0;
    }
}


