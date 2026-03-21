package com.qkyd.common.utils;

import com.github.pagehelper.PageHelper;
import com.qkyd.common.core.page.PageDomain;
import com.qkyd.common.core.page.TableSupport;
import com.qkyd.common.utils.sql.SqlUtil;

/**
 * 鍒嗛〉宸ュ叿绫?
 * 
 * @author qkyd
 */
public class PageUtils extends PageHelper
{
    /**
     * 璁剧疆璇锋眰鍒嗛〉鏁版嵁
     */
    public static void startPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 娓呯悊鍒嗛〉鐨勭嚎绋嬪彉閲?
     */
    public static void clearPage()
    {
        PageHelper.clearPage();
    }
}


