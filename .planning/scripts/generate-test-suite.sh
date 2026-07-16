#!/usr/bin/env bash
set -u

usage() {
  cat <<'EOF'
Usage:
  bash .planning/scripts/generate-test-suite.sh --planning <NNN-slug> [--story story-NN] [--task task-NN] [--format markdown|json]
  bash .planning/scripts/generate-test-suite.sh --planning <NNN-slug> --all [--format markdown|json]

Generates deterministic TEST-SUITE.md artifacts from repository signals.
It does not execute tests.
EOF
}

planning_id=""
story_id=""
task_id=""
all_scopes=false
format="markdown"
generated_files=()
generation_failed=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --planning)
      planning_id="${2:-}"
      shift 2
      ;;
    --story)
      story_id="${2:-}"
      shift 2
      ;;
    --task)
      task_id="${2:-}"
      shift 2
      ;;
    --all)
      all_scopes=true
      shift
      ;;
    --format)
      format="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      printf 'Unknown argument: %s\n' "$1" >&2
      usage >&2
      exit 2
      ;;
  esac
done

if [[ -z "$planning_id" ]]; then
  printf 'Missing --planning <NNN-slug>\n' >&2
  usage >&2
  exit 2
fi

case "$format" in
  markdown|json) ;;
  *)
    printf 'Invalid --format: %s\n' "$format" >&2
    usage >&2
    exit 2
    ;;
esac

root="$(pwd)"
planning_dir="$root/.planning/active/$planning_id"

if [[ ! -d "$planning_dir" ]]; then
  printf 'Planning not found: %s\n' "$planning_dir" >&2
  exit 1
fi

has_file() {
  [[ -f "$root/$1" ]]
}

has_glob() {
  find "$root" -path "$root/.planning" -prune -o -name "$1" -print -quit | grep -q .
}

contains() {
  local pattern="$1"
  shift
  grep -RIlE "$pattern" "$@" >/dev/null 2>&1
}

package_script_exists() {
  local script="$1"
  if [[ ! -f "$root/package.json" ]] || ! command -v node >/dev/null 2>&1; then
    return 1
  fi
  node -e "const p=require('$root/package.json'); process.exit(p.scripts && p.scripts['$script'] ? 0 : 1)" >/dev/null 2>&1
}

maven_profile_exists() {
  local profile="$1"
  [[ -f "$root/pom.xml" ]] && grep -Eq "<id>[[:space:]]*$profile[[:space:]]*</id>" "$root/pom.xml"
}

maven_has_cucumber() {
  [[ -f "$root/pom.xml" ]] && grep -Eqi "cucumber|gherkin" "$root/pom.xml"
}

has_gherkin_features() {
  find "$root" -path "$root/.planning" -prune -o -type f -name "*.feature" -print -quit | grep -q .
}

