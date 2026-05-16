import type { Config } from "tailwindcss";

const rgb = (varName: string) => `rgb(var(--${varName}) / <alpha-value>)`;

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      fontFamily: {
        sans: ["Inter", "system-ui", "-apple-system", "Segoe UI", "Roboto", "sans-serif"],
        mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "Menlo", "Consolas", "monospace"],
      },
      colors: {
        bg:        rgb("bg"),
        surface:   rgb("surface"),
        surface2:  rgb("surface-2"),
        border:    rgb("border"),
        fg:        rgb("fg"),
        "fg-muted":  rgb("fg-muted"),
        "fg-subtle": rgb("fg-subtle"),
        accent:    rgb("accent"),
        "accent-fg": rgb("accent-fg"),
        success:   rgb("success"),
        warning:   rgb("warning"),
        danger:    rgb("danger"),
        info:      rgb("info"),
        role: {
          dev:      rgb("role-dev"),
          qa:       rgb("role-qa"),
          devops:   rgb("role-devops"),
          designer: rgb("role-designer"),
          pm:       rgb("role-pm"),
        },
      },
      fontSize: {
        xs:  ["12px", "16px"],
        sm:  ["13px", "18px"],
        base:["14px", "20px"],
        md:  ["16px", "22px"],
        lg:  ["20px", "26px"],
        xl:  ["28px", "32px"],
      },
      borderRadius: {
        sm: "4px",
        DEFAULT: "6px",
        md: "6px",
        lg: "8px",
      },
    },
  },
  plugins: [],
} satisfies Config;
