package developer.fullstack.gestordocumento.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, unique = true)
    private String storagePath;

    @Column(nullable = false)
    private String contentType;

    private Long size;

    // 1. Campo email agregado al documento
    @Column(name = "owner_email")
    private String email;

    // Relación opcional con la entidad Usuario que lo subió
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id")
    private UserEntity uploadedBy;

    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters y Setters...
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserEntity getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UserEntity uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}