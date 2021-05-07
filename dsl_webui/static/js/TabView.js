function TabView(tabWrapper) {
    const container = document.getElementById(tabWrapper);
    const tabs = container.querySelectorAll(".tab-generator-tab");
    const headerWrapper = container.querySelector(".tab-generator-headers");

    const headers = [];
    const tabMap = {};
    const tabHeaderMap = {};

    for (let i = 0; i < tabs.length; i++) {
        const tab = tabs[i];
        const title = tab.dataset.tabTitle;
        if (title === undefined) continue;
        const tag = tab.dataset.tabTag;
        if (tag === undefined) continue;

        tabMap[tag] = tab;

        if (headerWrapper !== null) {
            const header = document.createElement("div");
            header.innerHTML = title;
            if (headerWrapper.dataset.tabHeaderClass !== undefined) {
                header.classList.add(headerWrapper.dataset.tabHeaderClass);
            }
            header.addEventListener("click", (event) => activateTab(tag));


            tabHeaderMap[tag] = header;
            headers.push(header);

            headerWrapper.appendChild(header);
        }
    }

    const activateTab = function (tag) {
        for (let i = 0; i < headers.length; i++) {
            headers[i].classList.remove("active");
        }
        for (let i = 0; i < tabs.length; i++) {
            tabs[i].classList.remove("active");
        }
        if (tag in tabHeaderMap) {
            tabHeaderMap[tag].classList.add("active");
        }
        if (tag in tabMap) {
            tabMap[tag].classList.add("active");
        }
    };

    return activateTab;
}