package org.jsonbuddy.pojo.testclasses;

public class ClassWithNumbers {
    private Integer intValue;
    private Long longValue;
    private Float floatValue;
    private Double doubleValue;

    private ClassWithNumbers() {
    }

    public Long getLongValue() {
        return longValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }
}