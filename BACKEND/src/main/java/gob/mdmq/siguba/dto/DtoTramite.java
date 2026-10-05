package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import gob.mdmq.siguba.Entidades.AreaInspeccion;
import gob.mdmq.siguba.Entidades.Barrio;
import gob.mdmq.siguba.Entidades.Denunciante;
import gob.mdmq.siguba.Entidades.Dependencia;
import gob.mdmq.siguba.Entidades.Especie;
import gob.mdmq.siguba.Entidades.MotivoInspeccion;
import gob.mdmq.siguba.Entidades.Tecnico;
import gob.mdmq.siguba.Entidades.TipoEvidencia;
import gob.mdmq.siguba.Entidades.Ubicacion;
import gob.mdmq.siguba.Entidades.Usuario;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoTramite {

    private Long idCodigoInterno;
    private String idTramite;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaDenuncia;

    private String narracion;
    private BigDecimal latitud;
    private BigDecimal longitud;

    @Temporal(TemporalType.DATE)
    private Date fechaInspeccion;

    @Temporal(TemporalType.TIME)
    private Date horaInspeccion;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaAsignacionTecnico;

    private Denunciante denunciante;
    private Tecnico tecnico;
    private Ubicacion ubicacion;
    private Dependencia dependencia;
    private AreaInspeccion areaInspeccion;
    private MotivoInspeccion motivoInspeccion;
    private TipoEvidencia tipoEvidencia;
    private Usuario usuario;
    private Barrio barrio;
    private List<Especie> especies;

    private String descripcion;
    private String conclusiones;
    private String recomendaciones;
    private String estado;

    public DtoTramite() {
    }

    public DtoTramite(Long idCodigoInterno,
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