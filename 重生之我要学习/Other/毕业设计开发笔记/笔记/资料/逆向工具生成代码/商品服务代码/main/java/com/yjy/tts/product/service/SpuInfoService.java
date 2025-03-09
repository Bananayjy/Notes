package com.yjy.tts.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjy.common.utils.PageUtils;
import com.yjy.tts.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author yjy
 * @email 2322092442@qq.com
 * @date 2024-02-24 14:16:32
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

