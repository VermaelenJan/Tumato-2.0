function saveSpec(name, id) {
    localStorage.setItem(name, id);
    let list = getSpecs();
    if (list.includes(name)) return;
    list.push(name);
    saveSpecList(list);
}

function deleteSpec(name) {
    const list = getSpecs();
    if (!list.includes(name)) return;
    const newList = list.filter(item => item !== name);
    saveSpecList(newList);
    localStorage.removeItem(name);
}

function nameExists(name) {
    return localStorage.getItem(name) !== null
}

function getSpecs() {
    if (localStorage.getItem('specs') === null) {
        return []
    } else {
        return JSON.parse(localStorage.getItem('specs'));
    }
}

function saveSpecList(specs) {
    localStorage.setItem('specs', JSON.stringify(specs));
}