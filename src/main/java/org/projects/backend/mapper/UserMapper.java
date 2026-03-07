package org.projects.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.projects.backend.pojo.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
//    Mapper是用来对数据库操作的，把对pojo类的CRUD转化至数据库中
//    Mapper需要有sql语句才能完成类数据至数据库的更新，不过mybatis-plus提供了SQL语句
//    使用mybatis-plus提供的SQL语句就是通过继承BaseMapper<>
}
