// Configuracion de desarrollo.
//
// url_api es relativa a proposito: en Docker nginx reenvia /api al backend y
// con `ng serve` lo hace proxy.conf.json. Asi el bundle no lleva host ni
// puerto embebidos y no hace falta CORS.
export const environment = {
  production: false,
  url_api: '/api',
};
