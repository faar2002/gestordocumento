package developer.fullstack.gestordocumento.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    private String middleName; // Opcional

    @Column(nullable = false)
    private String lastName;

    private String secondLastName; // Opcional

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // Se almacenará encriptada (BCrypt)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private CompanyEntity company;

    @ManyToMany
    @JoinTable(
        name = "user_authorized_systems",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "system_id")
    )
    private Set<SystemAccessEntity> authorizedSystems;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSecondLastName() { return secondLastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public CompanyEntity getCompany() { return company; }
    public void setCompany(CompanyEntity company) { this.company = company; }

    public Set<SystemAccessEntity> getAuthorizedSystems() { return authorizedSystems; }
    public void setAuthorizedSystems(Set<SystemAccessEntity> authorizedSystems) { this.authorizedSystems = authorizedSystems; }
}