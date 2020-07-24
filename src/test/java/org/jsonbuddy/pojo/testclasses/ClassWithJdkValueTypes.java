package org.jsonbuddy.pojo.testclasses;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ClassWithJdkValueTypes {

    public List<UUID> uuids;
    public LocalDate localDate;
    public Instant instant;
    public URI uri;
    public URL url;
    public InetAddress inetAddress;
}
