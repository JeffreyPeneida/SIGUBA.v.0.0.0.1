export class UTIL {

    public static generarUsuario(nombre: string, id: number): string {
        const primerNombre = nombre.split(" ")[0];
        return primerNombre + "tec" + id.toString().padStart(3, "0");
    }

    public static generarPassword(): string {

        const minus = "abcdefghijklmnopqrstuvwxyz";
        const mayus = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        const numeros = "0123456789";

        const todo = minus + mayus + numeros;

        const longitud = 10 + Math.floor(Math.random() * 6); // 10 a 15

        let pass = "";

        // Garantizar al menos uno de cada tipo
        pass += minus.charAt(Math.floor(Math.random() * minus.length));
        pass += mayus.charAt(Math.floor(Math.random() * mayus.length));
        pass += numeros.charAt(Math.floor(Math.random() * numeros.length));

        // Completar el resto
        for (let i = pass.length; i < longitud; i++) {
            pass += todo.charAt(Math.floor(Math.random() * todo.length));
        }

        // Mezclar caracteres
        return this.mezclar(pass);
    }

    private static mezclar(input: string): string {
        const array = input.split("");

        for (let i = 0; i < array.length; i++) {
            const j = Math.floor(Math.random() * array.length);

            const temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        return array.join("");
    }

    public static async generarUsuarioTec(nombre: string, apellido: string): Promise<string> {

        // Normalizar (quitar acentos)
        nombre = nombre.normalize("NFD").replace(/[\u0300-\u036f]/g, "");
        apellido = apellido.normalize("NFD").replace(/[\u0300-\u036f]/g, "");

        // Limpiar
        nombre = nombre.trim().toLowerCase().replace(/[^a-z]/g, "");
        apellido = apellido.trim().toLowerCase().replace(/[^a-z]/g, "");

        // Evitar vacíos
        if (nombre.length === 0) {
            nombre = "user";
        }
        if (apellido.length === 0) {
            apellido = "x";
        }

        const inicialApellido = apellido.substring(0, 1);

        const base = "tec" + nombre + inicialApellido;

        let usuarioFinal = base;
        let contador = 1;

        while (true) {

            //SQL a modificar
            const response = await fetch(`/api/tecnico/existe/${usuarioFinal}`);

            const data = await response.json();

            if (data.existe === false) {
                break;
            }

            usuarioFinal = base + contador.toString().padStart(2, "0");
            contador++;
        }

        return usuarioFinal;
    }
}