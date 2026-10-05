export class USUARIO {

    private nombre: string;
    private apellido: string;
    private cedula: string;
    private fechaNacimiento: Date;

    private usuario: string;
    private mail: string;
    private adminZonal: string;
    private password: string;

    constructor(
        nombre: string,
        apellido: string,
        cedula: string,
        usuario: string,
        mail: string,
        adminZonal: string,
        password: string,
        fechaNacimiento: Date
    ) {

        this.nombre = nombre;
        this.apellido = apellido;
        this.cedula = cedula;
        this.usuario = usuario;
        this.mail = mail;
        this.adminZonal = adminZonal;
        this.password = password;
        this.fechaNacimiento = fechaNacimiento;
    }

    // ===== GETTERS =====

    getNombre(): string {
        return this.nombre;
    }

    getApellido(): string {
        return this.apellido;
    }

    getCedula(): string {
        return this.cedula;
    }

    getFechaNacimiento(): Date {
        return this.fechaNacimiento;
    }

    getUsuario(): string {
        return this.usuario;
    }

    getMail(): string {
        return this.mail;
    }

    getAdminZonal(): string {
        return this.adminZonal;
    }

    getPassword(): string {
        return this.password;
    }

    // ===== SETTERS =====

    setUsuario(usuario: string): void {
        this.usuario = usuario;
    }

    setMail(mail: string): void {
        this.mail = mail;
    }

    setAdminZonal(adminZonal: string): void {
        this.adminZonal = adminZonal;
    }

    setPassword(password: string): void {
        this.password = password;
    }
}