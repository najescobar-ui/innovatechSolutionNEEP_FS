package cl.duoc.innovatech.recursos.entity;

// Functional roles, distinct from the Keycloak platform roles (PM/DEV/DIR
// in internal docs §5). A Keycloak DEV may correspond to multiple functional
// profiles here (a DEV in Keycloak could be RolRecurso.QA on one project).
public enum RolRecurso {
    DEV,
    PM,
    QA,
    DESIGNER,
    DEVOPS
}
