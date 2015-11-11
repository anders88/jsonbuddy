package org.jsonbuddy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

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
        private Instant orderDate;
        private OrderStatus status;
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

    private static enum OrderStatus {
        COMPLETE
    }

    @Test
    public void shouldBuildComplexJson() throws Exception {
        JsonObject orderJson = buildJsonOrder();
        Order order = convertToOrder(orderJson);
        JsonObject orderJson2 = convertToJson(order);
        assertThat(orderJson.toJson()).isEqualTo(orderJson2.toJson());
    }

    private JsonObject buildJsonOrder() {
        return new JsonObject()
                .put("customer", new JsonObject()
                        .put("name", "Darth Vader")
                        .put("address", "Death Star"))
                .put("date", Instant.now())
                .put("status", OrderStatus.COMPLETE)
                .put("tags", JsonArray.fromStrings("urgent", "international"))
                .put("orderLines", new JsonArray()
                        .add(new JsonObject().put("productId", 1).put("amount", 400.5))
                        .add(new JsonObject().put("productId", 2).put("amount", 11.5)));
    }

    private Order convertToOrder(JsonObject orderJson) {
        Order order = new Order();
        order.setCustomer(orderJson.objectValue("customer")
                .map(customerJson -> {
                        Customer customer = new Customer();
                        customer.setName(customerJson.requiredString("name"));
                        customer.setAddress(customerJson.requiredString("address"));
                        return customer;
                }).orElse(null));
        order.setOrderDate(orderJson.requiredInstant("date"));
        order.setStatus(orderJson.requiredEnum("status", OrderStatus.class)); // TODO
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
                .put("date", order.getOrderDate())
                .put("status", order.getStatus())
                .put("tags", JsonArray.fromStringList(order.getTagList()))
                .put("orderLines", JsonArray.map(order.getOrderLines(), line -> {
                        return new JsonObject()
                                .put("productId", line.getProductId())
                                .put("amount", line.getAmount());
                }));
    }
}
