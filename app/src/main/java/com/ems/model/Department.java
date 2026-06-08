package com.ems.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Entity
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Department name is required")
    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    // EAGER = load employees together with department in same query
    // Fine for our use case since we always show employee count
    @OneToMany(mappedBy = "department", fetch = FetchType.EAGER)
    private List<Employee> employees;
}
