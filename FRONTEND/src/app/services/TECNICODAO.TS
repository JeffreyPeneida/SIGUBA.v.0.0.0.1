import { TEC } from "../TECNICO/TEC";

export class TecnicoDAO {

    async buscarPorCedula(cedula: string): Promise<TEC | null> {

        try {

            const response = await fetch(`/api/tecnico/cedula/${cedula}`);

            if (!response.ok) return null;

            const data = await response.json();

            return new TEC(
                data.nombre,
                data.apellido,
                data.cedula,
                data.tecnico_usuario,
                data.correo,
                data.password
            );

        } catch (e: any) {

            console.log("Error buscando técnico por cédula:", e.message);
            return null;

        }
    }

    async buscarPorId(idTecnico: number): Promise<TEC | null> {

        try {

            const response = await fetch(`/api/tecnico/${idTecnico}`);

            if (!response.ok) return null;

            const data = await response.json();

            return new TEC(
                data.nombre,
                data.apellido,
                data.cedula,
                data.tecnico_usuario,
                data.correo,
                data.password
            );

        } catch (e: any) {

            console.log("Error buscando técnico por ID:", e.message);
            return null;

        }
    }

    async actualizarTecnico(id: number, t: TEC): Promise<boolean> {

        try {

            const response = await fetch(`/api/tecnico/${id}`, {

                method: "PUT",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    nombre: t.getNombre(),
                    apellido: t.getApellido(),
                    cedula: t.getCedula(),
                    tecnico_usuario: t.getUsuario(),
                    correo: t.getMail(),
                    password: t.getPassword()
                })

            });

            return response.ok;

        } catch (e: any) {

            console.log("Error actualizando técnico:", e.message);
            return false;

        }
    }
}