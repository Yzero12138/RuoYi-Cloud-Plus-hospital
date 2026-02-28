# Nephrology Ledger Tree Parent Display Fix

## TL;DR
> **Summary**: Fix the nephrology ledger tree table so parent/header rows show `来源=表头` when `queryTarget=NONE`, and show the count as a numerator/denominator percent (or numerator-only when denominator is missing).
> **Deliverables**:
> - Ledger list UI: `NONE` source label renders as “表头”
> - Ledger list UI: parent/header count cell shows `xx.xx%` when denominator > 0; otherwise shows numerator only
> - Unit tests (vitest) covering formatter logic
> **Effort**: Short
> **Parallel**: YES - 2 waves
> **Critical Path**: Add formatter utilities/tests → Wire into ledger table cells → Run `pnpm test:unit`

## Context
### Original Request
- 台账菜单树表父节点展示数据不对。
- 标题表来源为 `NONE`，需要：来源为 `NONE` 时展示为“表头”。
- 计数字段需要显示“分子/分母”计算出的百分比；当分母没有值时只展示分子。

### Interview Summary
- No additional user preferences provided; this plan applies defaults (see “Defaults Applied”).

### Metis Review (gaps addressed)
- Guardrails added for divide-by-zero, >100% handling, and preserving existing drill-down affordances.
- Added explicit unit tests and a deterministic command-gated verification step.

## Work Objectives
### Core Objective
- Make the ledger list tree table display correct, business-friendly parent/header values without changing backend schemas.

### Deliverables
- UI mapping: `queryTarget === 'NONE'` displays as “表头” in the ledger list “来源” column.
- UI display: parent/header count cell displays percent computed from `numeratorCount` and `denominatorCount`.
- UI edge case: if denominator is missing/0, show numerator only (no percent sign).
- Add vitest unit tests for formatting/mapping functions.

### Definition of Done (verifiable)
- `pnpm -C ruoyi-plus-vben5-hospital test:unit` exits with code 0.
- Unit tests assert:
  - `NONE → 表头`
  - `1/3 → 33.33%` (2 decimals)
  - denominator missing/0 → numerator-only string with no `%`

### Must Have
- No backend API contract/type changes for `NephrologyLedgerCountVo`.
- No changes to existing backend aggregation (`aggregateCount`) behavior.

### Must NOT Have
- Do not clamp percent values to 100%.
- Do not introduce NaN/Infinity/undefined into rendered percent strings.
- Do not expand scope into full queryTarget localization (only special-case `NONE`).

## Verification Strategy
> ZERO HUMAN INTERVENTION — all verification is agent-executed.
- Test decision: tests-after, vitest
- Primary verification: `pnpm -C ruoyi-plus-vben5-hospital test:unit`
- Evidence: `.sisyphus/evidence/task-*-*.txt` (command logs) and `.sisyphus/evidence/task-*-*.png` (optional UI screenshot if a lightweight UI check is performed)

## Execution Strategy
### Parallel Execution Waves
Wave 1 (parallel): implement pure formatter utilities + unit tests; update UI count cell rendering.
Wave 2 (depends on Wave 1): update “来源” column formatter; run tests and quick static checks.

### Dependency Matrix (full)
- 1 blocks 2,3,4
- 2 blocks 4
- 3 blocks 4

### Agent Dispatch Summary
- Wave 1: 2 tasks (frontend + tests)
- Wave 2: 2 tasks (frontend + verification)

## TODOs
> Implementation + Test = ONE task. Never separate.
> EVERY task includes agent-executed QA scenarios.

