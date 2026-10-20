// file: src/main/java/com/littlecase/myblog/vo/CommentVO.java
package com.littlecase.myblog.vo;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data
public class CommentVO {
    private Long id;
    private Long articleId;
    private Long parentId;
    private Long userId;
    private String userNickname;
    private String userAvatar;
    private String replyNickname; // 被回复人昵称
    private String content;
    private LocalDateTime createTime;
    private List<CommentVO> children; // 楼中楼
}