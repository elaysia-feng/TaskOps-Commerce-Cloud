package com.elias.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elias.auth.entity.Role;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {
    @Select("SELECT r.role_code FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
    List<String> findRoleCodesByUserId(Long userId);
}
