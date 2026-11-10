import React, { useEffect, useState } from "react";
import {
  Users,
  GraduationCap,
  Building2,
  BriefcaseBusiness,
  Search,
  Plus,
  ArrowUpRight,
  ArrowDownUp,
  ChevronLeft,
  ChevronRight,
  Pencil,
  Trash2,
  X,
  LogOut,
  ShieldCheck,
  ArrowRight,
  Upload,
  Eye,
  Check,
  LoaderCircle,
  LayoutGrid,
  Menu,
} from "lucide-react";
import { api, refreshCsrf, errorMessage, imageURL, allRecords } from "./api";
import profile from "./profile";

const modules = {
  students: {
    title: "Students",
    singular: "student",
    icon: GraduationCap,
    description: "Keep student information organized and up to date.",
    label: "name",
    empty: { name: "", age: 18 },
  },
  employees: {
    title: "Employees",
    singular: "employee",
    icon: Users,
    description: "The people, teams and roles behind your organization.",
    label: "name",
    empty: {
      name: "",
      age: 22,
      email: "",
      address: "",
      image: "",
      companyId: "",
      jobIds: [],
    },
  },
  companies: {
    title: "Companies",
    singular: "company",
    icon: Building2,
    description: "Manage organizations and the people connected to them.",
    label: "name",
    empty: { name: "", address: "", image: "" },
  },
  jobs: {
    title: "Jobs",
    singular: "job",
    icon: BriefcaseBusiness,
    description: "Define the opportunities and responsibilities in your team.",
    label: "title",
    empty: { title: "", description: "" },
  },
};
const blankPage = { content: [], page: 0, totalElements: 0, totalPages: 0 };
function Spinner() {
  return <LoaderCircle className="spin" size={18} />;
}
function Field({ label, error, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error && <small className="field-error">{error}</small>}
    </label>
  );
}

function SignIn({ onLogin }) {
  const [register, setRegister] = useState(false),
    [values, setValues] = useState({ username: "", password: "" }),
    [busy, setBusy] = useState(false),
    [error, setError] = useState(""),
    [notice, setNotice] = useState("");
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await refreshCsrf();
      if (register) {
        await api.post("/api/auth/register", values);
        setRegister(false);
        setNotice("Your account is ready. Sign in to continue.");
        setValues({ ...values, password: "" });
      } else {
        const r = await api.post("/api/auth/login", values);
        await refreshCsrf();
        onLogin(r.data);
      }
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className={`login-page theme-${profile.theme}`}>
      <section className="login-story">
        <div className="brand">
          <span className="brand-mark">
            <LayoutGrid size={23} />
          </span>
          {profile.brand}
          <span className="brand-dot">.</span>
        </div>
        <div>
          <span className="eyebrow">A LITTLE MORE ORGANIZED</span>
          <h1>
            Good people.
            <br />
            Great possibilities.
          </h1>
          <p>
            One connected space for your students, people and organizations.
          </p>
          <div className="story-cards">
            <span>
              <GraduationCap /> Students
            </span>
            <span>
              <Users /> Employees
            </span>
            <span>
              <Building2 /> Companies
            </span>
            <span>
              <BriefcaseBusiness /> Jobs
            </span>
          </div>
        </div>
        <footer>
          SE1 / Tutorial 11 <span>React + Spring Boot</span>
        </footer>
      </section>
      <section className="login-form-area">
        <div className="login-card">
          <span className="eyebrow">YOUR WORKSPACE</span>
          <h2>{register ? "Create an account" : "Welcome back"}</h2>
          <p>
            {register
              ? "New accounts have view access."
              : "Sign in to pick up where you left off."}
          </p>
          <form onSubmit={submit}>
            <Field label="Username">
              <input
                aria-label="Username"
                autoComplete="username"
                value={values.username}
                onChange={(e) =>
                  setValues({ ...values, username: e.target.value })
                }
                required
                minLength={register ? 3 : 1}
                maxLength={30}
              />
            </Field>
            <Field label="Password">
              <input
                aria-label="Password"
                type="password"
                autoComplete={register ? "new-password" : "current-password"}
                value={values.password}
                onChange={(e) =>
                  setValues({ ...values, password: e.target.value })
                }
                required
                minLength={register ? 8 : 1}
                maxLength={72}
              />
            </Field>
            {error && (
              <div className="alert error" role="alert">
                {error}
              </div>
            )}
            {notice && (
              <div className="alert success" role="status">
                {notice}
              </div>
            )}
            <button className="button primary full" disabled={busy}>
              {busy ? <Spinner /> : null}
              {register ? "Create account" : "Sign in"}
              <ArrowRight size={17} />
            </button>
          </form>
          <button
            className="text-button register"
            onClick={() => {
              setRegister(!register);
              setError("");
              setNotice("");
            }}
          >
            {register
              ? "Already have an account? Sign in"
              : "New here? Create an account"}
          </button>
          <div className="demo-note">
            <ShieldCheck size={19} />
            <div>
              <strong>Try the demo</strong>
              <p>
                Admin: <code>admin / Tut11@2026</code>
                <br />
                View only: <code>user / User11@2026</code>
              </p>
            </div>
          </div>
          <div className="owner">
            {profile.name}
            <span>{profile.id}</span>
          </div>
        </div>
      </section>
    </div>
  );
}

