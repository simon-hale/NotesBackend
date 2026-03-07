package org.projects.backend.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // 实现getter、setter
@NoArgsConstructor  // 实现无参构造函数
@AllArgsConstructor  // 实现有参构造函数
public class Directory {
    @TableId(type = IdType.AUTO)  // 定义主键，并设置自增
    private Integer id;  // 最好用类Integer，这样mybatis-plus不会提示警告
    private String name;
    private Integer parentId;
    private Integer userId;
}
