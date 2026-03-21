package com.qkyd.common.core.domain;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.qkyd.common.core.domain.entity.SysDept;
import com.qkyd.common.core.domain.entity.SysMenu;

/**
 * Treeselect鏍戠粨鏋勫疄浣撶被
 * 
 * @author qkyd
 */
public class TreeSelect implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 鑺傜偣ID */
    private Long id;

    /** 鑺傜偣鍚嶇О */
    private String label;

    /** 瀛愯妭鐐?*/
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelect> children;

    public TreeSelect()
    {

    }

    public TreeSelect(SysDept dept)
    {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.children = dept.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public TreeSelect(SysMenu menu)
    {
        this.id = menu.getMenuId();
        this.label = menu.getMenuName();
        this.children = menu.getChildren().stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public List<TreeSelect> getChildren()
    {
        return children;
    }

    public void setChildren(List<TreeSelect> children)
    {
        this.children = children;
    }
}


