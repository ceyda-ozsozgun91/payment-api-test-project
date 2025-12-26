package com.example.payment_api.controller;

import com.example.payment_api.model.PaymentRequest;
import com.example.payment_api.model.ProvisionRequest;
import com.example.payment_api.model.CompletePaymentRequest;
import com.example.payment_api.model.UpdatePaymentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final JdbcTemplate jdbcTemplate;

    public PaymentController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/customers")
    public List<Map<String, Object>> getCustomers() {

        String sql = """
                    SELECT c.name, SUM(o.amount) AS total_amount
                    FROM customers c
                    JOIN orders o ON c.id = o.customer_id
                    WHERE o.status = 'SUCCESS'
                    GROUP BY c.name
                """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/high-value-customers")
    public List<Map<String, Object>> getHighValueCustomers() {

        String sql = """
        SELECT
            c.id,
            c.name,
            SUM(o.amount) AS total_success_amount
        FROM customers c
        JOIN orders o ON c.id = o.customer_id
        WHERE o.status = 'SUCCESS'
        GROUP BY c.id, c.name
        HAVING SUM(o.amount) > 2000
    """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/top-success-customer")
    public List<Map<String, Object>> getTopSuccessCustomer() {

        String sql = """
        SELECT
            c.name,
            COUNT(o.id) AS success_count
        FROM customers c
        JOIN orders o ON c.id = o.customer_id
        WHERE o.status = 'SUCCESS'
        GROUP BY c.name
        ORDER BY success_count DESC
        LIMIT 1
    """;

        return jdbcTemplate.queryForList(sql);
    }

    @GetMapping("/customers-without-success")
    public List<Map<String, Object>> getCustomersWithoutSuccess() {

        String sql = """
        SELECT c.id, c.name
        FROM customers c
        LEFT JOIN orders o
        ON c.id = o.customer_id AND o.status = 'SUCCESS'
        WHERE o.id IS NULL
    """;

        return jdbcTemplate.queryForList(sql);
    }

    @PostMapping("/initiate")
    public Map<String, Object> initiatePayment(@RequestBody PaymentRequest request) {

        jdbcTemplate.update(
                "INSERT INTO payments (customer_id, amount, status) VALUES (?, ?, 'INITIATED')",
                request.getCustomerId(),
                request.getAmount()
        );

        Integer paymentId = jdbcTemplate.queryForObject(
                "SELECT currval(pg_get_serial_sequence('payments','id'))",
                Integer.class
        );
        return Map.of(
                "success", true,
                "paymentId", paymentId,
                "status", "INITIATED",
                "message", "Payment initiated successfully",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    @PostMapping("/provision")
    public Map<String, Object> provisionPayment(@RequestBody ProvisionRequest request) {

        //Odeme bilgilerini al
        String paymentSql = """
        SELECT customer_id, amount, status
        FROM payments
        WHERE id = ?
    """;

        Map<String, Object> payment =
                jdbcTemplate.queryForMap(paymentSql, request.getPaymentId());

        int customerId = (int) payment.get("customer_id");
        double amount = ((Number) payment.get("amount")).doubleValue();
        String status = (String) payment.get("status");

        //Status kontrolu
        if (!"INITIATED".equals(status)) {
            return Map.of(
                    "message", "Payment is not in INITIATED status"
            );
        }

        //Musteri bakiyesini kontrol et
        Double balance = jdbcTemplate.queryForObject(
                "SELECT balance FROM customers WHERE id = ?",
                Double.class,
                customerId
        );

        if (balance < amount) {
            //Yetersiz bakiye → FAILED
            jdbcTemplate.update(
                    "UPDATE payments SET status = 'FAILED' WHERE id = ?",
                    request.getPaymentId()
            );

            return Map.of(
                    "success", false,
                    "paymentId", request.getPaymentId(),
                    "status", "FAILED",
                    "message", "Insufficient balance",
                    "timestamp", java.time.LocalDateTime.now().toString()
            );
        }

        //Para bloke edilir (bakiye düşer)
        jdbcTemplate.update(
                "UPDATE customers SET balance = balance - ? WHERE id = ?",
                amount, customerId
        );

        //Odeme PROVISIONED olur
        jdbcTemplate.update(
                "UPDATE payments SET status = 'PROVISIONED' WHERE id = ?",
                request.getPaymentId()
        );

        return Map.of(
                "success", true,
                "paymentId", request.getPaymentId(),
                "status", "PROVISIONED",
                "message", "Payment provisioned successfully",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }


    @PostMapping("/complete")
    public Map<String, Object> completePayment(@RequestBody CompletePaymentRequest request) {

        //Payment status kontrolü
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM payments WHERE id = ?",
                String.class,
                request.getPaymentId()
        );

        if (!"PROVISIONED".equals(status)) {
            return Map.of(
                    "message", "Payment is not in PROVISIONED status"
            );
        }

        //SUCCESS yap
        jdbcTemplate.update(
                "UPDATE payments SET status = 'SUCCESS' WHERE id = ?",
                request.getPaymentId()
        );

        //Ledger kaydı
        jdbcTemplate.update(
                """
                INSERT INTO ledger (payment_id, entry_type, amount)
                SELECT id, 'DEBIT', amount
                FROM payments
                WHERE id = ?
                """,
                request.getPaymentId()
        );

        return Map.of(
                "success", true,
                "paymentId", request.getPaymentId(),
                "status", "SUCCESS",
                "message", "Payment completed successfully",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
    }

    @GetMapping("/{id}")
    public Map<String, Object> getPayment(@PathVariable int id) {

        return jdbcTemplate.queryForMap(
                """
                SELECT p.id,
                       p.amount,
                       p.status,
                       c.name AS customer_name
                FROM payments p
                JOIN customers c ON c.id = p.customer_id
                WHERE p.id = ?
                """,
                id
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updatePayment(@PathVariable int id, @RequestBody UpdatePaymentRequest request) {
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM payments WHERE id = ?",
                String.class,
                id
        );

        if (!"INITIATED".equals(status)) {
            return Map.of(
                    "message", "Only INITIATED payments can be updated"
            );
        }

        jdbcTemplate.update(
                "UPDATE payments SET amount = ? WHERE id = ?",
                request.getAmount(), id
        );

        return Map.of(
                "message", "Payment updated"
        );
    }

    @PatchMapping("/{id}/description")
    public Map<String, Object> updateDescription(@PathVariable int id, @RequestBody Map<String, String> body) {

        jdbcTemplate.update(
                "UPDATE payments SET description = ? WHERE id = ?",
                body.get("description"), id
        );

        return Map.of(
                "message", "Description updated"
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> cancelPayment(@PathVariable int id) {

        jdbcTemplate.update(
                "UPDATE payments SET cancelled = true WHERE id = ?",
                id
        );

        return Map.of(
                "message", "Payment cancelled"
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> options() {
        return ResponseEntity.ok()
                .header("Allow", "GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD")
                .build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> headPayment(@PathVariable int id) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payments WHERE id = ? AND cancelled = false",
                Integer.class,
                id
        );

        if (count == null || count == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

}