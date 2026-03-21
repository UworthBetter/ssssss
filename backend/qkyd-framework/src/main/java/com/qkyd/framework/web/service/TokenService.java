package com.qkyd.framework.web.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.qkyd.common.constant.CacheConstants;
import com.qkyd.common.constant.Constants;
import com.qkyd.common.core.domain.model.LoginUser;
import com.qkyd.common.core.redis.RedisCache;
import com.qkyd.common.utils.ServletUtils;
import com.qkyd.common.utils.StringUtils;
import com.qkyd.common.utils.ip.AddressUtils;
import com.qkyd.common.utils.ip.IpUtils;
import com.qkyd.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

/**
 * token楠岃瘉澶勭悊
 *
 * @author qkyd
 */
@Component
public class TokenService
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // 浠ょ墝鑷畾涔夋爣璇?
    @Value("${token.header}")
    private String header;

    // 浠ょ墝绉橀挜
    @Value("${token.secret}")
    private String secret;

    // 浠ょ墝鏈夋晥鏈燂紙榛樿30鍒嗛挓锛?
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 鑾峰彇鐢ㄦ埛韬唤淇℃伅
     *
     * @return 鐢ㄦ埛淇℃伅
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        // 鑾峰彇璇锋眰鎼哄甫鐨勪护鐗?
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            try
            {
                Claims claims = parseToken(token);
                // 瑙ｆ瀽瀵瑰簲鐨勬潈闄愪互鍙婄敤鎴蜂俊鎭?
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getTokenKey(uuid);
                LoginUser user = redisCache.getCacheObject(userKey);
                return user;
            }
            catch (Exception e)
            {
                log.error("鑾峰彇鐢ㄦ埛淇℃伅寮傚父'{}'", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 璁剧疆鐢ㄦ埛韬唤淇℃伅
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 鍒犻櫎鐢ㄦ埛韬唤淇℃伅
     */
    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token);
            redisCache.deleteObject(userKey);
        }
    }

    /**
     * 鍒涘缓浠ょ墝
     *
     * @param loginUser 鐢ㄦ埛淇℃伅
     * @return 浠ょ墝
     */
    public String createToken(LoginUser loginUser)
    {
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, token);
        return createToken(claims);
    }

    /**
     * 楠岃瘉浠ょ墝鏈夋晥鏈燂紝鐩稿樊涓嶈冻20鍒嗛挓锛岃嚜鍔ㄥ埛鏂扮紦瀛?
     *
     * @param loginUser
     * @return 浠ょ墝
     */
    public void verifyToken(LoginUser loginUser)
    {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN)
        {
            refreshToken(loginUser);
        }
    }

    /**
     * 鍒锋柊浠ょ墝鏈夋晥鏈?
     *
     * @param loginUser 鐧诲綍淇℃伅
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 鏍规嵁uuid灏唋oginUser缂撳瓨
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 璁剧疆鐢ㄦ埛浠ｇ悊淇℃伅
     *
     * @param loginUser 鐧诲綍淇℃伅
     */
    public void setUserAgent(LoginUser loginUser)
    {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 浠庢暟鎹０鏄庣敓鎴愪护鐗?
     *
     * @param claims 鏁版嵁澹版槑
     * @return 浠ょ墝
     */
    private String createToken(Map<String, Object> claims)
    {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        String token = Jwts.builder()
                .claims(claims)
                .signWith(key)
                .compact();
        return token;
    }

    /**
     * 浠庝护鐗屼腑鑾峰彇鏁版嵁澹版槑
     *
     * @param token 浠ょ墝
     * @return 鏁版嵁澹版槑
     */
    private Claims parseToken(String token)
    {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 浠庝护鐗屼腑鑾峰彇鐢ㄦ埛鍚?
     *
     * @param token 浠ょ墝
     * @return 鐢ㄦ埛鍚?
     */
    public String getUsernameFromToken(String token)
    {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 鑾峰彇璇锋眰token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request)
    {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}


