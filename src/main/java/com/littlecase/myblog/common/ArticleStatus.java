package com.littlecase.myblog.common;

/**
 * 文章状态枚举（PRD 5.1 文章发布审核流程）
 * <p>
 * DRAFT(0) → PENDING(1) → PUBLISHED(2) / REJECTED(3)
 * <pre>
 * 草稿 --提交审核--> 待审核 --审核通过--> 已发布 --下架--> 草稿
 *                          --驳回--> 已驳回 --修改/重新提交--> 草稿/待审核
 * </pre>
 */
public enum ArticleStatus {

    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String desc;

    ArticleStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /** 判断文章状态码是否为本状态 */
    public boolean is(Integer status) {
        return status != null && status == code;
    }

    /** 状态码 → 中文描述，未知返回“未知” */
    public static String descOf(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (ArticleStatus s : values()) {
            if (s.code == status) {
                return s.desc;
            }
        }
        return "未知";
    }
}
