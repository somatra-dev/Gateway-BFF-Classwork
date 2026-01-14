package auth.res_server.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // stores "ROLE_USER", "ROLE_ADMIN"

    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "roles_authorities",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    private List<Authority> authorities = new ArrayList<>();

    // Helper to extract short name
    public String getShortName() {
        if (name == null) return null;
        return name.startsWith("ROLE_") ? name.substring(5) : name;
    }

    // Optional: constructor for seeding
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}