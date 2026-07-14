/**
 * Site routes, declared once and strongly typed.
 *
 * Use `nav.*` instead of loose string literals when navigating or declaring
 * routes: the editor autocompletes the available paths and the compiler flags
 * any route that does not exist. Changing a URL here updates it everywhere.
 */
export const nav = {
  dashboard: "/",
  projects: "/projects",
  resources: "/resources",
  tasks: "/tasks",
  analytics: "/analytics",
  login: "/login",
  register: "/register",
} as const;

/** Union of every valid route, e.g. "/" | "/projects" | "/tasks" | ... */
export type AppRoute = (typeof nav)[keyof typeof nav];
