document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector("[data-resource-dashboard]");
    if (root) initializeResourceManagement(root);
});

function initializeResourceManagement(root) {
    const actor = root.dataset.actor || "gestora";
    const agendaForm = root.querySelector("[data-resource-agenda-form]");
    const blockForm = root.querySelector("[data-resource-block-form]");
    const blockResource = root.querySelector("[data-block-resource]");
    const agendaResource = root.querySelector("[data-agenda-resource]");
    const blockList = root.querySelector("[data-resource-block-list]");
    let resources = [];
    let impactPreview = null;
    let courses = [];
    let classes = [];
    let subjects = [];

    async function loadAcademicCatalogs() {
        try {
            [courses, classes, subjects] = await Promise.all([
                api("/api/courses"),
                api("/api/classes"),
                api("/api/subjects")
            ]);
        } catch (error) {
            console.error("Erro ao carregar dados acadêmicos:", error);
        }
    }

    function getLabel(items, id) {
        const item = items.find(value => value.id === id);

        if (!item) return id || "Não informado";

        return item.name || item.code || id;
    }

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

    function setResult(container, message, isError = false) {
        container.replaceChildren();
        const note = document.createElement("p");
        note.className = isError ? "manager-feedback is-error" : "manager-feedback";
        note.textContent = message;
        container.append(note);
    }

    function populateResources() {
        [blockResource, agendaResource].forEach((select) => {
            const selected = select.value;
            select.replaceChildren();
            const placeholder = document.createElement("option");
            placeholder.value = "";
            placeholder.textContent = "Selecione um recurso";
            select.append(placeholder);
            resources.forEach((resource) => {
                const option = document.createElement("option");
                option.value = resource.id;
                option.textContent = `${resource.code} · ${resource.name}`;
                select.append(option);
            });
            if (resources.some((resource) => resource.id === selected)) select.value = selected;
        });
    }

    async function loadResources() {
        try {
            resources = await api("/api/resources");
            populateResources();
        } catch (error) {
            setResult(root.querySelector("[data-block-impact-results]"), error.message, true);
            setResult(root.querySelector("[data-resource-agenda-results]"), error.message, true);
        }
    }

    agendaForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const values = new FormData(agendaForm);
        const resourceId = values.get("resourceId");
        const query = new URLSearchParams({ startsAt: values.get("startsAt"), endsAt: values.get("endsAt") });
        const output = root.querySelector("[data-resource-agenda-results]");
        try {
            const entries = await api(`/api/resources/${encodeURIComponent(resourceId)}/agenda?${query}`);
            renderAllocations(output, entries);
        } catch (error) { setResult(output, error.message, true); }
    });

    root.querySelector("[data-preview-block]").addEventListener("click", async () => {
        const values = new FormData(blockForm);
        const resourceId = values.get("resourceId");
        const query = new URLSearchParams({ startsAt: values.get("startsAt"), endsAt: values.get("endsAt") });
        const output = root.querySelector("[data-block-impact-results]");
        const createButton = root.querySelector("[data-create-block]");
        createButton.disabled = true;
        if (!resourceId || !values.get("startsAt") || !values.get("endsAt")) {
            setResult(output, "Selecione o recurso e o intervalo para consultar o impacto.", true);
            return;
        }
        try {
            impactPreview = await api(`/api/resources/${encodeURIComponent(resourceId)}/block-impact?${query}`);
            renderAllocations(output, impactPreview);
            output.dataset.previewKey = blockPreviewKey(values);
            createButton.disabled = false;
        } catch (error) {
            impactPreview = null;
            setResult(output, error.message, true);
        }
    });

    function blockPreviewKey(values) {
        return [values.get("resourceId"), values.get("startsAt"), values.get("endsAt")].join("|");
    }

    blockForm.addEventListener("input", invalidatePreview);
    blockForm.addEventListener("change", invalidatePreview);
    function invalidatePreview() {
        impactPreview = null;
        root.querySelector("[data-create-block]").disabled = true;
    }

    blockForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const values = new FormData(blockForm);
        const output = root.querySelector("[data-block-impact-results]");
        if (!impactPreview || output.dataset.previewKey !== blockPreviewKey(values)) {
            setResult(output, "Revise o impacto atualizado antes de criar o bloqueio.", true);
            return;
        }
        const payload = {
            resourceId: values.get("resourceId"),
            reason: values.get("reason"),
            responsible: actor,
            startsAt: values.get("startsAt"),
            endsAt: values.get("endsAt")
        };
        try {
            const block = await api("/api/resource-blocks", { method: "POST", body: JSON.stringify(payload) });
            setResult(output, `Bloqueio pendente. ${block.affectedAllocationIds.length} atividade(s) impactada(s).`);
            blockForm.reset();
            impactPreview = null;
            root.querySelector("[data-create-block]").disabled = true;
            await loadBlocks();
        } catch (error) { setResult(output, error.message, true); }
    });

    async function loadBlocks() {
        try {
            const blocks = await api("/api/resource-blocks");
            renderBlocks(blocks);
        } catch (error) { setResult(blockList, error.message, true); }
    }

    function renderBlocks(blocks) {
        blockList.replaceChildren();
        if (!blocks.length) {
            setResult(blockList, "Nenhum bloqueio registrado.");
            return;
        }
        blocks.sort((left, right) => right.startsAt.localeCompare(left.startsAt)).forEach((block) => {
            const resource = resources.find((item) => item.id === block.resourceId);
            const card = document.createElement("article");
            card.className = "manager-block-card";
            const details = document.createElement("div");
            const heading = document.createElement("div");
            heading.className = "manager-block-heading";
            const name = document.createElement("strong");
            name.textContent = resource ? `${resource.code} · ${resource.name}` : block.resourceId;
            const status = document.createElement("span");
            status.className = `schedule-status status-${block.status.toLowerCase()}`;
            status.textContent = block.status === "PENDING" ? "PENDENTE"
                : block.status === "CONFIRMED" ? "CONFIRMADO" : "CANCELADO";
            heading.append(name, status);
            const reason = document.createElement("p");
            reason.textContent = block.reason;
            const interval = document.createElement("small");
            interval.textContent = `${block.startsAt.replace("T", " ")} a ${block.endsAt.replace("T", " ")} · ${block.affectedAllocationIds.length} atividade(s) impactada(s)`;
            details.append(heading, reason, interval);
            const actions = document.createElement("div");
            actions.className = "planning-actions";
            if (block.status === "PENDING") {
                actions.append(actionButton("Confirmar", "bi-check2-circle", async () => {
                    try {
                        const interval = new URLSearchParams({ startsAt: block.startsAt, endsAt: block.endsAt });
                        const affected = await api(`/api/resources/${encodeURIComponent(block.resourceId)}/block-impact?${interval}`);
                        const impactText = affected.length
                            ? affected.map((entry, index) => [
                                `Aula ${index + 1}`,
                                `Curso: ${getLabel(courses, entry.courseId)}`,
                                `Turma: ${getLabel(classes, entry.classId)}`,
                                `Disciplina: ${getLabel(subjects, entry.subjectId)}`,
                                `ID da aula: ${entry.id ?? entry.allocationId ?? "Não informado"}`,
                                `Horário: ${entry.startsAt.replace("T", " ")} até ${entry.endsAt.replace("T", " ")}`
                            ].join("\n")).join("\n\n")
                            : "Nenhuma atividade será afetada.";
                        if (!window.confirm(`Atividades afetadas (${affected.length}):\n${impactText}\n\nConfirmar bloqueio?`)) return;
                        await api(`/api/resource-blocks/${encodeURIComponent(block.id)}/confirm?actor=${encodeURIComponent(actor)}`, { method: "POST" });
                        await loadBlocks();
                    } catch (error) { setResult(blockList, error.message, true); }
                }, "primary-button"));
            }
            if (block.status !== "CANCELLED") {
                actions.append(actionButton("Cancelar", "bi-x-circle", async () => {
                    if (!window.confirm("Cancelar este bloqueio?")) return;
                    try {
                        await api(`/api/resource-blocks/${encodeURIComponent(block.id)}/cancel?actor=${encodeURIComponent(actor)}`, { method: "POST" });
                        await loadBlocks();
                    } catch (error) { setResult(blockList, error.message, true); }
                }));
            }
            const history = document.createElement("details");
            history.className = "manager-block-history";
            const summary = document.createElement("summary");
            summary.textContent = "Histórico";
            const historyList = document.createElement("ul");
            block.history.forEach((entry) => {
                const item = document.createElement("li");
                item.textContent = `${entry.action} · ${entry.actor} · ${entry.occurredAt.replace("T", " ")}`;
                historyList.append(item);
            });
            history.append(summary, historyList);
            card.append(details, actions, history);
            blockList.append(card);
        });
    }

    function actionButton(label, icon, action, kind = "secondary-button") {
        const button = document.createElement("button");
        button.type = "button";
        button.className = kind;
        button.innerHTML = `<i class="bi ${icon}"></i>${label}`;
        button.addEventListener("click", action);
        return button;
    }

    function renderAllocations(container, entries) {
        container.replaceChildren();
        if (entries.length > 0) {
            const warning = document.createElement("div");

            warning.className = "manager-feedback";
            warning.setAttribute("role", "status");

            warning.textContent =
                `Atenção: esta manutenção afetará ${entries.length} ` +
                `atividade(s) já agendada(s).`;

            container.append(warning);
        }
        if (!entries.length) {
            setResult(container, "Nenhuma atividade impactada neste intervalo.");
            return;
        }

        entries.forEach((entry, index) => {
            const row = document.createElement("article");
            row.className = "manager-allocation-item";

            const name = document.createElement("strong");
            name.textContent = `Aula ${index + 1}`;

            const details = document.createElement("p");
            details.textContent = [
                `Curso: ${getLabel(courses, entry.courseId)}`,
                `Turma: ${getLabel(classes, entry.classId)}`,
                `Disciplina: ${getLabel(subjects, entry.subjectId)}`,
                `ID da aula: ${entry.allocationId ?? entry.id ?? "Não informado"}`
            ].join("\n");

            details.style.whiteSpace = "pre-line";

            const interval = document.createElement("p");
            interval.textContent =
                `Horário: ${entry.startsAt.replace("T", " ")} ` +
                `até ${entry.endsAt.replace("T", " ")} · ` +
                `Quantidade: ${entry.quantity}`;

            row.append(name, details, interval);


            if (
                container === root.querySelector("[data-block-impact-results]") &&
                entry.resourceType === "ROOM"
            ) {
                const roomSelect = document.createElement("select");
                const button = actionButton(
                    "Solicitar remanejamento",
                    "bi-arrow-left-right",
                    async () => {
                        if (!roomSelect.value) {
                            alert("Selecione outra sala.");
                            return;
                        }

                        button.disabled = true;

                        try {
                            const activityId = entry.allocationId.slice(
                                entry.scheduleId.length + 1
                            );

                            const room = resources.find(
                                item => item.id === roomSelect.value
                            );

                            const description =
                                `Remanejamento por manutenção. ` +
                                `Nova sala sugerida: ${room.code} - ${room.name} ` +
                                `(ID: ${room.id}).`;

                            await api(
                                `/api/schedules/${encodeURIComponent(
                                    entry.scheduleId
                                )}/adjustment-requests`,
                                {
                                    method: "POST",
                                    body: JSON.stringify({
                                        requester: "gestora",
                                        activityId,
                                        description
                                    })
                                }
                            );

                            alert("Solicitação enviada para revisão.");
                        } catch (error) {
                            alert(error.message);
                        } finally {
                            button.disabled = false;
                        }
                    }
                );

                button.disabled = true;

                const query = new URLSearchParams({
                    type: "ROOM",
                    startsAt: entry.startsAt,
                    endsAt: entry.endsAt
                });

                api(`/api/resources?${query}`)
                    .then(rooms => {
                        roomSelect.replaceChildren();

                        const placeholder = document.createElement("option");
                        placeholder.value = "";
                        placeholder.textContent = "Selecione outra sala";
                        roomSelect.append(placeholder);

                        rooms
                            .filter(room => room.id !== entry.resourceId)
                            .forEach(room => {
                                const option = document.createElement("option");
                                option.value = room.id;
                                option.textContent =
                                    `${room.code} - ${room.name}`;
                                roomSelect.append(option);
                            });

                        button.disabled = roomSelect.options.length <= 1;
                    })
                    .catch(error => {
                        setResult(container, error.message, true);
                    });

                row.append(roomSelect, button);
            }

            container.append(row);

        });
    }

    root.querySelector("[data-refresh-blocks]").addEventListener("click", loadBlocks);
    loadAcademicCatalogs();
    loadResources();
    loadBlocks();
}