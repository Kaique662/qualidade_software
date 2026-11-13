document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector("[data-academic-planning]");
    if (root) initializeAcademicPlanning(root);
});

function initializeAcademicPlanning(root) {
    const feedback = root.querySelector("[data-planning-feedback]");
    const state = { periods: [], courses: [], classes: [], subjects: [], teachers: [], resources: [], schedules: [], report: [] };
    const actor = root.dataset.actor || "admin";

    async function api(path, options = {}) {
        const headers = { ...(options.body ? { "Content-Type": "application/json" } : {}), ...options.headers };
        const response = await fetch(path, { ...options, headers });
        const text = await response.text();
        let body = null;
        if (text) {
            try { body = JSON.parse(text); } catch { body = text; }
        }
        if (!response.ok) {
            const message = typeof body === "object" && body?.detail ? body.detail
                : typeof body === "object" && body?.message ? body.message
                    : `Falha na solicitação (${response.status}).`;
            throw new Error(message);
        }
        return body;
    }

    function setFeedback(message, isError = false) {
        feedback.textContent = message;
        feedback.classList.toggle("is-error", isError);
    }

    function option(select, value, label) {
        const item = document.createElement("option");
        item.value = value;
        item.textContent = label;
        select.append(item);
    }

    function fillOptions(selector, values, labelFor, placeholder) {
        root.querySelectorAll(selector).forEach((select) => {
            const selected = select.value;
            select.replaceChildren();
            option(select, "", placeholder);
            values.forEach((value) => option(select, value.id, labelFor(value)));
            if (values.some((value) => value.id === selected)) select.value = selected;
        });
    }

    async function loadCatalogs() {
        const [periods, courses, classes, subjects, teachers, resources] = await Promise.all([
            api("/api/academic-periods"), api("/api/courses"), api("/api/classes"),
            api("/api/subjects"), api("/api/teachers"), api("/api/resources")
        ]);
        Object.assign(state, { periods, courses, classes, subjects, teachers, resources });
        fillOptions("[data-period-options], [data-schedule-period-filter], [data-report-period]",
            periods, (item) => item.name, "Selecione um período");
        fillOptions("[data-course-options]", courses, (item) => `${item.code} · ${item.name}`, "Selecione um curso");
        fillOptions("[data-class-options], [data-report-class]", classes,
            (item) => `${item.code} · ${item.name}`, "Selecione uma turma");
        fillOptions("[data-subject-options]", subjects, (item) => `${item.code} · ${item.name}`, "Selecione uma disciplina");
        fillOptions("[data-teacher-options], [data-report-teacher]", teachers,
            (item) => `${item.name} · ${item.id}`, "Selecione um professor");
        fillOptions("[data-report-room]", resources.filter((item) => item.type === "ROOM"),
            (item) => `${item.code} · ${item.name}`, "Todas as salas");
        fillOptions("[data-room-options]", resources.filter((item) => item.type === "ROOM"),
            (item) => `${item.code} · ${item.name}`, "Sem sala definida");
        fillOptions("[data-material-options]", resources.filter((item) => item.type === "MATERIAL"),
            (item) => `${item.code} · ${item.name}`, "Sem material");
        renderCatalogSummary();
    }

    function renderCatalogSummary() {
        const target = root.querySelector("[data-catalog-summary]");
        target.replaceChildren();
        [["Períodos", state.periods], ["Cursos", state.courses], ["Turmas", state.classes], ["Disciplinas", state.subjects]]
            .forEach(([label, values]) => {
                const item = document.createElement("div");
                item.className = "planning-count";
                const name = document.createElement("span");
                name.textContent = label;
                const count = document.createElement("strong");
                count.textContent = String(values.length);
                item.append(name, count);
                target.append(item);
            });
    }

    function formObject(form, fields) {
        const data = new FormData(form);
        return Object.fromEntries(fields.map((field) => [field, data.get(field) || null]));
    }

    async function submitJsonForm(form, path, fields, afterSave) {
        form.addEventListener("submit", async (event) => {
            event.preventDefault();
            try {
                await api(path, { method: "POST", body: JSON.stringify(formObject(form, fields)) });
                form.reset();
                setFeedback("Cadastro salvo.");
                await loadCatalogs();
                if (afterSave) await afterSave();
            } catch (error) {
                setFeedback(error.message, true);
            }
        });
    }

    submitJsonForm(root.querySelector("[data-create-period]"), "/api/academic-periods",
        ["name", "startsOn", "endsOn"], loadCatalogs);
    submitJsonForm(root.querySelector("[data-create-course]"), "/api/courses",
        ["code", "name", "description"], loadCatalogs);
    submitJsonForm(root.querySelector("[data-create-class]"), "/api/classes",
        ["code", "name", "courseId", "academicPeriodId"], loadCatalogs);
    submitJsonForm(root.querySelector("[data-create-subject]"), "/api/subjects",
        ["code", "name", "courseId"], loadCatalogs);

    const classSelect = root.querySelector('[data-activity-editor] [name="classId"]');
    const courseSelect = root.querySelector('[data-activity-editor] [name="courseId"]');
    classSelect.addEventListener("change", () => {
        const group = state.classes.find((item) => item.id === classSelect.value);
        if (group) courseSelect.value = group.courseId;
        filterSubjects();
    });
    courseSelect.addEventListener("change", filterSubjects);

    function filterSubjects() {
        const current = root.querySelector('[data-activity-editor] [name="subjectId"]');
        const courseId = courseSelect.value;
        const options = state.subjects.filter((item) => !courseId || item.courseId === courseId);
        fillOptions("[data-subject-options]", options, (item) => `${item.code} · ${item.name}`, "Selecione uma disciplina");
    }

    const queue = [];
    const queueElement = root.querySelector("[data-activity-queue]");
    root.querySelector("[data-add-activity]").addEventListener("click", () => {
        const editor = root.querySelector("[data-activity-editor]");
        const values = Object.fromEntries([...editor.querySelectorAll("input[name], select[name]")]
            .map((field) => [field.name, field.value]));

        const requiredFields = [
            "classId",
            "subjectId",
            "teacherId",
            "courseId",
            "startsAt",
            "endsAt"
        ];

        if (requiredFields.some((field) => !values[field])) {
            setFeedback(
                "Preencha turma, disciplina, professor, curso e horários.",
                true
            );
            return;
        }

        if (new Date(values.endsAt) <= new Date(values.startsAt)) {
            setFeedback("O fim da atividade deve ser posterior ao início.", true);
            return;
        }
        if (values.materialId && !values.roomResourceId) {
            setFeedback("Selecione uma sala antes de alocar material.", true);
            return;
        }
        queue.push({ ...values, teacherIds: [values.teacherId] });
        renderQueue();
        editor.querySelector('[name="startsAt"]').value = "";
        editor.querySelector('[name="endsAt"]').value = "";
        setFeedback(`${queue.length} atividade(s) no rascunho.`);
    });

    function renderQueue() {
        queueElement.replaceChildren();
        queue.forEach((activity, index) => {
            const item = document.createElement("li");
            const label = document.createElement("span");
            const group = state.classes.find((value) => value.id === activity.classId);
            const subject = state.subjects.find((value) => value.id === activity.subjectId);
            const room = state.resources.find((value) => value.id === activity.roomResourceId);
            const material = state.resources.find((value) => value.id === activity.materialId);
            label.textContent = `${group?.name || "Turma"} · ${subject?.name || "Disciplina"} · ${activity.startsAt.replace("T", " ")}${room ? ` · ${room.name}` : ""}${material ? ` · ${material.name} × ${activity.materialQuantity}` : ""}`;
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "icon-button";
            remove.title = "Remover atividade";
            remove.setAttribute("aria-label", "Remover atividade");
            remove.innerHTML = '<i class="bi bi-trash3"></i>';
            remove.addEventListener("click", () => { queue.splice(index, 1); renderQueue(); });
            item.append(label, remove);
            queueElement.append(item);
        });
    }

        const scheduleForm = root.querySelector("[data-create-schedule]");
        let savingSchedule = false;

        scheduleForm.addEventListener("submit", async (event) => {
            event.preventDefault();

            if (savingSchedule) return;

            if (!queue.length) {
                setFeedback(
                    "Adicione ao menos uma atividade à grade.",
                    true
                );
                return;
            }

            const form = event.currentTarget;
            const values = new FormData(form);

            // Envia a grade com as alocações incluídas.
            const schedule = {
                name: values.get("name"),
                academicPeriodId: values.get("academicPeriodId"),
                activities: queue.map((item) => {
                    const {
                        teacherId,
                        materialId,
                        materialQuantity,
                        ...activity
                    } = item;

                    return {
                        ...activity,
                        roomResourceId: activity.roomResourceId || null,
                        materials: materialId
                            ? [{
                                resourceId: materialId,
                                quantity: Number(materialQuantity)
                            }]
                            : []
                    };
                })
            };

            const submitButton = form.querySelector(
                'button[type="submit"]'
            );

            savingSchedule = true;

            if (submitButton) {
                submitButton.disabled = true;
            }

            try {
                await api(
                    `/api/schedules?actor=${encodeURIComponent(actor)}`,
                    {
                        method: "POST",
                        body: JSON.stringify(schedule)
                    }
                );

                queue.splice(0, queue.length);
                renderQueue();
                form.reset();

                setFeedback("Grade validada e rascunho salvo.");

                await loadSchedules();
                
            } catch (error) {
                setFeedback(
                    `Não foi possível salvar a grade: ${error.message}`,
                    true
                );

                // Mantém os dados para o usuário corrigir.

            } finally {
                savingSchedule = false;

                if (submitButton) {
                    submitButton.disabled = false;
                }
            }
        });


        function makeButton(label, icon, action, kind = "secondary-button") {
            const button = document.createElement("button");
            button.type = "button";
            button.className = kind;
            button.innerHTML = `<i class="bi ${icon}"></i>${label}`;
            button.addEventListener("click", action);
            return button;
        }

        async function loadSchedules() {
            try {
                state.schedules = await api("/api/schedules");
                renderSchedules();
                await loadAdjustments();
            } catch (error) {
                setFeedback(error.message, true);
            }
        }

        function renderSchedules() {
            const filter = root.querySelector("[data-schedule-period-filter]").value;
            const schedules = state.schedules.filter((item) => !filter || item.academicPeriodId === filter);
            const list = root.querySelector("[data-schedule-list]");
            const review = root.querySelector("[data-review-list]");
            list.replaceChildren();
            review.replaceChildren();
            schedules.forEach((schedule) => list.append(renderSchedule(schedule)));
            schedules.filter((item) => item.status === "PENDENTE_APROVACAO").forEach((schedule) => review.append(renderSchedule(schedule, true)));
            if (!schedules.length) list.append(emptyMessage("Nenhuma grade neste período."));
            if (!review.children.length) review.append(emptyMessage("Nenhuma grade aguarda aprovação."));
        }

        function renderSchedule(schedule, reviewMode = false) {
            const card = document.createElement("article");
            card.className = "planning-schedule-item";
            const top = document.createElement("div");
            top.className = "planning-schedule-top";
            const heading = document.createElement("div");
            const title = document.createElement("h4");
            title.textContent = schedule.name;
            const meta = document.createElement("p");
            const period = state.periods.find((item) => item.id === schedule.academicPeriodId);
            meta.textContent = `${period?.name || "Período"} · v${schedule.versionNumber} · ${schedule.activities?.length || 0} atividades`;
            heading.append(title, meta);
            const status = document.createElement("span");
            status.className = `schedule-status status-${schedule.status.toLowerCase()}`;
            status.textContent = schedule.status.replaceAll("_", " ");
            top.append(heading, status);
            const actions = document.createElement("div");
            actions.className = "planning-actions";
            actions.append(makeButton("Versões", "bi-clock-history", () => showVersions(schedule)));
            actions.append(makeButton("Auditoria", "bi-journal-check", () => showAudit(schedule)));
            if (schedule.status === "RASCUNHO") {
                actions.append(makeButton("Submeter", "bi-send", () => transition(schedule, "submit-for-approval"), "primary-button"));
            }
            if (schedule.status === "PENDENTE_APROVACAO") {
                actions.append(makeButton("Aprovar", "bi-check2-circle", () => transition(schedule, "approve"), "primary-button"));
                actions.append(makeButton("Rejeitar", "bi-x-circle", () => reject(schedule), "secondary-button"));
            }
            if (schedule.status === "APROVADA") {
                actions.append(makeButton("Publicar", "bi-megaphone", () => transition(schedule, "publish"), "primary-button"));
            }
            const output = document.createElement("pre");
            output.className = "planning-detail-output";
            output.hidden = true;
            card.append(top, actions, output);
            card.dataset.scheduleId = schedule.id;
            if (reviewMode) card.classList.add("is-review-item");
            return card;
        }

        function emptyMessage(text) {
            const message = document.createElement("p");
            message.className = "planning-empty";
            message.textContent = text;
            return message;
        }

        async function showVersions(schedule) {
            const card = root.querySelector(`[data-schedule-id="${CSS.escape(schedule.id)}"]`);
            const output = card?.querySelector(".planning-detail-output");
            if (!output) return;
            try {
                const versions = await api(`/api/schedules/${encodeURIComponent(schedule.id)}/versions`);
                output.hidden = false;
                output.replaceChildren();
                const select = document.createElement("select");
                select.setAttribute("aria-label", "Versão de origem");
                const selectTo = document.createElement("select");
                selectTo.setAttribute("aria-label", "Versão de destino");
                versions.forEach((version) => {
                    option(select, version.number, `v${version.number} · ${version.createdBy || "sistema"}`);
                    option(selectTo, version.number, `v${version.number} · ${version.createdBy || "sistema"}`);
                });
                select.value = String(Math.max(1, schedule.versionNumber - 1));
                selectTo.value = String(schedule.versionNumber);
                const compare = makeButton("Comparar", "bi-arrow-left-right", async () => {
                    try {
                        const changes = await api(`/api/schedules/${encodeURIComponent(schedule.id)}/versions/compare?fromVersion=${select.value}&toVersion=${selectTo.value}`);
                        output.dataset.comparison = JSON.stringify(changes, null, 2);
                        renderDetailOutput(output);
                    } catch (error) { setFeedback(error.message, true); }
                });
                const versionsText = document.createElement("p");
                versionsText.textContent = versions.map((version) => `v${version.number}: ${version.activities.length} atividades`).join(" · ");
                output.append(versionsText, select, selectTo, compare);
            } catch (error) { setFeedback(error.message, true); }
        }

        async function showAudit(schedule) {
            const card = root.querySelector(`[data-schedule-id="${CSS.escape(schedule.id)}"]`);
            const output = card?.querySelector(".planning-detail-output");
            if (!output) return;
            try {
                const events = await api(`/api/schedules/${encodeURIComponent(schedule.id)}/audit`);
                output.dataset.comparison = JSON.stringify(events, null, 2);
                output.hidden = false;
                renderDetailOutput(output);
            } catch (error) { setFeedback(error.message, true); }
        }

        function renderDetailOutput(output) {
            const pre = document.createElement("code");
            pre.textContent = output.dataset.comparison || "[]";
            output.append(pre);
        }

        async function transition(schedule, action) {
            try {
                await api(`/api/schedules/${encodeURIComponent(schedule.id)}/${action}?actor=${encodeURIComponent(actor)}`, { method: "POST" });
                setFeedback("Estado da grade atualizado.");
                await loadSchedules();
            } catch (error) { setFeedback(error.message, true); }
        }

        async function reject(schedule) {
            const justification = window.prompt("Justificativa da rejeição:");
            if (!justification?.trim()) return;
            try {
                await api(`/api/schedules/${encodeURIComponent(schedule.id)}/reject?actor=${encodeURIComponent(actor)}&justification=${encodeURIComponent(justification.trim())}`, { method: "POST" });
                setFeedback("Rejeição registrada.");
                await loadSchedules();
            } catch (error) { setFeedback(error.message, true); }
        }
        let adjustmentLoadVersion = 0;
        async function loadAdjustments() {
            const loadVersion = ++adjustmentLoadVersion;
            const list = root.querySelector("[data-adjustment-list]");
            let requests = [];
            for (const schedule of state.schedules) {
                try {
                    const items = await api(`/api/schedules/${encodeURIComponent(schedule.id)}/adjustment-requests`);
                    requests.push(...items.filter((item) => item.status === "ABERTA").map((item) => ({ ...item, schedule })));
                } catch { /* A grade pode ter sido removida durante a atualização da tela. */ }
            }
            if (loadVersion !== adjustmentLoadVersion) return;

            list.replaceChildren();
            if (!requests.length) {
                list.append(emptyMessage("Nenhuma solicitação de ajuste aberta."));
                return;
            }
            requests.forEach((request) => {
                const item = document.createElement("article");
                item.className = "planning-request-item";
                const text = document.createElement("div");
                const title = document.createElement("strong");
                title.textContent = request.schedule.name;
                const description = document.createElement("p");
                description.textContent = `${request.requester}: ${request.description}`;
                text.append(title, description);
                const respond = makeButton("Aceitar", "bi-check2", () => respondToAdjustment(request, "ACEITA"), "primary-button");
                const reject = makeButton("Rejeitar", "bi-x", () => respondToAdjustment(request, "REJEITADA"));
                item.append(text, respond, reject);
                list.append(item);
            });
        }

        async function respondToAdjustment(request, decision) {
            const response = window.prompt("Resposta para o solicitante:");
            if (!response?.trim()) return;
            try {
                await api(`/api/schedules/${encodeURIComponent(request.schedule.id)}/adjustment-requests/${encodeURIComponent(request.id)}?actor=${encodeURIComponent(actor)}&decision=${decision}&response=${encodeURIComponent(response.trim())}`, { method: "PATCH" });
                setFeedback("Solicitação respondida.");
                await loadAdjustments();
            } catch (error) { setFeedback(error.message, true); }
        }

        root.querySelectorAll("[data-planning-tab]").forEach((tab) => tab.addEventListener("click", () => {
            root.querySelectorAll("[data-planning-tab]").forEach((item) => {
                const active = item === tab;
                item.classList.toggle("is-active", active);
                item.setAttribute("aria-selected", String(active));
            });
            root.querySelectorAll("[data-planning-panel]").forEach((panel) => {
                panel.hidden = panel.dataset.planningPanel !== tab.dataset.planningTab;
            });
            if (tab.dataset.planningTab === "review") loadAdjustments();
        }));
        root.querySelector("[data-schedule-period-filter]").addEventListener("change", renderSchedules);
        root.querySelector("[data-refresh-schedules]").addEventListener("click", loadSchedules);
        root.querySelector("[data-run-report]").addEventListener("click", runReport);
        root.querySelector("[data-export-report]").addEventListener("click", exportReport);

        async function runReport() {
            const filters = {
                academicPeriodId: root.querySelector("[data-report-period]").value,
                classId: root.querySelector("[data-report-class]").value,
                teacherId: root.querySelector("[data-report-teacher]").value,
                roomResourceId: root.querySelector("[data-report-room]").value,
                status: root.querySelector("[data-report-status]").value
            };
            const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value));
            try {
                state.report = await api(`/api/schedule-reports?${query}`);
                const output = root.querySelector("[data-report-output]");
                output.textContent = `${state.report.length} grade(s) no resultado.`;
                root.querySelector("[data-export-report]").disabled = !state.report.length;
            } catch (error) { setFeedback(error.message, true); }
        }

        function exportReport() {
            const blob = new Blob([JSON.stringify(state.report, null, 2)], { type: "application/json" });
            const url = URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = "relatorio-de-grades.json";
            link.click();
            URL.revokeObjectURL(url);
        }

        (async () => {
            try {
                await loadCatalogs();
                await loadSchedules();
                setFeedback("Planejamento carregado.");
            } catch (error) {
                setFeedback(`Não foi possível carregar o planejamento: ${error.message}`, true);
            }
        })();
    }