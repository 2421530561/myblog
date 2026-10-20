// file: src/main/java/com/littlecase/myblog/mapper/CommentMapper.java
package com.littlecase.myblog.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.littlecase.myblog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface CommentMapper extends BaseMapper<Comment> { 
    
}