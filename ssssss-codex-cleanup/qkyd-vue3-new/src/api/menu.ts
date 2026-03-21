import request from '@/utils/request'

// 获取菜单列表
export function getMenuList() {
  return request({
    url: '/system/menu/list',
    method: 'get'
  })
}

// 获取菜单树
export function getMenuTree() {
  return request({
    url: '/system/menu/treeselect',
    method: 'get'
  })
}

// 获取角色菜单树
export function getRoleMenuTree(roleId: number) {
  return request({
    url: `/system/menu/roleMenuTreeselect/${roleId}`,
    method: 'get'
  })
}
