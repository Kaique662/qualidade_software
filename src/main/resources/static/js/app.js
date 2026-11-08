document.addEventListener("DOMContentLoaded", () => {
    const menuToggle = document.querySelector(".menu-toggle");
    const sideRail = document.querySelector(".side-rail");

    if (menuToggle && sideRail) {
        menuToggle.addEventListener("click", () => {
            const isOpen = sideRail.classList.toggle("is-open");
            menuToggle.setAttribute("aria-expanded", String(isOpen));
            menuToggle.setAttribute("aria-label", isOpen ? "Fechar menu" : "Abrir menu");
        });
    }

    document.querySelectorAll("[data-table-filter]").forEach((input) => {
        const tableBody = document.getElementById(input.dataset.tableFilter);
        if (!tableBody) return;

        const rows = [...tableBody.querySelectorAll("tr[data-search]")];
        const noResults = input.closest(".directory-page")?.querySelector("[data-no-results]");

        input.addEventListener("input", () => {
            const query = input.value.trim().toLocaleLowerCase("pt-BR");
            let visibleRows = 0;

            rows.forEach((row) => {
                const matches = row.dataset.search.toLocaleLowerCase("pt-BR").includes(query);
                row.hidden = !matches;
                if (matches) visibleRows += 1;
            });

            if (noResults) noResults.hidden = visibleRows > 0;
        });
    });

    document.querySelectorAll("form[data-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!window.confirm(form.dataset.confirm)) event.preventDefault();
        });
    });

    const resourceDashboard = document.querySelector("[data-resource-dashboard]");
    if (resourceDashboard) initializeResourceDashboard(resourceDashboard);
});