- [ ] 1. Add Ledger Display Formatters + Unit Tests

  **What to do**:
  - Create a small pure-TS formatter module for the ledger list page:
    - New file: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/formatters.ts`
    - Export EXACT functions:
      - `export function formatLedgerSourceLabel(queryTarget?: string): string`
        - Returns `"表头"` when `queryTarget === 'NONE'`; otherwise returns `queryTarget ?? ''`.
      - `export function formatLedgerCountText(numerator?: number, denominator?: number, digits = 2): string`
        - Treat missing numerator/denominator as 0.
        - If `denominator <= 0`, return numerator-only string: `${numerator}` (no `%`).
        - Else return percent string: `((numerator/denominator)*100).toFixed(digits) + '%'`.
  - Implement the two required behaviors:
    - Source label mapping: `NONE → 表头`.
    - Count display mapping (via `formatLedgerCountText`): if `denominatorCount > 0` then percent string with 2 decimals and `%`; else numerator-only string (no `%`).
  - Add vitest unit tests covering happy path and edge cases:
    - New file: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/__tests__/formatters.test.ts`
    - Test matrix (EXACT assertions):
      - `formatLedgerSourceLabel('NONE') === '表头'`
      - `formatLedgerSourceLabel('HD_COMPLICATION_NUMERATOR') === 'HD_COMPLICATION_NUMERATOR'`
      - `formatLedgerCountText(1, 3) === '33.33%'`
      - `formatLedgerCountText(0, 5) === '0.00%'`
      - `formatLedgerCountText(5, 0) === '5'`
      - `formatLedgerCountText(5, undefined) === '5'`

  **Must NOT do**:
  - Do not depend on DOM/VxeGrid internals in these formatter tests.
  - Do not change backend models or API.

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: localized frontend + tests.
  - Skills: []

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 2,3,4 | Blocked By: none

  **References**:
  - Existing ledger view: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/index.vue`
  - Column defs: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/data.ts`
  - Frontend type fields available: `ruoyi-plus-vben5-hospital/apps/web-antd/src/api/data-center/nephrology/ledger/model.ts`
  - Test runner config: `ruoyi-plus-vben5-hospital/vitest.config.ts`

  **Acceptance Criteria**:
  - [ ] New unit test file added and passing under `pnpm -C ruoyi-plus-vben5-hospital test:unit`.
  - [ ] Tests include cases: `NONE`, `1/3`, denom=0, denom undefined/null.

  **QA Scenarios**:
  ```
  Scenario: Formatter mapping and percent computation
    Tool: Bash
    Steps:
      1) pnpm -C ruoyi-plus-vben5-hospital test:unit
    Expected:
      - Exit code 0
      - Tests for ledger formatters pass
    Evidence: .sisyphus/evidence/task-1-formatters-test-unit.txt

  Scenario: Denominator missing should not render percent
    Tool: Vitest (via Bash)
    Steps:
      1) Run the specific new test file (or full suite) and confirm assertion: output contains no '%'
    Expected:
      - For denom <= 0, rendered string equals numerator-only
    Evidence: .sisyphus/evidence/task-1-formatters-no-denom.txt
  ```

  **Commit**: YES | Message: `fix(nephrology-ledger): add display formatters and tests` | Files: formatter module + test file

- [ ] 2. Update Ledger Count Cell to Show Percent on Parent/Header Rows

  **What to do**:
  - Create a small dedicated cell component for indicator/header rows so it can be unit-tested without VxeGrid:
    - New file: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/components/IndicatorCountCell.vue`
    - Props (EXACT):
      - `numerator: number`
      - `denominator: number`
      - `onNumeratorClick?: () => void`
      - `onDenominatorClick?: () => void`
    - Rendering rules (EXACT):
      - If `denominator > 0`:
        - show primary line: `formatLedgerCountText(numerator, denominator)` (e.g. `33.33%`)
        - show secondary line: clickable "分子：{numerator}" if `onNumeratorClick` provided, otherwise a plain span
        - show secondary line: clickable "分母：{denominator}" if `onDenominatorClick` provided, otherwise a plain span
      - If `denominator <= 0`:
        - show ONLY one line: clickable "分子：{numerator}" if `onNumeratorClick` provided, otherwise a plain span
        - do NOT render any percent text and do NOT render any denominator line
  - Wire the ledger list count cell to use this component for indicator/header rows:
    - Update `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/index.vue`
    - Indicator detection (EXACT): treat as indicator if `row.nodeType === 'I' || (Array.isArray(row.children) && row.children.length > 0)`
    - Pass `numerator={row.numeratorCount ?? 0}` and `denominator={row.denominatorCount ?? 0}`
    - Use existing handlers:
      - `onNumeratorClick={() => toSummaryDetail(row, NODE_TYPE.numerator)}`
      - `onDenominatorClick={() => toSummaryDetail(row, NODE_TYPE.denominator)}`
  - Add a unit test for this component:
    - New file: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/__tests__/indicator-count-cell.test.ts`
    - Use `@vue/test-utils` + vitest.
    - Assertions (EXACT):
      - With numerator=1, denominator=3: rendered text contains `33.33%`, `分子：1`, `分母：3`
      - With numerator=5, denominator=0: rendered text contains `分子：5` and does NOT contain `%` and does NOT contain `分母：`

  **Must NOT do**:
  - Do not remove existing leaf-row click-to-detail behavior.
  - Do not change API fields or backend calculations.

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: single Vue template change.
  - Skills: []

  **Parallelization**: Can Parallel: YES | Wave 1 | Blocks: 4 | Blocked By: 1

  **References**:
  - Ledger count cell slot: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/index.vue`
  - Backend aggregates provided: `ruoyi-modules/ruoyi-data-center/src/main/java/org/dromara/datacenter/nephrology/service/impl/NephrologyLedgerServiceImpl.java`

  **Acceptance Criteria**:
  - [ ] `IndicatorCountCell.vue` exists and is used by `ledger/index.vue` for indicator/header rows.
  - [ ] Unit test `indicator-count-cell.test.ts` passes and verifies percent + no-denom behavior.
  - [ ] Existing summary-detail navigation still works (numerator/denominator callbacks are invoked from the cell).

  **QA Scenarios**:
  ```
  Scenario: Component render logic covered by unit tests
    Tool: Bash
    Steps:
      1) pnpm -C ruoyi-plus-vben5-hospital test:unit
    Expected:
      - Exit code 0
    Evidence: .sisyphus/evidence/task-2-ledger-countcell-test-unit.txt

  Scenario: Manual lightweight UI smoke (optional, if environment available)
    Tool: Bash
    Steps:
      1) Start web-antd locally (if already wired to a running backend): pnpm -C ruoyi-plus-vben5-hospital dev:antd
      2) Open ledger page and verify a known indicator row shows 'xx.xx%'
    Expected:
      - Count cell displays percent / numerator-only per rules
    Evidence: .sisyphus/evidence/task-2-ledger-countcell-smoke.txt
  ```

  **Commit**: YES | Message: `fix(nephrology-ledger): show percent on indicator rows` | Files: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/index.vue`, `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/components/IndicatorCountCell.vue`, `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/__tests__/indicator-count-cell.test.ts`

