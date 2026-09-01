package developer.fullstack.gestordocumento.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "authorized_systems")
public class SystemAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String systemCode; // Ej: "CRM", "ERP", "PORTAL_WEB"

    @Column(nullable = false)
    private String description;

    private Boolean active = true;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSystemCode() { return systemCode; }
    public void setSystemCode(String systemCode) { this.systemCode = systemCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
