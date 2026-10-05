import { USUARIO } from "../USUARIO/user";

export class UsuarioDAO {

    // ================= INSERTAR =================
    async insertarUsuario(usuario: USUARIO): Promise<boolean> {

        try {

            const response = await fetch("/api/usuario", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nombre: usuario.getNombre(),
                    apellido: usuario.getApellido(),
                    cedula: usuario.getCedula(),
                    fecha_nacimiento: usuario.getFechaNacimiento(),
                    usuario: usuario.getUsuario(),
                    mail: usuario.getMail(),
                    admin_zonal: usuario.getAdminZonal(),
                    password: usuario.getPassword()
                })
            });

            return response.ok;

        } catch (e: any) {
            console.log("Error insertando usuario:", e.message);
            return false;
        }
    }

    // ================= BUSCAR POR CEDULA =================
    async buscarPorCedula(cedula: string): Promise<USUARIO | null> {

        try {

            const response = await fetch(`/api/usuario/cedula/${cedula}`);

            if (!response.ok) return null;

            const data = await response.json();

            return new USUARIO(
                data.nombre,
                data.apellido,
                data.cedula,
                data.usuario,
                data.mail,
                data.admin_zonal,
                data.password,
                new Date(data.fecha_nacimiento)
            );

        } catch (e: any) {
            console.log("Error buscando usuario por cédula:", e.message);
            return null;
        }
    }

    // ================= OBTENER TODOS =================
    async obtenerUsuarios(): Promise<USUARIO[]> {

        const lista: USUARIO[] = [];

        try {

            const response = await fetch("/api/usuario");

            if (!response.ok) return lista;

            const data = await response.json();

            for (const item of data) {

                const u = new USUARIO(
                    item.nombre,
                    item.apellido,
                    item.cedula,
                    item.usuario,
                    item.mail,
                    item.admin_zonal,
                    item.password,
                    new Date(item.fecha_nacimiento)
                );

                lista.push(u);
            }

        } catch (e: any) {
            console.log("Error buscando usuarios:", e.message);
        }

        return lista;
    }

    // ================= ACTUALIZAR =================
    async actualizarUsuario(id: number, u: USUARIO): Promise<boolean> {

        try {

            const response = await fetch(`/api/usuario/${id}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    nombre: u.getNombre(),
                    apellido: u.getApellido(),
                    cedula: u.getCedula(),
                    fecha_nacimiento: u.getFechaNacimiento(),
                    usuario: u.getUsuario(),
                    mail: u.getMail(),
                    admin_zonal: u.getAdminZonal(),
                    password: u.getPassword()
                })
            });

            return response.ok;

        } catch (e) {
            console.log("Error actualizando usuario:", e);
            return false;
        }
    }

    // ================= ELIMINAR =================
    async eliminarUsuario(cedula: string): Promise<boolean> {

        try {

            const response = await fetch(`/api/usuario/cedula/${cedula}`, {
                method: "DELETE"
            });

            return response.ok;

        } catch (e: any) {
            console.log("Error eliminando usuario:", e.message);
            return false;
        }
    }

    // ================= LOGIN =================
    async login(usuario: string, passwordIngresada: string): Promise<string | null> {

        try {

            const response = await fetch("/api/usuario/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    usuario: usuario,
                    password: passwordIngresada
                })
            });

            if (!response.ok) return null;

            const data = await response.json();

            return data.rol || null;

        } catch (e: any) {
            console.log("Error login:", e.message);
            return null;
        }
    }
}