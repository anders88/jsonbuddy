package org.jsonbuddy.pojo.testclasses;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ClassWithBigNumbers {
    private BigDecimal oneBigDec;
    private BigInteger oneBigInt;

    public BigDecimal getOneBigDec() {
        return oneBigDec;
    }

    public void setOneBigDec(BigDecimal oneBigDec) {
        this.oneBigDec = oneBigDec;
    }

    public BigInteger getOneBigInt() {
        return oneBigInt;
    }

    public void setOneBigInt(BigInteger oneBigInt) {
        this.oneBigInt = oneBigInt;
    }
}