function RecordForm({ kind, record, onClose, onSaved }) {
  const config = modules[kind];
  const [values, setValues] = useState(
    record
      ? {
          ...record,
          companyId: record.company?.id || "",
          jobIds: record.jobs?.map((j) => j.id) || [],
        }
      : structuredClone(config.empty),
  );
  const [options, setOptions] = useState({ companies: [], jobs: [] }),
    [errors, setErrors] = useState({}),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false),
    [uploading, setUploading] = useState(false),
    [optionsBusy, setOptionsBusy] = useState(kind === "employees");
  useEffect(() => {
    if (kind === "employees") {
      Promise.all([allRecords("companies"), allRecords("jobs")])
        .then(([companies, jobs]) => setOptions({ companies, jobs }))
        .catch((e) => setError(errorMessage(e)))
        .finally(() => setOptionsBusy(false));
    }
  }, [kind]);
  function change(key, value) {
    setValues((v) => ({ ...v, [key]: value }));
    setErrors((e) => ({ ...e, [key]: "" }));
  }
  async function upload(e) {
    const file = e.target.files[0];
    if (!file) return;
    setError("");
    if (
      !["image/png", "image/jpeg"].includes(file.type) ||
      file.size > 5 * 1024 * 1024
    ) {
      setError("Choose a PNG or JPEG image up to 5 MB.");
      return;
    }
    setUploading(true);
    try {
      const form = new FormData();
      form.append("file", file);
      change("image", (await api.post("/api/uploads", form)).data.url);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setUploading(false);
    }
  }
  async function save(e) {
    e.preventDefault();
    setBusy(true);
    setErrors({});
    setError("");
    try {
      const payload = { ...values };
      delete payload.id;
      delete payload.company;
      delete payload.jobs;
      if ("age" in payload) payload.age = Number(payload.age);
      if ("companyId" in payload)
        payload.companyId = payload.companyId
          ? Number(payload.companyId)
          : null;
      const result = record
        ? await api.put(`/api/${kind}/${record.id}`, payload)
        : await api.post(`/api/${kind}`, payload);
      onSaved(result.data);
    } catch (e) {
      setError(errorMessage(e));
      setErrors(e.response?.data?.errors || {});
    } finally {
      setBusy(false);
    }
  }
  const textField = (key, label, extra = {}) => (
    <Field key={key} label={label} error={errors[key]}>
      <input
        aria-label={label}
        value={values[key] ?? ""}
        onChange={(e) => change(key, e.target.value)}
        required
        {...extra}
      />
    </Field>
  );
  return (
    <div
      className="overlay"
      onClick={(e) => {
        if (e.target === e.currentTarget && !busy && !uploading) onClose();
      }}
    >
      <section
        className="dialog form-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="form-title"
      >
        <div className="dialog-header">
          <div>
            <span className="eyebrow">
              {record ? "UPDATE RECORD" : "GROW YOUR WORKSPACE"}
            </span>
            <h2 id="form-title">
              {record ? "Edit" : "Add"} {config.singular}
            </h2>
          </div>
          <button
            className="icon-button"
            aria-label="Close form"
            onClick={onClose}
            disabled={busy || uploading}
          >
            <X />
          </button>
        </div>
        <form onSubmit={save}>
          <div className="form-grid">
            {kind === "jobs" ? (
              <>
                {textField("title", "Job title", { maxLength: 100 })}
                <Field label="Description" error={errors.description}>
                  <textarea
                    aria-label="Description"
                    rows={4}
                    maxLength={2000}
                    value={values.description || ""}
                    onChange={(e) => change("description", e.target.value)}
                  />
                </Field>
              </>
            ) : (
              <>
                {textField(
                  "name",
                  "Full name",
                  kind === "students"
                    ? { maxLength: 150 }
                    : { minLength: 3, maxLength: 30 },
                )}
                {["students", "employees"].includes(kind) &&
                  textField("age", "Age", {
                    type: "number",
                    min: 18,
                    max: kind === "employees" ? 55 : undefined,
                  })}
                {kind === "employees" &&
                  textField("email", "Email address", { type: "email" })}
                {["employees", "companies"].includes(kind) &&
                  textField(
                    "address",
                    "Address",
                    kind === "companies" ? { minLength: 5, maxLength: 58 } : {},
                  )}
              </>
            )}
            {kind === "employees" && (
              <>
                <Field label="Company" error={errors.companyId}>
                  <select
                    aria-label="Company"
                    disabled={optionsBusy}
                    value={values.companyId}
                    onChange={(e) => change("companyId", e.target.value)}
                  >
                    <option value="">No company</option>
                    {options.companies.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.name}
                      </option>
                    ))}
                  </select>
                </Field>
                <fieldset className="job-options">
                  <legend>
                    Jobs <small>Select any that apply</small>
                  </legend>
                  {optionsBusy ? (
                    <Spinner />
                  ) : options.jobs.length ? (
                    options.jobs.map((j) => (
                      <label key={j.id}>
                        <input
                          type="checkbox"
                          checked={values.jobIds.includes(j.id)}
                          onChange={(e) =>
                            change(
                              "jobIds",
                              e.target.checked
                                ? [...values.jobIds, j.id]
                                : values.jobIds.filter((id) => id !== j.id),
                            )
                          }
                        />
                        {j.title}
                      </label>
                    ))
                  ) : (
                    <p>Add a job first to assign it here.</p>
                  )}
                </fieldset>
              </>
            )}
            {["employees", "companies"].includes(kind) && (
              <Field label="Photo / logo" error={errors.image}>
                <div className="upload-box">
                  {values.image ? (
                    <img src={imageURL(values.image)} alt="Selected image" />
                  ) : (
                    <Upload size={24} />
                  )}
                  <div>
                    <strong>
                      {uploading
                        ? "Uploading…"
                        : values.image
                          ? "Image selected"
                          : "Upload an image"}
                    </strong>
                    <small>PNG or JPEG · up to 5 MB</small>
                    <input
                      aria-label="Upload image"
                      type="file"
                      accept="image/png,image/jpeg"
                      onChange={upload}
                      disabled={uploading || busy}
                      required={!values.image}
                    />
                  </div>
                </div>
              </Field>
            )}
          </div>
          {error && (
            <div className="alert error" role="alert">
              {error}
            </div>
          )}
          <div className="dialog-footer">
            <button
              type="button"
              className="button secondary"
              onClick={onClose}
              disabled={busy || uploading}
            >
              Cancel
            </button>
            <button
              className="button primary"
              disabled={busy || uploading || optionsBusy}
            >
              {busy ? <Spinner /> : <Check size={17} />}{" "}
              {record ? "Save changes" : "Create " + config.singular}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

