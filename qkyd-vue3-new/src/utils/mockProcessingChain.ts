// 模拟处理链数据生成器

export const generateMockProcessingChain = (eventId: string | number) => {
  const now = new Date()
  const stages = [
    {
      name: '异常检测',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 6).toISOString(),
      details: {
        anomalies: [
          { type: '心率异常', value: '120 bpm', severity: 'high' },
          { type: '血氧异常', value: '92%', severity: 'medium' },
          { type: '体温异常', value: '38.5°C', severity: 'low' }
        ]
      }
    },
    {
      name: '事件产生',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 5).toISOString(),
      details: {
        eventType: '心血管异常'
      }
    },
    {
      name: 'AI解析与上下文补全',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 4).toISOString(),
      details: {
        patientInfo: '张三, 65岁, 男',
        medicalHistory: '高血压, 糖尿病, 冠心病',
        currentMedication: '阿司匹林, 美托洛尔, 硝酸甘油'
      }
    },
    {
      name: '风险评估',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 3).toISOString(),
      details: {
        riskLevel: 'high',
        riskScore: 8.5,
        confidence: 92,
        trend: '恶化'
      }
    },
    {
      name: '处置建议',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 2).toISOString(),
      details: {
        suggestion: '立即就医',
        priority: 'P1',
        department: '心内科'
      }
    },
    {
      name: '自动执行',
      status: 'completed' as const,
      timestamp: new Date(now.getTime() - 1).toISOString(),
      details: {
        executionStatus: 'success',
        notificationTargets: '患者, 家属, 医生'
      }
    },
    {
      name: '留痕记录',
      status: 'completed' as const,
      timestamp: now.toISOString(),
      details: {
        algorithmVersion: 'v2.0',
        processingTime: 6
      }
    }
  ]

  return {
    stages,
    totalDuration: 6
  }
}

// 生成随机处理状态
export const generateRandomProcessingStatus = () => {
  const statuses = ['completed', 'processing', 'pending'] as const
  return statuses[Math.floor(Math.random() * statuses.length)]
}

// 为异常列表添加处理链信息
export const enrichExceptionWithChain = (exception: any) => {
  const stageCount = Math.floor(Math.random() * 7) + 1
  const stages = [
    { name: '异常检测', status: 'completed' as const },
    { name: '事件产生', status: 'completed' as const },
    { name: 'AI解析与上下文补全', status: 'completed' as const },
    { name: '风险评估', status: 'completed' as const },
    { name: '处置建议', status: 'completed' as const },
    { name: '自动执行', status: 'completed' as const },
    { name: '留痕记录', status: 'completed' as const }
  ]

  // 随机设置最后一个阶段的状态
  if (stageCount < 7) {
    stages[stageCount].status = generateRandomProcessingStatus()
  }

  return {
    ...exception,
    stages: stages.slice(0, stageCount)
  }
}
