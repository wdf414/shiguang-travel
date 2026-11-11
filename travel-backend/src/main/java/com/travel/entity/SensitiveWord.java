package com.travel.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@TableName("sensitive_word")
public class SensitiveWord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String word;
    private Integer wordType;
    private String category;
    private Integer matchMode;
    private String replaceWord;
    private Integer isActive;
    
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public Integer getWordType() { return wordType; }
    public void setWordType(Integer wordType) { this.wordType = wordType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getMatchMode() { return matchMode; }
    public void setMatchMode(Integer matchMode) { this.matchMode = matchMode; }

    public String getReplaceWord() { return replaceWord; }
    public void setReplaceWord(String replaceWord) { this.replaceWord = replaceWord; }

    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
