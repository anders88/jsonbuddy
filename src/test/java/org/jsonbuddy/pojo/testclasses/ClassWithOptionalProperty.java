package org.jsonbuddy.pojo.testclasses;

import java.util.Optional;

public class ClassWithOptionalProperty {
    private Optional<String> ostr;

    public Optional<String> getOptStr() {
        return ostr;
    }

    public ClassWithOptionalProperty setOptStr(Optional<String> optStr) {
        this.ostr = optStr;
        return this;
    }
}
