package com.hailong.plantknow.model

import com.hailong.plantknow.util.Constants

/**
 * 阿里云通义千问API数据模型
 * 用于构建与阿里云大语言模型交互的请求和响应数据结构
 */
data class AliyunChatRequest(
    // 指定使用的AI模型，默认使用千问Flash版本（性能与成本均衡）
    val model: String = Constants.QWEN_FLASH_MODEL,

    // 对话消息列表，按时间顺序排列的多轮对话内容
    val messages: List<ChatMessage>,

    // 是否启用流式传输：Android端建议关闭以获得更好的稳定性
    // true: 流式输出（适合Web端实时显示）| false: 一次性返回完整结果
    val stream: Boolean = false,

    // 扩展参数配置：启用思维链功能，让AI展示推理过程
    val extra_body: Map<String, Any> = mapOf("enable_thinking" to true)
)

/**
 * 单条聊天消息实体
 * 代表对话中的一个发言回合
 */
data class ChatMessage(
    // 发言者角色：user-用户提问 | assistant-AI回答 | system-系统指令
    val role: String,

    // 消息具体内容，支持文本、代码等多种格式
    val content: String
)

/**
 * 阿里云API标准响应结构
 * 包含AI生成的回答和本次请求的资源消耗统计
 */
data class AliyunChatResponse(
    // AI生成的候选回答列表，通常包含1个或多个可能的回答
    val choices: List<ChatChoice>,

    // Token使用情况统计，用于监控API调用成本和配额管理
    val usage: Usage?
)

/**
 * AI生成的单个回答选项
 */
data class ChatChoice(
    // AI返回的消息内容，包含角色和具体回答文本
    val message: ChatMessage,

    // 生成结束原因：stop-正常结束 | length-达到长度限制 | content_filter-内容过滤
    val finish_reason: String?
)

/**
 * Token用量统计
 * 用于成本控制和API调用优化
 */
data class Usage(
    // 本次请求消耗的总Token数（输入+输出）
    val total_tokens: Int,

    // 输入内容的Token消耗（用户提问部分）
    val input_tokens: Int,

    // 输出内容的Token消耗（AI回答部分）
    val output_tokens: Int
)

// ============================================================================
// 流式响应相关数据模型（预留扩展，当前项目未使用）
// 适用于需要实时显示AI思考过程的场景
// ============================================================================

/**
 * 流式传输响应结构
 * 用于实时接收AI生成的文本片段
 */
data class AliyunStreamResponse(
    // 流式输出中的候选回答片段
    val choices: List<StreamChoice>
)

/**
 * 流式输出中的单个选择项
 */
data class StreamChoice(
    // 增量内容更新，包含AI的实时思考过程
    val delta: StreamDelta
)

/**
 * 流式输出的增量内容
 * 支持显示AI的推理过程和最终回答
 */
data class StreamDelta(
    // 思维链内容：展示AI的推理过程和思考路径
    val reasoning_content: String? = null,

    // 最终回答内容：AI生成的正式回复
    val content: String? = null
)