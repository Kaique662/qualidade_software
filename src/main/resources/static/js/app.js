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
});
