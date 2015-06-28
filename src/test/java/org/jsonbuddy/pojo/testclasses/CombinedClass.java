package org.jsonbuddy.pojo.testclasses;

public class CombinedClass {
    public final SimpleWithName person;
    public final String occupation;

    public CombinedClass() {
        person = null;
        occupation = null;
    }

    public CombinedClass(SimpleWithName person, String occupation) {
        this.person = person;
        this.occupation = occupation;
    }
}