function Detail({ kind, record, onClose }) {
  const [data, setData] = useState(null),
    [error, setError] = useState("");
  useEffect(() => {
    api
      .get(`/api/${kind}/${record.id}`)
      .then((r) => setData(r.data))
      .catch((e) => setError(errorMessage(e)));
  }, [kind, record.id]);
  return (
    <div className="overlay">
      <section
        className="dialog detail-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="detail-title"
      >
        <div className="dialog-header">
          <div>
            <span className="eyebrow">RECORD DETAILS</span>
            <h2 id="detail-title">{record.name || record.title}</h2>
          </div>
          <button
            className="icon-button"
            aria-label="Close details"
            onClick={onClose}
          >
            <X />
          </button>
        </div>
        {error ? (
          <div className="alert error" role="alert">
            {error}
          </div>
        ) : !data ? (
          <div className="loading">
            <Spinner /> Loading record…
          </div>
        ) : (
          <dl className="details">
            {Object.entries(data).map(([key, value]) => (
              <div key={key}>
                <dt>{key}</dt>
                <dd>
                  {key === "image" ? (
                    <img
                      className="detail-photo"
                      src={imageURL(value)}
                      alt="Record image"
                    />
                  ) : key === "company" ? (
                    value?.name || "Unassigned"
                  ) : key === "jobs" ? (
                    value.map((j) => j.title).join(", ") || "Unassigned"
                  ) : (
                    String(value ?? "—")
                  )}
                </dd>
              </div>
            ))}
          </dl>
        )}
      </section>
    </div>
  );
}

