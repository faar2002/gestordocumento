package developer.fullstack.gestordocumento.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaccion_firma")
public class TransaccionFirmaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID uuid = UUID.randomUUID(); // Inicializado automáticamente para evitar errores de nulos

    // Relación Many-to-One con el documento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "nombre_firmante", nullable = false)
    private String nombreFirmante;

    @Column(name = "email_firmante", nullable = false)
    private String emailFirmante;

    @Column(name = "orden")
    private Integer orden; // Opcional: para definir el orden secuencial de los firmantes

    @Column(nullable = false)
    private String estado = "PENDIENTE"; // PENDIENTE, FIRMADO

    @Column(name = "pdf_original_path")
    private String pdfOriginalPath;

    @Column(name = "pdf_firmado_path")
    private String pdfFirmadoPath;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Constructor vacío
    public TransaccionFirmaEntity() {}

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public DocumentEntity getDocument() { return document; }
    public void setDocument(DocumentEntity document) { this.document = document; }

    public String getNombreFirmante() { return nombreFirmante; }
    public void setNombreFirmante(String nombreFirmante) { this.nombreFirmante = nombreFirmante; }

    public String getEmailFirmante() { return emailFirmante; }
    public void setEmailFirmante(String emailFirmante) { this.emailFirmante = emailFirmante; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getPdfOriginalPath() { return pdfOriginalPath; }
    public void setPdfOriginalPath(String pdfOriginalPath) { this.pdfOriginalPath = pdfOriginalPath; }

    public String getPdfFirmadoPath() { return pdfFirmadoPath; }
    public void setPdfFirmadoPath(String pdfFirmadoPath) { this.pdfFirmadoPath = pdfFirmadoPath; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}