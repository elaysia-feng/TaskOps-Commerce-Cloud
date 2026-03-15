package com.elias.task.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.task.entity.InternshipTask;
import org.apache.ibatis.annotations.Mapper;

import jakarta.validation.constraints.NotBlank;

/**
 * 文件说明： InternshipTaskMapper.
 * 组件职责： 项目中的通用组件。
 */
@Mapper
public interface InternshipTaskMapper extends BaseMapper<InternshipTask> {
}
