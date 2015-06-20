package org.jsonbuddy.pojo.testclasses;

public class CombinedClassWithSetter {
    private SimpleWithName personx;
    private String occupation;

    public SimpleWithName getPerson() {
        return personx;
    }

    public void setPerson(SimpleWithName person) {
        this.personx = person;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}
