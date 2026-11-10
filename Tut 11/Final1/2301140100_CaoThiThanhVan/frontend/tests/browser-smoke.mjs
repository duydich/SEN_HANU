import { chromium } from "@playwright/test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";

const url = process.env.FRONTEND_URL || "http://localhost:3000";
const executablePath =
  process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE ||
  (process.platform === "win32"
    ? "C:/Program Files/Google/Chrome/Application/chrome.exe"
    : undefined);
const browser = await chromium.launch({ headless: true, executablePath });
const context = await browser.newContext({
  viewport: { width: 1440, height: 1000 },
});
const page = await context.newPage();
const errors = [];
page.on("pageerror", (e) => errors.push(e.message));
const out = process.env.EVIDENCE_DIR || "test-results";
fs.mkdirSync(out, { recursive: true });
const runId = Date.now().toString().slice(-8);
const names = {
  student: "Browser Student " + runId,
  company: "Browser Co " + runId,
  job: "Browser Job " + runId,
  employee: "Browser Person " + runId,
};
const image = Buffer.from(
  "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=",
  "base64",
);
const button = (label) =>
  page.getByRole("button", { name: label, exact: true });
const dialog = () => page.getByRole("dialog");
async function settled() {
  await page.locator('[aria-busy="false"]').waitFor();
  await page.waitForTimeout(350);
}
async function login(user = "admin", password = "Tut11@2026") {
  await page.getByLabel("Username", { exact: true }).fill(user);
  await page.getByLabel("Password", { exact: true }).fill(password);
  await button("Sign in").click();
  await page.getByRole("navigation").waitFor();
  await settled();
}
async function navigate(name) {
  if (await button("Open menu").isVisible()) await button("Open menu").click();
  await page
    .getByRole("navigation")
    .getByRole("button", { name: new RegExp(name) })
    .click();
  await settled();
}
async function search(kind, text) {
  await page.getByLabel("Search " + kind).fill(text);
  await page.waitForTimeout(450);
  await settled();
}
async function save(name) {
  await dialog().getByRole("button", { name, exact: true }).click();
  await dialog().waitFor({ state: "hidden" });
  await settled();
}
async function remove(name) {
  await button("Delete " + name).click();
  await dialog()
    .getByRole("button", { name: "Delete record", exact: true })
    .click();
  await dialog().waitFor({ state: "hidden" });
  await settled();
}
async function screenshot(name) {
  await page.screenshot({
    path: path.join(out, name + ".png"),
    fullPage: true,
  });
}
try {
  await page.goto(url);
  await page.getByRole("heading", { name: "Welcome back" }).waitFor();
  await screenshot("01-login");
  await login();
  await screenshot("02-students-desktop");
  await button("Next page").click();
  await settled();
  assert.match(await page.locator(".pagination").innerText(), /Page 2/);
  await page.getByLabel("Sort records").selectOption("name");
  await settled();
  await button("Toggle sort direction").click();
  await settled();
  await button("Add student").click();
  await dialog().getByLabel("Full name", { exact: true }).fill(names.student);
  await dialog().getByLabel("Age", { exact: true }).fill("24");
  await save("Create student");
  await search("students", names.student);
  assert.equal(await page.locator("tbody tr").count(), 1);
  await button("View " + names.student).click();
  await dialog().getByText(names.student, { exact: true }).waitFor();
  await button("Close details").click();
  await button("Edit " + names.student).click();
  await dialog().getByLabel("Age", { exact: true }).fill("25");
  await save("Save changes");
  assert.match(await page.locator("tbody").innerText(), /25 years/);
  await remove(names.student);
  await navigate("Companies");
  await button("Add company").click();
  await dialog().getByLabel("Full name", { exact: true }).fill(names.company);
  await dialog()
    .getByLabel("Address", { exact: true })
    .fill("120 Nguyen Trai, Ha Noi");
  await dialog()
    .getByLabel("Upload image")
    .setInputFiles({ name: "logo.png", mimeType: "image/png", buffer: image });
  await dialog().getByText("Image selected", { exact: true }).waitFor();
  await save("Create company");
  await search("companies", names.company);
  await button("Edit " + names.company).click();
  await dialog()
    .getByLabel("Address", { exact: true })
    .fill("125 Nguyen Trai, Ha Noi");
  await save("Save changes");
  await navigate("Jobs");
  await button("Add job").click();
  await dialog().getByLabel("Job title", { exact: true }).fill(names.job);
  await dialog()
    .getByLabel("Description", { exact: true })
    .fill("Created by a real browser integration check.");
  await save("Create job");
  await search("jobs", names.job);
  await button("Edit " + names.job).click();
  await dialog()
    .getByLabel("Description", { exact: true })
    .fill("Updated through React.");
  await save("Save changes");
  await navigate("Employees");
  await button("Add employee").click();
  await dialog().getByLabel("Full name", { exact: true }).fill(names.employee);
  await dialog().getByLabel("Age", { exact: true }).fill("30");
  await dialog()
    .getByLabel("Email address", { exact: true })
    .fill("browser@example.com");
  await dialog().getByLabel("Address", { exact: true }).fill("Ha Noi");
  await dialog()
    .getByLabel("Company", { exact: true })
    .selectOption({ label: names.company });
  await dialog()
    .getByRole("checkbox", { name: names.job, exact: true })
    .check();
  await dialog()
    .getByLabel("Upload image")
    .setInputFiles({
      name: "avatar.png",
      mimeType: "image/png",
      buffer: image,
    });
  await dialog().getByText("Image selected", { exact: true }).waitFor();
  await save("Create employee");
  await search("employees", names.employee);
  assert.match(
    await page.locator("tbody").innerText(),
    new RegExp(names.company),
  );
  assert.match(await page.locator("tbody").innerText(), new RegExp(names.job));
  await button("Edit " + names.employee).click();
  await dialog().getByLabel("Age", { exact: true }).fill("31");
  await save("Save changes");
  await button("View " + names.employee).click();
  await dialog().locator("dd").filter({ hasText: "31" }).waitFor();
  await screenshot("03-employee-detail");
  await button("Close details").click();
  await navigate("Companies");
  await search("companies", names.company);
  await remove(names.company);
  assert.match(await page.getByRole("alert").innerText(), /Unassign employees/);
  await navigate("Jobs");
  await search("jobs", names.job);
  await remove(names.job);
  assert.match(await page.getByRole("alert").innerText(), /Remove this job/);
  await navigate("Employees");
  await search("employees", names.employee);
  await remove(names.employee);
  await navigate("Companies");
  await search("companies", names.company);
  await remove(names.company);
  await navigate("Jobs");
  await search("jobs", names.job);
  await remove(names.job);
  await navigate("Employees");
  await screenshot("04-employees-desktop");
  await page.setViewportSize({ width: 390, height: 844 });
  await screenshot("05-mobile");
  assert.ok(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
    "No page-level horizontal overflow on mobile",
  );
  await button("Open menu").click();
  await button("Sign out").click();
  await page.getByRole("heading", { name: "Welcome back" }).waitFor();
  await button("New here? Create an account").click();
  const newUser = "browser_" + runId;
  await page.getByLabel("Username", { exact: true }).fill(newUser);
  await page.getByLabel("Password", { exact: true }).fill("BrowserPass11!");
  await button("Create account").click();
  await page.getByRole("heading", { name: "Welcome back" }).waitFor();
  await login(newUser, "BrowserPass11!");
  assert.equal(
    await page
      .getByRole("button", { name: "Add student", exact: true })
      .count(),
    0,
  );
  assert.equal(await page.getByRole("button", { name: /^Edit / }).count(), 0);
  await screenshot("06-readonly");
  assert.deepEqual(errors, [], "No unhandled browser errors");
  console.log(
    "PASS: login, registration, all 4 CRUD modules, detail, relations, delete conflicts, image upload, sorting, pagination, mobile layout and view-only role.",
  );
} finally {
  await browser.close();
}
