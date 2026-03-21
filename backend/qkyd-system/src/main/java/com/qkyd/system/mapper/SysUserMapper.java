package com.qkyd.system.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import com.qkyd.common.core.domain.entity.SysUser;

/**
 * 鐢ㄦ埛琛?鏁版嵁灞?
 *
 * @author qkyd
 */
public interface SysUserMapper
{
    /**
     * 鏍规嵁鏉′欢鍒嗛〉鏌ヨ鐢ㄦ埛鍒楄〃
     *
     * @param sysUser 鐢ㄦ埛淇℃伅
     * @return 鐢ㄦ埛淇℃伅闆嗗悎淇℃伅
     */
    public List<SysUser> selectUserList(SysUser sysUser);

    /**
     * 鏍规嵁鏉′欢鍒嗛〉鏌ヨ宸查厤鐢ㄦ埛瑙掕壊鍒楄〃
     *
     * @param user 鐢ㄦ埛淇℃伅
     * @return 鐢ㄦ埛淇℃伅闆嗗悎淇℃伅
     */
    public List<SysUser> selectAllocatedList(SysUser user);

    /**
     * 鏍规嵁鏉′欢鍒嗛〉鏌ヨ鏈垎閰嶇敤鎴疯鑹插垪琛?
     *
     * @param user 鐢ㄦ埛淇℃伅
     * @return 鐢ㄦ埛淇℃伅闆嗗悎淇℃伅
     */
    public List<SysUser> selectUnallocatedList(SysUser user);

    /**
     * 閫氳繃鐢ㄦ埛鍚嶆煡璇㈢敤鎴?
     *
     * @param userName 鐢ㄦ埛鍚?
     * @return 鐢ㄦ埛瀵硅薄淇℃伅
     */
    public SysUser selectUserByUserName(String userName);

    /**
     * 閫氳繃鐢ㄦ埛ID鏌ヨ鐢ㄦ埛
     *
     * @param userId 鐢ㄦ埛ID
     * @return 鐢ㄦ埛瀵硅薄淇℃伅
     */
    public SysUser selectUserById(Long userId);

    /**
     * 鏂板鐢ㄦ埛淇℃伅
     *
     * @param user 鐢ㄦ埛淇℃伅
     * @return 缁撴灉
     */
    public int insertUser(SysUser user);

    /**
     * 淇敼鐢ㄦ埛淇℃伅
     *
     * @param user 鐢ㄦ埛淇℃伅
     * @return 缁撴灉
     */
    public int updateUser(SysUser user);

    /**
     * 淇敼鐢ㄦ埛澶村儚
     *
     * @param userName 鐢ㄦ埛鍚?
     * @param avatar 澶村儚鍦板潃
     * @return 缁撴灉
     */
    public int updateUserAvatar(@Param("userName") String userName, @Param("avatar") String avatar);

    /**
     * 閲嶇疆鐢ㄦ埛瀵嗙爜
     *
     * @param userName 鐢ㄦ埛鍚?
     * @param password 瀵嗙爜
     * @return 缁撴灉
     */
    public int resetUserPwd(@Param("userName") String userName, @Param("password") String password);

    /**
     * 閫氳繃鐢ㄦ埛ID鍒犻櫎鐢ㄦ埛
     *
     * @param userId 鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteUserById(Long userId);

    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛淇℃伅
     *
     * @param userIds 闇€瑕佸垹闄ょ殑鐢ㄦ埛ID
     * @return 缁撴灉
     */
    public int deleteUserByIds(Long[] userIds);

    /**
     * 鏍￠獙鐢ㄦ埛鍚嶇О鏄惁鍞竴
     *
     * @param userName 鐢ㄦ埛鍚嶇О
     * @return 缁撴灉
     */
    public SysUser checkUserNameUnique(String userName);

    /**
     * 鏍￠獙鎵嬫満鍙风爜鏄惁鍞竴
     *
     * @param phonenumber 鎵嬫満鍙风爜
     * @return 缁撴灉
     */
    public SysUser checkPhoneUnique(String phonenumber);

    /**
     * 鏍￠獙email鏄惁鍞竴
     *
     * @param email 鐢ㄦ埛閭
     * @return 缁撴灉
     */
    public SysUser checkEmailUnique(String email);
}


