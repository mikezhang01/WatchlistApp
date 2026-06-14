import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./app/**/*.{ts,tsx}", "./components/**/*.{ts,tsx}", "./lib/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#202124",
        panel: "#f6f3ef",
        line: "#d8d2c7",
        accent: "#0f766e",
        berry: "#9f1239"
      }
    }
  },
  plugins: []
};

export default config;

