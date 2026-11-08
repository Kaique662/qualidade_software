document.addEventListener("DOMContentLoaded", () => {
    const root = document.querySelector("[data-teacher-portal]");
    if (root) initializeTeacherJourney(root);
});

function initializeTeacherJourney(root) {
    const teacherId = root.dataset.teacherId;
    const warning = root.querySelector("[data-teacher-link-warning]");
    const feedback = root.querySelector("[data-availability-feedback]");
    const state = { periods: [], schedules: [], availability: [] };

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

    if (!teacherId) {
        warning.hidden = false;
        root.querySelectorAll("form button, [data-refresh-availability], [data-refresh-schedules]")
            .forEach((control) => { control.disabled = true; });
        setFeedback("Este acesso ainda não está vinculado a um cadastro docente.", true);
        root.querySelectorAll("[data-teacher-tab]").forEach((tab) => {
            if (tab.dataset.teacherTab !== "availability") tab.disabled = true;
        });
        return;
    }

    const availabilityUrl = `/api/teachers/${encodeURIComponent(teacherId)}/availability`;
    const scheduleUrl = `/api/teachers/${encodeURIComponent(teacherId)}/schedules`;
    const periodSelect = root.querySelector("[data-teacher-periods]");
    const form = root.querySelector("[data-availability-form]");
    const saveAvailabilityButton = root.querySelector("[data-save-availability]");
    const cancelAvailabilityEdit = root.querySelector("[data-cancel-availability-edit]");
    const recurring = form.elements.recurringWeekly;
    const oneTimeFields = root.querySelector("[data-single-interval]");
    const recurringFields = root.querySelector("[data-weekly-interval]");

    function toggleRecurrence() {
        oneTimeFields.hidden = recurring.checked;
        recurringFields.hidden = !recurring.checked;
        form.elements.startsAt.required = !recurring.checked;
        form.elements.endsAt.required = !recurring.checked;
        ["validFrom", "validUntil", "dailyStartsAt", "dailyEndsAt"].forEach((name) => {
            form.elements[name].required = recurring.checked;
        });
    }
    recurring.addEventListener("change", toggleRecurrence);
    toggleRecurrence();

    async function loadPeriods() {
        state.periods = await api("/api/academic-periods");
        periodSelect.replaceChildren();
        state.periods.forEach((period) => {
            const option = document.createElement("option");
            option.value = period.id;
            option.textContent = period.name;
            periodSelect.append(option);
        });
    }

    async function loadAvailability() {
        if (!periodSelect.value) return;
        try {
            state.availability = await api(`${availabilityUrl}?academicPeriodId=${encodeURIComponent(periodSelect.value)}`);
            renderAvailability();
            setFeedback(`${state.availability.length} horário(s) registrado(s).`);
        } catch (error) { setFeedback(error.message, true); }
    }

    function renderAvailability() {
        const list = root.querySelector("[data-availability-list]");
        list.replaceChildren();
        if (!state.availability.length) {
            list.append(emptyMessage("Nenhum horário registrado para este período."));
            return;
        }
        state.availability.forEach((item) => {
            const row = document.createElement("article");
            row.className = "teacher-record-item";
            const details = document.createElement("div");
            const heading = document.createElement("strong");
            heading.textContent = `${item.type === "AVAILABLE" ? "Disponível" : "Indisponível"} · ${item.constraintKind === "MANDATORY" ? "Obrigatória" : "Preferência"}`;
            const date = document.createElement("p");
            date.textContent = item.recurringWeekly
                ? `${item.weekdays.join(", ")} · ${item.dailyStartsAt}–${item.dailyEndsAt} · ${item.validFrom} a ${item.validUntil}`
                : `${item.startsAt.replace("T", " ")} a ${item.endsAt.replace("T", " ")}`;
            details.append(heading, date);
            const remove = document.createElement("button");
            remove.type = "button";
            remove.className = "icon-button";
            remove.title = "Remover horário";
            remove.setAttribute("aria-label", "Remover horário");
            remove.innerHTML = '<i class="bi bi-trash3"></i>';
            remove.addEventListener("click", async () => {
                try {
                    await api(`${availabilityUrl}/${encodeURIComponent(item.id)}`, { method: "DELETE" });
                    await loadAvailability();
                } catch (error) { setFeedback(error.message, true); }
            });
            const actions = document.createElement("div");
            actions.className = "row-actions";
            const edit = document.createElement("button");
            edit.type = "button";
            edit.className = "icon-button";
            edit.title = "Editar horário";
            edit.setAttribute("aria-label", "Editar horário");
            edit.innerHTML = '<i class="bi bi-pencil-square"></i>';
            edit.addEventListener("click", () => editAvailability(item));
            actions.append(edit, remove);
            row.append(details, actions);
            list.append(row);
        });
    }

    function editAvailability(item) {
        form.dataset.availabilityId = item.id;
        form.elements.academicPeriodId.value = item.academicPeriodId;
        form.elements.type.value = item.type;
        form.elements.constraintKind.value = item.constraintKind;
        recurring.checked = item.recurringWeekly;
        toggleRecurrence();
        if (item.recurringWeekly) {
            form.elements.validFrom.value = item.validFrom;
            form.elements.validUntil.value = item.validUntil;
            form.elements.dailyStartsAt.value = item.dailyStartsAt;
            form.elements.dailyEndsAt.value = item.dailyEndsAt;
            form.querySelectorAll('[name="weekdays"]').forEach((checkbox) => {
                checkbox.checked = item.weekdays.includes(checkbox.value);
            });
        } else {
            form.elements.startsAt.value = item.startsAt.slice(0, 16);
            form.elements.endsAt.value = item.endsAt.slice(0, 16);
        }
        saveAvailabilityButton.innerHTML = '<i class="bi bi-check2"></i>Atualizar horário';
        cancelAvailabilityEdit.hidden = false;
        form.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    function resetAvailabilityForm() {
        delete form.dataset.availabilityId;
        form.reset();
        toggleRecurrence();
        saveAvailabilityButton.innerHTML = '<i class="bi bi-check2"></i>Salvar horário';
        cancelAvailabilityEdit.hidden = true;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(form);
        const payload = {
            academicPeriodId: data.get("academicPeriodId"),
            type: data.get("type"),
            constraintKind: data.get("constraintKind"),
            recurringWeekly: recurring.checked
        };
        if (recurring.checked) {
            payload.validFrom = data.get("validFrom");
            payload.validUntil = data.get("validUntil");
            payload.dailyStartsAt = data.get("dailyStartsAt");
            payload.dailyEndsAt = data.get("dailyEndsAt");
            payload.weekdays = data.getAll("weekdays");
            if (!payload.weekdays.length) {
                setFeedback("Selecione ao menos um dia da semana.", true);
                return;
            }
        } else {
            payload.startsAt = data.get("startsAt");
            payload.endsAt = data.get("endsAt");
        }
        try {
            const availabilityId = form.dataset.availabilityId;
            await api(availabilityId ? `${availabilityUrl}/${encodeURIComponent(availabilityId)}` : availabilityUrl, {
                method: availabilityId ? "PUT" : "POST", body: JSON.stringify(payload)
            });
            resetAvailabilityForm();
            await loadAvailability();
        } catch (error) { setFeedback(error.message, true); }
    });

    async function loadSchedules() {
        try {
            state.schedules = await api(scheduleUrl);
            renderSchedules();
            await Promise.all([loadTeacherRequests(), loadNotifications()]);
        } catch (error) {
            root.querySelector("[data-teacher-schedules]").replaceChildren(emptyMessage(error.message));
        }
    }

    function renderSchedules() {
        const list = root.querySelector("[data-teacher-schedules]");
        const scheduleSelect = root.querySelector("[data-adjustment-schedules]");
        list.replaceChildren();
        scheduleSelect.replaceChildren();
        state.schedules.forEach((schedule) => {
            const option = document.createElement("option");
            option.value = schedule.scheduleId;
            option.textContent = `${schedule.name} · ${schedule.academicPeriod} · v${schedule.versionNumber}`;
            scheduleSelect.append(option);
            const card = document.createElement("article");
            card.className = "teacher-record-item teacher-schedule-card";
            const header = document.createElement("div");
            const title = document.createElement("strong");
            title.textContent = schedule.name;
            const status = document.createElement("p");
            status.textContent = `${schedule.academicPeriod} · ${schedule.status.replaceAll("_", " ")} · v${schedule.versionNumber}`;
            header.append(title, status);
            card.append(header);
            schedule.activities.forEach((activity) => {
                const row = document.createElement("div");
                row.className = "teacher-activity-row";
                const details = document.createElement("div");
                const subject = document.createElement("strong");
                subject.textContent = activity.subject;
                const classDetails = document.createElement("p");
                classDetails.textContent = `${activity.className} · ${activity.course}`;
                const date = document.createElement("p");
                date.textContent = `${activity.startsAt.replace("T", " ")}–${activity.endsAt.slice(11, 16)}${activity.room ? ` · ${activity.room}` : ""}${activity.location ? ` · ${activity.location}` : ""}`;
                const resources = document.createElement("p");
                resources.textContent = activity.materials?.join(", ") || "";
                details.append(subject, classDetails, date, resources);
                row.append(details);
                card.append(row);
            });
            list.append(card);
        });
        if (!state.schedules.length) list.append(emptyMessage("Nenhuma grade proposta ou publicada está vinculada a este cadastro."));
        populateActivitySelect();
    }

    function populateActivitySelect() {
        const scheduleId = root.querySelector("[data-adjustment-schedules]").value;
        const schedule = state.schedules.find((item) => item.scheduleId === scheduleId);
        const select = root.querySelector("[data-adjustment-activities]");
        select.replaceChildren();
        const noActivity = document.createElement("option");
        noActivity.value = "";
        noActivity.textContent = "Grade inteira";
        select.append(noActivity);
        schedule?.activities.forEach((activity) => {
            const option = document.createElement("option");
            option.value = activity.activityId;
            option.textContent = `${activity.subject} · ${activity.className} · ${activity.startsAt.replace("T", " ")}`;
            select.append(option);
        });
    }

    root.querySelector("[data-adjustment-schedules]").addEventListener("change", populateActivitySelect);
    root.querySelector("[data-adjustment-form]").addEventListener("submit", async (event) => {
        event.preventDefault();
        const form = event.currentTarget;
        const formData = new FormData(form);
        const scheduleId = formData.get("scheduleId");
        if (!scheduleId) {
            setFeedback("Não há grade vinculada para solicitar ajuste.", true);
            return;
        }
        const payload = { activityId: formData.get("activityId") || null, description: formData.get("description") };
        try {
            await api(`/api/schedules/${encodeURIComponent(scheduleId)}/adjustment-requests`, {
                method: "POST", body: JSON.stringify(payload)
            });
            form.reset();
            populateActivitySelect();
            await loadTeacherRequests();
        } catch (error) { setFeedback(error.message, true); }
    });

    async function loadTeacherRequests() {
        const list = root.querySelector("[data-teacher-requests]");
        list.replaceChildren();
        const requests = [];
        for (const schedule of state.schedules) {
            try {
                const items = await api(`/api/schedules/${encodeURIComponent(schedule.scheduleId)}/adjustment-requests`);
                requests.push(...items.map((item) => ({ ...item, scheduleName: schedule.name })));
            } catch { /* A grade can change while the list is refreshing. */ }
        }
        requests.forEach((request) => {
            const item = document.createElement("article");
            item.className = "teacher-record-item";
            const heading = document.createElement("strong");
            heading.textContent = `${request.scheduleName} · ${request.status}`;
            const details = document.createElement("p");
            details.textContent = request.description;
            item.append(heading, details);
            if (request.response) {
                const response = document.createElement("p");
                response.textContent = `${request.respondedBy || "Coordenação"}: ${request.response}`;
                item.append(response);
            }
            list.append(item);
        });
        if (!requests.length) list.append(emptyMessage("Nenhuma solicitação registrada."));
    }

    async function loadNotifications() {
        const list = root.querySelector("[data-teacher-notifications]");
        list.replaceChildren();
        const notifications = [];
        for (const schedule of state.schedules) {
            try {
                const items = await api(`/api/schedules/${encodeURIComponent(schedule.scheduleId)}/notifications?recipient=${encodeURIComponent(teacherId)}`);
                notifications.push(...items.map((item) => ({ ...item, scheduleId: schedule.scheduleId })));
            } catch { /* Notification may be removed with its schedule. */ }
        }
        notifications.sort((left, right) => right.createdAt.localeCompare(left.createdAt));
        notifications.forEach((notification) => {
            const item = document.createElement("article");
            item.className = "teacher-record-item";
            const message = document.createElement("p");
            message.textContent = notification.message;
            const date = document.createElement("small");
            date.textContent = notification.createdAt.replace("T", " ");
            item.append(message, date);
            if (!notification.read) {
                const read = document.createElement("button");
                read.className = "secondary-button";
                read.type = "button";
                read.textContent = "Marcar como lida";
                read.addEventListener("click", async () => {
                    try {
                        await api(`/api/schedules/${encodeURIComponent(notification.scheduleId)}/notifications/${encodeURIComponent(notification.id)}/read?recipient=${encodeURIComponent(teacherId)}`, { method: "PATCH" });
                        await loadNotifications();
                    } catch (error) { setFeedback(error.message, true); }
                });
                item.append(read);
            }
            list.append(item);
        });
        if (!notifications.length) list.append(emptyMessage("Nenhuma notificação recebida."));
    }

    function emptyMessage(text) {
        const message = document.createElement("p");
        message.className = "planning-empty";
        message.textContent = text;
        return message;
    }

    root.querySelector("[data-refresh-availability]").addEventListener("click", loadAvailability);
    cancelAvailabilityEdit.addEventListener("click", resetAvailabilityForm);
    root.querySelector("[data-refresh-schedules]").addEventListener("click", loadSchedules);
    root.querySelector("[data-print-schedule]").addEventListener("click", () => window.print());
    periodSelect.addEventListener("change", loadAvailability);
    root.querySelectorAll("[data-teacher-tab]").forEach((tab) => tab.addEventListener("click", () => {
        root.querySelectorAll("[data-teacher-tab]").forEach((item) => {
            const active = item === tab;
            item.classList.toggle("is-active", active);
            item.setAttribute("aria-selected", String(active));
        });
        root.querySelectorAll("[data-teacher-panel]").forEach((panel) => {
            panel.hidden = panel.dataset.teacherPanel !== tab.dataset.teacherTab;
        });
        if (tab.dataset.teacherTab === "schedule") loadSchedules();
        if (tab.dataset.teacherTab === "requests") loadTeacherRequests();
        if (tab.dataset.teacherTab === "notifications") loadNotifications();
    }));

    (async () => {
        try {
            await loadPeriods();
            await loadAvailability();
            await loadSchedules();
        } catch (error) {
            setFeedback(error.message, true);
        }
    })();
}