export default function App() {
  const [user, setUser] = useState(null),
    [checking, setChecking] = useState(true),
    [kind, setKind] = useState("students"),
    [result, setResult] = useState(blankPage),
    [counts, setCounts] = useState({}),
    [search, setSearch] = useState(""),
    [query, setQuery] = useState(""),
    [page, setPage] = useState(0),
    [sort, setSort] = useState("id"),
    [direction, setDirection] = useState("asc"),
    [loading, setLoading] = useState(true),
    [error, setError] = useState(""),
    [revision, setRevision] = useState(0),
    [form, setForm] = useState(null),
    [detail, setDetail] = useState(null),
    [deleting, setDeleting] = useState(null),
    [deleteBusy, setDeleteBusy] = useState(false),
    [toast, setToast] = useState(""),
    [mobile, setMobile] = useState(false);
  const config = modules[kind],
    admin = user?.role === "ADMIN";
  useEffect(() => {
    const expired = () => {
      setUser(null);
      setForm(null);
      setDetail(null);
      setDeleting(null);
    };
    window.addEventListener("session-expired", expired);
    api
      .get("/api/auth/me")
      .then((r) => setUser(r.data))
      .catch(() => {})
      .finally(() => setChecking(false));
    return () => window.removeEventListener("session-expired", expired);
  }, []);
  useEffect(() => {
    const timer = setTimeout(() => {
      setQuery(search);
      setPage(0);
    }, 280);
    return () => clearTimeout(timer);
  }, [search]);
  useEffect(() => {
    if (!user) return;
    let active = true;
    setLoading(true);
    setError("");
    api
      .get(`/api/${kind}`, {
        params: { search: query, page, size: 5, sort, direction },
      })
      .then((r) => {
        if (active) {
          if (r.data.totalPages > 0 && page >= r.data.totalPages)
            setPage(r.data.totalPages - 1);
          else setResult(r.data);
        }
      })
      .catch((e) => {
        if (active) setError(errorMessage(e));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [user, kind, query, page, sort, direction, revision]);
  useEffect(() => {
    if (!user) return;
    Promise.all(
      Object.keys(modules).map(async (k) => [
        k,
        (await api.get(`/api/${k}`, { params: { size: 1 } })).data
          .totalElements,
      ]),
    )
      .then((entries) => setCounts(Object.fromEntries(entries)))
      .catch(() => {});
  }, [user, revision]);
  useEffect(() => {
    if (!toast) return;
    const timer = setTimeout(() => setToast(""), 4000);
    return () => clearTimeout(timer);
  }, [toast]);
  useEffect(() => {
    function escape(e) {
      if (e.key === "Escape" && !deleteBusy) {
        setForm(null);
        setDetail(null);
        setDeleting(null);
      }
    }
    document.addEventListener("keydown", escape);
    document.body.style.overflow = form || detail || deleting ? "hidden" : "";
    return () => {
      document.removeEventListener("keydown", escape);
      document.body.style.overflow = "";
    };
  }, [form, detail, deleting, deleteBusy]);
  function navigate(k) {
    setKind(k);
    setSearch("");
    setQuery("");
    setPage(0);
    setSort("id");
    setDirection("asc");
    setResult(blankPage);
    setMobile(false);
  }
  async function logout() {
    try {
      await api.post("/api/auth/logout");
      await refreshCsrf();
      setUser(null);
    } catch (e) {
      setError(errorMessage(e));
    }
  }
  async function remove() {
    setDeleteBusy(true);
    setError("");
    try {
      await api.delete(`/api/${kind}/${deleting.id}`);
      setDeleting(null);
      setRevision((v) => v + 1);
      setToast("Record deleted.");
    } catch (e) {
      setError(errorMessage(e));
      setDeleting(null);
    } finally {
      setDeleteBusy(false);
    }
  }
  if (checking)
    return (
      <div className="initial-loading">
        <Spinner /> Opening workspace…
      </div>
    );
  if (!user)
    return (
      <SignIn
        onLogin={(u) => {
          setUser(u);
          setRevision((v) => v + 1);
        }}
      />
    );
  const Icon = config.icon;
  return (
    <div className={`workspace theme-${profile.theme}`}>
      <aside className={`sidebar ${mobile ? "mobile-open" : ""}`}>
        <a
          className="brand"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            navigate("students");
          }}
        >
          <span className="brand-mark">
            <LayoutGrid size={21} />
          </span>
          {profile.brand}
          <span className="brand-dot">.</span>
        </a>
        <div className="workspace-caption">PEOPLE WORKSPACE</div>
        <nav aria-label="Main navigation">
          {Object.entries(modules).map(([key, m]) => (
            <button
              key={key}
              className={`nav-item ${kind === key ? "active" : ""}`}
              onClick={() => navigate(key)}
            >
              <m.icon size={19} />
              <span>{m.title}</span>
              <small>{counts[key] ?? "—"}</small>
            </button>
          ))}
        </nav>
        <div className="sidebar-note">
          <span className="note-icon">
            <ShieldCheck size={20} />
          </span>
          <strong>Connected by design</strong>
          <p>{profile.tagline}</p>
          <span className="version">
            TUTORIAL 11 <ArrowUpRight size={14} />
          </span>
        </div>
        <div className="sidebar-bottom">
          <span className="avatar">
            {user.username.slice(0, 2).toUpperCase()}
          </span>
          <div>
            <strong>{user.username}</strong>
            <small>{admin ? "Administrator" : "View access"}</small>
          </div>
          <button
            className="icon-button"
            aria-label="Sign out"
            onClick={logout}
          >
            <LogOut size={18} />
          </button>
        </div>
      </aside>
      <div className="main">
        <header className="topbar">
          <div className="breadcrumb">
            <button
              className="icon-button menu-button"
              aria-label="Open menu"
              onClick={() => setMobile(!mobile)}
            >
              <Menu size={20} />
            </button>
            Workspace <ChevronRight size={13} />
            <strong>{config.title}</strong>
          </div>
          <div className="submission">
            <span className="status-dot" /> {profile.name}
            <span className="student-id">{profile.id}</span>
          </div>
        </header>
        <main>
          <section className="page-heading">
            <div>
              <span className="eyebrow">YOUR PEOPLE, IN ONE PLACE</span>
              <h1>
                {config.title}
                <span className="heading-dot">.</span>
              </h1>
              <p>{config.description}</p>
            </div>
            {admin && (
              <button
                className="button primary"
                onClick={() => setForm({ record: null })}
              >
                <Plus size={18} />
                Add {config.singular}
              </button>
            )}
          </section>
          <section className="stats" aria-label="Workspace totals">
            {Object.entries(modules).map(([key, m], i) => (
              <button
                key={key}
                className={`stat-card ${kind === key ? "selected" : ""}`}
                onClick={() => navigate(key)}
              >
                <div className={`stat-icon tone-${i}`}>
                  <m.icon size={21} />
                </div>
                <div>
                  <span>{m.title}</span>
                  <strong>{counts[key] ?? "—"}</strong>
                </div>
                <ArrowUpRight className="stat-arrow" size={18} />
              </button>
            ))}
          </section>
          <section className="records-panel">
            <div className="panel-heading">
              <div>
                <h2>
                  {config.title} directory{" "}
                  <span className="count-badge">{result.totalElements}</span>
                </h2>
                <p>
                  {query
                    ? `Results for “${query}”`
                    : "A clear view of every record in your workspace."}
                </p>
              </div>
              <span className="access-badge">
                <ShieldCheck size={14} />
                {admin ? "Full access" : "View only"}
              </span>
            </div>
            <div className="toolbar">
              <div className="search-box">
                <Search size={18} />
                <input
                  aria-label={`Search ${config.title.toLowerCase()}`}
                  placeholder={`Search ${config.title.toLowerCase()}…`}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                />
                {search && (
                  <button
                    aria-label="Clear search"
                    className="icon-button"
                    onClick={() => setSearch("")}
                  >
                    <X size={15} />
                  </button>
                )}
              </div>
              <div className="sort-box">
                <ArrowDownUp size={16} />
                <select
                  aria-label="Sort records"
                  value={sort}
                  onChange={(e) => {
                    setSort(e.target.value);
                    setPage(0);
                  }}
                >
                  <option value="id">Date added</option>
                  <option value={config.label}>
                    {kind === "jobs" ? "Job title" : "Name"}
                  </option>
                  {["students", "employees"].includes(kind) && (
                    <option value="age">Age</option>
                  )}
                </select>
                <button
                  aria-label="Toggle sort direction"
                  className="sort-direction"
                  onClick={() => {
                    setDirection((d) => (d === "asc" ? "desc" : "asc"));
                    setPage(0);
                  }}
                >
                  {direction === "asc" ? "↑ Asc" : "↓ Desc"}
                </button>
              </div>
            </div>
            {error && (
              <div className="alert error table-error" role="alert">
                {error}
                <button
                  className="text-button"
                  onClick={() => setRevision((v) => v + 1)}
                >
                  Retry
                </button>
              </div>
            )}
            <div className="table-wrap" aria-busy={loading}>
              <table>
                <thead>
                  <tr>
                    <th className="number-column">ID</th>
                    <th>
                      {kind === "jobs"
                        ? "Job title"
                        : kind === "companies"
                          ? "Company"
                          : "Name"}
                    </th>
                    {kind === "students" && <th>Age</th>}
                    {kind === "companies" && <th>Address</th>}
                    {kind === "jobs" && <th>Description</th>}
                    {kind === "employees" && (
                      <>
                        <th>Company</th>
                        <th>Jobs</th>
                      </>
                    )}
                    <th className="actions-heading">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={6}>
                        <div className="loading">
                          <Spinner /> Loading records…
                        </div>
                      </td>
                    </tr>
                  ) : error ? (
                    <tr>
                      <td colSpan={6}>
                        <div className="empty-state">
                          Records could not be loaded. Please retry.
                        </div>
                      </td>
                    </tr>
                  ) : result.content.length === 0 ? (
                    <tr>
                      <td colSpan={6}>
                        <div className="empty-state">
                          <Icon size={32} />
                          <h3>
                            {query ? "No matching records" : "A fresh start"}
                          </h3>
                          <p>
                            {query
                              ? "Try another name or clear your search."
                              : `Add your first ${config.singular} to get started.`}
                          </p>
                        </div>
                      </td>
                    </tr>
                  ) : (
                    result.content.map((r, i) => (
                      <tr key={r.id}>
                        <td className="record-id">
                          {String(r.id).padStart(3, "0")}
                        </td>
                        <td>
                          <div className="record-person">
                            {r.image ? (
                              <img src={imageURL(r.image)} alt="" />
                            ) : (
                              <span className={`record-avatar tone-${i % 4}`}>
                                {(r.name || r.title)
                                  .split(" ")
                                  .map((s) => s[0])
                                  .slice(0, 2)
                                  .join("")}
                              </span>
                            )}
                            <div>
                              <button
                                className="record-name"
                                onClick={() => setDetail(r)}
                              >
                                {r.name || r.title}
                              </button>
                              <small>
                                {kind === "employees"
                                  ? r.email
                                  : kind === "students"
                                    ? "Student record"
                                    : kind === "companies"
                                      ? "Organization"
                                      : "Team opportunity"}
                              </small>
                            </div>
                          </div>
                        </td>
                        {kind === "students" && (
                          <td>
                            <span className="age-badge">{r.age} years</span>
                          </td>
                        )}
                        {kind === "companies" && (
                          <td className="muted-cell">{r.address}</td>
                        )}
                        {kind === "jobs" && (
                          <td className="muted-cell description-cell">
                            {r.description || "—"}
                          </td>
                        )}
                        {kind === "employees" && (
                          <>
                            <td>
                              <span className="company-badge">
                                {r.company?.name || "Unassigned"}
                              </span>
                            </td>
                            <td>
                              <div className="job-tags">
                                {r.jobs.length
                                  ? r.jobs.map((j) => (
                                      <span key={j.id}>{j.title}</span>
                                    ))
                                  : "—"}
                              </div>
                            </td>
                          </>
                        )}
                        <td>
                          <div className="row-actions">
                            <button
                              className="icon-button"
                              aria-label={`View ${r.name || r.title}`}
                              title="View details"
                              onClick={() => setDetail(r)}
                            >
                              <Eye size={17} />
                            </button>
                            {admin && (
                              <>
                                <button
                                  className="icon-button"
                                  aria-label={`Edit ${r.name || r.title}`}
                                  title="Edit"
                                  onClick={() => setForm({ record: r })}
                                >
                                  <Pencil size={16} />
                                </button>
                                <button
                                  className="icon-button danger"
                                  aria-label={`Delete ${r.name || r.title}`}
                                  title="Delete"
                                  onClick={() => setDeleting(r)}
                                >
                                  <Trash2 size={16} />
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
            <div className="table-footer">
              <span>
                {result.totalElements === 0
                  ? "No records"
                  : `Showing ${page * 5 + 1}–${Math.min((page + 1) * 5, result.totalElements)} of ${result.totalElements} records`}
              </span>
              <div className="pagination">
                <button
                  aria-label="Previous page"
                  disabled={page === 0 || loading}
                  onClick={() => setPage((p) => p - 1)}
                >
                  <ChevronLeft size={16} />
                </button>
                <span>
                  Page {result.totalPages === 0 ? 0 : page + 1} of{" "}
                  {result.totalPages}
                </span>
                <button
                  aria-label="Next page"
                  disabled={page + 1 >= result.totalPages || loading}
                  onClick={() => setPage((p) => p + 1)}
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          </section>
          <footer className="page-footer">
            <span>SE1 · Tutorial 11</span>
            <span>React / REST API / MySQL</span>
          </footer>
        </main>
      </div>
      {form && (
        <RecordForm
          key={`${kind}-${form.record?.id || "new"}`}
          kind={kind}
          record={form.record}
          onClose={() => setForm(null)}
          onSaved={() => {
            setForm(null);
            setRevision((v) => v + 1);
            setToast(
              form.record
                ? "Changes saved."
                : `${config.title.slice(0, -1)} created.`,
            );
          }}
        />
      )}
      {detail && (
        <Detail kind={kind} record={detail} onClose={() => setDetail(null)} />
      )}
      {deleting && (
        <div className="overlay">
          <section
            className="dialog delete-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-title"
          >
            <span className="delete-symbol">
              <Trash2 size={25} />
            </span>
            <h2 id="delete-title">Delete {config.singular}?</h2>
            <p>
              <strong>{deleting.name || deleting.title}</strong> will be
              permanently removed. This action cannot be undone.
            </p>
            <div className="dialog-footer">
              <button
                className="button secondary"
                disabled={deleteBusy}
                onClick={() => setDeleting(null)}
              >
                Keep record
              </button>
              <button
                className="button destructive"
                disabled={deleteBusy}
                onClick={remove}
              >
                {deleteBusy ? <Spinner /> : <Trash2 size={16} />}Delete record
              </button>
            </div>
          </section>
        </div>
      )}
      {toast && (
        <div className="toast" role="status">
          <Check size={18} />
          {toast}
        </div>
      )}
    </div>
  );
}
