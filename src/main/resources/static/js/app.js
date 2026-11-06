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
            row.append(item, type, location, quantity, stateCell);
            rows.append(row);
        });
        table.hidden = visibleResources.length === 0;
        root.querySelector("[data-resource-empty]").hidden = visibleResources.length > 0;
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
            features: [],
            state: "AVAILABLE",
            preparationBufferMinutes: 0,
            cleanupBufferMinutes: 0
        };
        if (type === "ROOM") {
            resource.capacity = Number(data.get("capacity"));
        } else {
            resource.totalQuantity = Number(data.get("totalQuantity"));
            resource.unit = data.get("unit");
        }

        formFeedback.textContent = "Salvando recurso...";
        try {
            const response = await fetch("/api/resources", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(resource)
            });
            if (!response.ok) throw new Error();
            form.reset();
            typeInput.dispatchEvent(new Event("change"));
            formFeedback.textContent = "Recurso cadastrado.";
            await loadResources();
        } catch {
            formFeedback.textContent = "Não foi possível cadastrar. Confira os dados e a conexão com o banco.";
        }
    });

    search.addEventListener("input", renderResources);
    filter.addEventListener("change", renderResources);
    loadResources();
}
