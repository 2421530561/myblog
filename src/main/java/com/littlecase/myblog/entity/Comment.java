package com.littlecase.myblog.entity;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.*;
import java.time.LocalDateTime;
import java.util.List;


@Data   
@TableName("comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long articleId;
    private Long userId;
    private Long parentId;//NULL=顶层评论
    private String content;//评论内容
    private Integer status;//0=隐藏 1=正常
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    //非数据库字段
    @TableField(exist = false)
    private String userAvatar;
    @TableField(exist = false)
    private String userNickname;
    @TableField(exist = false)
    private String replyNickname;//被回复人昵称（楼中楼展示用）
    @TableField(exist = false)
    private List<Comment> children;//子评论（楼中楼）

    
}