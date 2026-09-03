# Comandos del Scaffold — API de Gestión de Franquicias

> Registro de los comandos ejecutados para generar y verificar el microservicio
> `franchise-api` con el scaffold Clean Architecture de Bancolombia.
> Plugin usado: `co.com.bancolombia.cleanArchitecture` **v4.0.5** (última disponible al momento: 4.6.1).

---

## 1. Verificación del entorno

```powershell
java -version      # requiere JDK 17+ (en este proyecto: Java 21)
gradle -version    # Gradle instalado globalmente
git --version
```

---

## 2. Archivos base (antes de generar)

En una carpeta vacía (`franchise-api/`) se crean estos dos archivos mínimos para aplicar el plugin:

```groovy
// build.gradle
plugins {
    id "co.com.bancolombia.cleanArchitecture" version "4.0.5"
}
```

```groovy
// settings.gradle
rootProject.name = 'franchise-api'
```

---

## 3. F0 — Generación del scaffold

```powershell
# Primer intento (FALLÓ: la flag --coverage no existe en 4.0.5)
gradle cleanArchitecture --package=co.com.nequi.franchise --type=reactive --name=FranchiseApi --coverage=jacoco --no-daemon

# Intento correcto (sin --coverage)
gradle cleanArchitecture --package=co.com.nequi.franchise --type=reactive --name=FranchiseApi --no-daemon --console=plain
```

- `--type=reactive` genera un proyecto Spring WebFlux (no bloqueante).
- Este comando también crea el Gradle wrapper (`gradlew` / `gradlew.bat`).

---

## 4. Generación de módulos

(Con el wrapper que el scaffold generó.)

```powershell
.\gradlew.bat generateModel --name=Franchise --console=plain --no-daemon
.\gradlew.bat generateModel --name=Branch --console=plain --no-daemon
.\gradlew.bat generateModel --name=Product --console=plain --no-daemon
.\gradlew.bat generateUseCase --name=Franchise --console=plain --no-daemon
.\gradlew.bat generateEntryPoint --type=webflux --console=plain --no-daemon
.\gradlew.bat generateDrivenAdapter --type=mongodb --console=plain --no-daemon
```

---

## 5. Compilaciones por módulo (verificación por fase)

```powershell
.\gradlew.bat :model:compileJava --console=plain --no-daemon
.\gradlew.bat :usecase:compileJava --console=plain --no-daemon
.\gradlew.bat :mongo-repository:compileJava --console=plain --no-daemon
.\gradlew.bat :reactive-web:compileJava --console=plain --no-daemon
```

---

## 6. Compilación y ensamblado integral

```powershell
.\gradlew.bat compileJava --console=plain --no-daemon
.\gradlew.bat assemble --console=plain --no-daemon
```

---

## 7. Arranque de la app (verificación de wiring del contexto Spring)

```powershell
.\gradlew.bat :app-service:bootRun --console=plain --no-daemon
```

---

## Notas útiles

- `--console=plain` y `--no-daemon` se usaron para logs limpios; **no son obligatorios**.
- La primera generación falló solo por `--coverage=jacoco` (no existe en v4.0.5); JaCoCo se agrega manualmente después.
- El scaffold detecta el JDK del entorno y fija el toolchain; en este proyecto se ajustó a Java 21 en `main.gradle` (`languageVersion = JavaLanguageVersion.of(21)`).
- El primer comando (`cleanArchitecture`) usa el `gradle` global; los siguientes usan el wrapper `.\gradlew.bat`.
- `bootRun` levanta en `:8080` aunque no haya MongoDB (la conexión es lazy) — sirve para validar el wiring de beans del contexto.

---

## Nombres de módulos Gradle generados

| Módulo Gradle       | Ruta                                              | Rol                          |
|---------------------|---------------------------------------------------|------------------------------|
| `model`             | `domain/model`                                    | Entidades + puertos          |
| `usecase`           | `domain/usecase`                                  | Casos de uso                 |
| `reactive-web`      | `infrastructure/entry-points/reactive-web`        | RouterFunctions + Handlers   |
| `mongo-repository`  | `infrastructure/driven-adapters/mongo-repository` | Adaptador MongoDB reactivo   |
| `app-service`       | `applications/app-service`                        | Bootstrap / wiring / main    |
