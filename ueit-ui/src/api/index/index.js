import request from '@/utils/request'

export function ageSexGroupCount(){
  return request({
    url   : '/index/ageSexGroupCount',
    method: 'get'
  })
}

export function exception(data){
    return request({
        url   : '/index/exception',
        method: 'get',
        params: data
    })
}

export function charge(data){
  return request({
      url   : '/index/charge',
      method: 'get',
      params: data
  })
}

// 查询实时数据
export function realTimeData(data){
  return request({
    url   : '/index/realTimeData',
    method: 'get',
    params: data
  })
}

// 查询实时数据
export function indexUserLocation(data){
  return request({
    url   : '/index/indexUserLocation',
    method: 'get',
    params: data
  })
}


