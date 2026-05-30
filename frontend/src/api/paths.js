/** Chemins API (évite les confusions, ex. GET /rendez-vous réservé aux admins). */
export const RDV_MES = "/rendez-vous/mes-rendez-vous";
export const RDV_MON_AGENDA = "/rendez-vous/mon-agenda";
export const RDV_TOUS_ADMIN = "/rendez-vous/tous";
export const rdvReprogrammer = (id) => `/rendez-vous/${id}/reprogrammer`;
