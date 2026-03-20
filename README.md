# Minimal Mainline Algorithm Package

目标：仅提纯“连续监测数据的房颤相关异常心律风险筛查与预警”主线算法，不做全仓库重构。

## 先扫描并列方案（已执行）

### 方案 A（最小可运行，已采用）
- 主线固定为 Layer 1 + Layer 2：`CPC Encoder + Dual-Head Evidential Risk Head`。
- 保留训练/评估/推理/基线对比所需最少模块。
- 去除 Layer 3/4（因果、元学习）及工程部署耦合。

### 方案 B（更接近原项目）
- 直接复用 `src/models/cecm_pipeline_v3.py` 及其依赖。
- 优点：改动小。缺点：带入 Layer 3/4 与大量非主线依赖，不够“提纯”。

最终采用方案 A。

## 最小包目录

```text
minimal_algorithm_pkg/
├─ mainline_algo/
│  ├─ models/
│  │  ├─ encoder.py
│  │  ├─ risk_heads.py
│  │  └─ pipeline.py
│  ├─ losses/
│  │  └─ evidential_loss.py
│  ├─ data/
│  │  └─ dataset.py
│  └─ utils/
│     ├─ missing_value_handler.py
│     └─ negative_sampling.py
└─ scripts/
   ├─ train.py
   ├─ eval.py
   └─ ablation.py
```

## 必保留主线文件（按能力）
- 编码器：`mainline_algo/models/encoder.py`
- 风险预测头：`mainline_algo/models/risk_heads.py`
- 不确定性估计：`mainline_algo/models/risk_heads.py` + `mainline_algo/losses/evidential_loss.py`
- pipeline：`mainline_algo/models/pipeline.py`
- dataset/preprocess：`mainline_algo/data/dataset.py`
- train：`scripts/train.py`
- eval：`scripts/eval.py`
- baseline/ablation：`scripts/ablation.py`

## 排除范围（本次不纳入最小包）
- `mini-program/**`（前后端/部署链路）
- `docs/**`（文档资产）
- `video/**`, `media/**`（多媒体）
- `src/models/causal_*`, `src/models/dag_constraint.py`（Layer 3 因果）
- `src/models/causal_meta_learning_v3.py`（Layer 4 元学习）
- 推荐/家属关怀相关服务与路由
- ONNX、量化、可视化、报告生成脚本

## 旧路径 -> 新路径映射
- `src/models/cpc_encoder.py` -> `minimal_algorithm_pkg/mainline_algo/models/encoder.py`
- `src/models/evidential_heads.py` -> `minimal_algorithm_pkg/mainline_algo/models/risk_heads.py`
- `src/losses/evidential_loss.py` -> `minimal_algorithm_pkg/mainline_algo/losses/evidential_loss.py`
- `src/utils/negative_sampling.py` -> `minimal_algorithm_pkg/mainline_algo/utils/negative_sampling.py`
- `src/utils/missing_value_handler.py` -> `minimal_algorithm_pkg/mainline_algo/utils/missing_value_handler.py`
- `scripts/benchmark_layer12_regression.py`（数据构造/训练评估逻辑提取） ->
  - `minimal_algorithm_pkg/mainline_algo/data/dataset.py`
  - `minimal_algorithm_pkg/scripts/train.py`
  - `minimal_algorithm_pkg/scripts/eval.py`
  - `minimal_algorithm_pkg/scripts/ablation.py`

## 运行

```bash
# 训练
python minimal_algorithm_pkg/scripts/train.py --epochs 5

# 评估
python minimal_algorithm_pkg/scripts/eval.py --ckpt minimal_algorithm_pkg/checkpoints/mainline.pt

# 基线/消融
python minimal_algorithm_pkg/scripts/ablation.py --epochs 3
```
