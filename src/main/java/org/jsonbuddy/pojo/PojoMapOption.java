package org.jsonbuddy.pojo;

public enum PojoMapOption {
    USE_INTERFACE_FIELDS;

    private PojoMappingRule myMapper;

    public PojoMappingRule myMappingRule() {
        if (myMapper != null) {
            return myMapper;
        }
        switch (this) {
            case USE_INTERFACE_FIELDS:
                try {
                    myMapper = new DynamicInterfaceMapper();
                } catch (Throwable ex) {
                    throw new RuntimeException("USE_INTERFACE_FIELDS is dependent on net.bytebuddy:byte-buddy. Add to your pom.xml");
                }
                break;
            default:
                throw new RuntimeException("No defined mapper for " + this);
        }
        return myMapper;
    }
}