function initializeResourceDashboard(root) {
    const feedback = root.querySelector("[data-resource-feedback]");
    const table = root.querySelector("[data-resource-table]");
    const rows = root.querySelector("[data-resource-rows]");
    const search = root.querySelector("[data-resource-search]");
    const filter = root.querySelector("[data-resource-filter]");
    const form = root.querySelector("[data-resource-form]");
    const formFeedback = root.querySelector("[data-form-feedback]");
    const typeInput = root.querySelector("[data-resource-type]");
    const saveButton = root.querySelector("[data-save-resource]");
    const cancelEditButton = root.querySelector("[data-cancel-resource-edit]");
    let resources = [];

    const labels = {
        AVAILABLE: "Disponível",
        IN_USE: "Em uso",
        MAINTENANCE: "Em manutenção",
        INACTIVE: "Inativo"
    };

    function updateSummary() {
        root.querySelector('[data-count="rooms"]').textContent = resources.filter((resource) => resource.type === "ROOM").length;
        root.querySelector('[data-count="materials"]').textContent = resources.filter((resource) => resource.type === "MATERIAL").length;
        root.querySelector('[data-count="maintenance"]').textContent = resources.filter((resource) => resource.state === "MAINTENANCE").length;
    }

    function renderResources() {
        const query = search.value.trim().toLocaleLowerCase("pt-BR");
        const visibleResources = resources.filter((resource) => {
            const matchesType = !filter.value || resource.type === filter.value;
            const searchable = `${resource.code} ${resource.name} ${resource.location}`.toLocaleLowerCase("pt-BR");
            return matchesType && searchable.includes(query);
        });
        rows.replaceChildren();
        visibleResources.forEach((resource) => {
            const row = document.createElement("tr");
            const item = document.createElement("td");
            const itemDetails = document.createElement("span");
            itemDetails.className = "resource-name-cell";
            const name = document.createElement("strong");
            name.textContent = resource.name;
            const code = document.createElement("small");
            code.textContent = resource.code;
            itemDetails.append(name, code);
            item.append(itemDetails);

            const type = document.createElement("td");
            type.textContent = resource.type === "ROOM" ? "Sala" : "Material";
            const location = document.createElement("td");
            location.textContent = resource.location;
            const quantity = document.createElement("td");
            quantity.textContent = resource.type === "ROOM"
                ? `${resource.capacity ?? "—"} lugares`
                : `${resource.availableQuantity ?? resource.totalQuantity ?? "—"} ${resource.unit ?? "unidades"}`;

            const stateCell = document.createElement("td");
            const stateSelect = document.createElement("select");
            stateSelect.className = "resource-state-select";
            stateSelect.setAttribute("aria-label", `Condição de ${resource.name}`);
            Object.entries(labels).forEach(([value, label]) => {
                const option = document.createElement("option");
                option.value = value;
                option.textContent = label;
                option.selected = resource.state === value;
                stateSelect.append(option);
            });
            stateSelect.addEventListener("change", () => changeResourceState(resource, stateSelect.value));
            stateCell.append(stateSelect);
            const actions = document.createElement("td");
            const edit = document.createElement("button");
            edit.type = "button";
            edit.className = "icon-button";
            edit.title = "Editar recurso";
            edit.setAttribute("aria-label", `Editar ${resource.name}`);
            edit.innerHTML = '<i class="bi bi-pencil-square"></i>';
            edit.addEventListener("click", () => editResource(resource));
            actions.append(edit);
            row.append(item, type, location, quantity, stateCell, actions);
            rows.append(row);
        });
        table.hidden = visibleResources.length === 0;
        root.querySelector("[data-resource-empty]").hidden = visibleResources.length > 0;
    }

    function editResource(resource) {
        form.dataset.resourceId = resource.id;
        form.elements.code.value = resource.code;
        form.elements.name.value = resource.name;
        form.elements.type.value = resource.type;
        typeInput.dispatchEvent(new Event("change"));
        form.elements.location.value = resource.location;
        form.elements.capacity.value = resource.capacity ?? "";
        form.elements.totalQuantity.value = resource.totalQuantity ?? "";
        form.elements.unit.value = resource.unit ?? "";
        form.elements.features.value = (resource.features ?? []).join(", ");
        form.elements.preparationBufferMinutes.value = resource.preparationBufferMinutes ?? 0;
        form.elements.cleanupBufferMinutes.value = resource.cleanupBufferMinutes ?? 0;
        form.elements.restricted.checked = Boolean(resource.restricted);
        saveButton.innerHTML = '<i class="bi bi-check2"></i> Atualizar recurso';
        cancelEditButton.hidden = false;
        root.querySelector("[data-resource-create]").open = true;
        form.scrollIntoView({ behavior: "smooth", block: "center" });
    }

    function resetResourceForm() {
        delete form.dataset.resourceId;
        form.reset();
        typeInput.dispatchEvent(new Event("change"));
        saveButton.innerHTML = '<i class="bi bi-check2"></i> Salvar recurso';
        cancelEditButton.hidden = true;
        formFeedback.textContent = "";
    }

    async function loadResources() {
        feedback.textContent = "Carregando inventário...";
        try {
            const response = await fetch("/api/resources");
            if (!response.ok) throw new Error("Não foi possível consultar os recursos.");
            resources = await response.json();
            feedback.hidden = true;
            updateSummary();
            renderResources();
        } catch {
            feedback.hidden = false;
            feedback.textContent = "Não foi possível carregar o inventário. Verifique a conexão com o banco de dados e tente atualizar a página.";
            table.hidden = true;
        }
    }

    async function changeResourceState(resource, state) {
        try {
            const response = await fetch(`/api/resources/${encodeURIComponent(resource.id)}/state?state=${encodeURIComponent(state)}`, { method: "PATCH" });
            if (!response.ok) throw new Error();
            resource.state = state;
            updateSummary();
            renderResources();
        } catch {
            feedback.hidden = false;
            feedback.textContent = "Não foi possível atualizar a condição do recurso.";
            await loadResources();
        }
    }

    typeInput.addEventListener("change", () => {
        const material = typeInput.value === "MATERIAL";
        root.querySelectorAll("[data-room-field]").forEach((field) => { field.hidden = material; });
        root.querySelectorAll("[data-material-field]").forEach((field) => { field.hidden = !material; });
        form.elements.capacity.required = !material;
        form.elements.totalQuantity.required = material;
        form.elements.unit.required = material;
    });

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(form);
        const type = data.get("type");
        const resource = {
            code: data.get("code"),
            name: data.get("name"),
            type,
            location: data.get("location"),
            features: String(data.get("features") || "").split(",").map((item) => item.trim()).filter(Boolean),
            state: resources.find((item) => item.id === form.dataset.resourceId)?.state ?? "AVAILABLE",
            preparationBufferMinutes: Number(data.get("preparationBufferMinutes") || 0),
            cleanupBufferMinutes: Number(data.get("cleanupBufferMinutes") || 0),
            restricted: data.get("restricted") === "on"
        };
        if (type === "ROOM") {
            resource.capacity = Number(data.get("capacity"));
        } else {
            resource.totalQuantity = Number(data.get("totalQuantity"));
            resource.unit = data.get("unit");
        }

        formFeedback.textContent = "Salvando recurso...";
        try {
            const resourceId = form.dataset.resourceId;
            const response = await fetch(resourceId ? `/api/resources/${encodeURIComponent(resourceId)}` : "/api/resources", {
                method: resourceId ? "PUT" : "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(resource)
            });
            if (!response.ok) throw new Error();
            resetResourceForm();
            formFeedback.textContent = resourceId ? "Recurso atualizado." : "Recurso cadastrado.";
            await loadResources();
        } catch {
            formFeedback.textContent = "Não foi possível cadastrar. Confira os dados e a conexão com o banco.";
        }
    });

    search.addEventListener("input", renderResources);
    filter.addEventListener("change", renderResources);
    cancelEditButton.addEventListener("click", resetResourceForm);
    loadResources();
}
