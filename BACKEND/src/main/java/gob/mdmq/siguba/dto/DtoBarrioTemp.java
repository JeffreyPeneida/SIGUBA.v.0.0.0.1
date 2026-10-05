package gob.mdmq.siguba.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DtoBarrioTemp implements Comparable<DtoBarrioTemp> {

    private String barrio;

    public DtoBarrioTemp() {
    }

    public DtoBarrioTemp(String barrio) {
        this.barrio = barrio;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    @Override
    public int compareTo(DtoBarrioTemp o) {

        if (o == null) {
            return 1;
        }

        if (this.barrio == null && o.barrio == null) {
            return 0;
        }

        if (this.barrio == null) {
            return -1;
        }

        if (o.barrio == null) {
            return 1;
        }

        return this.barrio.compareToIgnoreCase(o.barrio);
    }
}