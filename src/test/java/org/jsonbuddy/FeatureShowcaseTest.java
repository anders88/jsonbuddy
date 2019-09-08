package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

public class FeatureShowcaseTest {
    private static class Customer {
        private String name;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    private static class Order {
        private Customer customer;
        private UUID id;
        private Instant orderDate;
        private OrderStatus status;
        private URL statusUrl;
        private List<String> tagList;
        private List<OrderLine> orderLines;

        public Customer getCustomer() {
            return customer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public Instant getOrderDate() {
            return orderDate;
        }

        public void setOrderDate(Instant orderDate) {
            this.orderDate = orderDate;
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }

        public List<String> getTagList() {
            return tagList;
        }

        public void setTagList(List<String> tagList) {
            this.tagList = tagList;
        }

        public List<OrderLine> getOrderLines() {
            return orderLines;
        }

        public void setOrderLines(List<OrderLine> orderLines) {
            this.orderLines = orderLines;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public URL getStatusUrl() {
            return statusUrl;
        }

        public void setStatusUrl(URL statusUrl) {
            this.statusUrl = statusUrl;
        }
    }

    private static class OrderLine {
        private long productId;
        private double amount;

        public OrderLine(long productId, double amount) {
            this.productId = productId;
            this.amount = amount;
        }

        public long getProductId() {
            return productId;
        }

        public double getAmount() {
            return amount;
        }
    }

    private enum OrderStatus {
        COMPLETE
    }

    @Test
    public void shouldBuildComplexJson() throws Exception {
        JsonObject orderJson = buildJsonOrder();
        Order order = convertToOrder(orderJson);
        JsonObject orderJson2 = convertToJson(order);
        assertThat(orderJson.toJson()).isEqualTo(orderJson2.toJson());
    }

    private JsonObject buildJsonOrder() throws MalformedURLException {
        return new JsonObject()
                .put("customer", new JsonObject()
                        .put("name", "Darth Vader")
                        .put("address", "Death Star"))
                .put("id", UUID.randomUUID())
                .put("date", Instant.now())
                .put("status", OrderStatus.COMPLETE)
                .put("statusUrl", new URL("http://www.example.com/status"))
                .put("tags", JsonArray.fromStrings("urgent", "international"))
                .put("orderLines", new JsonArray()
                        .add(new JsonObject().put("productId", 1).put("amount", 400.5))
                        .add(new JsonObject().put("productId", 2).put("amount", 11.5)));
    }

    private Order convertToOrder(JsonObject orderJson) throws MalformedURLException {
        Order order = new Order();
        order.setCustomer(orderJson.objectValue("customer")
                .map(customerJson -> {
                    Customer customer = new Customer();
                    customer.setName(customerJson.requiredString("name"));
                    customer.setAddress(customerJson.requiredString("address"));
                    return customer;
                }).orElse(null));
        order.setId(UUID.fromString(orderJson.requiredString("id")));
        order.setOrderDate(orderJson.requiredInstant("date"));
        order.setStatus(orderJson.requiredEnum("status", OrderStatus.class)); // TODO
        order.setStatusUrl(new URL(orderJson.requiredString("statusUrl")));
        order.setTagList(orderJson.requiredArray("tags").strings());
        order.setOrderLines(orderJson.requiredArray("orderLines").objects(
                lineJson ->  new OrderLine(lineJson.requiredLong("productId"), lineJson.requiredDouble("amount"))));
        return order;
    }

    private JsonObject convertToJson(Order order) {
        return new JsonObject()
                .put("customer", new JsonObject()
                        .put("name", order.getCustomer().getName())
                        .put("address", order.getCustomer().getAddress()))
                .put("id", order.getId())
                .put("date", order.getOrderDate())
                .put("status", order.getStatus())
                .put("statusUrl", order.getStatusUrl())
                .put("tags", JsonArray.fromStringList(order.getTagList()))
                .put("orderLines", JsonArray.map(order.getOrderLines(), line ->
                    new JsonObject()
                            .put("productId", line.getProductId())
                            .put("amount", line.getAmount())
                ));
    }
}
