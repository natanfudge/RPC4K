module.exports = {
  root: true,
  env: {browser: true, es2020: true},
  parser: "@typescript-eslint/parser",
  parserOptions: {
    tsconfigRootDir: "",
    project: [
      "./tsconfig.json"
    ]
  },
  plugins: [
    "@typescript-eslint"
  ],
  extends: [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:@typescript-eslint/recommended-requiring-type-checking"
  ],
  ignorePatterns: ["vite.config.*", "*.js", "*.cjs", "*.config.ts" , "generated/*", "dist/*"],
  rules: {
    "@typescript-eslint/no-unnecessary-boolean-literal-compare": "warn",
    "@typescript-eslint/consistent-indexed-object-style": "warn",
    "require-atomic-updates": "warn",
    "array-callback-return": "warn",
    "curly": ["warn", "multi-line"],
    "default-param-last": "warn",
    "no-constructor-return": "error",
    "no-eval": "error",
    "no-extend-native": "off",
    "no-extra-bind": "warn",
    "no-extra-label": "warn",
    "no-implicit-coercion": "error",
    "no-implied-eval": "error",
    "no-invalid-this": "error",
    "no-iterator": "error",
    "no-new": "warn",
    "no-new-func": "warn",
    "no-new-wrappers": "error",
    "no-proto": "error",
    "no-return-assign": "error",
    "no-script-url": "error",
    "no-self-compare": "warn",
    "no-sequences": "error",
    "no-throw-literal": "error",
    "no-useless-call": "warn",
    "no-useless-concat": "warn",
    "@typescript-eslint/strict-boolean-expressions": [
      "error", {
        "allowString": false,
        "allowNumber": false,
        "allowNullableObject": false,
        "allowNullableEnum": false
      }
    ],
    "@typescript-eslint/no-namespace": "off",
    "@typescript-eslint/explicit-module-boundary-types": "off",
    "@typescript-eslint/no-non-null-assertion": "off",
    "@typescript-eslint/restrict-plus-operands": "off",
    "eqeqeq": "warn",
    "@typescript-eslint/no-unsafe-assignment": "off",
    "@typescript-eslint/no-empty-function": "off",
    "@typescript-eslint/no-unused-vars": "off",
    "@typescript-eslint/no-unsafe-member-access": "off",
    "@typescript-eslint/restrict-template-expressions": "off",
    "react/jsx-no-undef": "off",
    "@typescript-eslint/no-unsafe-call": "off",
    "@typescript-eslint/ban-ts-comment": "off",
    "@typescript-eslint/no-unsafe-return": "warn",
    "no-constant-condition": "warn",
    "@typescript-eslint/no-inferrable-types": "warn",
    "no-empty": "warn",
    "no-inner-declarations": "off",
    "prefer-const": "warn"
  }
}