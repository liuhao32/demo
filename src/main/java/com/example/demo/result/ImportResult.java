package com.example.demo.result;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author YCKJ3275
 */
@Getter
@Setter
@Builder
public class ImportResult<T> {

    /**
     * 解析失败的集合
     */
    private List<T> fails;
    /**
     * 解析失败的位置集合
     */
    private List<String> failPos;
    /**
     * 解析失败的原因集合
     */
    private List<String> failCause;
    /**
     * 解析成功的集合
     */
    private List<T> succs;

}
