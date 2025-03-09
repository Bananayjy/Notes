package com.yjy.tts.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjy.common.utils.PageUtils;
import com.yjy.tts.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author yjy
 * @email 2322092442@qq.com
 * @date 2024-02-24 14:16:32
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