- [ ] 3. Update Ledger “Source” Column to Render NONE as Header Label

  **What to do**:
  - Update the ledger list column definition for `queryTarget` (title “来源”) to display:
    - If `queryTarget === 'NONE'`: “表头”
    - Otherwise: the original value
  - Implement via a VXE column `formatter` or a cell slot; prefer the minimal, local approach consistent with this repo.

  **Must NOT do**:
  - Do not rename the underlying field (`queryTarget` remains the data key).

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: single TS column config change.
  - Skills: []

  **Parallelization**: Can Parallel: YES | Wave 2 | Blocks: 4 | Blocked By: 1

  **References**:
  - Column defs: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/data.ts`
  - Constant meaning: `ruoyi-modules/ruoyi-data-center/src/main/java/org/dromara/datacenter/nephrology/constants/NephrologyLedgerConstants.java`

  **Acceptance Criteria**:
  - [ ] The “来源” cell renders “表头” when row.queryTarget is `NONE`.
  - [ ] Existing non-NONE queryTarget values render unchanged.

  **QA Scenarios**:
  ```
  Scenario: Source label mapping unit-tested
    Tool: Bash
    Steps:
      1) pnpm -C ruoyi-plus-vben5-hospital test:unit
    Expected:
      - Exit code 0
      - Test asserts NONE -> 表头
    Evidence: .sisyphus/evidence/task-3-ledger-source-test-unit.txt

  Scenario: No unintended mapping
    Tool: Vitest (via Bash)
    Steps:
      1) Assert a non-NONE code remains unchanged (e.g., 'HD_COMPLICATION_NUMERATOR')
    Expected:
      - Output equals original code
    Evidence: .sisyphus/evidence/task-3-ledger-source-non-none.txt
  ```

  **Commit**: YES | Message: `fix(nephrology-ledger): render NONE source as header` | Files: `ruoyi-plus-vben5-hospital/apps/web-antd/src/views/data-center/nephrology/ledger/data.ts`

- [ ] 4. Verification Run + Regression Check

  **What to do**:
  - Run unit tests.
  - Run a quick TypeScript check for the affected package if available (optional; keep time-bounded).

  **Recommended Agent Profile**:
  - Category: `quick` — Reason: command execution only.
  - Skills: []

  **Parallelization**: Can Parallel: NO | Wave 2 | Blocks: none | Blocked By: 1,2,3

  **References**:
  - Monorepo scripts: `ruoyi-plus-vben5-hospital/package.json`

  **Acceptance Criteria**:
  - [ ] `pnpm -C ruoyi-plus-vben5-hospital test:unit` exits with code 0.

  **QA Scenarios**:
  ```
  Scenario: Full unit test run
    Tool: Bash
    Steps:
      1) pnpm -C ruoyi-plus-vben5-hospital test:unit
    Expected:
      - Exit code 0
    Evidence: .sisyphus/evidence/task-4-test-unit.txt

  Scenario: Optional typecheck (time-boxed)
    Tool: Bash
    Steps:
      1) pnpm -C ruoyi-plus-vben5-hospital check:type
    Expected:
      - Exit code 0 (or skip if too slow / not configured in environment)
    Evidence: .sisyphus/evidence/task-4-check-type.txt
  ```

  **Commit**: NO

## Final Verification Wave (4 parallel agents, ALL must APPROVE)
- [ ] F1. Plan Compliance Audit — oracle
- [ ] F2. Code Quality Review — unspecified-high
- [ ] F3. UI/UX Fidelity Check — unspecified-high
- [ ] F4. Scope Fidelity Check — deep

## Commit Strategy
- Commit 1: formatter utilities + tests
- Commit 2: ledger count cell percent display + source label mapping (may be combined if small)

## Defaults Applied
- Percent format: 2 decimals, `toFixed(2)`; allow >100%.
- Indicator row detection: `nodeType === 'I'` OR `children?.length > 0`.
- Preserve existing summary-detail links under the percent line.

## Success Criteria
- Ledger list parent/header rows show percent per rules.
- Ledger list “来源” column shows “表头” instead of `NONE` for header rows.
- `pnpm -C ruoyi-plus-vben5-hospital test:unit` passes.
