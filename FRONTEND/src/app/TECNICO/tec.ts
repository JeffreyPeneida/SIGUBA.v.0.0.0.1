export class TEC {

  constructor(
    private nombre: string,
    private apellido: string,
    private cedula: string,
    private usuario: string,
    private correo: string,
    private password: string
  ) {}

  getNombre(): string {
    return this.nombre;
  }

  getApellido(): string {
    return this.apellido;
  }

  getCedula(): string {
    return this.cedula;
  }

  getUsuario(): string {
    return this.usuario;
  }

  getMail(): string {
    return this.correo;
  }

  getPassword(): string {
    return this.password;
  }

}
