package org.jsonbuddy.pojo.testclasses;

public class ClassWithNumbers {
    private Integer intValue;
    private Long longValue;

    private ClassWithNumbers() {
    }

    public Long getLongValue() {
        return longValue;
    }

    public Integer getIntValue() {
        return intValue;
    }
}