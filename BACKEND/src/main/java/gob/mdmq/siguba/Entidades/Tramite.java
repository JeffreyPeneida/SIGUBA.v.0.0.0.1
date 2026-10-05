package gob.mdmq.siguba.Entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "UBA_TRAMITE", schema = "PROYECTO_UBA")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Tramite implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CODIGO_INTERNO")
    private Long idCodigoInterno;

    @Column(name = "ID_TRAMITE", length = 10, unique = true)
    private String idTramite;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_DENUNCIA")
    private Date fechaDenuncia;

    @Lob
    @Column(name = "NARRACION")
    private String narracion;

    @Column(name = "LATITUD", precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(name = "LONGITUD", precision = 11, scale = 8)
    private BigDecimal longitud;

    @Temporal(TemporalType.DATE)
    @Column(name = "FECHA_INSPECCION")
    private Date fechaInspeccion;

    @Temporal(TemporalType.TIME)
    @Column(name = "HORA_INSPECCION")
    private Date horaInspeccion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_ASIGNACION_TECNICO")
    private Date fechaAsignacionTecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DENUNCIANTE")
    private Denunciante denunciante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TECNICO")
    private Tecnico tecnico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_UBICACION")
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DEPENDENCIA")
    private Dependencia dependencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_AREA")
    private AreaInspeccion areaInspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_MOTIVO")
    private MotivoInspeccion motivoInspeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_EVIDENCIA")
    private TipoEvidencia tipoEvidencia;

    /** Quien registro la denuncia. No viaja al navegador: la entidad lleva el hash de la contrasena. */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_REGISTRA")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BARRIO")
    private Barrio barrio;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "UBA_TRAMITE_ESPECIE",
        schema = "PROYECTO_UBA",
        joinColumns = @JoinColumn(name = "ID_CODIGO_INTERNO"),
        inverseJoinColumns = @JoinColumn(name = "ID_ESPECIE")
    )
    private List<Especie> especies;

    @JsonIgnore
    @OneToMany(mappedBy = "tramite", fetch = FetchType.LAZY,
               cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UsoRodenticida> usosRodenticida;
    
    @Column(name = "DESCRIPCION", length = 500)
    private String descripcion;

    @Column(name = "CONCLUSIONES", length = 500)
    private String conclusiones;

    @Column(name = "RECOMENDACIONES", length = 500)
    private String recomendaciones;

    @Column(name = "ESTADO", length = 20)
    private String estado;

    public Tramite() {
    }

    public Tramite(Long idCodigoInterno,
                   String idTramite,
                   Date fechaDenuncia,
                   String narracion,
                   BigDecimal latitud,
                   BigDecimal longitud,
                   Date fechaInspeccion,
                   Date horaInspeccion,
                   Date fechaAsignacionTecnico,
                   Denunciante denunciante,
                   Tecnico tecnico,
                   Ubicacion ubicacion,
                   Dependencia dependencia,
                   AreaInspeccion areaInspeccion,
                   MotivoInspeccion motivoInspeccion,
                   TipoEvidencia tipoEvidencia,
                   Usuario usuario,
                   Barrio barrio,
                   List<Especie> especies,
                   String descripcion,
                   String conclusiones,
                   String recomendaciones,
                   String estado) {

        this.idCodigoInterno = idCodigoInterno;
        this.idTramite = idTramite;
        this.fechaDenuncia = fechaDenuncia;
        this.narracion = narracion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.fechaInspeccion = fechaInspeccion;
        this.horaInspeccion = horaInspeccion;
        this.fechaAsignacionTecnico = fechaAsignacionTecnico;
        this.denunciante = denunciante;
        this.tecnico = tecnico;
        this.ubicacion = ubicacion;
        this.dependencia = dependencia;
        this.areaInspeccion = areaInspeccion;
        this.motivoInspeccion = motivoInspeccion;
        this.tipoEvidencia = tipoEvidencia;
        this.usuario = usuario;
        this.barrio = barrio;
        this.especies = especies;
        this.descripcion = descripcion;
        this.conclusiones = conclusiones;
        this.recomendaciones = recomendaciones;
        this.estado = estado;
    }

    public Long getIdCodigoInterno() {
        return idCodigoInterno;
    }

    public void setIdCodigoInterno(Long idCodigoInterno) {
        this.idCodigoInterno = idCodigoInterno;
    }

    public String getIdTramite() {
        return idTramite;
    }

    public void setIdTramite(String idTramite) {
        this.idTramite = idTramite;
    }

    public Date getFechaDenuncia() {
        return fechaDenuncia;
    }

    public void setFechaDenuncia(Date fechaDenuncia) {
        this.fechaDenuncia = fechaDenuncia;
    }

    public String getNarracion() {
        return narracion;
    }

    public void setNarracion(String narracion) {
        this.narracion = narracion;
    }

    public BigDecimal getLatitud() {
        return latitud;
    }

    public void setLatitud(BigDecimal latitud) {
        this.latitud = latitud;
    }

    public BigDecimal getLongitud() {
        return longitud;
    }

    public void setLongitud(BigDecimal longitud) {
        this.longitud = longitud;
    }

    public Date getFechaInspeccion() {
        return fechaInspeccion;
    }

    public void setFechaInspeccion(Date fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }

    public Date getHoraInspeccion() {
        return horaInspeccion;
    }

    public void setHoraInspeccion(Date horaInspeccion) {
        this.horaInspeccion = horaInspeccion;
    }

    public Date getFechaAsignacionTecnico() {
        return fechaAsignacionTecnico;
    }

    public void setFechaAsignacionTecnico(Date fechaAsignacionTecnico) {
        this.fechaAsignacionTecnico = fechaAsignacionTecnico;
    }

    public Denunciante getDenunciante() {
        return denunciante;
    }

    public void setDenunciante(Denunciante denunciante) {
        this.denunciante = denunciante;
    }

    public Tecnico getTecnico() {
        return tecnico;
    }

    public void setTecnico(Tecnico tecnico) {
        this.tecnico = tecnico;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Dependencia getDependencia() {
        return dependencia;
    }

    public void setDependencia(Dependencia dependencia) {
        this.dependencia = dependencia;
    }

    public AreaInspeccion getAreaInspeccion() {
        return areaInspeccion;
    }

    public void setAreaInspeccion(AreaInspeccion areaInspeccion) {
        this.areaInspeccion = areaInspeccion;
    }

    public MotivoInspeccion getMotivoInspeccion() {
        return motivoInspeccion;
    }

    public void setMotivoInspeccion(MotivoInspeccion motivoInspeccion) {
        this.motivoInspeccion = motivoInspeccion;
    }

    public TipoEvidencia getTipoEvidencia() {
        return tipoEvidencia;
    }

    public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {
        this.tipoEvidencia = tipoEvidencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public List<Especie> getEspecies() {
        return especies;
    }

    public void setEspecies(List<Especie> especies) {
        this.especies = especies;
    }

    public List<UsoRodenticida> getUsosRodenticida() {
        return usosRodenticida;
    }

    public void setUsosRodenticida(List<UsoRodenticida> usosRodenticida) {
        this.usosRodenticida = usosRodenticida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getConclusiones() {
        return conclusiones;
    }

    public void setConclusiones(String conclusiones) {
        this.conclusiones = conclusiones;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
    
}