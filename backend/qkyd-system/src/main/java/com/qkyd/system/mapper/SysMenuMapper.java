package com.qkyd.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.qkyd.common.core.domain.entity.SysMenu;

/**
 * 鑿滃崟琛?鏁版嵁灞?
 *
 * @author qkyd
 */
public interface SysMenuMapper
{
    /**
     * 鏌ヨ绯荤粺鑿滃崟鍒楄〃
     *
     * @param menu 鑿滃崟淇℃伅
     * @return 鑿滃崟鍒楄〃
     */
    public List<SysMenu> selectMenuList(SysMenu menu);

    /**
     * 鏍规嵁鐢ㄦ埛鎵€鏈夋潈闄?
     *
     * @return 鏉冮檺鍒楄〃
     */
    public List<String> selectMenuPerms();

    /**
     * 鏍规嵁鐢ㄦ埛鏌ヨ绯荤粺鑿滃崟鍒楄〃
     *
     * @param menu 鑿滃崟淇℃伅
     * @return 鑿滃崟鍒楄〃
     */
    public List<SysMenu> selectMenuListByUserId(SysMenu menu);

    /**
     * 鏍规嵁瑙掕壊ID鏌ヨ鏉冮檺
     * 
     * @param roleId 瑙掕壊ID
     * @return 鏉冮檺鍒楄〃
     */
    public List<String> selectMenuPermsByRoleId(Long roleId);

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鏉冮檺
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鏉冮檺鍒楄〃
     */
    public List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鑿滃崟
     *
     * @return 鑿滃崟鍒楄〃
     */
    public List<SysMenu> selectMenuTreeAll();

    /**
     * 鏍规嵁鐢ㄦ埛ID鏌ヨ鑿滃崟
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鑿滃崟鍒楄〃
     */
    public List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 鏍规嵁瑙掕壊ID鏌ヨ鑿滃崟鏍戜俊鎭?
     * 
     * @param roleId 瑙掕壊ID
     * @param menuCheckStrictly 鑿滃崟鏍戦€夋嫨椤规槸鍚﹀叧鑱旀樉绀?
     * @return 閫変腑鑿滃崟鍒楄〃
     */
    public List<Long> selectMenuListByRoleId(@Param("roleId") Long roleId, @Param("menuCheckStrictly") boolean menuCheckStrictly);

    /**
     * 鏍规嵁鑿滃崟ID鏌ヨ淇℃伅
     *
     * @param menuId 鑿滃崟ID
     * @return 鑿滃崟淇℃伅
     */
    public SysMenu selectMenuById(Long menuId);

    /**
     * 鏄惁瀛樺湪鑿滃崟瀛愯妭鐐?
     *
     * @param menuId 鑿滃崟ID
     * @return 缁撴灉
     */
    public int hasChildByMenuId(Long menuId);

    /**
     * 鏂板鑿滃崟淇℃伅
     *
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    public int insertMenu(SysMenu menu);

    /**
     * 淇敼鑿滃崟淇℃伅
     *
     * @param menu 鑿滃崟淇℃伅
     * @return 缁撴灉
     */
    public int updateMenu(SysMenu menu);

    /**
     * 鍒犻櫎鑿滃崟绠＄悊淇℃伅
     *
     * @param menuId 鑿滃崟ID
     * @return 缁撴灉
     */
    public int deleteMenuById(Long menuId);

    /**
     * 鏍￠獙鑿滃崟鍚嶇О鏄惁鍞竴
     *
     * @param menuName 鑿滃崟鍚嶇О
     * @param parentId 鐖惰彍鍗旾D
     * @return 缁撴灉
     */
    public SysMenu checkMenuNameUnique(@Param("menuName") String menuName, @Param("parentId") Long parentId);
}


