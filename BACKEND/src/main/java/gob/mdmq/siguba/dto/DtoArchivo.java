package gob.mdmq.siguba.dto;

/** Resultado de subir un archivo: la ruta que se guarda y la URL para verlo. */
public class DtoArchivo {

    private String ruta;
    private String url;
    private String nombreOriginal;
    private long tamano;

    public DtoArchivo() {
    }

    public DtoArchivo(String ruta, String url, String nombreOriginal, long tamano) {
        this.ruta = ruta;
        this.url = url;
        this.nombreOriginal = nombreOriginal;
        this.tamano = tamano;
    }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getNombreOriginal() { return nombreOriginal; }
    public void setNombreOriginal(String nombreOriginal) { this.nombreOriginal = nombreOriginal; }

    public long getTamano() { return tamano; }
    public void setTamano(long tamano) { this.tamano = tamano; }
}
