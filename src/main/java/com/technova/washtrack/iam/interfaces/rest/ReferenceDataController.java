package com.technova.washtrack.iam.interfaces.rest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.technova.washtrack.shared.exception.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Identity & Access and laundry catalog data (users, roles, roleAssignments, laundryServices) served as simple
 * collections seeded from {@code seed/compat-seed.json}.
 * shortcut: schemaless in-memory maps, no password hashing; replace with typed JPA aggregates when IAM is built.
 */
@RestController
@RequestMapping("/api/v1/{collection:users|roles|roleAssignments|laundryServices}")
@Tag(name = "Reference data", description = "Users, roles, role assignments and laundry services (in-memory)")
public class ReferenceDataController {

    private final Map<String, Map<Long, Map<String, Object>>> store = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> sequences = new ConcurrentHashMap<>();

    ReferenceDataController() throws IOException {
        Map<String, List<Map<String, Object>>> seed;
        try (InputStream in = new ClassPathResource("seed/compat-seed.json").getInputStream()) {
            seed = JsonMapper.builder().build().readValue(in, new TypeReference<>() {
            });
        }
        seed.forEach((name, rows) -> {
            Map<Long, Map<String, Object>> table = new ConcurrentHashMap<>();
            long max = 0;
            for (Map<String, Object> row : rows) {
                long id = ((Number) row.get("id")).longValue();
                table.put(id, new LinkedHashMap<>(row));
                max = Math.max(max, id);
            }
            store.put(name, table);
            sequences.put(name, new AtomicLong(max));
        });
    }

    @GetMapping
    @Operation(summary = "List a collection; every query parameter filters by exact value")
    List<Map<String, Object>> list(@PathVariable String collection, @RequestParam Map<String, String> filters) {
        List<Map<String, Object>> rows = new ArrayList<>(table(collection).values());
        rows.sort((a, b) -> Long.compare(idOf(a), idOf(b)));
        return rows.stream()
                .filter(row -> filters.entrySet().stream()
                        .allMatch(f -> String.valueOf(row.get(f.getKey())).equals(f.getValue())))
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one row by id")
    Map<String, Object> get(@PathVariable String collection, @PathVariable Long id) {
        return find(collection, id);
    }

    @PostMapping
    @Operation(summary = "Create a row")
    ResponseEntity<Map<String, Object>> create(@PathVariable String collection,
            @RequestBody Map<String, Object> body) {
        long id = sequences.get(collection).incrementAndGet();
        Map<String, Object> row = new LinkedHashMap<>(body);
        row.put("id", id);
        table(collection).put(id, row);
        return ResponseEntity.created(URI.create("/api/v1/" + collection + "/" + id)).body(row);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a row")
    Map<String, Object> replace(@PathVariable String collection, @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        find(collection, id);
        Map<String, Object> row = new LinkedHashMap<>(body);
        row.put("id", id);
        table(collection).put(id, row);
        return row;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Merge fields into a row")
    Map<String, Object> patch(@PathVariable String collection, @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Map<String, Object> row = find(collection, id);
        row.putAll(body);
        row.put("id", id);
        return row;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a row")
    ResponseEntity<Void> delete(@PathVariable String collection, @PathVariable Long id) {
        find(collection, id);
        table(collection).remove(id);
        return ResponseEntity.noContent().build();
    }

    private Map<Long, Map<String, Object>> table(String collection) {
        return store.get(collection);
    }

    private Map<String, Object> find(String collection, Long id) {
        Map<String, Object> row = table(collection).get(id);
        if (row == null) {
            throw new ResourceNotFoundException(collection, id);
        }
        return row;
    }

    private static long idOf(Map<String, Object> row) {
        return ((Number) row.get("id")).longValue();
    }
}
