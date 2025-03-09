package com.yjy.tts.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjy.common.utils.PageUtils;
import com.yjy.tts.product.entity.ProductAttrValueEntity;

import java.util.Map;

/**
 * spu属性值
 *
 * @author yjy
 * @email 2322092442@qq.com
 * @date 2024-02-24 14:16:32
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