maven_acceptance_configuration() {
  if [[ ! -f "$root/pom.xml" ]]; then
    printf 'N/A — no Maven `pom.xml` detected.'
    return
  fi

  if maven_profile_exists "acceptanceTests"; then
    cat <<'EOF'
Detected Maven profile `acceptanceTests`.

- Verify that it runs Failsafe during `integration-test` and `verify`.
- Verify that Cucumber/Gherkin tests boot the packaged artifact, not an in-memory mocked application context.
- External dependencies must be redirected by configuration to local fakes or disposable containers.
EOF
    return
  fi

  if ! maven_has_cucumber && ! has_gherkin_features; then
    cat <<'EOF'
Maven detected, but no Cucumber/Gherkin dependency or `.feature` files were found.

If acceptance tests are required, add Cucumber/Gherkin features and create profile `acceptanceTests` with Failsafe.
EOF
    return
  fi

  cat <<'EOF'
Required Maven acceptance-test configuration scaffold:

1. Add profile `acceptanceTests` to `pom.xml`.

```xml
<profile>
  <id>acceptanceTests</id>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-failsafe-plugin</artifactId>
        <configuration>
          <includes>
            <include>**/*AcceptanceIT.java</include>
            <include>**/*CucumberIT.java</include>
          </includes>
          <systemPropertyVariables>
            <artifact.path>${project.build.directory}/${project.build.finalName}.jar</artifact.path>
            <spring.profiles.active>acceptance</spring.profiles.active>
          </systemPropertyVariables>
        </configuration>
        <executions>
          <execution>
            <goals>
              <goal>integration-test</goal>
              <goal>verify</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</profile>
```

2. Add a Cucumber/JUnit runner such as `src/test/java/.../AcceptanceCucumberIT.java`.

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.acceptance")
public class AcceptanceCucumberIT {
}
```

3. In the acceptance harness, start local fakes before the artifact boots:

- Database: Testcontainers PostgreSQL/MySQL/etc. when persistence must be real.
- HTTP dependencies: WireMock or MockServer.
- Queues/brokers: Testcontainers or a local fake, depending on protocol risk.

4. Start the packaged artifact as an external process with environment variables pointing to those fakes:

```text
SPRING_PROFILES_ACTIVE=acceptance
DATABASE_URL=<testcontainer-jdbc-url>
PAYMENTS_BASE_URL=<wiremock-url>
NOTIFICATIONS_BASE_URL=<wiremock-url>
```

The application must externalize dependency endpoints through configuration. Do not use `@MockBean` for this gate; `@MockBean` belongs in Spring-context integration tests, not artifact-level acceptance tests.
EOF
}

acceptance_dependency_inventory() {
  local compose_files="None detected"
  local env_files="None detected"
  local db_signals="None detected"
  local http_signals="None detected"
  local queue_signals="None detected"
  local storage_signals="None detected"
  local secret_signals="None detected"
  local internal_modules="None detected"

  compose_files="$(find "$root" -maxdepth 3 -path "$root/.planning" -prune -o -type f \( -name "docker-compose.yml" -o -name "compose.yml" -o -name "docker-compose.*.yml" -o -name "compose.*.yml" \) -print | sed "s|$root/||" | sort | paste -sd ', ' -)"
  [[ -n "$compose_files" ]] || compose_files="None detected"

  env_files="$(find "$root" -maxdepth 4 -path "$root/.planning" -prune -o -type f \( -name ".env.example" -o -name "application-*.yml" -o -name "application-*.yaml" -o -name "application-*.properties" \) -print | sed "s|$root/||" | sort | paste -sd ', ' -)"
  [[ -n "$env_files" ]] || env_files="None detected"

  if find "$root" -path "$root/.planning" -prune -o -type f \( -path "*/db/migration/*" -o -path "*/migrations/*" -o -name "schema.prisma" \) -print -quit | grep -q .; then
    db_signals="Migrations/schema detected; prefer Testcontainers or disposable local database with real migrations."
  elif [[ -f "$root/pom.xml" ]] && grep -Eqi "postgres|mysql|mariadb|jdbc|flyway|liquibase|hibernate|jpa" "$root/pom.xml"; then
    db_signals="Maven persistence dependencies detected; identify datasource env vars and prefer Testcontainers for acceptance."
  elif [[ -f "$root/package.json" ]] && grep -Eqi "prisma|typeorm|sequelize|mongoose|pg|mysql|sqlite" "$root/package.json"; then
    db_signals="Node persistence dependencies detected; identify datasource env vars and use container/fake DB as appropriate."
  fi

  if grep -RIlE "https?://|base[-_.]?url|WebClient|RestTemplate|Feign|axios|fetch\\(" "$root" --exclude-dir=.git --exclude-dir=.planning --exclude-dir=node_modules --exclude-dir=target >/dev/null 2>&1; then
    http_signals="HTTP client or base-url references detected; route each external API to WireMock, MockServer, or local fake."
  fi

  if grep -RIlE "kafka|rabbit|sqs|sns|pubsub|jms|amqp|queue|topic" "$root" --exclude-dir=.git --exclude-dir=.planning --exclude-dir=node_modules --exclude-dir=target >/dev/null 2>&1; then
    queue_signals="Queue/broker references detected; use Testcontainers or local fake depending on protocol risk."
  fi

  if grep -RIlE "s3|blob|bucket|minio|filesystem|file storage|storage" "$root" --exclude-dir=.git --exclude-dir=.planning --exclude-dir=node_modules --exclude-dir=target >/dev/null 2>&1; then
    storage_signals="Object/file storage references detected; use MinIO, temp directories, or local fake storage."
  fi

  if grep -RIlE "API_KEY|TOKEN|SECRET|PASSWORD|CLIENT_SECRET|DATABASE_URL|BASE_URL" "$root" --exclude-dir=.git --exclude-dir=.planning --exclude-dir=node_modules --exclude-dir=target >/dev/null 2>&1; then
    secret_signals="Environment variable or secret-like names detected; acceptance profile must provide non-production dummy values."
  fi

  if [[ -f "$root/pom.xml" ]] && grep -q "<modules>" "$root/pom.xml"; then
    internal_modules="Maven multi-module project detected; ensure dependent modules are built before acceptance tests."
  elif [[ -f "$root/package.json" ]] && grep -q "\"workspaces\"" "$root/package.json"; then
    internal_modules="Package workspaces detected; ensure internal packages are built/linked before acceptance tests."
  fi

  cat <<EOF
| Dependency Type | Detected Signals | Acceptance Strategy |
|-----------------|------------------|---------------------|
| Internal modules/packages | $internal_modules | Build from the same commit; do not call stale installed artifacts. |
| Runtime compose files | $compose_files | Use as fixture source or convert required services to Testcontainers/local fakes. |
| Environment/config files | $env_files | Map every required variable to acceptance-safe values before booting the artifact. |
| Database/persistence | $db_signals | Prefer real disposable DB via Testcontainers; run migrations/bootstrap before scenarios. |
| External HTTP services | $http_signals | Mock with WireMock/MockServer/local fake; assert expected requests and responses. |
| Queues/brokers/events | $queue_signals | Use Testcontainers for protocol fidelity or local fake for contract-only behavior. |
| Object/file storage | $storage_signals | Use MinIO, temp directories, or fake storage; clean data after scenarios. |
| Secrets/credentials | $secret_signals | Use dummy non-production values; never depend on real SaaS credentials. |

Acceptance setup must explicitly define build, boot, readiness, seed data, scenario execution, and teardown for every dependency above. If a dependency is intentionally out of scope, record the reason and the residual risk.
EOF
}

add_command_if() {
  local condition="$1"
  local command="$2"
  local file="$3"
  if eval "$condition"; then
    printf '%s\n' "$command" >> "$file"
  fi
}

commands_as_markdown() {
  local file="$1"
  local fallback="$2"
  if [[ -s "$file" ]]; then
    sed 's/^/`/; s/$/`<br>/' "$file" | tr -d '\n' | sed 's/<br>$//'
  else
    printf '%s' "$fallback"
  fi
}

first_existing_story_dir() {
  local id="$1"
  find "$planning_dir/02-deepening" -maxdepth 1 -type d -name "$id-*" 2>/dev/null | sort | head -1
}

first_existing_task_file() {
  local story_dir="$1"
  local id="$2"
  find "$story_dir" -maxdepth 1 -type f -name "$id-*.md" 2>/dev/null | sort | head -1
}

extract_task_context() {
  local task_file="$1"
  if [[ -z "$task_file" || ! -f "$task_file" ]]; then
    printf 'No specific task file selected.'
    return
  fi

  awk '
    /^## Technical Design/ { in_design=1; next }
    /^## / && in_design { exit }
    in_design && /Affected files \/ components/ { print; found=1 }
    END { if (!found) print "Affected files/components not declared explicitly." }
  ' "$task_file"
}

detect_commands() {
  local tmpdir="$1"

  : > "$tmpdir/unit"
  : > "$tmpdir/coverage"
  : > "$tmpdir/integration"
  : > "$tmpdir/acceptance"
  : > "$tmpdir/static"
  : > "$tmpdir/style"
  : > "$tmpdir/architecture"
  : > "$tmpdir/security"
  : > "$tmpdir/smoke"
  : > "$tmpdir/mutation"

  if [[ -f "$root/package.json" ]]; then
    package_script_exists test && printf 'npm test\n' >> "$tmpdir/unit"
    package_script_exists "test:unit" && printf 'npm run test:unit\n' >> "$tmpdir/unit"
    package_script_exists coverage && printf 'npm run coverage\n' >> "$tmpdir/coverage"
    package_script_exists "test:coverage" && printf 'npm run test:coverage\n' >> "$tmpdir/coverage"
    package_script_exists "test:integration" && printf 'npm run test:integration\n' >> "$tmpdir/integration"
    package_script_exists "test:e2e" && printf 'npm run test:e2e\n' >> "$tmpdir/acceptance"
    package_script_exists e2e && printf 'npm run e2e\n' >> "$tmpdir/acceptance"
    package_script_exists lint && printf 'npm run lint\n' >> "$tmpdir/static"
    package_script_exists typecheck && printf 'npm run typecheck\n' >> "$tmpdir/static"
    package_script_exists "check" && printf 'npm run check\n' >> "$tmpdir/static"
    package_script_exists "audit" && printf 'npm audit\n' >> "$tmpdir/security"
    package_script_exists "test:mutation" && printf 'npm run test:mutation\n' >> "$tmpdir/mutation"
    if grep -qE '"(@playwright/test|playwright)"|"cypress"' "$root/package.json"; then
      printf 'npx playwright test\n' >> "$tmpdir/acceptance"
    fi
  fi

  if [[ -x "$root/mvnw" || -f "$root/pom.xml" ]]; then
    local mvn="./mvnw"
    [[ -x "$root/mvnw" ]] || mvn="mvn"
    printf '%s test\n' "$mvn" >> "$tmpdir/unit"
    printf '%s verify\n' "$mvn" >> "$tmpdir/integration"
    printf '%s test jacoco:report\n' "$mvn" >> "$tmpdir/coverage"
    if maven_profile_exists "acceptanceTests"; then
      printf '%s -PacceptanceTests verify\n' "$mvn" >> "$tmpdir/acceptance"
    elif maven_has_cucumber || has_gherkin_features; then
      printf 'Configure Maven profile acceptanceTests, then run %s -PacceptanceTests verify for Cucumber/Gherkin acceptance tests\n' "$mvn" >> "$tmpdir/acceptance"
    fi
    [[ -f "$root/pom.xml" ]] && grep -q "sonar" "$root/pom.xml" && printf '%s sonar:sonar\n' "$mvn" >> "$tmpdir/static"
    [[ -f "$root/pom.xml" ]] && grep -q "checkstyle" "$root/pom.xml" && printf '%s checkstyle:check\n' "$mvn" >> "$tmpdir/style"
    [[ -f "$root/pom.xml" ]] && grep -q "spotbugs" "$root/pom.xml" && printf '%s spotbugs:check\n' "$mvn" >> "$tmpdir/static"
    [[ -f "$root/pom.xml" ]] && grep -q "pmd" "$root/pom.xml" && printf '%s pmd:check\n' "$mvn" >> "$tmpdir/static"
    [[ -f "$root/pom.xml" ]] && grep -qi "archunit" "$root/pom.xml" && printf '%s test -Dtest=*Architecture*,*ArchUnit*\n' "$mvn" >> "$tmpdir/architecture"
    [[ -f "$root/pom.xml" ]] && grep -qi "pitest" "$root/pom.xml" && printf '%s org.pitest:pitest-maven:mutationCoverage\n' "$mvn" >> "$tmpdir/mutation"
  fi

  if [[ -x "$root/gradlew" || -f "$root/build.gradle" || -f "$root/build.gradle.kts" ]]; then
    local gradle="./gradlew"
    [[ -x "$root/gradlew" ]] || gradle="gradle"
    printf '%s test\n' "$gradle" >> "$tmpdir/unit"
    printf '%s check\n' "$gradle" >> "$tmpdir/static"
    contains "jacoco" "$root/build.gradle"* && printf '%s jacocoTestReport\n' "$gradle" >> "$tmpdir/coverage"
    contains "sonarqube|sonar" "$root/build.gradle"* && printf '%s sonarqube\n' "$gradle" >> "$tmpdir/static"
  fi

  if [[ -f "$root/pyproject.toml" || -f "$root/requirements.txt" ]]; then
    printf 'pytest\n' >> "$tmpdir/unit"
    printf 'pytest --cov\n' >> "$tmpdir/coverage"
    printf 'pytest tests/integration\n' >> "$tmpdir/integration"
    command -v uv >/dev/null 2>&1 && printf 'uv run pytest\n' >> "$tmpdir/unit"
    [[ -f "$root/pyproject.toml" ]] && grep -q "ruff" "$root/pyproject.toml" && printf 'ruff check .\n' >> "$tmpdir/static"
    [[ -f "$root/pyproject.toml" ]] && grep -q "mypy" "$root/pyproject.toml" && printf 'mypy .\n' >> "$tmpdir/static"
  fi

  if [[ -f "$root/go.mod" ]]; then
    printf 'go test ./...\n' >> "$tmpdir/unit"
    printf 'go test ./... -coverprofile=coverage.out\n' >> "$tmpdir/coverage"
    printf 'go test -tags=integration ./...\n' >> "$tmpdir/integration"
    printf 'go vet ./...\n' >> "$tmpdir/static"
    command -v golangci-lint >/dev/null 2>&1 && printf 'golangci-lint run\n' >> "$tmpdir/static"
  fi

  if [[ -f "$root/Cargo.toml" ]]; then
    printf 'cargo test\n' >> "$tmpdir/unit"
    printf 'cargo tarpaulin\n' >> "$tmpdir/coverage"
    printf 'cargo clippy -- -D warnings\n' >> "$tmpdir/static"
    printf 'cargo fmt --check\n' >> "$tmpdir/style"
  fi

  if has_glob "sonar-project.properties"; then
    printf 'sonar-scanner\n' >> "$tmpdir/static"
  fi

  if [[ -f "$root/docker-compose.yml" || -f "$root/compose.yml" ]]; then
    printf 'docker compose up -d\n' >> "$tmpdir/smoke"
    printf 'docker compose ps\n' >> "$tmpdir/smoke"
    printf 'docker compose up --build --abort-on-container-exit\n' >> "$tmpdir/acceptance"
  fi

  if find "$root" -path "$root/.planning" -prune -o -type f \( -name "*ArchitectureTest.*" -o -name "*ArchUnit*.*" \) -print -quit | grep -q .; then
    printf 'Run architecture tests matching *ArchitectureTest* or *ArchUnit* with the project test runner\n' >> "$tmpdir/architecture"
  fi

  if find "$root" -path "$root/.planning" -prune -o -type f \( -name "*checkstyle*" -o -name ".editorconfig" -o -name ".prettierrc*" -o -name "eslint.config.*" \) -print -quit | grep -q .; then
    printf 'Run configured formatter/linter and inspect style violations\n' >> "$tmpdir/style"
  fi
}

write_suite() {
  local scope="$1"
  local output="$2"
  local story_dir="${3:-}"
  local task_file="${4:-}"
  local tmpdir
  tmpdir="$(mktemp -d)"
  detect_commands "$tmpdir"

  local unit coverage integration acceptance static_analysis style architecture security smoke mutation maven_acceptance acceptance_dependencies
  unit="$(commands_as_markdown "$tmpdir/unit" "Add or identify the unit-test command for this stack.")"
  coverage="$(commands_as_markdown "$tmpdir/coverage" "Add coverage command or define minimum coverage evidence.")"
  integration="$(commands_as_markdown "$tmpdir/integration" "Add integration command that exercises real component boundaries; prefer Docker/Testcontainers/sandbox services.")"
  acceptance="$(commands_as_markdown "$tmpdir/acceptance" "Add acceptance/e2e command; prefer Docker or an isolated environment.")"
  static_analysis="$(commands_as_markdown "$tmpdir/static" "Add static analysis command: typecheck, lint, SonarQube, SpotBugs, PMD, Ruff, mypy, go vet, clippy, etc.")"
  style="$(commands_as_markdown "$tmpdir/style" "Add code-style command or explicit style-guide review checklist.")"
  architecture="$(commands_as_markdown "$tmpdir/architecture" "Add automated architecture checks when available; otherwise review DDD, Hexagonal, DRY, SOLID, and GoF usage against project guides.")"
  security="$(commands_as_markdown "$tmpdir/security" "Add dependency/security scan when available.")"
  smoke="$(commands_as_markdown "$tmpdir/smoke" "Use .planning/SMOKE-TESTS.md or define local runtime smoke checks.")"
  mutation="$(commands_as_markdown "$tmpdir/mutation" "Optional: add mutation testing when configured.")"
  maven_acceptance="$(maven_acceptance_configuration)"
  acceptance_dependencies="$(acceptance_dependency_inventory)"

  mkdir -p "$(dirname "$output")"
  cat > "$output" <<EOF
# Test Suite — $scope

> Generated by \`.planning/scripts/generate-test-suite.sh\`.
> This file is deterministic scaffolding. Prefer repository commands over AI-generated test plans; use AI only to fill gaps the script cannot infer.

---

## Scope

- **Planning:** \`$planning_id\`
- **Story:** ${story_id:-all stories}
- **Task:** ${task_id:-all tasks}
- **Generated at:** $(date +%Y-%m-%d)
- **Task context:** $(extract_task_context "$task_file")

---

## Quality Gates

| Gate | Command or Evidence | Automation Level | Required When |
|------|---------------------|------------------|---------------|
| Unit tests | $unit | Automated when command exists | Any code behavior changes |
| Coverage | $coverage | Automated when command exists | New/changed logic, risk-sensitive branches |
| Integration tests | $integration | Automated preferred | Components communicate through real adapters, DB, queues, HTTP, files, or generated clients |
| Acceptance / e2e tests | $acceptance | Automated preferred | User-visible workflows, APIs, CLIs, deployment paths; for Maven services prefer Cucumber/Gherkin through profile \`acceptanceTests\`, booting the artifact in isolation and mocking external dependencies |
| Static analysis | $static_analysis | Automated preferred | Every software task before PR review |
| Code style / formatting | $style | Automated or checklist | Every changed source file |
| Architecture and design guides | $architecture | Automated if ArchUnit/equivalent exists; otherwise checklist | DDD, Hexagonal, DRY, SOLID, GoF, module boundaries, dependency direction |
| Runtime smoke | $smoke | Automated or documented manual evidence | App/service/worker startup, migrations, connectivity, changed surface |
| Security / dependency scan | $security | Automated when configured | Dependency, auth, input handling, secret, or deployment changes |
| Mutation / test strength | $mutation | Optional automated | Critical business rules, safety/security logic, complex branching |

---

## Independent Acceptance Environment

- Prefer Docker Compose, Testcontainers, local emulators, or a sandbox profile over shared developer services.
- For Maven services with Cucumber/Gherkin, prefer a dedicated \`acceptanceTests\` profile that packages/boots the artifact in an isolated environment and mocks external dependencies with local fakes, WireMock, MockServer, Testcontainers, or equivalent fixtures.
- Record exact environment variables, seed data, ports, and teardown commands.
- Tests must be non-destructive or use disposable data.

---

## Acceptance Dependency Inventory

$acceptance_dependencies

---

## Maven Acceptance Test Configuration

$maven_acceptance

---

## Coverage And Evidence

| Target | Minimum Evidence |
|--------|------------------|
| Planning | Each story has an executable or explicitly manual quality gate set |
| Story | Each task maps to at least unit/static/smoke evidence, plus integration or acceptance when component boundaries are crossed |
| Task | The task PR includes command output or linked CI evidence for every applicable gate |

---

## Gaps To Fill Manually

- [ ] Confirm commands are correct for this repository and adjust placeholders.
- [ ] Add task-specific test names, coverage thresholds, fixtures, and data setup.
- [ ] Complete the acceptance dependency inventory: every internal module, database, broker, HTTP service, storage dependency, credential, port, seed dataset, readiness check, and teardown action must have a concrete acceptance strategy.
- [ ] Link code-style or architecture guide files that govern this scope.
- [ ] Decide which optional gates are required for this task/story based on risk.
EOF

  rm -rf "$tmpdir"
  generated_files+=("$output")
}

generate_for_scope() {
  local scope_story="$1"
  local scope_task="$2"
  local story_dir=""
  local task_file=""
  local output=""
  local scope_label=""

  story_id="$scope_story"
  task_id="$scope_task"

  if [[ -z "$story_id" ]]; then
    output="$planning_dir/TEST-SUITE.md"
    scope_label="planning $planning_id"
  else
    story_dir="$(first_existing_story_dir "$story_id")"
    if [[ -z "$story_dir" ]]; then
      printf 'Story not found for %s under %s\n' "$story_id" "$planning_dir/02-deepening" >&2
      return 1
    fi
    if [[ -z "$task_id" ]]; then
      output="$story_dir/TEST-SUITE.md"
      scope_label="story $story_id"
    else
      task_file="$(first_existing_task_file "$story_dir" "$task_id")"
      if [[ -z "$task_file" ]]; then
        printf 'Task not found for %s under %s\n' "$task_id" "$story_dir" >&2
        return 1
      fi
      local task_base
      task_base="$(basename "$task_file" .md)"
      output="$story_dir/test-suites/$task_base-test-suite.md"
      scope_label="task $task_id"
    fi
  fi

  write_suite "$scope_label" "$output" "$story_dir" "$task_file"
}

if [[ "$all_scopes" == true ]]; then
  if [[ -n "$story_id" ]]; then
    selected_story="$story_id"
    generate_for_scope "$selected_story" "" || generation_failed=true
    selected_story_dir="$(first_existing_story_dir "$selected_story")"
    if [[ -n "$selected_story_dir" ]]; then
      while IFS= read -r task; do
        task_base="$(basename "$task")"
        if [[ "$task_base" =~ ^(task-[0-9]+)- ]]; then
          generate_for_scope "$selected_story" "${BASH_REMATCH[1]}" || generation_failed=true
        fi
      done < <(find "$selected_story_dir" -maxdepth 1 -type f -name 'task-*.md' 2>/dev/null | sort)
    fi
  else
    generate_for_scope "" "" || generation_failed=true
    while IFS= read -r dir; do
      base="$(basename "$dir")"
      if [[ "$base" =~ ^(story-[0-9]+)- ]]; then
        sid="${BASH_REMATCH[1]}"
        generate_for_scope "$sid" "" || generation_failed=true
        while IFS= read -r task; do
          task_base="$(basename "$task")"
          if [[ "$task_base" =~ ^(task-[0-9]+)- ]]; then
            generate_for_scope "$sid" "${BASH_REMATCH[1]}" || generation_failed=true
          fi
        done < <(find "$dir" -maxdepth 1 -type f -name 'task-*.md' | sort)
      fi
    done < <(find "$planning_dir/02-deepening" -maxdepth 1 -type d -name 'story-*' 2>/dev/null | sort)
  fi
else
  generate_for_scope "$story_id" "$task_id" || generation_failed=true
fi

json_escape() {
  local value="$1"
  value="${value//\\/\\\\}"
  value="${value//\"/\\\"}"
  value="${value//$'\n'/\\n}"
  printf '%s' "$value"
}

rel_path() {
  local path="$1"
  printf '%s' "${path#$root/}"
}

print_report() {
  local file rel i

  if [[ "$format" == "json" ]]; then
    printf '{'
    printf '"planning":"%s",' "$(json_escape "$planning_id")"
    printf '"story":"%s",' "$(json_escape "$story_id")"
    printf '"task":"%s",' "$(json_escape "$task_id")"
    printf '"all":%s,' "$all_scopes"
    printf '"generated":['
    for i in "${!generated_files[@]}"; do
      [[ "$i" == "0" ]] || printf ','
      rel="$(rel_path "${generated_files[$i]}")"
      printf '"%s"' "$(json_escape "$rel")"
    done
    printf ']}'
    printf '\n'
    return
  fi

  printf '# Test Suite Generation\n\n'
  printf 'Planning: `%s`\n\n' "$planning_id"
  if [[ "${#generated_files[@]}" -eq 0 ]]; then
    printf 'No files generated.\n'
    return
  fi
  printf 'Generated or refreshed files:\n\n'
  for file in "${generated_files[@]}"; do
    printf '%s\n' "- \`$(rel_path "$file")\`"
  done
  printf '\nReview each file and fill any repository-specific gaps before relying on the suite as execution evidence.\n'
}

print_report

if [[ "$generation_failed" == true ]]; then
  exit 1
fi
