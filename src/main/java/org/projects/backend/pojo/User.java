package org.projects.backend.pojo;

//pojo层，用来实现将数据库的二维表转为类

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 这三行注解是用来调用lombok来实现一些机械化方法，这样就不用手动写了
@Data  // 实现getter、setter
@NoArgsConstructor  // 实现无参构造函数
@AllArgsConstructor  // 实现有参构造函数
public class User {
    @TableId(type = IdType.AUTO)  // 定义主键，并设置自增
    private Integer id;  // 最好用类Integer，这样mybatis-plus不会提示警告
    private String username;
    private String password;
}
